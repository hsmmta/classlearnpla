<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="org.example.model.Question" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>问题审核 - 班级讨论区</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/auth/style.css">
    <style>
        .audit-container {
            width: 1000px;
        }
        .audit-table {
            width: 100%;
            border-collapse: collapse;
        }
        .audit-table th, .audit-table td {
            border-bottom: 1px solid #e8e8e8;
            padding: 15px;
            text-align: left;
        }
        .action-btn {
            padding: 5px 10px;
            border-radius: 4px;
            text-decoration: none;
            color: white;
            margin-right: 5px;
        }
        .approve-btn { background-color: #5cb85c; }
        .reject-btn { background-color: #d9534f; }
        .view-btn { background-color: #4a90e2; }
    </style>
</head>
<body>
    <div class="page-title">
        <h1>问题审核</h1>
    </div>
    <div class="form-container audit-container">
        <table class="audit-table">
            <thead>
                <tr>
                    <th>标题</th>
                    <th>提问者</th>
                    <th>时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <%
                    List<Question> questions = (List<Question>) request.getAttribute("pendingQuestions");
                    if (questions != null && !questions.isEmpty()) {
                        for (Question q : questions) {
                %>
                    <tr>
                        <td><%= q.getQuestionTitle() %></td>
                        <td><%= q.getUserName() %></td>
                        <td><%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(q.getCreationTime()) %></td>
                        <td>
                            <a href="${pageContext.request.contextPath}/discussion/view?id=<%= q.getQuestionID() %>" target="_blank" class="action-btn view-btn">查看</a>
                            <a href="${pageContext.request.contextPath}/discussion/updateState?id=<%= q.getQuestionID() %>&state=审核通过" class="action-btn approve-btn">通过</a>
                            <a href="${pageContext.request.contextPath}/discussion/updateState?id=<%= q.getQuestionID() %>&state=审核不通过" class="action-btn reject-btn">驳回</a>
                        </td>
                    </tr>
                <%
                        }
                    } else {
                %>
                    <tr><td colspan="4" style="text-align:center; padding: 40px;">没有待审核的问题。</td></tr>
                <%
                    }
                %>
            </tbody>
        </table>
    </div>
</body>
</html>