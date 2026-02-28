package org.example.servlet;

import org.example.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// 注解路径简化，和注册的/register风格一致，前端表单直接对应
@WebServlet("/addPrize")
public class AddPrizeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 统一编码，和注册功能一致，避免中文乱码
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 2. 获取前端表单参数，和AddPrize.html的input/select name一一对应
        String goodsID = request.getParameter("goodsID");
        String goodsName = request.getParameter("goodsName");
        String goodsType = request.getParameter("goodsType");
        String needPointsStr = request.getParameter("needPoints");
        String currentNumStr = request.getParameter("currentNum");

        // 3. 第一步校验：必填项非空（带*的字段），弹窗提示+回退，和注册风格一致
        if (goodsID == null || goodsID.trim().isEmpty() ||
                goodsName == null || goodsName.trim().isEmpty() ||
                goodsType == null || goodsType.trim().isEmpty() ||
                needPointsStr == null || needPointsStr.trim().isEmpty() ||
                currentNumStr == null || currentNumStr.trim().isEmpty()) {
            response.getWriter().write("<script>alert('上架失败！所有带*字段为必填项'); window.history.back();</script>");
            return;
        }

        // 4. 第二步校验：字段长度（匹配数据库表字段长度，避免插入失败）
        if (goodsID.length() > 15) {
            response.getWriter().write("<script>alert('上架失败！奖品ID不能超过15个字符'); window.history.back();</script>");
            return;
        }
        if (goodsName.length() > 20) {
            response.getWriter().write("<script>alert('上架失败！奖品名称不能超过20个字符'); window.history.back();</script>");
            return;
        }

        // 5. 第三步校验：数字类型（积分/库存必须为有效数字）
        int needPoints;
        int currentNum;
        try {
            needPoints = Integer.parseInt(needPointsStr);
            currentNum = Integer.parseInt(currentNumStr);
            // 积分必须>0，库存必须>=0
            if (needPoints <= 0) {
                response.getWriter().write("<script>alert('上架失败！兑换积分必须大于0'); window.history.back();</script>");
                return;
            }
            if (currentNum < 0) {
                response.getWriter().write("<script>alert('上架失败！库存数量不能为负数'); window.history.back();</script>");
                return;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.getWriter().write("<script>alert('上架失败！积分/库存必须为有效数字'); window.history.back();</script>");
            return;
        }

        // 6. 数据库操作：插入goodslist表，完全复用DBUtil和注册的写法
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 校验数据库连接（防止DBUtil返回null）
            if (conn == null) {
                response.getWriter().write("<script>alert('上架失败！数据库连接失败，请联系管理员'); window.history.back();</script>");
                return;
            }
            // SQL语句：匹配你的数据库表（无goodsStatus字段）
            String sql = "INSERT INTO goodslist (goodsID, goodsName, goodsType, needPoints, currentNum) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            // 占位符赋值，顺序和SQL一致
            pstmt.setString(1, goodsID.trim());
            pstmt.setString(2, goodsName.trim());
            pstmt.setString(3, goodsType);
            pstmt.setInt(4, needPoints);
            pstmt.setInt(5, currentNum);

            // 执行插入，获取受影响行数
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // 上架成功：弹窗提示 + 跳转到奖品列表页（ManagePrize.jsp）
                System.out.println("奖品上架成功：ID=" + goodsID);
                response.getWriter().write("<script>alert('奖品上架成功！'); window.location.href='" + request.getContextPath() + "/Administrator/ManagePrize.jsp';</script>");
            } else {
                // 插入失败：弹窗提示 + 回退
                response.getWriter().write("<script>alert('上架失败！无数据被修改'); window.history.back();</script>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // 区分错误类型，精准提示（和注册的主键冲突提示一致）
            if (e.getErrorCode() == 1062) {
                // 主键冲突：奖品ID重复
                response.getWriter().write("<script>alert('上架失败！该奖品ID已存在，请更换'); window.history.back();</script>");
            } else {
                // 其他数据库错误
                response.getWriter().write("<script>alert('上架失败！数据库错误：" + e.getMessage() + "'); window.history.back();</script>");
            }
        } finally {
            // 关闭资源，复用DBUtil的重载方法
            DBUtil.close(conn, pstmt);
        }
    }

    // 拒绝GET请求直接访问，跳转到添加奖品页
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/Administrator/AddPrize.html");
    }
}