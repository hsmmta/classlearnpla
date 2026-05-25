package org.example.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

final class UserDataPurgeUtil {
    private UserDataPurgeUtil() {
    }

    static void purge(Connection conn, String userID) {
        // Discussion data
        execIgnore(conn,
                "UPDATE question SET bestAnswerID = NULL WHERE bestAnswerID IN (" +
                        "SELECT commentID FROM (SELECT commentID FROM question_comment WHERE userID = ?) t" +
                        ")", userID);
        execIgnore(conn,
                "DELETE FROM question_comment_like WHERE commentID IN (" +
                        "SELECT commentID FROM (SELECT commentID FROM question_comment WHERE userID = ?) t" +
                        ")", userID);
        execIgnore(conn,
                "DELETE FROM question_comment_like WHERE questionID IN (" +
                        "SELECT questionID FROM (SELECT questionID FROM question WHERE userID = ?) t" +
                        ")", userID);
        execIgnore(conn, "DELETE FROM question_comment_like WHERE userID = ?", userID);
        execIgnore(conn,
                "DELETE FROM question_comment WHERE questionID IN (" +
                        "SELECT questionID FROM (SELECT questionID FROM question WHERE userID = ?) t" +
                        ")", userID);
        execIgnore(conn, "DELETE FROM question_comment WHERE userID = ?", userID);
        execIgnore(conn,
                "DELETE FROM question_image WHERE questionID IN (" +
                        "SELECT questionID FROM (SELECT questionID FROM question WHERE userID = ?) t" +
                        ")", userID);
        execIgnore(conn, "DELETE FROM question WHERE userID = ?", userID);

        // Material data
        execIgnore(conn,
                "DELETE FROM comment WHERE materialID IN (" +
                        "SELECT materialID FROM (SELECT materialID FROM material WHERE userID = ?) t" +
                        ")", userID);
        execIgnore(conn, "DELETE FROM comment WHERE userID = ?", userID);
        execIgnore(conn, "DELETE FROM material WHERE userID = ?", userID);

        // Points and governance data
        execIgnore(conn, "DELETE FROM exchange_request WHERE userID = ?", userID);
        execIgnore(conn, "DELETE FROM pointop WHERE userID = ?", userID);
        execIgnore(conn, "DELETE FROM points WHERE userID = ?", userID);
        execIgnore(conn, "DELETE FROM user_penalty WHERE userID = ?", userID);
        execIgnore(conn, "DELETE FROM user_checkin WHERE userID = ?", userID);
        execIgnore(conn, "DELETE FROM reset_pwd_code WHERE userID = ?", userID);
    }

    private static void execIgnore(Connection conn, String sql, String userID) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }
}
