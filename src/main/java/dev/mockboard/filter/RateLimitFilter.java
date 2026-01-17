package dev.mockboard.filter;

import dev.mockboard.Constants;
import dev.mockboard.cache.RateLimiterCache;
import dev.mockboard.common.utils.RequestUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RateLimiterCache rateLimiterCache;

    private static final Set<String> EXTENSIONS_TO_IGNORE = Set.of(
            ".js", ".css", ".ico", ".png", ".woff", ".woff2"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!Constants.RATE_LIMIT_ENABLED) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        var request = (HttpServletRequest) servletRequest;
        var response = (HttpServletResponse) servletResponse;

        var ip = RequestUtils.getClientIp(request);
        var path = request.getRequestURI();
        var method = request.getMethod();

        if (shouldSkip(path)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        var allowed = true;
        var normalizedPath = path.endsWith("/") && path.length() > 1
                ? path.substring(0, path.length() - 1)
                : path;
        if ("/api/boards".equals(normalizedPath) && "POST".equals(method)) {
            allowed = rateLimiterCache.allowBoardCreation(ip);
        } else if (normalizedPath.startsWith("/m")) {
            allowed = rateLimiterCache.allowMockExecution(ip);
        }

        if (allowed) {
            allowed = rateLimiterCache.allowOtherRequests(ip);
        }

        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(Constants.DEFAULT_RATE_LIMIT_ERROR_RESPONSE);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean shouldSkip(String path) {
        return EXTENSIONS_TO_IGNORE.stream().anyMatch(path::endsWith);
    }
}
