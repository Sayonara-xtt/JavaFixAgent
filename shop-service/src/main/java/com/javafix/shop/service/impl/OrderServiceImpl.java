package com.javafix.shop.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javafix.shop.dto.CreateOrderRequest;
import com.javafix.shop.dto.OrderItemRequest;
import com.javafix.shop.entity.Order;
import com.javafix.shop.entity.OrderItem;
import com.javafix.shop.entity.Product;
import com.javafix.shop.entity.User;
import com.javafix.shop.enums.OrderStatus;
import com.javafix.shop.exception.BusinessException;
import com.javafix.shop.exception.ErrorCode;
import com.javafix.shop.mapper.OrderItemMapper;
import com.javafix.shop.mapper.OrderMapper;
import com.javafix.shop.mapper.ProductMapper;
import com.javafix.shop.mapper.UserMapper;
import com.javafix.shop.service.OrderService;
import com.javafix.shop.vo.OrderItemVO;
import com.javafix.shop.vo.OrderVO;
import com.javafix.shop.vo.PageResult;

/**
 * 订单领域核心业务：创建订单（扣库存）与取消订单（还库存）。
 *
 * <p>写路径使用事务：任一步失败必须整单回滚，避免出现“有订单无库存”或负库存。</p>
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public OrderServiceImpl(
            UserMapper userMapper,
            ProductMapper productMapper,
            OrderMapper orderMapper,
            OrderItemMapper orderItemMapper) {
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    /**
     * 创建订单流程：
     * <ol>
     *   <li>校验用户存在</li>
     *   <li>逐行校验商品，并用条件 SQL 原子扣库存</li>
     *   <li>按明细汇总金额后写入订单与订单项（快照商品名/单价）</li>
     * </ol>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO create(CreateOrderRequest request) {
        // 1) 用户必须存在；缺失返回受控业务异常，而不是 NPE
        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "user not found");
        }

        List<OrderItem> pendingItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 2) 先扣库存再建单：扣减失败会触发回滚，保证库存与订单一致
        for (OrderItemRequest itemRequest : request.getItems()) {
            // selectById 受 @TableLogic 影响，已逻辑删除的商品视为不存在
            Product product = productMapper.selectById(itemRequest.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "product not found");
            }

            // 条件更新：仅当 stock >= quantity 时成功；影响行数不为 1 即库存不足
            int updated = productMapper.decreaseStock(product.getId(), itemRequest.getQuantity());
            if (updated != 1) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "insufficient stock");
            }

            // 订单金额由明细推导，禁止调用方直接传入总价
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            // 明细保存下单时的商品快照，避免后续改价影响历史订单
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setSubtotal(subtotal);
            pendingItems.add(orderItem);
        }

        // 3) 持久化订单头
        Order order = new Order();
        order.setUserId(user.getId());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CREATED.name());
        orderMapper.insert(order);

        // 4) 持久化订单明细（依赖已生成的 orderId）
        for (OrderItem orderItem : pendingItems) {
            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);
        }

        return getById(order.getId());
    }

    /** 查询订单详情（含明细）；不存在时返回受控业务错误。 */
    @Override
    public OrderVO getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "order not found");
        }
        return toVo(order, loadItems(id));
    }

    /** 订单分页：页码/页大小非法时回落到默认值 1 / 20。 */
    @Override
    public PageResult<OrderVO> page(long page, long size) {
        long safePage = page <= 0 ? 1 : page;
        long safeSize = size <= 0 ? 20 : size;
        Page<Order> orderPage = orderMapper.selectPage(
                new Page<>(safePage, safeSize),
                new LambdaQueryWrapper<Order>().orderByDesc(Order::getId));

        List<OrderVO> records = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            records.add(toVo(order, loadItems(order.getId())));
        }
        return new PageResult<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal(), records);
    }

    /**
     * 取消订单流程（幂等约束）：
     * <ol>
     *   <li>仅 CREATED 可取消</li>
     *   <li>已 CANCELLED 再次取消直接失败，避免库存被恢复两次</li>
     *   <li>先改状态，再按明细精确还库存一次</li>
     * </ol>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO cancel(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "order not found");
        }
        // 幂等：成功取消后不允许再次成功取消
        if (OrderStatus.CANCELLED.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CANCELLABLE, "order already cancelled");
        }
        if (!OrderStatus.CREATED.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CANCELLABLE, "order is not cancellable");
        }

        // 查到 CREATED 只是快照；条件更新才是并发场景下的取消资格判定。
        // MySQL 在持有行锁的更新中检查状态，竞争失败者不得进入还库存流程。
        if (orderMapper.cancelCreated(id) != 1) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CANCELLABLE, "order is not cancellable");
        }
        order.setStatus(OrderStatus.CANCELLED.name());

        List<OrderItem> items = loadItems(id);
        for (OrderItem item : items) {
            // 按下单数量精确恢复，确保只恢复一次
            int restored = productMapper.increaseStock(item.getProductId(), item.getQuantity());
            // 商品缺失/已删除时不能静默成功：回滚状态与此前恢复的库存。
            if (restored != 1) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "inventory restoration target unavailable");
            }
        }
        return toVo(order, items);
    }

    private List<OrderItem> loadItems(Long orderId) {
        return orderItemMapper.selectList(
                // 按明细创建顺序读取，保证接口顺序稳定、恢复库存流程可复现。
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
                        .orderByAsc(OrderItem::getId));
    }

    private OrderVO toVo(Order order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setUserId(order.getUserId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        List<OrderItemVO> itemVos = new ArrayList<>();
        for (OrderItem item : items) {
            OrderItemVO itemVo = new OrderItemVO();
            itemVo.setId(item.getId());
            itemVo.setProductId(item.getProductId());
            itemVo.setProductName(item.getProductName());
            itemVo.setProductPrice(item.getProductPrice());
            itemVo.setQuantity(item.getQuantity());
            itemVo.setSubtotal(item.getSubtotal());
            itemVos.add(itemVo);
        }
        vo.setItems(itemVos);
        return vo;
    }
}
