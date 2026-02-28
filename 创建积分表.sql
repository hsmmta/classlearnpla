-- 焚决项目 - 积分表创建脚本
-- 用途：创建points表用于存储用户积分

USE web_demo;

-- 创建points表
CREATE TABLE IF NOT EXISTS points (
    userID VARCHAR(20) PRIMARY KEY,
    userName VARCHAR(20),
    points INT DEFAULT 0,
    FOREIGN KEY (userID) REFERENCES user(userphone),
    FOREIGN KEY (userName) REFERENCES user(userName)
);

-- 验证表创建
DESC points;

-- 查看现有数据（如果有的话）
SELECT * FROM points;

-- 为已存在的用户初始化积分（可选）
-- 执行此语句可以为user表中的所有用户初始化积分（如果他们还没有积分记录）
INSERT IGNORE INTO points (userID, userName, points)
SELECT userphone, userName, 0 FROM user;

-- 验证数据
SELECT * FROM points;

