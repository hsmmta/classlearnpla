-- 完整诊断脚本：检查问题审核和资料上传

USE web_demo;

-- ========== 第1部分：问题审核诊断 ==========
SELECT '========================' as log;
SELECT '问题审核诊断' as log;
SELECT '========================' as log;

-- 检查是否有待审核的问题
SELECT '待审核的问题数量:' as label;
SELECT COUNT(*) as count FROM question WHERE questionState = '待审核';

-- 显示所有待审核问题
SELECT '详细的待审核问题:' as label;
SELECT questionID, questionTitle, questionState, userID, creationTime
FROM question
WHERE questionState = '待审核'
ORDER BY creationTime DESC;

-- 问题状态分布
SELECT '问题状态分布:' as label;
SELECT questionState, COUNT(*) as count
FROM question
GROUP BY questionState;

-- 最近的所有问题
SELECT '最近提交的10个问题:' as label;
SELECT questionID, questionTitle, questionState, creationTime
FROM question
ORDER BY creationTime DESC
LIMIT 10;

-- ========== 第2部分：资料上传诊断 ==========
SELECT '========================' as log;
SELECT '资料上传诊断' as log;
SELECT '========================' as log;

-- 检查是否有待审核的资料
SELECT '待审核的资料数量:' as label;
SELECT COUNT(*) as count FROM material WHERE materialState = '待审核';

-- 显示所有待审核资料
SELECT '详细的待审核资料:' as label;
SELECT materialID, materialTitle, materialState, userID, uploadTime
FROM material
WHERE materialState = '待审核'
ORDER BY uploadTime DESC;

-- 资料状态分布
SELECT '资料状态分布:' as label;
SELECT materialState, COUNT(*) as count
FROM material
GROUP BY materialState;

-- 最近的所有资料
SELECT '最近上传的10个资料:' as label;
SELECT materialID, materialTitle, materialState, uploadTime
FROM material
ORDER BY uploadTime DESC
LIMIT 10;

-- ========== 第3部分：用户和表关系检查 ==========
SELECT '========================' as log;
SELECT '用户和表关系检查' as log;
SELECT '========================' as log;

-- 检查user表中有多少用户
SELECT '用户数量:' as label;
SELECT COUNT(*) as count FROM user;

-- 列出所有用户
SELECT '所有用户:' as label;
SELECT userphone, userName FROM user LIMIT 10;

-- 检查问题和用户的外键关系
SELECT '问题中不存在的用户:' as label;
SELECT DISTINCT q.userID
FROM question q
LEFT JOIN user u ON q.userID = u.userphone
WHERE u.userphone IS NULL;

-- 检查资料和用户的外键关系
SELECT '资料中不存在的用户:' as label;
SELECT DISTINCT m.userID
FROM material m
LEFT JOIN user u ON m.userID = u.userphone
WHERE u.userphone IS NULL;

-- ========== 第4部分：审核表检查 ==========
SELECT '========================' as log;
SELECT '审核权限表检查' as log;
SELECT '========================' as log;

-- 检查admin表中有多少管理员
SELECT '管理员数量:' as label;
SELECT COUNT(*) as count FROM admin;

-- 列出所有管理员
SELECT '所有管理员:' as label;
SELECT adminID FROM admin;

