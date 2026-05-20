<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/mobile.css">
    <title>提出问题 - 班级讨论区</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/auth/style.css">
    <style>
        /* Overrides for discussion page */
        .form-container {
            width: 800px;
        }
        .input-group textarea {
            width: 100%;
            padding: 10px;
            border: 1px solid #d9d9d9;
            border-radius: 4px;
            font-size: 16px;
            box-sizing: border-box;
            height: 150px;
            resize: vertical;
        }
        .back-btn {
            display: inline-block;
            margin-bottom: 15px;
            padding: 8px 16px;
            background-color: #6c757d;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            font-size: 14px;
            transition: background-color 0.3s;
        }
        .back-btn:hover {
            background-color: #5a6268;
        }
    </style>
</head>
<body>
    <div class="page-title">
        <h1>班级讨论区</h1>
        <h2>提出一个新问题</h2>
    </div>

    <div class="form-container">
        <a href="${pageContext.request.contextPath}/discussion/list" class="back-btn">← 返回讨论区</a>
        <form action="${pageContext.request.contextPath}/discussion/ask" method="post">
            <div class="input-group">
                <label for="questionTitle">问题标题</label>
                <input type="text" id="questionTitle" name="questionTitle" required>
            </div>
            <div class="input-group">
                <label for="questionContent">问题描述</label>
                <textarea id="questionContent" name="questionContent" required></textarea>
            </div>
            <button type="submit" class="submit-btn">提交问题</button>
        </form>
    </div>
</body>
</html>