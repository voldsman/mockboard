package dev.mockboard.core.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestUtils {

    public static String getClientIp(HttpServletRequest request) {
        var forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public static String extractMockPath(HttpServletRequest request, String apiKey) {
        var fullPath = request.getRequestURI();
        var prefix = "/m/" + apiKey;

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
