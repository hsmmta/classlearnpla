<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.example.model.Material" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>编辑资料 - 班级学习社区平台</title>
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
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        h2 {
            text-align: center;
            color: #333;
            margin-bottom: 30px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            font-size: 16px;
            color: #555;
            margin-bottom: 8px;
        }
        .form-group input[type="text"],
        .form-group select,
        .form-group textarea {
            width: 100%;
            padding: 10px;
            border: 1px solid #d9d9d9;
            border-radius: 4px;
            font-size: 16px;
            box-sizing: border-box;
        }
        .form-group textarea {
            height: 200px;
            resize: vertical;
        }
        .submit-btn {
            display: block;
            width: 100%;
            padding: 12px;
            border: none;
            border-radius: 4px;
            background-color: #4a90e2;
            color: white;
            font-size: 18px;
            cursor: pointer;
            text-align: center;
        }
    </style>
</head>
<body>

    <div class="container">
        <h2>编辑学习资料</h2>
        <% Material material = (Material) request.getAttribute("material");
           if (material != null) { %>
        <form action="${pageContext.request.contextPath}/material/edit" method="post">
            <input type="hidden" name="materialID" value="<%= material.getMaterialID() %>">
            <div class="form-group">
                <label for="materialTitle">资料标题</label>
                <input type="text" id="materialTitle" name="materialTitle" value="<%= material.getMaterialTitle() %>" required>
            </div>
            <div class="form-group">
                <label for="materialSubject">所属科目</label>
                <select id="materialSubject" name="materialSubject" required>
                    <option value="数学" <%= "数学".equals(material.getMaterialSubject()) ? "selected" : "" %>>数学</option>
                    <option value="英语" <%= "英语".equals(material.getMaterialSubject()) ? "selected" : "" %>>英语</option>
                    <option value="计算机" <%= "计算机".equals(material.getMaterialSubject()) ? "selected" : "" %>>计算机</option>
                    <option value="思想政治" <%= "思想政治".equals(material.getMaterialSubject()) ? "selected" : "" %>>思想政治</option>
                    <option value="其他" <%= "其他".equals(material.getMaterialSubject()) ? "selected" : "" %>>其他</option>
                </select>
            </div>
            <div class="form-group">
                <label for="materialContent">资料内容</label>
                <textarea id="materialContent" name="materialContent" required><%= material.getMaterialContent() %></textarea>
            </div>
            <button type="submit" class="submit-btn">保存更改</button>
        </form>
        <% } else { %>
            <p>资料不存在或无法编辑。</p>
        <% } %>
    </div>

</body>
</html>