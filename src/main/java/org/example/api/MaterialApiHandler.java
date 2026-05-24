package org.example.api;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.example.util.DBUtil;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;

public class MaterialApiHandler {
    private static final String UPLOAD_DIR = "uploads/materials";
    private static final long MAX_PDF_SIZE = 30L * 1024 * 1024;

    void handle(String path, String method, HttpServletRequest request, HttpServletResponse response, ServletContext ctx)
            throws Exception {
        if (!SessionHelper.isLoggedIn(request) && !path.contains("/download")) {
            JsonResponse.write(response, HttpServletResponse.SC_UNAUTHORIZED, JsonResponse.fail("请先登录"));
            return;
        }

        if ("/materials".equals(path) && "GET".equals(method)) {
            listMaterials(request, response, false);
            return;
        }
        if ("/materials/mine".equals(path) && "GET".equals(method)) {
            listMyMaterials(request, response);
            return;
        }
        if ("/materials/pending".equals(path) && "GET".equals(method)) {
            if (!SessionHelper.isAdmin(request)) {
                JsonResponse.write(response, HttpServletResponse.SC_FORBIDDEN, JsonResponse.fail("无权限"));
                return;
            }
            listPending(response);
            return;
        }
        if ("/materials/approved".equals(path) && "GET".equals(method)) {
            if (!SessionHelper.isAdmin(request)) {
                JsonResponse.write(response, HttpServletResponse.SC_FORBIDDEN, JsonResponse.fail("无权限"));
                return;
            }
            listApproved(response);
            return;
        }
        if ("/materials".equals(path) && "POST".equals(method)) {
            upload(request, response, ctx);
            return;
        }

        if (path.startsWith("/materials/")) {
            String rest = path.substring("/materials/".length());
            if (rest.contains("/comments/") && rest.endsWith("/admin-delete") && "POST".equals(method)) {
                if (!SessionHelper.isAdmin(request)) {
                    JsonResponse.write(response, HttpServletResponse.SC_FORBIDDEN, JsonResponse.fail("无权限"));
                    return;
                }
                String[] parts = rest.split("/");
                if (parts.length >= 4) {
                    adminDeleteComment(parts[0], parts[2], response);
                    return;
                }
            }
            if (rest.contains("/comments/") && "DELETE".equals(method)) {
                String[] parts = rest.split("/");
                if (parts.length >= 3) {
                    deleteOwnComment(parts[0], parts[2], request, response);
                    return;
                }
            }
            if (rest.endsWith("/download") && "GET".equals(method)) {
                String id = rest.substring(0, rest.length() - "/download".length());
                download(id, response, ctx);
                return;
            }
            if (rest.endsWith("/download-attachment") && "GET".equals(method)) {
                String id = rest.substring(0, rest.length() - "/download-attachment".length());
                downloadAttachment(id, response, ctx);
                return;
            }
            if (rest.endsWith("/preview-images") && "GET".equals(method)) {
                String id = rest.substring(0, rest.length() - "/preview-images".length());
                listPreviewImages(id, response, ctx);
                return;
            }
            if (rest.contains("/preview-images/") && "GET".equals(method)) {
                String[] parts = rest.split("/preview-images/");
                if (parts.length == 2) {
                    servePreviewImage(parts[0], parts[1], response, ctx);
                    return;
                }
            }
            if (rest.endsWith("/comments") && "POST".equals(method)) {
                String id = rest.substring(0, rest.length() - "/comments".length());
                addComment(id, request, response);
                return;
            }
            if (rest.endsWith("/audit") && "POST".equals(method)) {
                if (!SessionHelper.isAdmin(request)) {
                    JsonResponse.write(response, HttpServletResponse.SC_FORBIDDEN, JsonResponse.fail("无权限"));
                    return;
                }
                String id = rest.substring(0, rest.length() - "/audit".length());
                audit(id, request, response);
                return;
            }
            if (rest.endsWith("/admin-delete") && "POST".equals(method)) {
                if (!SessionHelper.isAdmin(request)) {
                    JsonResponse.write(response, HttpServletResponse.SC_FORBIDDEN, JsonResponse.fail("无权限"));
                    return;
                }
                String id = rest.substring(0, rest.length() - "/admin-delete".length());
                adminDeleteMaterial(id, response);
                return;
            }
            String materialId = rest;
            if ("GET".equals(method)) {
                getDetail(materialId, request, response);
            } else if ("DELETE".equals(method)) {
                deleteMaterial(materialId, request, response);
            } else if ("PUT".equals(method)) {
                updateMaterial(materialId, request, response);
            } else {
                JsonResponse.write(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, JsonResponse.fail("方法不允许"));
            }
            return;
        }
        JsonResponse.write(response, HttpServletResponse.SC_NOT_FOUND, JsonResponse.fail("接口不存在"));
    }

