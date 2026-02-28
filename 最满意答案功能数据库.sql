-- 最满意答案功能 - 数据库表更新

USE web_demo;

-- 1. 为 question 表添加 bestAnswerID 列（记录哪个回答是最满意答案）
ALTER TABLE question
ADD COLUMN IF NOT EXISTS bestAnswerID INT DEFAULT NULL COMMENT '最满意答案的评论ID';

-- 2. 为 question_comment 表添加 isBestAnswer 列（标记是否为最满意答案）
ALTER TABLE question_comment
ADD COLUMN IF NOT EXISTS isBestAnswer TINYINT(1) DEFAULT 0 COMMENT '是否为最满意答案：0-否，1-是';

-- 3. 验证表结构
DESC question;
DESC question_comment;

-- 4. 查看数据
SELECT questionID, questionTitle, questionOwner, bestAnswerID FROM question LIMIT 5;
SELECT commentID, questionID, userID, commentContent, isBestAnswer FROM question_comment LIMIT 5;

