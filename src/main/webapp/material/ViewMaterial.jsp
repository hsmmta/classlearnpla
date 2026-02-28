<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.example.model.Material" %>
<%@ page import="org.example.model.Comment" %>
<%@ page import="java.util.List" %>
<%!
    public String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() != 11) {
            return "******";
        }
        return phone.substring(0, 3) + "******" + phone.substring(9);
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>查看资料 - 班级学习社区平台</title>
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
        .material-header h1 {
            font-size: 28px;
            color: #333;
            margin-bottom: 10px;
        }
        .material-meta {
            font-size: 14px;
            color: #888;
            margin-bottom: 30px;
            border-bottom: 1px solid #eee;
            padding-bottom: 15px;
        }
        .material-meta span {
            margin-right: 20px;
        }
        .material-content {
            font-size: 16px;
            line-height: 1.8;
            color: #444;
            white-space: pre-wrap;
            padding-bottom: 30px;
            border-bottom: 1px solid #eee;
        }
        .comment-section {
            margin-top: 30px;
        }
        .comment-section h3 {
            margin-bottom: 20px;
        }
        .comment-form textarea {
            width: 100%;
            height: 80px;
            padding: 10px;
            border: 1px solid #d9d9d9;
            border-radius: 4px;
            resize: vertical;
        }
        .comment-form button {
            margin-top: 10px;
            padding: 10px 20px;
            border: none;
            background-color: #4a90e2;
            color: white;
            border-radius: 4px;
            cursor: pointer;
            float: right;
        }
        .comment-list {
            margin-top: 60px;
        }
        .comment {
            border-bottom: 1px solid #eee;
            padding: 15px 0;
        }
        .comment:last-child {
            border-bottom: none;
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
        .comment-content {
            margin-top: 8px;
            color: #555;
        }
        .delete-btn {
            color: #d9534f;
            text-decoration: none;
            font-size: 12px;
        }
    </style>
</head>
<body>
    <div class="container">
        <%
            Material material = (Material) request.getAttribute("material");
            List<Comment> comments = (List<Comment>) request.getAttribute("comments");
            String currentUserID = (String) session.getAttribute("phone");
            if (material != null) {
        %>
            <div class="material-header">
                <h1><%= material.getMaterialTitle() %></h1>
            </div>
            <div class="material-meta">
                <span><strong>上传者:</strong> <%= maskPhoneNumber(material.getUserID()) %></span>
                <span><strong>上传时间:</strong> <%= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(material.getUploadTime()) %></span>
            </div>
            <div class="material-content">
                <%= material.getMaterialContent() %>
            </div>

            <div class="comment-section">
                <h3>发表评论</h3>
                <form action="${pageContext.request.contextPath}/comment/add" method="post" class="comment-form">
                    <input type="hidden" name="materialID" value="<%= material.getMaterialID() %>">
                    <textarea name="commentContent" placeholder="输入你的评论..." required></textarea>
                    <button type="submit">发表</button>
                </form>

                <div class="comment-list">
                    <h3>全部评论</h3>
                    <% if (comments != null && !comments.isEmpty()) {
                        for (Comment comment : comments) { %>
                            <div class="comment">
                                <div class="comment-header">
                                    <div>
                                        <span class="comment-author"><%= comment.getUserName() %></span>
                                        <span class="comment-time"><%= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(comment.getCommentTime()) %></span>
                                    </div>
                                    <% if (currentUserID != null && currentUserID.equals(comment.getUserID())) { %>
                                        <a href="${pageContext.request.contextPath}/comment/deleteMaterialComment?commentID=<%= comment.getCommentID() %>&materialID=<%= material.getMaterialID() %>" class="delete-btn" onclick="return confirm('确定要删除这条评论吗？');">删除</a>
                                    <% } %>
                                </div>
                                <p class="comment-content"><%= comment.getCommentContent() %></p>
                            </div>
                        <% }
                    } else { %>
                        <p>暂无评论，快来抢沙发吧！</p>
                    <% } %>
                </div>
            </div>

        <% } else { %>
            <p>无法加载该资料，可能已被删除或不存在。</p>
        <% } %>
    </div>
</body>
</html>