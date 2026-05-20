package org.example.servlet.material;

import org.example.model.Material;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/material/edit")
public class EditMaterialServlet extends HttpServlet {

    private static final String UPLOAD_DIRECTORY = "uploads/materials";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MAX_REQUEST_SIZE = 15 * 1024 * 1024;

    // Handles fetching the material data to display on the edit page
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String materialID = request.getParameter("id");
        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");

        if (userID == null) {
            response.sendRedirect(request.getContextPath() + "/auth/signin.html");
            return;
        }

        Material material = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            // Ensure user can only edit their own material
            String sql = "SELECT materialID, materialTitle, materialSubject, materialContent, materialType, filePath FROM material WHERE materialID = ? AND userID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, materialID);
            pstmt.setString(2, userID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                material = new Material();
                material.setMaterialID(rs.getString("materialID"));
                material.setMaterialTitle(rs.getString("materialTitle"));
                material.setMaterialSubject(rs.getString("materialSubject"));
                material.setMaterialContent(rs.getString("materialContent"));
                material.setMaterialType(rs.getString("materialType"));
                material.setFilePath(rs.getString("filePath"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }

        if (material != null) {
            request.setAttribute("material", material);
            request.getRequestDispatcher("/material/EditMaterial.jsp").forward(request, response);
        } else {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<h1>错误</h1><p>资料不存在或您没有权限编辑。</p>");
        }
    }

    // Handles updating the material data after submission
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");

        if (userID == null) {
            response.sendRedirect(request.getContextPath() + "/auth/signin.html");
            return;
        }

        if (!ServletFileUpload.isMultipartContent(request)) {
            handleSimplePost(request, response, userID);
            return;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1024 * 1024);
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MAX_FILE_SIZE);
        upload.setSizeMax(MAX_REQUEST_SIZE);
        upload.setHeaderEncoding("UTF-8");

        String materialID = null;
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
                        case "materialID": materialID = fieldValue; break;
                        case "materialTitle": materialTitle = fieldValue; break;
                        case "materialSubject": materialSubject = fieldValue; break;
                        case "materialType": materialType = fieldValue; break;
                        case "materialContent": materialContent = fieldValue; break;
                    }
                } else {
                    if ("pdfFile".equals(item.getFieldName()) && item.getSize() > 0) {
                        pdfFileItem = item;
                    }
                }
            }

            if (materialID == null || materialTitle == null || materialSubject == null || materialType == null) {
                response.getWriter().write("<script>alert('缺少必要的参数'); window.history.back();</script>");
                return;
            }

            String filePath = null;

            if ("pdf".equals(materialType)) {
                if (pdfFileItem != null) {
                    String fileName = pdfFileItem.getName();
                    if (!fileName.toLowerCase().endsWith(".pdf")) {
                        response.getWriter().write("<script>alert('只能上传PDF格式的文件'); window.history.back();</script>");
                        return;
                    }
                    deleteOldFile(materialID, userID);

                    String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
                    File uploadDir = new File(uploadPath);
                    if (!uploadDir.exists()) { uploadDir.mkdirs(); }

                    String savedFileName = materialID + ".pdf";
                    String fullPath = uploadPath + File.separator + savedFileName;
                    pdfFileItem.write(new File(fullPath));

                    filePath = UPLOAD_DIRECTORY + "/" + savedFileName;
                    materialContent = null;
                } else {
                    filePath = getExistingFilePath(materialID, userID);
                    materialContent = null;
                }
            } else {
                deleteOldFile(materialID, userID);
                filePath = null;
                if (materialContent == null || materialContent.trim().isEmpty()) {
                    response.getWriter().write("<script>alert('文本内容不能为空'); window.history.back();</script>");
                    return;
                }
            }

            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                conn = DBUtil.getConnection();
                String sql = "UPDATE material SET materialTitle = ?, materialSubject = ?, materialContent = ?, materialType = ?, filePath = ?, materialState = '待审核' WHERE materialID = ? AND userID = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, materialTitle);
                pstmt.setString(2, materialSubject);
                pstmt.setString(3, materialContent);
                pstmt.setString(4, materialType);
                pstmt.setString(5, filePath);
                pstmt.setString(6, materialID);
                pstmt.setString(7, userID);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    response.getWriter().write("<script>alert('修改成功！资料已重新提交审核。'); window.location.href='" + request.getContextPath() + "/material/manage';</script>");
                } else {
                    response.getWriter().write("<script>alert('修改失败，资料不存在或无权限。'); window.history.back();</script>");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.getWriter().write("<script>alert('数据库错误'); window.history.back();</script>");
            } finally {
                DBUtil.close(conn, pstmt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("<script>alert('处理过程中发生错误'); window.history.back();</script>");
        }
    }

    private void handleSimplePost(HttpServletRequest request, HttpServletResponse response, String userID) throws IOException {
        String materialID = request.getParameter("materialID");
        String materialTitle = request.getParameter("materialTitle");
        String materialSubject = request.getParameter("materialSubject");
        String materialContent = request.getParameter("materialContent");

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE material SET materialTitle = ?, materialSubject = ?, materialContent = ?, materialType = 'text', filePath = NULL, materialState = '待审核' WHERE materialID = ? AND userID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, materialTitle);
            pstmt.setString(2, materialSubject);
            pstmt.setString(3, materialContent);
            pstmt.setString(4, materialID);
            pstmt.setString(5, userID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        response.sendRedirect(request.getContextPath() + "/material/manage");
    }

    private String getExistingFilePath(String materialID, String userID) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT filePath FROM material WHERE materialID = ? AND userID = ?");
            pstmt.setString(1, materialID);
            pstmt.setString(2, userID);
            rs = pstmt.executeQuery();
            if (rs.next()) { return rs.getString("filePath"); }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    private void deleteOldFile(String materialID, String userID) {
        String oldPath = getExistingFilePath(materialID, userID);
        if (oldPath != null && !oldPath.trim().isEmpty()) {
            String fullPath = getServletContext().getRealPath("") + File.separator + oldPath;
            File oldFile = new File(fullPath);
            if (oldFile.exists()) { oldFile.delete(); }
        }
    }
}