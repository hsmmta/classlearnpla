<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
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
    </style>
</head>
<body>

    <div class="container">
        <h2>上传学习资料</h2>
        <form action="${pageContext.request.contextPath}/material/upload" method="post">
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
                <label for="materialContent">资料内容</label>
                <textarea id="materialContent" name="materialContent" placeholder="请输入或粘贴资料内容..." required></textarea>
            </div>
            <button type="submit" class="submit-btn">提 交</button>
        </form>
    </div>

</body>
</html>