package dev.mockboard;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    // defaults
    public static final int BOARD_API_KEY_LENGTH = 20;
    public static final int BOARD_OWNER_TOKEN_LENGTH = 48;

    // cache
    public static final int DEFAULT_CACHE_MAX_ENTRIES = Env.getInt("MBD_CACHE_DEFAULT_MAX_ENTRIES", 10_000);
    public static final int DEFAULT_CACHE_EXP_AFTER_ACCESS_MINUTES = Env.getInt("MBD_CACHE_DEFAULT_EXP_AFTER_ACCESS_MINUTES", 15);

    // headers
    public static final String OWNER_TOKEN_HEADER_KEY = "X-Owner-Token";

    // events
    public static final int MAX_EVENT_CONSUMER_DRAIN_ELEMS = Env.getInt("MBD_EVENT_MAX_EVENT_DRAIN", 200);

    // scheduler
    public static final int CREATED_EVENTS_PROCESS_DELAY = 5_000;
    public static final int UPDATED_EVENTS_PROCESS_DELAY = 13_000;
    public static final int DELETED_EVENTS_PROCESS_DELAY = 21_000;

    // validations
    public static final Pattern VALID_PATH_PATTERN = Pattern.compile("^/[a-zA-Z0-9/_\\-*{}]+$");
    public static final Set<String> VALID_HTTP_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");
    public static final int MAX_PATH_LENGTH = Env.getInt("MBD_VALIDATION_MOCK_MAX_PATH_LENGTH", 250);
    public static final int MAX_BODY_LENGTH = Env.getInt("MBD_VALIDATION_MOCK_MAX_BODY_LENGTH", 5_000);
    public static final int MAX_WILDCARDS = Env.getInt("MBD_VALIDATION_MOCK_MAX_WILDCARDS", 3);
    public static final int MAX_HEADERS_SIZE = Env.getInt("MBD_VALIDATION_MOCK_MAX_HEADERS", 5);
    public static final int MAX_HEADER_KEY_LENGTH = Env.getInt("MBD_VALIDATION_MOCK_MAX_HEADER_KEY_LENGTH", 100);
    public static final int MAX_HEADER_VALUE_LENGTH = Env.getInt("MBD_VALIDATION_MOCK_MAX_HEADER_VALUE_LENGTH", 500);

    // app
    public static final int MAX_MOCK_RULES = 12;
}
