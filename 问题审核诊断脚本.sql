-- 问题审核诊断 SQL 脚本

USE web_demo;

-- 1. 检查是否有待审核的问题
SELECT '=== 待审核的问题 ===' as diagnostic;
SELECT questionID, questionTitle, questionState, creationTime, userID
FROM question
WHERE questionState = '待审核'
ORDER BY creationTime DESC;

-- 2. 检查所有问题的状态分布
SELECT '=== 问题状态分布 ===' as diagnostic;
SELECT
    questionState,
    COUNT(*) as count
FROM question
GROUP BY questionState;

-- 3. 检查最近提交的问题（不管状态如何）
SELECT '=== 最近的问题（任何状态）===' as diagnostic;
SELECT questionID, questionTitle, questionState, creationTime, userID
FROM question
ORDER BY creationTime DESC
LIMIT 10;

-- 4. 检查问题提交者是否存在于 user 表
SELECT '=== 检查外键关系 ===' as diagnostic;
SELECT
    q.questionID,
    q.questionTitle,
    q.userID,
    q.questionState,
    u.userphone,
    u.userName
FROM question q
LEFT JOIN user u ON q.userID = u.userphone
WHERE u.userphone IS NULL
LIMIT 10;

-- 5. 检查所有的已审核问题
SELECT '=== 审核通过的问题 ===' as diagnostic;
SELECT questionID, questionTitle, creationTime, userID
FROM question
WHERE questionState = '审核通过'
ORDER BY creationTime DESC
LIMIT 5;

-- 6. 检查最满意答案的设置
SELECT '=== 已设置的最满意答案 ===' as diagnostic;
SELECT
    q.questionID,
    q.questionTitle,
    q.bestAnswerID,
    qc.userID,
    qc.commentContent
FROM question q
LEFT JOIN question_comment qc ON q.bestAnswerID = qc.commentID
WHERE q.bestAnswerID IS NOT NULL;

