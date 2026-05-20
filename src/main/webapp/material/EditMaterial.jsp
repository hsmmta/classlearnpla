<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.example.model.Material" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/mobile.css">
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
        .form-group input:focus,
        .form-group select:focus,
        .form-group textarea:focus {
            outline: none;
            border-color: #4a90e2;
            box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.2);
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
        .submit-btn:hover {
            background-color: #357abd;
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
        .current-pdf-info {
            background-color: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 4px;
            padding: 12px;
            margin-bottom: 10px;
            color: #555;
            font-size: 14px;
        }
        .current-pdf-info a {
            color: #4a90e2;
            text-decoration: none;
        }
        .current-pdf-info a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>

    <div class="container">
        <a href="${pageContext.request.contextPath}/material/manage" class="back-btn">&larr; 返回我的资料</a>
        <h2>编辑学习资料</h2>
        <% Material material = (Material) request.getAttribute("material");
           if (material != null) {
               String mType = material.getMaterialType();
               if (mType == null) mType = "text";
        %>
        <form action="${pageContext.request.contextPath}/material/edit" method="post" enctype="multipart/form-data">
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
                <label for="materialType">资料类型</label>
                <select id="materialType" name="materialType" required onchange="toggleContentType()">
                    <option value="text" <%= "text".equals(mType) ? "selected" : "" %>>文本内容</option>
                    <option value="pdf" <%= "pdf".equals(mType) ? "selected" : "" %>>PDF文件</option>
                </select>
            </div>
            <div class="form-group" id="textContentGroup" style="<%= "pdf".equals(mType) ? "display:none;" : "" %>">
                <label for="materialContent">资料内容</label>
                <textarea id="materialContent" name="materialContent"><%= material.getMaterialContent() != null ? material.getMaterialContent() : "" %></textarea>
            </div>
            <div class="form-group" id="pdfFileGroup" style="<%= "pdf".equals(mType) ? "" : "display:none;" %>">
                <% if ("pdf".equals(mType) && material.getFilePath() != null) { %>
                <div class="current-pdf-info">
                    当前已有PDF文件：<a href="${pageContext.request.contextPath}/material/downloadPdf?path=<%= material.getFilePath() %>" target="_blank">查看/下载当前PDF</a>
                    <br><small>如需替换，请选择新文件；如不替换，留空即可。</small>
                </div>
                <% } %>
                <label for="pdfFile">上传新PDF文件</label>
                <input type="file" id="pdfFile" name="pdfFile" accept=".pdf">
            </div>
            <button type="submit" class="submit-btn">保存更改</button>
        </form>
        <% } else { %>
            <p>资料不存在或无法编辑。</p>
        <% } %>
    </div>

    <script>
        function toggleContentType() {
            var materialType = document.getElementById('materialType').value;
            var textContentGroup = document.getElementById('textContentGroup');
            var pdfFileGroup = document.getElementById('pdfFileGroup');

            if (materialType === 'text') {
                textContentGroup.style.display = 'block';
                pdfFileGroup.style.display = 'none';
            } else {
                textContentGroup.style.display = 'none';
                pdfFileGroup.style.display = 'block';
            }
        }
    </script>

</body>
</html>

