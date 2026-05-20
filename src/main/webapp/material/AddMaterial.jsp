<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/mobile.css">
    <title>上传资料 - 班级学习社区平台</title>
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
    </style>
</head>
<body>

    <div class="container">
        <a href="${pageContext.request.contextPath}/material/search" class="back-btn">← 返回资料区</a>
        <h2>上传学习资料</h2>
        <form action="${pageContext.request.contextPath}/material/upload" method="post" enctype="multipart/form-data">
            <div class="form-group">
                <label for="materialTitle">资料标题</label>
                <input type="text" id="materialTitle" name="materialTitle" placeholder="请输入资料的标题" required>
            </div>
            <div class="form-group">
                <label for="materialSubject">所属科目</label>
                <select id="materialSubject" name="materialSubject" required>
                    <option value="" disabled selected>请选择科目</option>
                    <option value="数学">数学</option>
                    <option value="英语">英语</option>
                    <option value="计算机">计算机</option>
                    <option value="思想政治">思想政治</option>
                    <option value="其他">其他</option>
                </select>
            </div>
            <div class="form-group">
                <label for="materialType">资料类型</label>
                <select id="materialType" name="materialType" required onchange="toggleContentType()">
                    <option value="text" selected>文本内容</option>
                    <option value="pdf">PDF文件</option>
                </select>
            </div>
            <div class="form-group" id="textContentGroup">
                <label for="materialContent">资料内容</label>
                <textarea id="materialContent" name="materialContent" placeholder="请输入或粘贴资料内容..."></textarea>
            </div>
            <div class="form-group" id="pdfFileGroup" style="display:none;">
                <label for="pdfFile">上传PDF文件</label>
                <input type="file" id="pdfFile" name="pdfFile" accept=".pdf">
            </div>
            <button type="submit" class="submit-btn">提 交</button>
        </form>
    </div>

    <script>
        function toggleContentType() {
            const materialType = document.getElementById('materialType').value;
            const textContentGroup = document.getElementById('textContentGroup');
            const pdfFileGroup = document.getElementById('pdfFileGroup');
            const textContent = document.getElementById('materialContent');
            const pdfFile = document.getElementById('pdfFile');

            if (materialType === 'text') {
                textContentGroup.style.display = 'block';
                pdfFileGroup.style.display = 'none';
                textContent.required = true;
                pdfFile.required = false;
            } else {
                textContentGroup.style.display = 'none';
                pdfFileGroup.style.display = 'block';
                textContent.required = false;
                pdfFile.required = true;
            }
        }
    </script>

</body>
</html>