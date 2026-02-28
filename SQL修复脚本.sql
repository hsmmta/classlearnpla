-- 焚决项目数据库修复脚本
-- 使用说明：将此脚本复制到MySQL客户端或Navicat中执行
-- 或在命令行中执行：mysql -u root -p web_demo < 数据库修复脚本.sql

-- 1. 进入数据库
USE web_demo;

-- 2. 查看当前表结构（参考）
-- DESC user;

-- 3. 添加缺失的列

-- 添加studentID列（如果不存在）
ALTER TABLE user ADD COLUMN IF NOT EXISTS studentID VARCHAR(15) COMMENT '学号' AFTER classID;

-- 添加gender列（如果不存在）
ALTER TABLE user ADD COLUMN IF NOT EXISTS gender VARCHAR(10) COMMENT '性别（male/female）' AFTER userEmail;

-- 4. 验证表结构
DESC user;

-- 预期输出应该包含这些列：
-- userphone, userPassword, userName, classID, studentID, gender, userEmail, userStatus

-- 5. 检查现有数据
SELECT COUNT(*) as user_count FROM user;

-- 6. 如果有现有数据但新列为空，可以设置默认值
-- 更新现有用户的性别为默认值（可选）
-- UPDATE user SET gender = 'male' WHERE gender IS NULL OR gender = '';

-- 7. 完成
-- 如果以上SQL执行成功，表示数据库修复完成
-- 现在可以重新编译部署Java应用了

