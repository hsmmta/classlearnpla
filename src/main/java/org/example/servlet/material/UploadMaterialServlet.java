package org.example.servlet.material;

import org.example.util.DBUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@WebServlet("/material/upload")
public class UploadMaterialServlet extends HttpServlet {
    private static final String UPLOAD_DIRECTORY = "uploads/materials";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_REQUEST_SIZE = 15 * 1024 * 1024; // 15MB

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");
        String userName = (String) session.getAttribute("userName");

        if (userID == null || userID.trim().isEmpty() || userName == null || userName.trim().isEmpty()) {
            response.getWriter().write("<script>alert('用户未登录，请先登录'); window.location.href='" + request.getContextPath() + "/auth/signin.html';</script>");
            return;
        }

        // Check if request is multipart
        if (!ServletFileUpload.isMultipartContent(request)) {
            response.getWriter().write("<script>alert('表单格式错误'); window.history.back();</script>");
            return;
        }

        // Configure upload settings
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1024 * 1024); // 1MB
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MAX_FILE_SIZE);
        upload.setSizeMax(MAX_REQUEST_SIZE);
        upload.setHeaderEncoding("UTF-8");

        String materialTitle = null;
        String materialSubject = null;
        String materialType = null;
        String materialContent = null;
        FileItem pdfFileItem = null;

        try {
            List<FileItem> formItems = upload.parseRequest(request);

            for (FileItem item : formItems) {
                if (item.isFormField()) {
                    String fieldName = item.getFieldName();
                    String fieldValue = item.getString("UTF-8");

                    switch (fieldName) {
                        case "materialTitle":
                            materialTitle = fieldValue;
                            break;
                        case "materialSubject":
                            materialSubject = fieldValue;
                            break;
                        case "materialType":
                            materialType = fieldValue;
                            break;
                        case "materialContent":
                            materialContent = fieldValue;
                            break;
                    }
                } else {
                    // This is a file field
                    if ("pdfFile".equals(item.getFieldName()) && item.getSize() > 0) {
                        pdfFileItem = item;
                    }
                }
            }

            // Validate required fields
            if (materialTitle == null || materialTitle.trim().isEmpty() ||
                materialSubject == null || materialSubject.trim().isEmpty() ||
                materialType == null || materialType.trim().isEmpty()) {
                response.getWriter().write("<script>alert('标题、科目和资料类型为必填项'); window.history.back();</script>");
                return;
            }

            // Validate based on material type
            if ("text".equals(materialType)) {
                if (materialContent == null || materialContent.trim().isEmpty()) {
                    response.getWriter().write("<script>alert('文本内容不能为空'); window.history.back();</script>");
                    return;
                }
            } else if ("pdf".equals(materialType)) {
                if (pdfFileItem == null) {
                    response.getWriter().write("<script>alert('请上传PDF文件'); window.history.back();</script>");
                    return;
                }

                // Validate file type
                String fileName = pdfFileItem.getName();
                if (!fileName.toLowerCase().endsWith(".pdf")) {
                    response.getWriter().write("<script>alert('只能上传PDF格式的文件'); window.history.back();</script>");
                    return;
                }
            }

            String materialID = UUID.randomUUID().toString();
            String filePath = null;

            // Handle PDF file upload
            if ("pdf".equals(materialType) && pdfFileItem != null) {
                String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                String originalFileName = new File(pdfFileItem.getName()).getName();
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String savedFileName = materialID + fileExtension;
                String fullPath = uploadPath + File.separator + savedFileName;

                File storeFile = new File(fullPath);
                pdfFileItem.write(storeFile);

                filePath = UPLOAD_DIRECTORY + "/" + savedFileName;
            }

            // Save to database
            Connection conn = null;
            PreparedStatement pstmt = null;

            try {
                conn = DBUtil.getConnection();

                // Verify user exists
                String checkUserSql = "SELECT userphone FROM user WHERE userphone = ?";
                pstmt = conn.prepareStatement(checkUserSql);
                pstmt.setString(1, userID);
                java.sql.ResultSet rs = pstmt.executeQuery();

                if (!rs.next()) {
                    response.getWriter().write("<script>alert('用户不存在，请重新登录'); window.location.href='" + request.getContextPath() + "/auth/signin.html';</script>");
                    return;
                }
                rs.close();
                pstmt.close();

                // Insert material
                String sql = "INSERT INTO material (materialID, materialTitle, materialContent, userID, uploaderName, materialSubject, materialState, materialType, filePath) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, materialID);
                pstmt.setString(2, materialTitle);
                pstmt.setString(3, materialContent);
                pstmt.setString(4, userID);
                pstmt.setString(5, userName);
                pstmt.setString(6, materialSubject);
                pstmt.setString(7, "待审核");
                pstmt.setString(8, materialType);
                pstmt.setString(9, filePath);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    response.getWriter().write("<script>alert('上传成功！您的资料已提交审核，请耐心等待。'); window.location.href='" + request.getContextPath() + "/material/search';</script>");
                } else {
                    response.getWriter().write("<script>alert('上传失败，无法保存到数据库，请重试。'); window.history.back();</script>");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.getWriter().write("<script>alert('数据库错误：" + e.getMessage().replace("'", "\\'") + "'); window.history.back();</script>");
            } finally {
                DBUtil.close(conn, pstmt);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("<script>alert('文件上传过程中发生错误'); window.history.back();</script>");
        }
    }
}