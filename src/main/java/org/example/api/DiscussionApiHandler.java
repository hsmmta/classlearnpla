package org.example.api;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.example.util.DBUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DiscussionApiHandler {
    private static final String QUESTION_IMAGE_DIR = "uploads/questions";
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final int MAX_IMAGE_COUNT = 3;

    void handle(String path, String method, HttpServletRequest request, HttpServletResponse response, ServletContext ctx) throws Exception {
        if (path.equals("/questions/pending") && "GET".equals(method)) {
            if (!SessionHelper.isAdmin(request)) {
                JsonResponse.write(response, HttpServletResponse.SC_FORBIDDEN, JsonResponse.fail("无权限"));
                return;
            }
            listPending(response);
            return;
        }
        if (path.equals("/questions/approved") && "GET".equals(method)) {
            if (!SessionHelper.isAdmin(request)) {
                JsonResponse.write(response, HttpServletResponse.SC_FORBIDDEN, JsonResponse.fail("无权限"));
                return;
            }
            listApproved(response);
            return;
        }
        if (path.startsWith("/questions/images/") && "GET".equals(method)) {
            String imageId = path.substring("/questions/images/".length());
            serveQuestionImage(imageId, response, ctx);
            return;
        }
        if (!SessionHelper.isLoggedIn(request)) {
            JsonResponse.write(response, HttpServletResponse.SC_UNAUTHORIZED, JsonResponse.fail("请先登录"));
            return;
        }
        if ("/questions".equals(path) && "GET".equals(method)) {
            listQuestions(response);
            return;
        }
        if ("/questions".equals(path) && "POST".equals(method)) {
            askQuestion(request, response);
            return;
        }
        if ("/questions/mine".equals(path) && "GET".equals(method)) {
            listMyQuestions(request, response);
            return;
        }
        if (path.startsWith("/questions/")) {
            String rest = path.substring("/questions/".length());
            if (rest.contains("/comments/") && "DELETE".equals(method)) {
                String[] parts = rest.split("/");
                if (parts.length >= 3) {
                    deleteOwnComment(parts[0], parts[2], request, response);
                    return;
                }
            }
            if (rest.endsWith("/comments") && "POST".equals(method)) {
                String qid = rest.substring(0, rest.length() - "/comments".length());
                addComment(qid, request, response);
                return;
            }
            if (rest.contains("/comments/") && rest.endsWith("/like") && "POST".equals(method)) {
                String[] parts = rest.split("/");
                if (parts.length >= 3) {
                    likeComment(parts[0], parts[2], request, response);
                    return;
                }
            }
            if (rest.endsWith("/best-answer") && "POST".equals(method)) {
                String qid = rest.substring(0, rest.length() - "/best-answer".length());
                setBestAnswer(qid, request, response);
                return;
            }
            if (rest.endsWith("/audit") && "POST".equals(method)) {
                if (!SessionHelper.isAdmin(request)) {
                    JsonResponse.write(response, HttpServletResponse.SC_FORBIDDEN, JsonResponse.fail("无权限"));
                    return;
                }
                String qid = rest.substring(0, rest.length() - "/audit".length());
                auditQuestion(qid, request, response);
                return;
            }
            if (rest.endsWith("/admin-delete") && "POST".equals(method)) {
                if (!SessionHelper.isAdmin(request)) {
                    JsonResponse.write(response, HttpServletResponse.SC_FORBIDDEN, JsonResponse.fail("无权限"));
                    return;
                }
                String qid = rest.substring(0, rest.length() - "/admin-delete".length());
                adminDeleteQuestion(qid, response);
                return;
            }
            if ("DELETE".equals(method)) {
                deleteOwnQuestion(rest, request, response);
                return;
            }
            if ("GET".equals(method)) {
                getQuestion(rest, response);
                return;
            }
        }
        JsonResponse.write(response, HttpServletResponse.SC_NOT_FOUND, JsonResponse.fail("接口不存在"));
    }

    private void listQuestions(HttpServletResponse response) throws SQLException, IOException {
        Connection conn = DBUtil.getConnection();
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT q.questionID, q.questionTitle, q.questionContent, q.creationTime, u.userName FROM question q JOIN user u ON q.userID = u.userphone WHERE q.questionState = '审核通过' ORDER BY q.creationTime DESC";
        try {
            ensureQuestionImageTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> q = new HashMap<>();
                q.put("questionID", rs.getString("questionID"));
                q.put("questionTitle", rs.getString("questionTitle"));
                q.put("questionContent", rs.getString("questionContent"));
                q.put("creationTime", rs.getTimestamp("creationTime"));
                q.put("userName", rs.getString("userName"));
                q.put("firstImageUrl", queryFirstQuestionImage(conn, rs.getString("questionID")));
                list.add(q);
            }
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(list));
    }

    private void listMyQuestions(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = SessionHelper.getUserId(request);
        Connection conn = DBUtil.getConnection();
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT questionID, questionTitle, questionContent, questionState, creationTime, bestAnswerID FROM question WHERE userID = ? ORDER BY creationTime DESC";
        try {
            ensureQuestionImageTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> q = new HashMap<>();
                q.put("questionID", rs.getString("questionID"));
                q.put("questionTitle", rs.getString("questionTitle"));
                q.put("questionContent", rs.getString("questionContent"));
                q.put("questionState", rs.getString("questionState"));
                q.put("creationTime", rs.getTimestamp("creationTime"));
                q.put("firstImageUrl", queryFirstQuestionImage(conn, rs.getString("questionID")));
                try {
                    int best = rs.getInt("bestAnswerID");
                    q.put("bestAnswerID", rs.wasNull() ? null : best);
                } catch (SQLException e) {
                    q.put("bestAnswerID", null);
                }
                list.add(q);
            }
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(list));
    }

    private void listPending(HttpServletResponse response) throws SQLException, IOException {
        Connection conn = DBUtil.getConnection();
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT questionID, questionTitle, questionContent, creationTime FROM question WHERE questionState = '待审核' ORDER BY creationTime ASC";
        try {
            ensureQuestionImageTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> q = new HashMap<>();
                    String qid = rs.getString("questionID");
                    q.put("questionID", qid);
                    q.put("questionTitle", rs.getString("questionTitle"));
                    q.put("questionContent", rs.getString("questionContent"));
                    q.put("creationTime", rs.getTimestamp("creationTime"));
                    q.put("imageUrls", queryQuestionImages(conn, qid));
                    list.add(q);
                }
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(list));
    }

    private void listApproved(HttpServletResponse response) throws SQLException, IOException {
        Connection conn = DBUtil.getConnection();
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT questionID, questionTitle, questionContent, creationTime FROM question WHERE questionState = '审核通过' ORDER BY creationTime DESC";
        try {
            ensureQuestionImageTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> q = new HashMap<>();
                    String qid = rs.getString("questionID");
                    q.put("questionID", qid);
                    q.put("questionTitle", rs.getString("questionTitle"));
                    q.put("questionContent", rs.getString("questionContent"));
                    q.put("creationTime", rs.getTimestamp("creationTime"));
                    q.put("imageUrls", queryQuestionImages(conn, qid));
                    list.add(q);
                }
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(list));
    }

    private void getQuestion(String id, HttpServletResponse response) throws SQLException, IOException {
        Connection conn = DBUtil.getConnection();
        Map<String, Object> question = null;
        List<Map<String, Object>> comments = new ArrayList<>();
        List<String> imageUrls = new ArrayList<>();
        try {
            ensureQuestionImageTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT q.*, u.userName FROM question q JOIN user u ON q.userID = u.userphone WHERE q.questionID = ? AND q.questionState = '审核通过'")) {
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    question = new HashMap<>();
                    question.put("questionID", rs.getString("questionID"));
                    question.put("questionTitle", rs.getString("questionTitle"));
                    question.put("questionContent", rs.getString("questionContent"));
                    question.put("userID", rs.getString("userID"));
                    question.put("userName", rs.getString("userName"));
                    question.put("creationTime", rs.getTimestamp("creationTime"));
                    try {
                        int best = rs.getInt("bestAnswerID");
                        question.put("bestAnswerID", rs.wasNull() ? null : best);
                    } catch (SQLException e) {
                        question.put("bestAnswerID", null);
                    }
                }
            }
            imageUrls = queryQuestionImages(conn, id);
            if (question != null) {
                question.put("imageUrls", imageUrls);
                ensureQuestionCommentReplyColumns(conn);
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT qc.*, u.userName, u2.userName AS replyToUserName " +
                                "FROM question_comment qc " +
                                "JOIN user u ON qc.userID = u.userphone " +
                                "LEFT JOIN user u2 ON qc.replyToUserID = u2.userphone " +
                                "WHERE qc.questionID = ? " +
                                "ORDER BY IFNULL(qc.parentCommentID, qc.commentID) ASC, qc.commentTime ASC")) {
                    ps.setString(1, id);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        Map<String, Object> c = new HashMap<>();
                        c.put("commentID", rs.getInt("commentID"));
                        c.put("userID", rs.getString("userID"));
                        c.put("userName", rs.getString("userName"));
                        c.put("commentContent", rs.getString("commentContent"));
                        c.put("commentTime", rs.getTimestamp("commentTime"));
                        c.put("likes", rs.getInt("likes"));
                        c.put("parentCommentID", rs.getObject("parentCommentID"));
                        c.put("replyToUserID", rs.getString("replyToUserID"));
                        c.put("replyToUserName", rs.getString("replyToUserName"));
                        c.put("deleted", rs.getInt("deleted") == 1);
                        try {
                            c.put("isBestAnswer", rs.getBoolean("isBestAnswer"));
                        } catch (SQLException e) {
                            c.put("isBestAnswer", false);
                        }
                        comments.add(c);
                    }
                }
            }
        } finally {
            conn.close();
        }
        if (question == null) {
            JsonResponse.write(response, JsonResponse.fail("问题不存在"));
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("question", question);
        data.put("comments", comments);
        JsonResponse.write(response, JsonResponse.ok(data));
    }

    private void askQuestion(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String title = null;
        String content = null;
        List<FileItem> imageItems = new ArrayList<>();
        boolean multipart = ServletFileUpload.isMultipartContent(request);
        if (multipart) {
            ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
            upload.setHeaderEncoding("UTF-8");
            upload.setFileSizeMax(MAX_IMAGE_SIZE);
            upload.setSizeMax(MAX_IMAGE_SIZE * MAX_IMAGE_COUNT + 2L * 1024 * 1024);
            List<FileItem> items;
            try {
                items = upload.parseRequest(request);
            } catch (FileUploadBase.FileSizeLimitExceededException e) {
                JsonResponse.write(response, JsonResponse.fail("单张图片不能超过 5MB"));
                return;
            } catch (FileUploadBase.SizeLimitExceededException e) {
                JsonResponse.write(response, JsonResponse.fail("上传图片总大小过大"));
                return;
            } catch (Exception e) {
                JsonResponse.write(response, JsonResponse.fail("上传解析失败"));
                return;
            }
            for (FileItem item : items) {
                if (item.isFormField()) {
                    String field = item.getFieldName();
                    if ("questionTitle".equals(field)) {
                        title = item.getString("UTF-8");
                    } else if ("questionContent".equals(field)) {
                        content = item.getString("UTF-8");
                    }
                    continue;
                }
                if ("questionImages".equals(item.getFieldName()) && item.getSize() > 0) {
                    if (item.getContentType() == null || !item.getContentType().startsWith("image/")) {
                        JsonResponse.write(response, JsonResponse.fail("仅支持图片格式"));
                        return;
                    }
                    imageItems.add(item);
                }
            }
        } else {
            title = request.getParameter("questionTitle");
            content = request.getParameter("questionContent");
        }
        if (title == null || content == null) {
            JsonResponse.write(response, JsonResponse.fail("标题和内容必填"));
            return;
        }
        title = title.trim();
        content = content.trim();
        if (title.isEmpty() || content.isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("标题和内容必填"));
            return;
        }
        if (imageItems.size() > MAX_IMAGE_COUNT) {
            JsonResponse.write(response, JsonResponse.fail("最多上传 3 张图片"));
            return;
        }
        String userID = SessionHelper.getUserId(request);
        String qid = UUID.randomUUID().toString();
        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO question (questionID, questionTitle, questionContent, userID, questionState) VALUES (?, ?, ?, ?, '待审核')")) {
                ps.setString(1, qid);
                ps.setString(2, title);
                ps.setString(3, content);
                ps.setString(4, userID);
                ps.executeUpdate();
            }
            ensureQuestionImageTable(conn);
            if (!imageItems.isEmpty()) {
                ServletContext ctx = request.getServletContext();
                String uploadPath = getQuestionImageStorageDir(ctx).getAbsolutePath();
                new File(uploadPath).mkdirs();
                int index = 0;
                for (FileItem item : imageItems) {
                    String ext = ".jpg";
                    String name = item.getName();
                    if (name != null) {
                        String lower = name.toLowerCase(Locale.ROOT);
                        if (lower.endsWith(".png")) ext = ".png";
                        else if (lower.endsWith(".gif")) ext = ".gif";
                        else if (lower.endsWith(".webp")) ext = ".webp";
                        else if (lower.endsWith(".jpeg")) ext = ".jpeg";
                    }
                    String saved = qid + "-" + (index++) + ext;
                    File savedFile = new File(uploadPath + File.separator + saved);
                    item.write(savedFile);
                    String imagePath = QUESTION_IMAGE_DIR + "/" + saved;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO question_image (questionID, imagePath, diskPath, sortOrder) VALUES (?, ?, ?, ?)")) {
                        ps.setString(1, qid);
                        ps.setString(2, imagePath);
                        ps.setString(3, savedFile.getAbsolutePath());
                        ps.setInt(4, index);
                        ps.executeUpdate();
                    }
                }
            }
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("提问失败: " + e.getMessage()));
            return;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(Map.of("questionID", qid)));
    }

    private void addComment(String qid, HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String content = request.getParameter("commentContent");
        if (content == null || content.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("回复内容不能为空"));
            return;
        }
        content = content.trim();
        String userID = SessionHelper.getUserId(request);
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
        Connection conn = DBUtil.getConnection();
        try {
            ensureQuestionCommentReplyColumns(conn);
            if (parentIdInt != null) {
                String targetUser = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT userID FROM question_comment WHERE questionID = ? AND commentID = ?")) {
                    ps.setString(1, qid);
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
                    "INSERT INTO question_comment (questionID, userID, commentContent, parentCommentID, replyToUserID) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, qid);
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
        JsonResponse.write(response, JsonResponse.ok("回复成功"));
    }

    private void likeComment(String qid, String commentId, HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int cid;
        try {
            cid = Integer.parseInt(commentId);
        } catch (Exception e) {
            JsonResponse.write(response, JsonResponse.fail("评论参数错误"));
            return;
        }
        String userID = SessionHelper.getUserId(request);
        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
            // 每个用户对同一条回复仅允许点赞一次
            try (PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS question_comment_like (" +
                            "questionID VARCHAR(64) NOT NULL," +
                            "commentID INT NOT NULL," +
                            "userID VARCHAR(64) NOT NULL," +
                            "likeTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                            "PRIMARY KEY (questionID, commentID, userID)" +
                            ")")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO question_comment_like (questionID, commentID, userID) VALUES (?, ?, ?)")) {
                ps.setString(1, qid);
                ps.setInt(2, cid);
                ps.setString(3, userID);
                ps.executeUpdate();
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) {
                    conn.rollback();
                    JsonResponse.write(response, JsonResponse.fail("你已经点过赞了"));
                    return;
                }
                throw e;
            }
            int affected;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE question_comment SET likes = likes + 1 WHERE questionID = ? AND commentID = ?")) {
                ps.setString(1, qid);
                ps.setInt(2, cid);
                affected = ps.executeUpdate();
            }
            if (affected == 0) {
                conn.rollback();
                JsonResponse.write(response, JsonResponse.fail("回复不存在"));
                return;
            }
            conn.commit();
            JsonResponse.write(response, JsonResponse.ok("点赞成功"));
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("点赞失败"));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void setBestAnswer(String qid, HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String commentId = request.getParameter("commentID");
        String userID = SessionHelper.getUserId(request);
        int cid;
        try {
            cid = Integer.parseInt(commentId);
        } catch (Exception e) {
            JsonResponse.write(response, JsonResponse.fail("评论参数错误"));
            return;
        }
        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
            ensureQuestionCommentReplyColumns(conn);
            String questionUser = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT userID, bestAnswerID FROM question WHERE questionID = ?")) {
                ps.setString(1, qid);
                ResultSet rs = ps.executeQuery();
                if (!rs.next() || !userID.equals(rs.getString("userID"))) {
                    JsonResponse.write(response, JsonResponse.fail("仅提问者可设置"));
                    conn.rollback();
                    return;
                }
                questionUser = rs.getString("userID");
                try {
                    if (rs.getObject("bestAnswerID") != null) {
                        JsonResponse.write(response, JsonResponse.fail("已设置过最满意答案"));
                        conn.rollback();
                        return;
                    }
                } catch (SQLException ignored) {
                }
            }
            String answerUser = null;
            Integer deleted = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT userID, deleted FROM question_comment WHERE questionID = ? AND commentID = ?")) {
                ps.setString(1, qid);
                ps.setInt(2, cid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    answerUser = rs.getString("userID");
                    try {
                        deleted = rs.getInt("deleted");
                    } catch (SQLException ignored) {
                    }
                }
            }
            if (answerUser == null) {
                JsonResponse.write(response, JsonResponse.fail("回复不存在"));
                conn.rollback();
                return;
            }
            if (deleted != null && deleted == 1) {
                JsonResponse.write(response, JsonResponse.fail("该回复已删除"));
                conn.rollback();
                return;
            }
            if (questionUser != null && questionUser.equals(answerUser)) {
                JsonResponse.write(response, JsonResponse.fail("不能将自己的回复设为最满意答案"));
                conn.rollback();
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE question SET bestAnswerID = ? WHERE questionID = ?")) {
                ps.setInt(1, cid);
                ps.setString(2, qid);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE question_comment SET isBestAnswer = 1 WHERE questionID = ? AND commentID = ?")) {
                ps.setString(1, qid);
                ps.setInt(2, cid);
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE points SET points = points + 5 WHERE userID = ?")) {
                ps.setString(1, answerUser);
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, answerUser);
                ps.setString(2, "+5");
                ps.setString(3, "回答被设为最满意答案");
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }
            conn.commit();
            JsonResponse.write(response, JsonResponse.ok("设置成功"));
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail(e.getMessage()));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void ensureQuestionImageTable(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS question_image (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT," +
                        "questionID VARCHAR(64) NOT NULL," +
                        "imagePath VARCHAR(512) NOT NULL," +
                        "diskPath VARCHAR(1024) NULL," +
                        "sortOrder INT DEFAULT 0," +
                        "INDEX idx_question_image_qid (questionID)" +
                        ")")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE question_image ADD COLUMN diskPath VARCHAR(1024) NULL")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private void ensureQuestionCommentReplyColumns(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE question_comment ADD COLUMN parentCommentID INT NULL")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE question_comment ADD COLUMN replyToUserID VARCHAR(64) NULL")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE question_comment ADD COLUMN deleted TINYINT(1) NOT NULL DEFAULT 0")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private String queryFirstQuestionImage(Connection conn, String questionID) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM question_image WHERE questionID = ? ORDER BY sortOrder ASC, id ASC LIMIT 1")) {
            ps.setString(1, questionID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return "/api/questions/images/" + rs.getInt("id");
            }
            return null;
        }
    }

    private List<String> queryQuestionImages(Connection conn, String questionID) throws SQLException {
        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM question_image WHERE questionID = ? ORDER BY sortOrder ASC, id ASC")) {
            ps.setString(1, questionID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add("/api/questions/images/" + rs.getInt("id"));
            }
        }
        return list;
    }

    private void serveQuestionImage(String imageId, HttpServletResponse response, ServletContext ctx) throws Exception {
        int id;
        try {
            id = Integer.parseInt(imageId);
        } catch (Exception e) {
            response.sendError(404);
            return;
        }
        Connection conn = DBUtil.getConnection();
        String imagePath = null;
        String diskPath = null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT imagePath, diskPath FROM question_image WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                imagePath = rs.getString("imagePath");
                diskPath = rs.getString("diskPath");
            }
        } finally {
            conn.close();
        }
        File file = null;
        if (diskPath != null && !diskPath.trim().isEmpty()) {
            file = new File(diskPath);
        }
        if ((file == null || !file.exists()) && imagePath != null && !imagePath.trim().isEmpty()) {
            file = new File(ctx.getRealPath("") + File.separator + imagePath.replace("/", File.separator));
        }
        if ((file == null || !file.exists()) && imagePath != null && !imagePath.trim().isEmpty()) {
            String name = new File(imagePath.replace("/", File.separator)).getName();
            file = new File(getQuestionImageStorageDir(ctx), name);
        }
        if (file == null || !file.exists()) {
            response.sendError(404);
            return;
        }
        String contentType = Files.probeContentType(file.toPath());
        response.setContentType(contentType == null ? "image/jpeg" : contentType);
        Files.copy(file.toPath(), response.getOutputStream());
    }

    private File getQuestionImageStorageDir(ServletContext ctx) {
        String base = System.getenv("APP_UPLOAD_DIR");
        if (base == null || base.trim().isEmpty()) {
            base = System.getProperty("user.dir") + File.separator + "runtime-uploads";
        }
        return new File(base, "questions");
    }

    private void auditQuestion(String qid, HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String action = request.getParameter("action");
        String state = "approve".equals(action) ? "审核通过" : "审核拒绝";
        Connection conn = DBUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE question SET questionState = ? WHERE questionID = ?")) {
            ps.setString(1, state);
            ps.setString(2, qid);
            ps.executeUpdate();
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok("审核完成"));
    }

    private void adminDeleteQuestion(String qid, HttpServletResponse response) throws SQLException, IOException {
        Connection conn = DBUtil.getConnection();
        try {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM question_comment WHERE questionID = ?")) {
                ps.setString(1, qid);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM question_image WHERE questionID = ?")) {
                ps.setString(1, qid);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM question WHERE questionID = ?")) {
                ps.setString(1, qid);
                int n = ps.executeUpdate();
                if (n == 0) {
                    JsonResponse.write(response, JsonResponse.fail("帖子不存在"));
                    return;
                }
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok("管理员已删除帖子"));
    }

    private void deleteOwnQuestion(String qid, HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = SessionHelper.getUserId(request);
        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
            String owner = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT userID FROM question WHERE questionID = ? FOR UPDATE")) {
                ps.setString(1, qid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) owner = rs.getString("userID");
            }
            if (owner == null) {
                conn.rollback();
                JsonResponse.write(response, JsonResponse.fail("帖子不存在"));
                return;
            }
            if (!owner.equals(userID)) {
                conn.rollback();
                JsonResponse.write(response, JsonResponse.fail("只能删除自己发布的帖子"));
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM question_comment_like WHERE questionID = ?")) {
                ps.setString(1, qid);
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM question_comment WHERE questionID = ?")) {
                ps.setString(1, qid);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM question_image WHERE questionID = ?")) {
                ps.setString(1, qid);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM question WHERE questionID = ? AND userID = ?")) {
                ps.setString(1, qid);
                ps.setString(2, userID);
                int n = ps.executeUpdate();
                if (n == 0) {
                    conn.rollback();
                    JsonResponse.write(response, JsonResponse.fail("删除失败"));
                    return;
                }
            }
            conn.commit();
            JsonResponse.write(response, JsonResponse.ok("已删除帖子"));
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("删除失败"));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void deleteOwnComment(String qid, String commentId, HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
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
            ensureQuestionCommentReplyColumns(conn);
            String owner = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT userID FROM question_comment WHERE questionID = ? AND commentID = ?")) {
                ps.setString(1, qid);
                ps.setInt(2, cid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    owner = rs.getString("userID");
                }
            }
            if (owner == null) {
                JsonResponse.write(response, JsonResponse.fail("评论不存在"));
                return;
            }
            if (!userID.equals(owner)) {
                JsonResponse.write(response, JsonResponse.fail("只能删除自己的评论"));
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE question_comment SET deleted = 1, commentContent = '该评论已删除' WHERE questionID = ? AND commentID = ? AND userID = ?")) {
                ps.setString(1, qid);
                ps.setInt(2, cid);
                ps.setString(3, userID);
                int n = ps.executeUpdate();
                if (n == 0) {
                    JsonResponse.write(response, JsonResponse.fail("删除失败"));
                    return;
                }
            }
            JsonResponse.write(response, JsonResponse.ok("评论已删除"));
        } finally {
            conn.close();
        }
    }
}
