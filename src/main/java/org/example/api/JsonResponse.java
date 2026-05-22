package org.example.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public final class JsonResponse {
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    public final boolean success;
    public final Object data;
    public final String msg;

    public JsonResponse(boolean success, Object data, String msg) {
        this.success = success;
        this.data = data;
        this.msg = msg == null ? "" : msg;
    }

    public static JsonResponse ok(Object data) {
        return new JsonResponse(true, data, "");
    }

    public static JsonResponse ok(String msg) {
        return new JsonResponse(true, null, msg);
    }

    public static JsonResponse fail(String msg) {
        return new JsonResponse(false, null, msg);
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static void write(HttpServletResponse response, JsonResponse body) throws IOException {
        write(response, HttpServletResponse.SC_OK, body);
    }

    public static void write(HttpServletResponse response, int status, JsonResponse body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(body.toJson());
        }
    }

    public static Gson gson() {
        return GSON;
    }
}
