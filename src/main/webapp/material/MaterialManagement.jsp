<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="org.example.model.Material" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/mobile.css">
    <title>我的资料管理 - 班级学习社区平台</title>
    <style>
        body {
            font-family: "Helvetica Neue", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
            background-color: #f0f2f5;
            margin: 0;
            padding: 20px;
        }
        .container {
            max-width: 1000px;
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
        .material-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        .material-table th, .material-table td {
            border: 1px solid #e8e8e8;
            padding: 12px 15px;
            text-align: left;
        }
        .material-table th {
            background-color: #fafafa;
            font-weight: 600;
        }
        .material-table tr:hover {
            background-color: #f5f5f5;
        }
        .action-btn {
            color: #4a90e2;
            text-decoration: none;
            margin-right: 10px;
        }
        .action-btn.delete {
            color: #d9534f;
        }
        .action-btn:hover {
            text-decoration: underline;
        }
        .no-data {
            text-align: center;
            color: #999;
            padding: 40px;
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

    <div class="container">
        <a href="${pageContext.request.contextPath}/material/search" class="back-btn">← 返回资料区</a>
        <h2>我的资料管理</h2>
        <table class="material-table">
            <thead>
                <tr>
                    <th>资料ID</th>
                    <th>资料标题</th>
                    <th>所属科目</th>
                    <th>资料类型</th>
                    <th>上传时间</th>
                    <th>审核状态</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <%
                    List<Material> materials = (List<Material>) request.getAttribute("materials");
                    if (materials == null || materials.isEmpty()) {
                %>
                    <tr>
                        <td colspan="7" class="no-data">您还没有上传任何资料。</td>
                    </tr>
                <%
                    } else {
                        for (Material material : materials) {
                %>
                    <tr>
                        <td><%= material.getMaterialID() %></td>
                        <td><%= material.getMaterialTitle() %></td>
                        <td><%= material.getMaterialSubject() %></td>
                        <td><%= "pdf".equals(material.getMaterialType()) ? "PDF文件" : "文本内容" %></td>
                        <td><%= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(material.getUploadTime()) %></td>
                        <td><%= material.getMaterialState() %></td>
                        <td>
                            <a href="${pageContext.request.contextPath}/material/edit?id=<%= material.getMaterialID() %>" class="action-btn">编辑</a>
                            <a href="${pageContext.request.contextPath}/material/delete?id=<%= material.getMaterialID() %>" class="action-btn delete" onclick="return confirm('确定要删除这份资料吗？');">删除</a>
                        </td>
                    </tr>
                <%
                        }
                    }
                %>
            </tbody>
        </table>
    </div>

</body>
</html>