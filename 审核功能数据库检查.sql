-- 资料审核和问题审核 - 数据库表结构确认和更新

USE web_demo;

-- 1. 检查 material 表是否有 materialState 列
SHOW COLUMNS FROM material LIKE 'materialState';

-- 如果没有 materialState 列，添加它
ALTER TABLE material ADD COLUMN IF NOT EXISTS materialState VARCHAR(20) DEFAULT '待审核' COMMENT '资料状态：待审核、已通过、已拒绝';

-- 2. 检查 question 表是否有 questionState 列
SHOW COLUMNS FROM question LIKE 'questionState';

-- 如果没有 questionState 列，添加它
ALTER TABLE question ADD COLUMN IF NOT EXISTS questionState VARCHAR(20) DEFAULT '待审核' COMMENT '问题状态：待审核、已通过、已拒绝';

-- 3. 查看待审核的资料
SELECT materialID, materialTitle, materialSubject, uploaderName, uploadTime, materialState
FROM material
WHERE materialState = '待审核'
ORDER BY uploadTime DESC;

-- 4. 查看待审核的问题
SELECT questionID, questionTitle, questionContent, questionOwner, createTime, questionState
FROM question
WHERE questionState = '待审核'
ORDER BY createTime DESC;

-- 5. 更新现有资料为已通过状态（如果需要）
UPDATE material SET materialState = '已通过' WHERE materialState IS NULL OR materialState = '';

-- 6. 更新现有问题为已通过状态（如果需要）
UPDATE question SET questionState = '已通过' WHERE questionState IS NULL OR questionState = '';

-- 7. 验证
DESC material;
DESC question;

