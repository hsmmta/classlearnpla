<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="org.example.model.Material" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>资料审核 - 班级学习社区平台</title>
    <style>
        body {
            font-family: "Helvetica Neue", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
            background-color: #f0f2f5;
            margin: 0;
            padding: 20px;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background-color: #fff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        h2 {
            text-align: center;
            color: #333;
            margin-bottom: 30px;
        }
        .audit-table {
            width: 100%;
            border-collapse: collapse;
        }
        .audit-table th, .audit-table td {
            border: 1px solid #e8e8e8;
            padding: 12px 15px;
            text-align: left;
        }
        .audit-table th {
            background-color: #fafafa;
        }
        .action-btn {
            padding: 5px 10px;
            border-radius: 4px;
            text-decoration: none;
            color: white;
            margin-right: 5px;
        }
        .approve-btn {
            background-color: #5cb85c;
        }
        .reject-btn {
            background-color: #d9534f;
        }
        .view-btn {
             background-color: #4a90e2;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>待审核资料列表</h2>
        <table class="audit-table">
            <thead>
                <tr>
                    <th>资料ID</th>
                    <th>资料标题</th>
                    <th>所属科目</th>
                    <th>上传者</th>
                    <th>上传时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <%
                    List<Material> materials = (List<Material>) request.getAttribute("pendingMaterials");
                    if (materials != null && !materials.isEmpty()) {
                        for (Material material : materials) {
                %>
                    <tr>
                        <td><%= material.getMaterialID() %></td>
                        <td><%= material.getMaterialTitle() %></td>
                        <td><%= material.getMaterialSubject() %></td>
                        <td><%= material.getUploaderName() %></td>
                        <td><%= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(material.getUploadTime()) %></td>
                        <td>
                            <a href="/material/view_content?id=<%= material.getMaterialID() %>" target="_blank" class="action-btn view-btn">查看内容</a>
                            <a href="/material/updateState?id=<%= material.getMaterialID() %>&state=审核通过" class="action-btn approve-btn">通过</a>
                            <a href="/material/updateState?id=<%= material.getMaterialID() %>&state=审核不通过" class="action-btn reject-btn">驳回</a>
                        </td>
                    </tr>
                <%
                        }
                    } else {
                %>
                    <tr><td colspan="6" style="text-align:center; padding: 20px;">当前没有待审核的资料。</td></tr>
                <%
                    }
                %>
            </tbody>
        </table>
    </div>
</body>
</html>