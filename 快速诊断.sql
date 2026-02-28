-- 快速诊断：路径和权限问题

USE web_demo;

-- 1. 验证是否有待审核问题
SELECT '=== 待审核问题 ===' as test;
SELECT questionID, questionTitle, creationTime FROM question WHERE questionState = '待审核' LIMIT 5;

-- 2. 验证是否有待审核资料
SELECT '=== 待审核资料 ===' as test;
SELECT materialID, materialTitle, uploadTime FROM material WHERE materialState = '待审核' LIMIT 5;

-- 3. 检查admin表（确保有管理员账号）
SELECT '=== 管理员账号 ===' as test;
SELECT adminID FROM admin;

-- 4. 检查user表（确保有用户）
SELECT '=== 用户账号 ===' as test;
SELECT userphone, userName FROM user LIMIT 5;

