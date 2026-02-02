package dev.mockboard.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestUtils {

    public static String getClientIp(HttpServletRequest request) {
        var forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        var realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    public static String extractMockPath(String boardId, HttpServletRequest request) {
        var fullPath = request.getRequestURI();
        var prefix = "/m/" + boardId;

        if (fullPath.startsWith(prefix)) {
            var path = fullPath.substring(prefix.length());
            if (path.isEmpty()) {
                return "/";
            }

            if (!path.startsWith("/")) {
                return "/" + path;
            }
            return path;
        }

        // should not happen
        return fullPath;
    }
}
