-- 焚决项目 - 积分商城数据库表创建脚本
-- 用途：创建goods表（商品表）和pointop表（交易记录表）

USE web_demo;

-- 1. 创建goods表（商品表）
CREATE TABLE IF NOT EXISTS goods (
    itemID INT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    itemName VARCHAR(50) NOT NULL COMMENT '商品名称',
    needPoint INT NOT NULL COMMENT '所需积分',
    `desc` VARCHAR(255) COMMENT '商品描述'
);

-- 2. 创建pointop表（交易记录表）
CREATE TABLE IF NOT EXISTS pointop (
    opID INT AUTO_INCREMENT PRIMARY KEY COMMENT '交易记录ID',
    userID VARCHAR(20) NOT NULL COMMENT '用户ID',
    pointOP VARCHAR(50) NOT NULL COMMENT '积分变化，如+10或-1',
    detail VARCHAR(255) COMMENT '交易详情，如兑换123或获得积分',
    `time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    FOREIGN KEY (userID) REFERENCES user(userphone)
);

-- 3. 添加初始商品（itemID为1，itemName为"666"，needPoint为1，desc为"good"）
INSERT INTO goods (itemID, itemName, needPoint, `desc`)
VALUES (1, '666', 1, 'good');

-- 4. 验证表创建
DESC goods;
DESC pointop;

-- 5. 查看初始数据
SELECT * FROM goods;
SELECT * FROM pointop;

