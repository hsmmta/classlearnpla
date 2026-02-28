<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="org.example.model.Material" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>资料查询 - 班级学习社区平台</title>
    <style>
        body {
            font-family: "Helvetica Neue", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
            background-color: #f0f2f5;
            margin: 0;
            padding: 0;
        }
        /* 导航栏样式 */
        .nav-bar {
            background-color: white;
            padding: 15px 0;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
            margin-bottom: 30px;
        }
        .nav-bar ul {
            list-style: none;
            display: flex;
            justify-content: center;
            gap: 40px;
            margin: 0;
            padding: 0;
        }
        .nav-bar a {
            text-decoration: none;
            color: #333;
            font-size: 16px;
            transition: color 0.3s;
        }
        .nav-bar a:hover {
            color: #0088ff;
        }
        .nav-bar .active {
            color: #0088ff;
            font-weight: bold;
        }
        .container {
            max-width: 1000px;
            margin: 0 auto;
            background-color: #fff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            margin-bottom: 50px;
        }
        h2 {
            text-align: center;
            color: #333;
            margin-bottom: 30px;
        }
        .action-buttons {
            display: flex;
            gap: 10px;
            margin-bottom: 20px;
            justify-content: center;
        }
        .action-buttons a, .action-buttons button {
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            text-decoration: none;
            font-size: 16px;
            cursor: pointer;
            transition: background-color 0.3s;
        }
        .upload-btn {
            background-color: #4a90e2;
            color: white;
        }
        .upload-btn:hover {
            background-color: #357abd;
        }
        .my-materials-btn {
            background-color: #50c878;
            color: white;
        }
        .my-materials-btn:hover {
            background-color: #3a9b5c;
        }
        .search-bar {
            display: flex;
            margin-bottom: 20px;
        }
        .search-bar input[type="text"] {
            flex-grow: 1;
            padding: 10px;
            border: 1px solid #d9d9d9;
            border-radius: 4px 0 0 4px;
            font-size: 16px;
        }
        .search-bar button {
            padding: 10px 20px;
            border: none;
            background-color: #4a90e2;
            color: white;
            border-radius: 0 4px 4px 0;
            cursor: pointer;
            font-size: 16px;
        }
        .results-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        .results-table th, .results-table td {
            border: 1px solid #e8e8e8;
            padding: 12px 15px;
            text-align: left;
        }
        .results-table th {
            background-color: #fafafa;
        }
        .view-link {
            color: #4a90e2;
            text-decoration: none;
        }
    </style>
</head>
<body>
    <div class="nav-bar">
        <ul>
            <li><a href="${pageContext.request.contextPath}/home/index.jsp">首页</a></li>
            <li><a href="${pageContext.request.contextPath}/material/search" class="active">资料区</a></li>
            <li><a href="${pageContext.request.contextPath}/discussion/list">讨论区</a></li>
            <li><a href="${pageContext.request.contextPath}/Prize/DisplayPrize.jsp">积分商城</a></li>
            <li><a href="${pageContext.request.contextPath}/personalInfo/getUserInfo.jsp">个人中心</a></li>
            <li><a href="${pageContext.request.contextPath}/auth/signin.html">退出登录</a></li>
        </ul>
    </div>

    <div class="container">
        <h2>资料查询</h2>

        <div class="action-buttons">
            <a href="${pageContext.request.contextPath}/material/AddMaterial.jsp" class="upload-btn">📤 上传资料</a>
            <a href="${pageContext.request.contextPath}/material/manage" class="my-materials-btn">📋 我的资料</a>
        </div>

        <form action="${pageContext.request.contextPath}/material/search" method="get" class="search-bar">
            <input type="text" name="query" placeholder="输入关键词搜索资料..." value="${param.query}">
            <button type="submit">搜 索</button>
        </form>

        <table class="results-table">
            <thead>
                <tr>
                    <th>资料标题</th>
                    <th>所属科目</th>
                    <th>上传者</th>
                    <th>上传时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <%
                    List<Material> results = (List<Material>) request.getAttribute("results");
                    if (results != null && !results.isEmpty()) {
                        for (Material material : results) {
                %>
                    <tr>
                        <td><%= material.getMaterialTitle() %></td>
                        <td><%= material.getMaterialSubject() %></td>
                        <td><%= material.getUploaderName() %></td>
                        <td><%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(material.getUploadTime()) %></td>
                        <td><a href="${pageContext.request.contextPath}/material/view?id=<%= material.getMaterialID() %>" class="view-link">查看</a></td>
                    </tr>
                <%
                        }
                    } else if (request.getAttribute("results") != null) {
                %>
                    <tr><td colspan="5" style="text-align:center; padding: 20px;">未找到相关资料。</td></tr>
                <%
                    }
                %>
            </tbody>
        </table>
    </div>
</body>
</html>