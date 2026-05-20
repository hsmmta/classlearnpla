<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.example.model.Material" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/mobile.css">
    <title>审核查看资料 - 管理后台</title>
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
        .admin-badge {
            display: inline-block;
            background-color: #d9534f;
            color: white;
            padding: 3px 10px;
            border-radius: 4px;
            font-size: 12px;
            margin-left: 10px;
            vertical-align: middle;
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
        }
        .pdf-viewer {
            width: 100%;
            min-height: 800px;
            border: 1px solid #ddd;
            margin-bottom: 30px;
        }
        .pdf-download-btn {
            display: inline-block;
            padding: 10px 20px;
            background-color: #5cb85c;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            margin-bottom: 20px;
        }
        .pdf-download-btn:hover {
            background-color: #4cae4c;
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
            cursor: pointer;
            border: none;
        }
        .back-btn:hover {
            background-color: #5a6268;
        }
        .audit-actions {
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #eee;
            text-align: center;
        }
        .audit-actions a {
            display: inline-block;
            padding: 10px 30px;
            border-radius: 4px;
            color: white;
            text-decoration: none;
            font-size: 16px;
            margin: 0 10px;
        }
        .approve-btn { background-color: #5cb85c; }
        .approve-btn:hover { background-color: #4cae4c; }
        .reject-btn { background-color: #d9534f; }
        .reject-btn:hover { background-color: #c9302c; }
    </style>
</head>
<body>
    <div class="container">
        <button class="back-btn" onclick="closeAndRefresh()">&larr; 关闭并返回审核列表</button>
        <span class="admin-badge">管理员审核视图</span>

        <%
            Material material = (Material) request.getAttribute("material");
            if (material != null) {
        %>
            <div class="material-header">
                <h1><%= material.getMaterialTitle() %></h1>
            </div>
            <div class="material-meta">
                <span><strong>上传者:</strong> <%= material.getUploaderName() %></span>
                <span><strong>上传时间:</strong> <%= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(material.getUploadTime()) %></span>
                <span><strong>所属科目:</strong> <%= material.getMaterialSubject() %></span>
                <span><strong>资料类型:</strong> <%= "pdf".equals(material.getMaterialType()) ? "PDF文件" : "文本内容" %></span>
            </div>

            <% if ("pdf".equals(material.getMaterialType()) && material.getFilePath() != null) { %>
                <a href="${pageContext.request.contextPath}/material/downloadPdf?path=<%= material.getFilePath() %>" class="pdf-download-btn" target="_blank">下载/查看PDF</a>
                <iframe src="${pageContext.request.contextPath}/material/downloadPdf?path=<%= material.getFilePath() %>" class="pdf-viewer"></iframe>
            <% } else { %>
                <div class="material-content"><%= material.getMaterialContent() != null ? material.getMaterialContent() : "（无文本内容）" %></div>
            <% } %>

            <div class="audit-actions">
                <a href="javascript:void(0);" class="approve-btn" onclick="auditAction('<%= material.getMaterialID() %>', '审核通过')">✓ 审核通过</a>
                <a href="javascript:void(0);" class="reject-btn" onclick="auditAction('<%= material.getMaterialID() %>', '审核不通过')">✗ 驳回</a>
            </div>
        <% } else { %>
            <p>无法加载该资料，可能已被删除或不存在。</p>
        <% } %>
    </div>

    <script>
        function closeAndRefresh() {
            if (window.opener) {
                window.opener.location.reload();
            }
            window.close();
            // 如果浏览器阻止关闭（非JS打开的窗口），则跳转回审核页
            window.location.href = '${pageContext.request.contextPath}/material/audit';
        }

        function auditAction(materialID, state) {
            var msg = (state === '审核通过') ? '确定通过该资料？' : '确定驳回该资料？';
            if (!confirm(msg)) return;

            // 用fetch发送审核请求，完成后关闭标签页并刷新父页面
            fetch('${pageContext.request.contextPath}/material/updateState?id=' + encodeURIComponent(materialID) + '&state=' + encodeURIComponent(state))
                .then(function() {
                    if (window.opener) {
                        window.opener.location.reload();
                    }
                    window.close();
                    window.location.href = '${pageContext.request.contextPath}/material/audit';
                });
        }
    </script>
</body>
</html>

