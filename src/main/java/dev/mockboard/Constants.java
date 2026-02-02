package dev.mockboard;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String APP_VERSION = "0.3-beta";

    // defaults
    public static final int BOARD_ID_LENGTH = 24;
    public static final int BOARD_OWNER_TOKEN_LENGTH = 48;
    public static final String WILDCARD = "*";
    public static final String OWNER_TOKEN_HEADER_KEY = "X-Owner-Token";

    // app limits
    public static final boolean MAX_ACTIVE_BOARDS_CHECK_ENABLED = Env.getBool("MBD_MAX_ACTIVE_BOARDS_CHECK_ENABLED", true);
    public static final int MAX_ACTIVE_BOARDS = Env.getInt("MBD_MAX_ACTIVE_BOARDS", 500);
    public static final int MAX_MOCK_RULES = Env.getInt("MBD_MAX_MOCK_RULES", 12);
    public static final int MAX_WEBHOOKS = Env.getInt("MBD_MAX_WEBHOOKS", 15);

    // cache
    public static final int DEFAULT_CACHE_MAX_ENTRIES = Env.getInt("MBD_CACHE_DEFAULT_MAX_ENTRIES", 5_000);
    public static final int DEFAULT_CACHE_EXP_AFTER_ACCESS_MINUTES = Env.getInt("MBD_CACHE_DEFAULT_EXP_AFTER_ACCESS_MINUTES", 15);

    // events
    public static final int EVENT_DEDUP_PROCESS_DELAY = Env.getInt("MBD_EVENT_DEDUP_PROCESS_DELAY", 30_000);
    public static final int EVENT_CONSUMER_DRAIN_WEBHOOK_ELEMS = Env.getInt("MBD_EVENT_CONSUMER_DRAIN_WEBHOOK_ELEMS", 500);

    // validations
    public static final Pattern VALID_PATH_PATTERN = Pattern.compile("^/[a-zA-Z0-9/_\\-*]+$");
    public static final Set<String> VALID_HTTP_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");
    public static final int MAX_PATH_LENGTH = Env.getInt("MBD_VALIDATION_MOCK_MAX_PATH_LENGTH", 250);
    public static final int MAX_QUERY_STRING_LENGTH = Env.getInt("MBD_VALIDATION_MOCK_MAX_QUERY_STRING_LENGTH", 250);
    public static final int MAX_BODY_LENGTH = Env.getInt("MBD_VALIDATION_MOCK_MAX_BODY_LENGTH", 5_000);
    public static final int MAX_WILDCARDS = Env.getInt("MBD_VALIDATION_MOCK_MAX_WILDCARDS", 3);
    public static final int MAX_HEADERS_SIZE = Env.getInt("MBD_VALIDATION_MOCK_MAX_HEADERS_SIZE", 5);
    public static final int MAX_WEBHOOK_HEADERS_SIZE = Env.getInt("MBD_VALIDATION_MOCK_MAX_WEBHOOK_HEADERS_SIZE", 15);
    public static final int MAX_HEADER_KEY_LENGTH = Env.getInt("MBD_VALIDATION_MOCK_MAX_HEADER_KEY_LENGTH", 100);
    public static final int MAX_HEADER_VALUE_LENGTH = Env.getInt("MBD_VALIDATION_MOCK_MAX_HEADER_VALUE_LENGTH", 500);
    public static final int MAX_ALLOWED_DELAY = Env.getInt("MBD_VALIDATION_MOCK_MAX_ALLOWED_DELAY", 10_000);

    // sse
    public static final int MAX_SSE_EMITTERS_PER_BOARD = 1;
    public static final long SSE_EMITTER_TTL = 1_800_000L; // 30min
    public static final long SSE_EMITTER_HEARTBEAT_RATE = 30_000L; // 30sec
    public static final String SSE_EMITTER_EVENT_WEBHOOK = "webhook-event";
    public static final String SSE_EMITTER_EVENT_PING = "ping";
    public static final String SSE_EMITTER_EVENT_SHUTDOWN = "server-shutdown";

    // rate limiter
    public static final boolean RATE_LIMIT_ENABLED = Env.getBool("MBD_RATE_LIMIT_ENABLED", false);
    public static final int RATE_LIMIT_MAX_BOARDS_PER_HOUR = Env.getInt("MBD_RATE_LIMIT_MAX_BOARDS_PER_HOUR", 2);
    public static final int RATE_LIMIT_MAX_MOCK_EXECUTIONS_PER_MINUTE = Env.getInt("MBD_RATE_LIMIT_MAX_MOCK_EXECUTIONS_PER_MINUTE", 20);
    public static final int RATE_LIMIT_MAX_OTHER_REQUESTS_PER_MINUTE = Env.getInt("MBD_RATE_LIMIT_MAX_OTHER_REQUESTS_PER_MINUTE", 80);

    // default messages
    public static final String DEFAULT_EXECUTION_RESPONSE = "{\"message\": \"Hello from Mockboard.dev\"}";
    public static final String DEFAULT_WEBHOOK_PARSING_ERROR_RESPONSE = "{\"message\": \"Unable to read request body\"}";
    public static final String DEFAULT_RATE_LIMIT_ERROR_RESPONSE = "{\"error\":\"Rate limit exceeded\"}";
}
