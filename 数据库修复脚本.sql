-- 焚决项目 - 数据库修复脚本
-- 用途：为user表添加缺失的列

-- 首先检查user表结构
DESC user;

-- 如果缺少以下列，执行对应的ALTER TABLE语句

-- 1. 添加gender列（如果不存在）
ALTER TABLE user ADD COLUMN gender VARCHAR(10) DEFAULT 'male' AFTER userEmail;

-- 2. 添加studentID列（如果不存在）
ALTER TABLE user ADD COLUMN studentID VARCHAR(15) AFTER classID;

-- 验证表结构
DESC user;

-- 预期的user表结构应该包含以下列：
-- userphone (VARCHAR, PRIMARY KEY)
-- userPassword (VARCHAR)
-- userName (VARCHAR)
-- classID (VARCHAR)
-- studentID (VARCHAR) - 可能需要添加
-- gender (VARCHAR) - 可能需要添加
-- userEmail (VARCHAR)
-- userStatus (INT)

