<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="org.example.model.Question" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/mobile.css">
    <title>班级讨论区</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/auth/style.css">
    <style>
        .list-container {
            width: 900px;
        }
        .question-list {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        .question-list th, .question-list td {
            border-bottom: 1px solid #e8e8e8;
            padding: 15px;
            text-align: left;
        }
        .question-list th {
            background-color: #fafafa;
        }
        .question-list tr:hover {
            background-color: #f5f5f5;
        }
        .question-title a {
            color: #333;
            text-decoration: none;
            font-weight: 600;
            font-size: 16px;
        }
        .question-title a:hover {
            color: #4a90e2;
        }
        .ask-btn-container {
            text-align: right;
            margin-bottom: 20px;
        }
        .back-btn {
            display: inline-block;
            margin-bottom: 20px;
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
    </div>

    <div class="form-container list-container">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <a href="${pageContext.request.contextPath}/home/index.jsp" class="back-btn">← 返回首页</a>
            <a href="${pageContext.request.contextPath}/discussion/AskQuestion.jsp" class="submit-btn" style="display: inline-block; width: auto;">我有问题</a>
        </div>
        <table class="question-list">
            <thead>
                <tr>
                    <th>标题</th>
                    <th>提问者</th>
                    <th>发布时间</th>
                </tr>
            </thead>
            <tbody>
                <%
                    List<Question> questions = (List<Question>) request.getAttribute("questions");
                    if (questions != null && !questions.isEmpty()) {
                        for (Question q : questions) {
                %>
                    <tr>
                        <td class="question-title"><a href="${pageContext.request.contextPath}/discussion/view?id=<%= q.getQuestionID() %>"><%= q.getQuestionTitle() %></a></td>
                        <td><%= q.getUserName() %></td>
                        <td><%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(q.getCreationTime()) %></td>
                    </tr>
                <%
                        }
                    } else {
                %>
                    <tr><td colspan="3" style="text-align:center; padding: 40px;">当前没有已发布的讨论。</td></tr>
                <%
                    }
                %>
            </tbody>
        </table>
    </div>
</body>
</html>