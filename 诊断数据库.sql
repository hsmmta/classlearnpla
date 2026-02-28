-- 积分商城问题诊断SQL
-- 在MySQL中逐步执行以下命令检查问题

-- 1. 检查数据库
USE web_demo;

-- 2. 检查goods表是否存在
SHOW TABLES LIKE 'goods';

-- 3. 如果不存在，创建goods表
CREATE TABLE IF NOT EXISTS goods (
    itemID INT AUTO_INCREMENT PRIMARY KEY,
    itemName VARCHAR(50) NOT NULL,
    needPoint INT NOT NULL,
    `desc` VARCHAR(255)
);

-- 4. 检查pointop表是否存在
SHOW TABLES LIKE 'pointop';

-- 5. 如果不存在，创建pointop表
CREATE TABLE IF NOT EXISTS pointop (
    opID INT AUTO_INCREMENT PRIMARY KEY,
    userID VARCHAR(20) NOT NULL,
    pointOP VARCHAR(50) NOT NULL,
    detail VARCHAR(255),
    `time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userID) REFERENCES user(userphone)
);

-- 6. 检查goods表中是否有数据
SELECT COUNT(*) as goods_count FROM goods;

-- 7. 如果没有数据，添加测试商品
INSERT INTO goods (itemName, needPoint, `desc`) VALUES ('666', 1, 'good');
INSERT INTO goods (itemName, needPoint, `desc`) VALUES ('测试商品2', 5, '这是测试商品');
INSERT INTO goods (itemName, needPoint, `desc`) VALUES ('测试商品3', 10, '积分商品测试');

-- 8. 查看所有商品
SELECT * FROM goods;

-- 9. 检查goods表结构
DESC goods;

-- 10. 检查pointop表结构
DESC pointop;

-- 完成！如果以上都执行成功，数据库部分就OK了

