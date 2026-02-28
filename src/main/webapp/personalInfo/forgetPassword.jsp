<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // 检查用户是否已登录
    String userID = (String) session.getAttribute("userID");
    String userEmail = (String) session.getAttribute("userEmail");

    // 如果未登录，重定向到登录页面
    if (userID == null || userID.trim().isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/auth/signin.html");
        return;
    }

    // 如果没有绑定邮箱，重定向到修改信息页面
    if (userEmail == null || userEmail.trim().isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/personalInfo/changeUserInfo.jsp?msg=请先绑定邮箱");
        return;
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>班级学习社区平台-找回密码</title>
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
        .reset-container {
            width: 500px;
            background-color: white;
            border-radius: 12px;
            padding: 40px;
            box-shadow: 0 0 16px rgba(0, 0, 0, 0.08);
        }
        .reset-title {
            font-size: 20px;
            font-weight: bold;
            color: #222;
            margin-bottom: 30px;
            text-align: center;
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
        .form-input {
            width: 100%;
            padding: 12px 15px;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 14px;
            transition: border-color 0.3s;
        }
        .form-input:disabled {
            background-color: #f5f5f5;
            color: #999;
        }
        .form-input:focus {
            outline: none;
            border-color: #0088ff;
        }
        .code-row {
            display: flex;
            gap: 10px;
        }
        .code-input {
            flex: 1;
        }
        .code-btn {
            width: 120px;
            padding: 12px 15px;
            background-color: #0088ff;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            cursor: pointer;
            transition: background-color 0.3s;
        }
        .code-btn:disabled {
            background-color: #999;
            cursor: not-allowed;
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
        // 发送验证码
        function sendVerifyCode() {
            let codeBtn = document.getElementById("sendCodeBtn");
            // 禁用按钮防止重复点击
            codeBtn.disabled = true;
            codeBtn.innerText = "发送中...";

            // 异步请求发送验证码
            let xhr = new XMLHttpRequest();
            xhr.open("POST", "${pageContext.request.contextPath}/sendResetPwdCode", true);
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4) {
                    let result = JSON.parse(xhr.responseText);
                    alert(result.msg); // 弹窗提示发送结果
                    if (result.success) {
                        // 60秒倒计时
                        let count = 60;
                        codeBtn.innerText = count + "秒后重发";
                        let timer = setInterval(() => {
                            count--;
                            codeBtn.innerText = count + "秒后重发";
                            if (count <= 0) {
                                clearInterval(timer);
                                codeBtn.disabled = false;
                                codeBtn.innerText = "获取验证码";
                            }
                        }, 1000);
                    } else {
                        codeBtn.disabled = false;
                        codeBtn.innerText = "获取验证码";
                    }
                }
            };
            xhr.send();
        }

        // 提交重置密码
        // 替换原有submitResetPwd函数
        function submitResetPwd() {
            let verifyCode = document.getElementById("verifyCode").value.trim();
            let newPwd = document.getElementById("newPwd").value.trim();
            let confirmPwd = document.getElementById("confirmPwd").value.trim();

            if (verifyCode === "") {
                alert("请输入验证码！");
                document.getElementById("verifyCode").focus();
                return false;
            }
            if (newPwd === "" || newPwd.length < 6) {
                alert("新密码不能为空且长度不少于6位！");
                document.getElementById("newPwd").focus();
                return false;
            }
            if (newPwd !== confirmPwd) {
                alert("两次输入的密码不一致！");
                document.getElementById("confirmPwd").focus();
                return false;
            }

            // 优化异步请求：增加错误处理
            let xhr = new XMLHttpRequest();
            xhr.open("POST", "${pageContext.request.contextPath}/resetPassword", true);
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            // 监听请求状态变化（关键：确保正确处理响应）
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4) {
                    if (xhr.status === 200) {
                        try {
                            let result = JSON.parse(xhr.responseText);
                            alert(result.msg); // 弹窗提示结果
                            if (result.success) {
                                // 跳转登录页（确保路径正确）
                                setTimeout(() => {
                                    window.location.href = "${pageContext.request.contextPath}/auth/signin.html";
                                }, 1500);
                            }
                        } catch (e) {
                            alert("响应解析失败：" + e.message);
                        }
                    } else {
                        alert("请求失败，状态码：" + xhr.status);
                    }
                }
            };
            // 发送参数（确保参数名和Servlet一致）
            xhr.send("verifyCode=" + verifyCode + "&newPwd=" + newPwd + "&confirmPwd=" + confirmPwd);
            return false;
        }
    </script>
</head>
<body>
<div class="reset-container">
    <div class="reset-title">找回密码</div>
    <form onsubmit="return submitResetPwd()">
        <!-- 绑定邮箱（不可修改） -->
        <div class="form-item">
            <label class="form-label">绑定邮箱</label>
            <input type="email" id="userEmail" class="form-input"
                   value="${empty sessionScope.userEmail ? '' : sessionScope.userEmail}"
                   disabled>
        </div>
        <!-- 验证码输入 -->
        <div class="form-item">
            <label class="form-label">验证码</label>
            <div class="code-row">
                <input type="text" id="verifyCode" class="form-input code-input" placeholder="请输入邮箱验证码">
                <button type="button" id="sendCodeBtn" class="code-btn" onclick="sendVerifyCode()">获取验证码</button>
            </div>
        </div>
        <!-- 新密码 -->
        <div class="form-item">
            <label class="form-label">新密码</label>
            <input type="password" id="newPwd" class="form-input" placeholder="请输入新密码（不少于6位）">
        </div>
        <!-- 确认新密码 -->
        <div class="form-item">
            <label class="form-label">确认新密码</label>
            <input type="password" id="confirmPwd" class="form-input" placeholder="请再次输入新密码">
        </div>
        <!-- 提交按钮 -->
        <button type="submit" class="submit-btn">确认重置</button>
    </form>
    <a href="${pageContext.request.contextPath}/personalInfo/getUserInfo.jsp" class="back-btn">返回个人中心</a>
</div>
</body>
</html>