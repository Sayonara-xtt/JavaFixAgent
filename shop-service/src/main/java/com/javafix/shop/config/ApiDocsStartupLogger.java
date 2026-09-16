package com.javafix.shop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 应用启动完成后在日志中输出接口文档访问地址。
 */
@Component
public class ApiDocsStartupLogger {

    private static final Logger log = LoggerFactory.getLogger(ApiDocsStartupLogger.class);

    private final Environment environment;
    private final ServerProperties serverProperties;

    public ApiDocsStartupLogger(Environment environment, ServerProperties serverProperties) {
        this.environment = environment;
        this.serverProperties = serverProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logApiDocsUrls() {
        int port = serverProperties.getPort() == null ? 8080 : serverProperties.getPort();
        String contextPath = serverProperties.getServlet().getContextPath();
        if (contextPath == null || contextPath.isBlank() || "/".equals(contextPath)) {
            contextPath = "";
        }

        String swaggerUiPath = environment.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");
        String apiDocsPath = environment.getProperty("springdoc.api-docs.path", "/v3/api-docs");
        String baseUrl = "http://localhost:" + port + contextPath;

        log.info("----------------------------------------------------------");
        log.info("接口文档已就绪:");
        log.info("  Knife4j : {}/doc.html", baseUrl);
        log.info("  Swagger : {}{}", baseUrl, swaggerUiPath);
        log.info("  OpenAPI : {}{}", baseUrl, apiDocsPath);
        log.info("----------------------------------------------------------");
    }
}
