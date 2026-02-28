<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.example.model.Question" %>
<%@ page import="org.example.model.QuestionComment" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>查看问题 - 班级讨论区</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/auth/style.css">
    <style>
        .discussion-container {
            width: 800px;
        }
        .question-header h2 {
            font-size: 24px;
            margin-bottom: 10px;
        }
        .question-meta {
            font-size: 14px;
            color: #888;
            margin-bottom: 20px;
        }
        .question-content {
            font-size: 16px;
            line-height: 1.7;
            padding-bottom: 20px;
            border-bottom: 1px solid #eee;
        }
        .comment-section {
            margin-top: 30px;
        }
        .comment-form textarea {
            width: 100%;
            height: 100px;
            padding: 10px;
            border: 1px solid #d9d9d9;
            border-radius: 4px;
        }
        .comment-form button {
            margin-top: 10px;
            float: right;
        }
        .comment-list {
            margin-top: 70px;
        }
        .comment {
            display: flex;
            flex-direction: column;
            align-items: flex-start; /* Align items to the left */
            padding: 15px 0;
            border-top: 1px solid #eee;
        }
        .comment-header {
            display: flex;
            justify-content: space-between;
            width: 100%;
            align-items: center;
        }
        .comment-author {
            font-weight: bold;
        }
        .comment-time {
            font-size: 12px;
            color: #999;
            margin-left: 10px;
        }
        .comment-body {
            margin-top: 8px;
            padding: 10px 15px;
            border-radius: 15px;
            max-width: 80%;
            background-color: #f0f2f5; /* Default background for others' comments */
        }
        .comment.is-own .comment-body {
            background-color: #e1f5fe; /* Blue background for own comments */
            color: #01579b;
        }
        .comment-actions {
            display: flex;
            align-items: center;
        }
        .like-btn, .delete-btn {
            color: #888;
            text-decoration: none;
            font-size: 12px;
            margin-left: 15px;
        }
        .like-btn:hover, .delete-btn:hover {
            color: #4a90e2;
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
        .set-best-btn {
            background-color: #5cb85c;
            color: white;
            text-decoration: none;
            padding: 5px 12px;
            border-radius: 4px;
            font-size: 12px;
            margin-left: 15px;
        }
        .set-best-btn:hover {
            background-color: #4cae4c;
        }
        .comment.best-answer {
            border: 2px solid #ff9800;
            background-color: #fff8e1;
            padding: 18px 0;
        }
    </style>
</head>
<body>
    <%
        Question question = (Question) request.getAttribute("question");
        List<QuestionComment> comments = (List<QuestionComment>) request.getAttribute("comments");
        String currentUserID = (String) session.getAttribute("userID");
        // 判断当前用户是否为提问者
        boolean isQuestionOwner = question != null && currentUserID != null && currentUserID.equals(question.getUserID());
        // 是否已经设置过最满意答案
        boolean hasBestAnswer = question != null && question.getBestAnswerID() != null;
    %>
    <div class="page-title">
        <h1>班级讨论区</h1>
    </div>

    <div class="form-container discussion-container">
        <% if (question != null) { %>
            <div class="question-header">
                <h2><%= question.getQuestionTitle() %></h2>
                <p class="question-meta">
                    提问者: <%= question.getUserName() %> |
                    时间: <%= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(question.getCreationTime()) %>
                </p>
            </div>
            <div class="question-content">
                <%= question.getQuestionContent() %>
            </div>

            <div class="comment-section">
                <h3>你的回答</h3>
                <form action="${pageContext.request.contextPath}/discussion/comment" method="post" class="comment-form">
                    <input type="hidden" name="questionID" value="<%= question.getQuestionID() %>">
                    <textarea name="commentContent" required></textarea>
                    <button type="submit" class="submit-btn">提交回答</button>
                </form>

                <div class="comment-list">
                    <h3>所有回答</h3>
                    <% if (comments != null && !comments.isEmpty()) {
                        for (QuestionComment c : comments) {
                            boolean isOwnComment = currentUserID != null && currentUserID.equals(c.getUserID());
                            boolean isBest = c.isBestAnswer();
                    %>
                            <div class="comment <%= isOwnComment ? "is-own" : "" %> <%= isBest ? "best-answer" : "" %>">
                                <div class="comment-header">
                                    <div>
                                        <span class="comment-author"><%= c.getUserName() %></span>
                                        <span class="comment-time"><%= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(c.getCommentTime()) %></span>
                                        <% if (isBest) { %>
                                            <span class="best-answer-badge">⭐ 最满意答案</span>
                                        <% } %>
                                    </div>
                                    <div class="comment-actions">
                                        <a href="${pageContext.request.contextPath}/discussion/likeComment?commentID=<%= c.getCommentID() %>&questionID=<%= question.getQuestionID() %>" class="like-btn">
                                            👍 赞 (<%= c.getLikes() %>)
                                        </a>
                                        <% if (isOwnComment) { %>
                                            <a href="${pageContext.request.contextPath}/discussion/deleteComment?commentID=<%= c.getCommentID() %>&questionID=<%= question.getQuestionID() %>" class="delete-btn" onclick="return confirm('确定删除吗？');">删除</a>
                                        <% } %>
                                        <%
                                        // 只有提问者可以设置最满意答案，且问题还没有设置过最满意答案，且不是自己的回答
                                        if (isQuestionOwner && !hasBestAnswer && !isOwnComment) {
                                        %>
                                            <a href="${pageContext.request.contextPath}/discussion/setBestAnswer?questionID=<%= question.getQuestionID() %>&commentID=<%= c.getCommentID() %>"
                                               class="set-best-btn"
                                               onclick="return confirm('确定设为最满意答案吗？设置后不能修改。');">
                                                设为最满意答案
                                            </a>
                                        <% } %>
                                    </div>
                                </div>
                                <div class="comment-body">
                                    <%= c.getCommentContent() %>
                                </div>
                            </div>
                        <% }
                    } else { %>
                        <p>暂无回答。</p>
                    <% } %>
                </div>
            </div>
        <% } else { %>
            <p>问题不存在或已被删除。</p>
        <% } %>
    </div>
</body>
</html>