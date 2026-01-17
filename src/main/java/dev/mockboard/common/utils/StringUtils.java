package dev.mockboard.common.utils;

import dev.mockboard.Constants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import static org.springframework.util.StringUtils.countOccurrencesOf;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

    public static String generate(int length) {
        return RandomStringUtils.secure().nextAlphanumeric(length);
    }

    public static String removeTrailingSlash(String requestPath) {
        if (requestPath == null) return "";
        if (requestPath.length() > 1 && requestPath.endsWith("/")) {
            return requestPath.substring(0, requestPath.length() - 1);
        }
        return requestPath;
    }

    public static int countWildcards(String path) {
        if (path == null) return 0;
        return countOccurrencesOf(path, Constants.WILDCARD);
    }
}
