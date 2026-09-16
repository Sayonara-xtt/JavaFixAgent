-- Phase 3 business tables for the order-domain MVP.
CREATE TABLE IF NOT EXISTS `user` (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(64)  NOT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS product (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(128) NOT NULL,
    price       DECIMAL(12, 2) NOT NULL,
    stock       INT          NOT NULL,
    version     INT          NOT NULL DEFAULT 0,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS orders (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT         NOT NULL,
    total_amount  DECIMAL(12, 2) NOT NULL,
    status        VARCHAR(32)    NOT NULL,
    deleted       TINYINT        NOT NULL DEFAULT 0,
    created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_orders_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS order_item (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id       BIGINT         NOT NULL,
    product_id     BIGINT         NOT NULL,
    product_name   VARCHAR(128)   NOT NULL,
    product_price  DECIMAL(12, 2) NOT NULL,
    quantity       INT            NOT NULL,
    subtotal       DECIMAL(12, 2) NOT NULL,
    INDEX idx_order_item_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
