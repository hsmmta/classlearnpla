-- 焚决项目 - 积分表创建脚本（简化版本）
-- 用途：创建points表用于存储用户积分
-- 说明：使用userID作为主键和外键，避免复杂的外键约束

USE web_demo;

-- 创建points表（简化版本，只用userID作为外键）
CREATE TABLE IF NOT EXISTS points (
    userID VARCHAR(20) PRIMARY KEY,
    userName VARCHAR(20),
    points INT DEFAULT 0,
    FOREIGN KEY (userID) REFERENCES user(userphone)
);

-- 验证表创建
DESC points;

-- 查看现有数据（如果有的话）
SELECT * FROM points;

-- 为已存在的用户初始化积分
INSERT IGNORE INTO points (userID, userName, points)
SELECT userphone, userName, 0 FROM user;

-- 验证数据
SELECT * FROM points;

-- 查看创建的表和索引
SHOW TABLES LIKE 'points';
SHOW INDEX FROM points;

