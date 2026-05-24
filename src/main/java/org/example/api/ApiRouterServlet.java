package org.example.api;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/api/*")
public class ApiRouterServlet extends HttpServlet {
    private final AuthApiHandler auth = new AuthApiHandler();
    private final MaterialApiHandler material = new MaterialApiHandler();
    private final DiscussionApiHandler discussion = new DiscussionApiHandler();
    private final PrizeApiHandler prize = new PrizeApiHandler();
    private final ProfileApiHandler profile = new ProfileApiHandler();
    private final AdminApiHandler admin = new AdminApiHandler();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getPathInfo();
        if (path == null) {
            path = "/";
        }
        String method = request.getMethod();

        try {
            if ("/session".equals(path) && "GET".equals(method)) {
                auth.session(request, response);
                return;
            }
            if (path.startsWith("/auth/")) {
                auth.handle(path, method, request, response);
                return;
            }
            if (path.startsWith("/materials")) {
                material.handle(path, method, request, response, getServletContext());
                return;
            }
            if (path.startsWith("/questions")) {
                discussion.handle(path, method, request, response, getServletContext());
                return;
            }
            if (path.startsWith("/prizes") || path.startsWith("/points")) {
                prize.handle(path, method, request, response);
                return;
            }
            if (path.startsWith("/profile")) {
                profile.handle(path, method, request, response);
                return;
            }
            if (path.startsWith("/admin/")) {
                admin.handle(path, method, request, response);
                return;
            }
            JsonResponse.write(response, HttpServletResponse.SC_NOT_FOUND, JsonResponse.fail("接口不存在"));
        } catch (Exception e) {
            e.printStackTrace();
            if (response.isCommitted()) {
                return;
            }
            try {
                JsonResponse.write(response, JsonResponse.fail("服务器错误: " + e.getMessage()));
            } catch (IllegalStateException ignored) {
                // OutputStream may have already been obtained for binary responses.
            }
        }
    }
}
