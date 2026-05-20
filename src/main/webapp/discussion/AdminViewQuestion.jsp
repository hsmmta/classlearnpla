<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.example.model.Question" %>
<%@ page import="org.example.model.QuestionComment" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/mobile.css">
    <title>审核查看问题 - 管理后台</title>
    <style>
        body {
            font-family: "Helvetica Neue", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
            background-color: #f0f2f5;
            margin: 0;
            padding: 20px;
        }
        .container {
            max-width: 800px;
            margin: 0 auto;
            background-color: #fff;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        .admin-badge {
            display: inline-block;
            background-color: #d9534f;
            color: white;
            padding: 3px 10px;
            border-radius: 4px;
            font-size: 12px;
            margin-left: 10px;
            vertical-align: middle;
        }
        .question-header h2 {
            font-size: 24px;
            color: #333;
            margin-bottom: 10px;
        }
        .question-meta {
            font-size: 14px;
            color: #888;
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 1px solid #eee;
        }
        .question-content {
            font-size: 16px;
            line-height: 1.8;
            color: #444;
            white-space: pre-wrap;
            padding-bottom: 20px;
            border-bottom: 1px solid #eee;
        }
        .comment-list {
            margin-top: 30px;
        }
        .comment-list h3 {
            margin-bottom: 15px;
            color: #333;
        }
        .comment {
            padding: 15px 0;
            border-top: 1px solid #eee;
        }
        .comment-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .comment-author {
            font-weight: bold;
            color: #333;
        }
        .comment-time {
            font-size: 12px;
            color: #999;
            margin-left: 10px;
        }
        .comment-body {
            margin-top: 8px;
            padding: 10px 15px;
            border-radius: 8px;
            background-color: #f0f2f5;
            color: #444;
            line-height: 1.6;
        }
        .best-answer-badge {
            display: inline-block;
            background-color: #ff9800;
            color: white;
            padding: 3px 10px;
            border-radius: 12px;
            font-size: 12px;
            margin-left: 10px;
            font-weight: bold;
        }
        .comment.best-answer {
            border: 2px solid #ff9800;
            background-color: #fff8e1;
            border-radius: 8px;
            padding: 15px;
            margin-top: 10px;
        }
        .like-count {
            font-size: 12px;
            color: #888;
            margin-left: 15px;
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
            cursor: pointer;
            border: none;
        }
        .back-btn:hover {
            background-color: #5a6268;
        }
        .audit-actions {
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #eee;
            text-align: center;
        }
        .audit-actions a {
            display: inline-block;
            padding: 10px 30px;
            border-radius: 4px;
            color: white;
            text-decoration: none;
            font-size: 16px;
            margin: 0 10px;
        }
        .approve-btn { background-color: #5cb85c; }
        .approve-btn:hover { background-color: #4cae4c; }
        .reject-btn { background-color: #d9534f; }
        .reject-btn:hover { background-color: #c9302c; }
        .status-info {
            display: inline-block;
            padding: 3px 10px;
            border-radius: 4px;
            font-size: 13px;
            font-weight: bold;
        }
        .status-pending { background-color: #fcf8e3; color: #8a6d3b; }
        .status-approved { background-color: #dff0d8; color: #3c763d; }
        .status-rejected { background-color: #f2dede; color: #a94442; }
    </style>
</head>
<body>
    <%
        Question question = (Question) request.getAttribute("question");
        List<QuestionComment> comments = (List<QuestionComment>) request.getAttribute("comments");
    %>
    <div class="container">
        <button class="back-btn" onclick="closeAndRefresh()">← 关闭并返回审核列表</button>
        <span class="admin-badge">管理员审核视图</span>

        <% if (question != null) { %>
            <div class="question-header">
                <h2><%= question.getQuestionTitle() %></h2>
            </div>
            <div class="question-meta">
                <span><strong>提问者:</strong> <%= question.getUserName() %></span> &nbsp;|&nbsp;
                <span><strong>提问时间:</strong> <%= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(question.getCreationTime()) %></span>
            </div>
            <div class="question-content"><%= question.getQuestionContent() %></div>

            <div class="comment-list">
                <h3>所有回答 (<%= comments != null ? comments.size() : 0 %>)</h3>
                <% if (comments != null && !comments.isEmpty()) {
                    for (QuestionComment c : comments) {
                        boolean isBest = c.isBestAnswer();
                %>
                    <div class="comment <%= isBest ? "best-answer" : "" %>">
                        <div class="comment-header">
                            <div>
                                <span class="comment-author"><%= c.getUserName() %></span>
                                <span class="comment-time"><%= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(c.getCommentTime()) %></span>
                                <% if (isBest) { %>
                                    <span class="best-answer-badge">⭐ 最满意答案</span>
                                <% } %>
                            </div>
                            <span class="like-count">👍 <%= c.getLikes() %></span>
                        </div>
                        <div class="comment-body"><%= c.getCommentContent() %></div>
                    </div>
                <% }
                } else { %>
                    <p style="color:#999;">暂无回答。</p>
                <% } %>
            </div>

            <div class="audit-actions">
                <a href="javascript:void(0);" class="approve-btn" onclick="auditAction('<%= question.getQuestionID() %>', '审核通过')">✓ 审核通过</a>
                <a href="javascript:void(0);" class="reject-btn" onclick="auditAction('<%= question.getQuestionID() %>', '审核不通过')">✗ 驳回</a>
            </div>
        <% } else { %>
            <p>问题不存在或已被删除。</p>
        <% } %>
    </div>

    <script>
        function closeAndRefresh() {
            if (window.opener) {
                window.opener.location.reload();
            }
            window.close();
            window.location.href = '${pageContext.request.contextPath}/discussion/audit';
        }

        function auditAction(questionID, state) {
            var msg = (state === '审核通过') ? '确定通过该问题？' : '确定驳回该问题？';
            if (!confirm(msg)) return;

            fetch('${pageContext.request.contextPath}/discussion/updateState?id=' + encodeURIComponent(questionID) + '&state=' + encodeURIComponent(state))
                .then(function() {
                    if (window.opener) {
                        window.opener.location.reload();
                    }
                    window.close();
                    window.location.href = '${pageContext.request.contextPath}/discussion/audit';
                });
        }
    </script>
</body>
</html>

