-- 仅供本机开发测试使用；账户权限限制在 javafix_shop_test 数据库。
CREATE DATABASE IF NOT EXISTS javafix_shop_test
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'javafix_test'@'localhost' IDENTIFIED BY 'javafix_test';
ALTER USER 'javafix_test'@'localhost' IDENTIFIED BY 'javafix_test';
GRANT ALL PRIVILEGES ON javafix_shop_test.* TO 'javafix_test'@'localhost';

CREATE USER IF NOT EXISTS 'javafix_test'@'127.0.0.1' IDENTIFIED BY 'javafix_test';
ALTER USER 'javafix_test'@'127.0.0.1' IDENTIFIED BY 'javafix_test';
GRANT ALL PRIVILEGES ON javafix_shop_test.* TO 'javafix_test'@'127.0.0.1';

FLUSH PRIVILEGES;
