-- 焚决项目 - 积分表创建脚本（修复版本）
-- 用途：创建points表用于存储用户积分
-- 修复：添加 userName 列的唯一索引以支持外键约束

USE web_demo;

-- 第一步：为user表的userName列添加唯一索引（必须先做这个）
-- 这是创建外键前的必要条件
ALTER TABLE user ADD UNIQUE INDEX idx_userName (userName);

-- 第二步：创建points表
CREATE TABLE IF NOT EXISTS points (
    userID VARCHAR(20) PRIMARY KEY,
    userName VARCHAR(20),
    points INT DEFAULT 0,
    FOREIGN KEY (userID) REFERENCES user(userphone),
    FOREIGN KEY (userName) REFERENCES user(userName)
);

-- 第三步：验证表创建
DESC points;

-- 第四步：查看现有数据（如果有的话）
SELECT * FROM points;

-- 第五步：为已存在的用户初始化积分（可选）
-- 执行此语句可以为user表中的所有用户初始化积分（如果他们还没有积分记录）
INSERT IGNORE INTO points (userID, userName, points)
SELECT userphone, userName, 0 FROM user;

-- 第六步：验证数据
SELECT * FROM points;

-- 查看索引
SHOW INDEX FROM points;
SHOW INDEX FROM user;

