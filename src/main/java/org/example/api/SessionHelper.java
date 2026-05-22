package org.example.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public final class SessionHelper {
    private SessionHelper() {
    }

    public static String getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute("userID");
    }

    public static String getUserType(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute("userType");
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        return getUserId(request) != null;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        return "admin".equals(getUserType(request));
    }

    public static Map<String, Object> sessionPayload(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        String userID = (String) session.getAttribute("userID");
        if (userID == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userID", userID);
        map.put("userName", session.getAttribute("userName"));
        map.put("userType", session.getAttribute("userType"));
        map.put("classID", session.getAttribute("classID"));
        map.put("gender", session.getAttribute("gender"));
        map.put("studentID", session.getAttribute("studentID"));
        map.put("userEmail", session.getAttribute("userEmail"));
        return map;
    }
}
