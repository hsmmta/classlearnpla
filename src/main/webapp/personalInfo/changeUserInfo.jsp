<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/mobile.css">
    <title>班级学习社区平台-修改个人信息</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: "Microsoft YaHei", sans-serif;
        }
        body {
            background-color: #f0f4f9;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 20px;
        }
        .info-container {
            width: 500px;
            background-color: white;
            border-radius: 12px;
            padding: 40px;
            box-shadow: 0 0 16px rgba(0, 0, 0, 0.08);
        }
        .info-title {
            font-size: 20px;
            font-weight: bold;
            color: #222;
            margin-bottom: 30px;
        }
        .form-item {
            margin-bottom: 20px;
        }
        .form-label {
            display: block;
            font-size: 14px;
            color: #666;
            margin-bottom: 8px;
        }
        .form-input, .form-select {
            width: 100%;
            padding: 12px 15px;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 14px;
            transition: border-color 0.3s;
        }
        .form-input:focus, .form-select:focus {
            outline: none;
            border-color: #0088ff;
        }
        .submit-btn {
            width: 100%;
            padding: 12px;
            background-color: #0088ff;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 500;
            cursor: pointer;
            transition: background-color 0.3s;
            margin-top: 10px;
        }
        .submit-btn:hover {
            background-color: #0077ee;
        }
        .back-btn {
            display: block;
            text-align: center;
            margin-top: 20px;
            color: #666;
            font-size: 14px;
            text-decoration: none;
        }
        .back-btn:hover {
            color: #0088ff;
            text-decoration: underline;
        }
    </style>
    <script>
        // 提交修改
        function submitChangeInfo() {
            // 1. 获取表单值
            let userName = document.getElementById("userName").value.trim();
            let classID = document.getElementById("classID").value.trim();
            let gender = document.getElementById("gender").value;
            let studentID = document.getElementById("studentID").value.trim();
            let userEmail = document.getElementById("userEmail").value.trim();

            // 2. 前端校验
            if (userName === "") {
                alert("昵称不能为空！");
                document.getElementById("userName").focus();
                return false;
            }
            if (userName.length > 20) {
                alert("昵称长度不能超过20位！");
                document.getElementById("userName").focus();
                return false;
            }
            if (classID === "") {
                alert("班级编号不能为空！");
                document.getElementById("classID").focus();
                return false;
            }
            if (classID.length > 15) {
                alert("班级编号长度不能超过15位！");
                document.getElementById("classID").focus();
                return false;
            }
            if (gender === "") {
                alert("请选择性别！");
                document.getElementById("gender").focus();
                return false;
            }
            if (studentID === "") {
                alert("学号不能为空！");
                document.getElementById("studentID").focus();
                return false;
            }
            if (studentID.length > 15) {
                alert("学号长度不能超过15位！");
                document.getElementById("studentID").focus();
                return false;
            }
            if (userEmail !== "" && !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(userEmail)) {
                alert("请输入正确的邮箱格式！");
                document.getElementById("userEmail").focus();
                return false;
            }

            // 3. 构造请求参数
            let params = "userName=" + encodeURIComponent(userName) +
                "&classID=" + encodeURIComponent(classID) +
                "&gender=" + encodeURIComponent(gender) +
                "&studentID=" + encodeURIComponent(studentID) +
                "&userEmail=" + encodeURIComponent(userEmail);

            // 4. 异步提交
            let xhr = new XMLHttpRequest();
            xhr.open("POST", "${pageContext.request.contextPath}/changeUserInfo", true);
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4) {
                    if (xhr.status === 200) {
                        let result = JSON.parse(xhr.responseText);
                        alert(result.msg);
                        if (result.success) {
                            // 修改成功，返回个人信息页
                            window.location.href = "${pageContext.request.contextPath}/personalInfo/getUserInfo.jsp";
                        }
                    } else {
                        alert("请求失败，HTTP状态码：" + xhr.status);
                    }
                }
            };
            xhr.send(params);
            return false;
        }
    </script>
</head>
<body>
<div class="info-container">
    <div class="info-title">修改个人信息</div>
    <form onsubmit="return submitChangeInfo()">
        <!-- 昵称：JSP直接渲染Session中的值 -->
        <div class="form-item">
            <label class="form-label">昵称</label>
            <input type="text" id="userName" name="userName" class="form-input"
                   value="${empty sessionScope.userName ? '' : sessionScope.userName}" maxlength="20">
        </div>
        <!-- 班级编号：JSP直接渲染Session中的值 -->
        <div class="form-item">
            <label class="form-label">班级编号</label>
            <input type="text" id="classID" name="classID" class="form-input"
                   value="${empty sessionScope.classID ? '' : sessionScope.classID}" maxlength="15">
        </div>
        <!-- 性别：JSP判断并选中对应选项 -->
        <div class="form-item">
            <label class="form-label">性别</label>
            <select id="gender" name="gender" class="form-select">
                <option value="">请选择性别</option>
                <option value="male" ${sessionScope.gender == 'male' ? 'selected' : ''}>男</option>
                <option value="female" ${sessionScope.gender == 'female' ? 'selected' : ''}>女</option>
            </select>
        </div>
        <!-- 学号：JSP直接渲染Session中的值 -->
        <div class="form-item">
            <label class="form-label">学号</label>
            <input type="text" id="studentID" name="studentID" class="form-input"
                   value="${empty sessionScope.studentID ? '' : sessionScope.studentID}" maxlength="15">
        </div>
        <!-- 邮箱：JSP直接渲染Session中的值 -->
        <div class="form-item">
            <label class="form-label">邮箱</label>
            <input type="email" id="userEmail" name="userEmail" class="form-input"
                   value="${empty sessionScope.userEmail ? '' : sessionScope.userEmail}" placeholder="选填">
        </div>
        <!-- 保存按钮 -->
        <button type="submit" class="submit-btn">保存修改</button>
    </form>
    <a href="${pageContext.request.contextPath}/personalInfo/getUserInfo.jsp" class="back-btn">返回个人中心</a>
</div>
</body>
</html>