    private void listMaterials(HttpServletRequest request, HttpServletResponse response, boolean mine) throws IOException, SQLException {
        String q = request.getParameter("q");
        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            JsonResponse.write(response, JsonResponse.fail("数据库连接失败"));
            return;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        String sql;
        if (q != null && !q.trim().isEmpty()) {
            sql = "SELECT materialID, materialTitle, materialSubject, uploaderName, uploadTime, materialType FROM material WHERE materialState = '审核通过' AND (materialTitle LIKE ? OR materialContent LIKE ?) ORDER BY uploadTime DESC";
        } else {
            sql = "SELECT materialID, materialTitle, materialSubject, uploaderName, uploadTime, materialType FROM material WHERE materialState = '审核通过' ORDER BY uploadTime DESC";
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (q != null && !q.trim().isEmpty()) {
                String like = "%" + q + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rowMaterial(rs));
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(list));
    }

    private void listMyMaterials(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        String userID = SessionHelper.getUserId(request);
        Connection conn = DBUtil.getConnection();
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT materialID, materialTitle, materialSubject, materialState, uploadTime, materialType FROM material WHERE userID = ? ORDER BY uploadTime DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = rowMaterial(rs);
                m.put("materialState", rs.getString("materialState"));
                list.add(m);
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(list));
    }

    private void listPending(HttpServletResponse response) throws IOException, SQLException {
        Connection conn = DBUtil.getConnection();
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT materialID, materialTitle, materialSubject, uploaderName, uploadTime, materialType, materialContent, filePath FROM material WHERE materialState = '待审核' ORDER BY uploadTime ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> m = rowMaterial(rs);
                m.put("materialContent", rs.getString("materialContent"));
                m.put("filePath", rs.getString("filePath"));
                list.add(m);
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(list));
    }

    private void listApproved(HttpServletResponse response) throws IOException, SQLException {
        Connection conn = DBUtil.getConnection();
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT materialID, materialTitle, materialSubject, uploaderName, uploadTime, materialType, materialContent, filePath FROM material WHERE materialState = '审核通过' ORDER BY uploadTime DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> m = rowMaterial(rs);
                m.put("materialContent", rs.getString("materialContent"));
                m.put("filePath", rs.getString("filePath"));
                list.add(m);
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(list));
    }

    private void getDetail(String id, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        Connection conn = DBUtil.getConnection();
        Map<String, Object> material = null;
        List<Map<String, Object>> comments = new ArrayList<>();
        String userID = SessionHelper.getUserId(request);
        boolean isAdmin = SessionHelper.isAdmin(request);
        try {
            String sql = isAdmin
                    ? "SELECT * FROM material WHERE materialID = ?"
                    : "SELECT * FROM material WHERE materialID = ? AND (materialState = '审核通过' OR userID = ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                if (!isAdmin) {
                    ps.setString(2, userID == null ? "" : userID);
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    material = fullMaterial(rs);
                }
            }
            if (material != null) {
                ensureCommentReplyColumns(conn);
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT c.commentID, c.userID, c.commentContent, c.commentTime, c.parentCommentID, c.replyToUserID, c.deleted, " +
                                "u.userName, u2.userName AS replyToUserName " +
                                "FROM comment c " +
                                "JOIN user u ON c.userID = u.userphone " +
                                "LEFT JOIN user u2 ON c.replyToUserID = u2.userphone " +
                                "WHERE c.materialID = ? " +
                                "ORDER BY IFNULL(c.parentCommentID, c.commentID) ASC, c.commentTime ASC")) {
                    ps.setString(1, id);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        Map<String, Object> c = new HashMap<>();
                        c.put("commentID", rs.getInt("commentID"));
                        c.put("userID", rs.getString("userID"));
                        c.put("userName", rs.getString("userName"));
                        c.put("commentContent", rs.getString("commentContent"));
                        c.put("commentTime", rs.getTimestamp("commentTime"));
                        c.put("parentCommentID", rs.getObject("parentCommentID"));
                        c.put("replyToUserID", rs.getString("replyToUserID"));
                        c.put("replyToUserName", rs.getString("replyToUserName"));
                        c.put("deleted", rs.getInt("deleted") == 1);
                        comments.add(c);
                    }
                }
            }
        } finally {
            conn.close();
        }
        if (material == null) {
            JsonResponse.write(response, JsonResponse.fail("资料不存在"));
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("material", material);
        data.put("comments", comments);
        JsonResponse.write(response, JsonResponse.ok(data));
    }

    private void upload(HttpServletRequest request, HttpServletResponse response, ServletContext ctx) throws Exception {
        if (!ServletFileUpload.isMultipartContent(request)) {
            JsonResponse.write(response, JsonResponse.fail("请使用 multipart 上传"));
            return;
        }
        String userID = SessionHelper.getUserId(request);
        String userName = (String) request.getSession().getAttribute("userName");
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setHeaderEncoding("UTF-8");
        upload.setFileSizeMax(MAX_PDF_SIZE);
        upload.setSizeMax(MAX_PDF_SIZE + (5L * 1024 * 1024));
        String materialTitle = null, materialSubject = null, materialType = null, materialContent = null;
        FileItem pdfFile = null;
        List<FileItem> items;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            JsonResponse.write(response, JsonResponse.fail("PDF 文件过大，单个文件最大 30MB"));
            return;
        } catch (FileUploadBase.SizeLimitExceededException e) {
            JsonResponse.write(response, JsonResponse.fail("上传内容过大，请控制在 35MB 内"));
            return;
        }
        for (FileItem item : items) {
            if (item.isFormField()) {
                switch (item.getFieldName()) {
                    case "materialTitle" -> materialTitle = item.getString("UTF-8");
                    case "materialSubject" -> materialSubject = item.getString("UTF-8");
                    case "materialType" -> materialType = item.getString("UTF-8");
                    case "materialContent" -> materialContent = item.getString("UTF-8");
                }
            } else if ("pdfFile".equals(item.getFieldName()) && item.getSize() > 0) {
                pdfFile = item;
            }
        }
        if (materialTitle == null || materialSubject == null || materialType == null) {
            JsonResponse.write(response, JsonResponse.fail("标题、科目和类型必填"));
            return;
        }
        String materialID = UUID.randomUUID().toString();
        String filePath = null;
        String diskPath = null;
        if ("pdf".equals(materialType)) {
            if (pdfFile == null) {
                JsonResponse.write(response, JsonResponse.fail("请上传 PDF"));
                return;
            }
            File storageDir = getMaterialStorageDir(ctx);
            String uploadPath = storageDir.getAbsolutePath();
            new File(uploadPath).mkdirs();
            String saved = materialID + ".pdf";
            File savedFile = new File(uploadPath + File.separator + saved);
            pdfFile.write(savedFile);
            filePath = UPLOAD_DIR + "/" + saved;
            diskPath = savedFile.getAbsolutePath();
        }
        Connection conn = DBUtil.getConnection();
        try {
            ensureMaterialDiskPathColumn(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO material (materialID, materialTitle, materialContent, userID, uploaderName, materialSubject, materialState, materialType, filePath, diskPath) VALUES (?, ?, ?, ?, ?, ?, '待审核', ?, ?, ?)")) {
                ps.setString(1, materialID);
                ps.setString(2, materialTitle);
                ps.setString(3, materialContent);
                ps.setString(4, userID);
                ps.setString(5, userName);
                ps.setString(6, materialSubject);
                ps.setString(7, materialType);
                ps.setString(8, filePath);
                ps.setString(9, diskPath);
                ps.executeUpdate();
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(Map.of("materialID", materialID)));
    }

    private void addComment(String materialId, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        String content = request.getParameter("commentContent");
        if (content == null || content.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("评论不能为空"));
            return;
        }
        content = content.trim();
        String parentCommentID = request.getParameter("parentCommentID");
        String replyToUserID = request.getParameter("replyToUserID");
        Integer parentIdInt = null;
        if (parentCommentID != null && !parentCommentID.trim().isEmpty()) {
            try {
                parentIdInt = Integer.parseInt(parentCommentID.trim());
            } catch (Exception e) {
                JsonResponse.write(response, JsonResponse.fail("回复参数错误"));
                return;
            }
        }
        String userID = SessionHelper.getUserId(request);
        Connection conn = DBUtil.getConnection();
        try {
            ensureCommentReplyColumns(conn);
            if (parentIdInt != null) {
                String targetUser = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT userID FROM comment WHERE materialID = ? AND commentID = ?")) {
                    ps.setString(1, materialId);
                    ps.setInt(2, parentIdInt);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        targetUser = rs.getString("userID");
                    }
                }
                if (targetUser == null) {
                    JsonResponse.write(response, JsonResponse.fail("被回复的评论不存在"));
                    return;
                }
                if (replyToUserID == null || replyToUserID.trim().isEmpty()) {
                    replyToUserID = targetUser;
                }
            } else {
                replyToUserID = null;
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO comment (materialID, userID, commentContent, parentCommentID, replyToUserID) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, materialId);
                ps.setString(2, userID);
                ps.setString(3, content);
                if (parentIdInt == null) {
                    ps.setNull(4, Types.INTEGER);
                } else {
                    ps.setInt(4, parentIdInt);
                }
                if (replyToUserID == null || replyToUserID.trim().isEmpty()) {
                    ps.setNull(5, Types.VARCHAR);
                } else {
                    ps.setString(5, replyToUserID.trim());
                }
                ps.executeUpdate();
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok("评论成功"));
    }

    private void audit(String id, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        String action = request.getParameter("action");
        String state = "approve".equals(action) ? "审核通过" : "审核拒绝";
        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
            ensureMaterialRewardColumn(conn);
            String userID = null;
            String title = "";
            int rewardGranted = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT userID, materialTitle, rewardGranted FROM material WHERE materialID = ? FOR UPDATE")) {
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    userID = rs.getString("userID");
                    title = rs.getString("materialTitle");
                    rewardGranted = rs.getInt("rewardGranted");
                } else {
                    conn.rollback();
                    JsonResponse.write(response, JsonResponse.fail("资料不存在"));
                    return;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE material SET materialState = ? WHERE materialID = ?")) {
                ps.setString(1, state);
                ps.setString(2, id);
                ps.executeUpdate();
            }
            if ("审核通过".equals(state) && rewardGranted == 0 && userID != null) {
                ensurePointsRow(conn, userID);
                try (PreparedStatement ps = conn.prepareStatement("UPDATE points SET points = points + 10 WHERE userID = ?")) {
                    ps.setString(1, userID);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, '+10', ?, NOW())")) {
                    ps.setString(1, userID);
                    ps.setString(2, "资料审核通过奖励：" + title);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("UPDATE material SET rewardGranted = 1 WHERE materialID = ?")) {
                    ps.setString(1, id);
                    ps.executeUpdate();
                }
            }
            conn.commit();
            JsonResponse.write(response, JsonResponse.ok("审核完成"));
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("审核失败"));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void deleteMaterial(String id, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        String userID = SessionHelper.getUserId(request);
        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
            ensureMaterialRewardColumn(conn);
            String owner = null;
            String state = null;
            int rewardGranted = 0;
            String title = "";
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT userID, materialState, rewardGranted, materialTitle FROM material WHERE materialID = ? FOR UPDATE")) {
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    owner = rs.getString("userID");
                    state = rs.getString("materialState");
                    rewardGranted = rs.getInt("rewardGranted");
                    title = rs.getString("materialTitle");
                }
            }
            if (owner == null || !owner.equals(userID)) {
                conn.rollback();
                JsonResponse.write(response, JsonResponse.fail("删除失败或无权限"));
                return;
            }
            if ("审核通过".equals(state) && rewardGranted == 1) {
                ensurePointsRow(conn, userID);
                try (PreparedStatement ps = conn.prepareStatement("UPDATE points SET points = points - 10 WHERE userID = ?")) {
                    ps.setString(1, userID);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, '-10', ?, NOW())")) {
                    ps.setString(1, userID);
                    ps.setString(2, "删除已通过资料扣回积分：" + title);
                    ps.executeUpdate();
                }
                int warningCount = UserGovernance.increaseWarningAndMaybeBan(conn, userID);
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, '0', ?, NOW())")) {
                    ps.setString(1, userID);
                    ps.setString(2, "删除已通过资料触发警告，当前警告次数：" + warningCount);
                    ps.executeUpdate();
                } catch (SQLException ignored) {
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM comment WHERE materialID = ?")) {
                ps.setString(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM material WHERE materialID = ? AND userID = ?")) {
                ps.setString(1, id);
                ps.setString(2, userID);
                int n = ps.executeUpdate();
                if (n == 0) {
                    conn.rollback();
                    JsonResponse.write(response, JsonResponse.fail("删除失败或无权限"));
                    return;
                }
            }
            conn.commit();
            JsonResponse.write(response, JsonResponse.ok("已删除"));
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("删除失败"));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void adminDeleteMaterial(String id, HttpServletResponse response) throws IOException, SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM comment WHERE materialID = ?")) {
                ps.setString(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM material WHERE materialID = ?")) {
                ps.setString(1, id);
                int n = ps.executeUpdate();
                if (n == 0) {
                    JsonResponse.write(response, JsonResponse.fail("资料不存在"));
                    return;
                }
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok("管理员已删除资料"));
    }

    private void adminDeleteComment(String materialId, String commentId, HttpServletResponse response) throws IOException, SQLException {
        int cid;
        try {
            cid = Integer.parseInt(commentId);
        } catch (Exception e) {
            JsonResponse.write(response, JsonResponse.fail("评论参数错误"));
            return;
        }
        Connection conn = DBUtil.getConnection();
        try {
            ensureCommentReplyColumns(conn);
            conn.setAutoCommit(false);
            Integer parentCommentID = null;
            boolean exists = false;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT parentCommentID FROM comment WHERE materialID = ? AND commentID = ? FOR UPDATE")) {
                ps.setString(1, materialId);
                ps.setInt(2, cid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    exists = true;
                    Object parentObj = rs.getObject("parentCommentID");
                    if (parentObj instanceof Number) {
                        parentCommentID = ((Number) parentObj).intValue();
                    }
                }
            }
            if (!exists) {
                conn.rollback();
                JsonResponse.write(response, JsonResponse.fail("评论不存在"));
                return;
            }
            boolean isRoot = parentCommentID == null || parentCommentID == 0;
            if (isRoot) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM comment WHERE materialID = ? AND (commentID = ? OR parentCommentID = ?)")) {
                    ps.setString(1, materialId);
                    ps.setInt(2, cid);
                    ps.setInt(3, cid);
                    ps.executeUpdate();
                }
                conn.commit();
                JsonResponse.write(response, JsonResponse.ok("评论及其回复已删除"));
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM comment WHERE materialID = ? AND commentID = ?")) {
                    ps.setString(1, materialId);
                    ps.setInt(2, cid);
                    ps.executeUpdate();
                }
                conn.commit();
                JsonResponse.write(response, JsonResponse.ok("评论已删除"));
            }
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("删除失败"));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void ensureMaterialRewardColumn(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE material ADD COLUMN rewardGranted TINYINT(1) NOT NULL DEFAULT 0")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private void ensureMaterialDiskPathColumn(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE material ADD COLUMN diskPath VARCHAR(1024) NULL")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private void ensureCommentReplyColumns(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE comment ADD COLUMN parentCommentID INT NULL")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE comment ADD COLUMN replyToUserID VARCHAR(64) NULL")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE comment ADD COLUMN deleted TINYINT(1) NOT NULL DEFAULT 0")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private void ensurePointsRow(Connection conn, String userID) throws SQLException {
        String userName = "";
        try (PreparedStatement ps = conn.prepareStatement("SELECT userName FROM user WHERE userphone = ?")) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) userName = rs.getString("userName");
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO points (userID, userName, points) VALUES (?, ?, 0) ON DUPLICATE KEY UPDATE userName = VALUES(userName)")) {
            ps.setString(1, userID);
            ps.setString(2, userName == null ? "" : userName);
            ps.executeUpdate();
        }
    }

    private void updateMaterial(String id, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        String userID = SessionHelper.getUserId(request);
        String title = trimParam(request.getParameter("materialTitle"));
        String subject = trimParam(request.getParameter("materialSubject"));
        String content = request.getParameter("materialContent");
        if (title == null || title.isEmpty() || subject == null || subject.isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("标题和科目不能为空"));
            return;
        }
        Connection conn = DBUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE material SET materialTitle=?, materialSubject=?, materialContent=? WHERE materialID=? AND userID=?")) {
            ps.setString(1, title);
            ps.setString(2, subject);
            ps.setString(3, content == null ? "" : content);
            ps.setString(4, id);
            ps.setString(5, userID);
            int n = ps.executeUpdate();
            if (n == 0) {
                JsonResponse.write(response, JsonResponse.fail("更新失败或无权限"));
                return;
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok("更新成功"));
    }

    private String trimParam(String value) {
        return value == null ? null : value.trim();
    }

    private void download(String id, HttpServletResponse response, ServletContext ctx) throws Exception {
        MaterialFileMeta meta = queryMaterialFileMeta(id);
        if (meta.filePath == null) {
            response.sendError(404);
            return;
        }
        File file = resolveMaterialPdfFile(meta, ctx);
        if (file == null || !file.exists()) {
            response.sendError(404);
            return;
        }
        response.setContentType("application/pdf");
        String encodedTitle = java.net.URLEncoder.encode(meta.title + ".pdf", "UTF-8").replace("+", "%20");
        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedTitle);
        try {
            Files.copy(file.toPath(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ioe) {
            if (isClientAbort(ioe)) {
                // Client closed the connection while downloading/previewing PDF.
                return;
            }
            throw ioe;
        }
    }

    private void downloadAttachment(String id, HttpServletResponse response, ServletContext ctx) throws Exception {
        MaterialFileMeta meta = queryMaterialFileMeta(id);
        if (meta.filePath == null) {
            response.sendError(404);
            return;
        }
        File file = resolveMaterialPdfFile(meta, ctx);
        if (file == null || !file.exists()) {
            response.sendError(404);
            return;
        }
        response.setContentType("application/pdf");
        String encodedTitle = java.net.URLEncoder.encode(meta.title + ".pdf", "UTF-8").replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedTitle);
        try {
            Files.copy(file.toPath(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ioe) {
            if (isClientAbort(ioe)) {
                return;
            }
            throw ioe;
        }
    }

    private void listPreviewImages(String id, HttpServletResponse response, ServletContext ctx) throws Exception {
        MaterialFileMeta meta = queryMaterialFileMeta(id);
        if (meta.filePath == null) {
            JsonResponse.write(response, JsonResponse.fail("资料不存在"));
            return;
        }
        File pdfFile = resolveMaterialPdfFile(meta, ctx);
        if (pdfFile == null || !pdfFile.exists()) {
            JsonResponse.write(response, JsonResponse.fail("PDF 文件不存在"));
            return;
        }

        File materialDir = new File(getMaterialPreviewDir(ctx), id);
        ensurePreviewImages(id, pdfFile, materialDir);
        File[] imageFiles = materialDir.listFiles((d, name) -> name.toLowerCase().endsWith(".jpg"));
        if (imageFiles == null || imageFiles.length == 0) {
            JsonResponse.write(response, JsonResponse.fail("预览图生成失败"));
            return;
        }
        Arrays.sort(imageFiles, Comparator.comparing(File::getName));
        List<String> urls = new ArrayList<>();
        for (File image : imageFiles) {
            urls.add("/api/materials/" + id + "/preview-images/" + image.getName() + "?t=" + image.lastModified());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("pageCount", imageFiles.length);
        data.put("images", urls);
        JsonResponse.write(response, JsonResponse.ok(data));
    }

    private void servePreviewImage(String id, String filename, HttpServletResponse response, ServletContext ctx) throws Exception {
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            response.sendError(400);
            return;
        }
        File file = new File(new File(getMaterialPreviewDir(ctx), id), filename);
        if (!file.exists()) {
            response.sendError(404);
            return;
        }
        response.setContentType("image/jpeg");
        try {
            Files.copy(file.toPath(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ioe) {
            if (isClientAbort(ioe)) return;
            throw ioe;
        }
    }

    private void ensurePreviewImages(String materialId, File pdfFile, File outputDir) throws Exception {
        File[] existing = outputDir.listFiles((d, name) -> name.toLowerCase().endsWith(".jpg"));
        if (existing != null && existing.length > 0) {
            return;
        }
        synchronized (("material-preview-" + materialId).intern()) {
            existing = outputDir.listFiles((d, name) -> name.toLowerCase().endsWith(".jpg"));
            if (existing != null && existing.length > 0) {
                return;
            }
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                throw new IOException("无法创建预览目录");
            }
            try (PDDocument document = PDDocument.load(pdfFile)) {
                PDFRenderer renderer = new PDFRenderer(document);
                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    BufferedImage image = renderer.renderImageWithDPI(i, 120);
                    File out = new File(outputDir, String.format("%04d.jpg", i + 1));
                    ImageIO.write(image, "JPEG", out);
                    image.flush();
                }
            }
        }
    }

    private MaterialFileMeta queryMaterialFileMeta(String id) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            ensureMaterialDiskPathColumn(conn);
            try (PreparedStatement ps = conn.prepareStatement("SELECT filePath, materialTitle, diskPath FROM material WHERE materialID = ?")) {
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    MaterialFileMeta meta = new MaterialFileMeta();
                    meta.filePath = rs.getString("filePath");
                    String title = rs.getString("materialTitle");
                    meta.title = (title == null || title.trim().isEmpty()) ? "download" : title;
                    meta.diskPath = rs.getString("diskPath");
                    return meta;
                }
            }
            return new MaterialFileMeta();
        } finally {
            conn.close();
        }
    }

    private File resolveMaterialPdfFile(MaterialFileMeta meta, ServletContext ctx) {
        File file = null;
        if (meta.diskPath != null && !meta.diskPath.trim().isEmpty()) {
            file = new File(meta.diskPath);
        }
        if ((file == null || !file.exists()) && meta.filePath != null && !meta.filePath.trim().isEmpty()) {
            file = new File(ctx.getRealPath("") + File.separator + meta.filePath.replace("/", File.separator));
        }
        if ((file == null || !file.exists()) && meta.filePath != null && !meta.filePath.trim().isEmpty()) {
            String name = new File(meta.filePath.replace("/", File.separator)).getName();
            file = new File(getMaterialStorageDir(ctx), name);
        }
        if ((file == null || !file.exists()) && meta.filePath != null && !meta.filePath.trim().isEmpty()) {
            String name = new File(meta.filePath.replace("/", File.separator)).getName();
            file = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "uploads" + File.separator + "materials", name);
        }
        return file;
    }

    private Map<String, Object> rowMaterial(ResultSet rs) throws SQLException {
        Map<String, Object> m = new HashMap<>();
        m.put("materialID", rs.getString("materialID"));
        m.put("materialTitle", rs.getString("materialTitle"));
        m.put("materialSubject", rs.getString("materialSubject"));
        try { m.put("uploaderName", rs.getString("uploaderName")); } catch (SQLException ignored) {}
        m.put("uploadTime", rs.getTimestamp("uploadTime"));
        try {
            m.put("materialType", rs.getString("materialType"));
        } catch (SQLException ignored) {
        }
        return m;
    }

    private Map<String, Object> fullMaterial(ResultSet rs) throws SQLException {
        Map<String, Object> m = rowMaterial(rs);
        m.put("materialContent", rs.getString("materialContent"));
        m.put("materialType", rs.getString("materialType"));
        m.put("filePath", rs.getString("filePath"));
        m.put("userID", rs.getString("userID"));
        return m;
    }

    private File getMaterialStorageDir(ServletContext ctx) {
        String base = System.getenv("APP_UPLOAD_DIR");
        if (base == null || base.trim().isEmpty()) {
            base = System.getProperty("user.dir") + File.separator + "runtime-uploads";
        }
        return new File(base, "materials");
    }

    private File getMaterialPreviewDir(ServletContext ctx) {
        String base = System.getenv("APP_UPLOAD_DIR");
        if (base == null || base.trim().isEmpty()) {
            base = System.getProperty("user.dir") + File.separator + "runtime-uploads";
        }
        return new File(base, "material-previews");
    }

    private boolean isClientAbort(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            String name = cur.getClass().getName();
            String msg = cur.getMessage();
            if (name.contains("ClientAbortException")) return true;
            if (msg != null) {
                String m = msg.toLowerCase();
                if (m.contains("connection reset by peer") || m.contains("broken pipe")) return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    private static class MaterialFileMeta {
        String filePath;
        String diskPath;
        String title = "download";
    }

    private void deleteOwnComment(String materialId, String commentId, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int cid;
        try {
            cid = Integer.parseInt(commentId);
        } catch (Exception e) {
            JsonResponse.write(response, JsonResponse.fail("评论参数错误"));
            return;
        }
        String userID = SessionHelper.getUserId(request);
        Connection conn = DBUtil.getConnection();
        try {
            ensureCommentReplyColumns(conn);
            conn.setAutoCommit(false);
            String owner = null;
            Integer parentCommentID = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT userID, parentCommentID FROM comment WHERE materialID = ? AND commentID = ? FOR UPDATE")) {
                ps.setString(1, materialId);
                ps.setInt(2, cid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    owner = rs.getString("userID");
                    Object parentObj = rs.getObject("parentCommentID");
                    if (parentObj instanceof Number) {
                        parentCommentID = ((Number) parentObj).intValue();
                    }
                }
            }
            if (owner == null) {
                conn.rollback();
                JsonResponse.write(response, JsonResponse.fail("评论不存在"));
                return;
            }
            if (!userID.equals(owner)) {
                conn.rollback();
                JsonResponse.write(response, JsonResponse.fail("只能删除自己的评论"));
                return;
            }

            boolean isRoot = parentCommentID == null || parentCommentID == 0;
            if (isRoot) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM comment WHERE materialID = ? AND (commentID = ? OR parentCommentID = ?)")) {
                    ps.setString(1, materialId);
                    ps.setInt(2, cid);
                    ps.setInt(3, cid);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM comment WHERE materialID = ? AND commentID = ? AND userID = ?")) {
                    ps.setString(1, materialId);
                    ps.setInt(2, cid);
                    ps.setString(3, userID);
                    int n = ps.executeUpdate();
                    if (n == 0) {
                        conn.rollback();
                        JsonResponse.write(response, JsonResponse.fail("删除失败"));
                        return;
                    }
                }
            }
            conn.commit();
            JsonResponse.write(response, JsonResponse.ok(isRoot ? "评论及其回复已删除" : "评论已删除"));
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("删除失败"));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}
