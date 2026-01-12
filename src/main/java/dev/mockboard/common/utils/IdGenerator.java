package dev.mockboard.common.utils;

import de.huxhorn.sulky.ulid.ULID;
import dev.mockboard.Constants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IdGenerator {

    private static final ULID ULID = new ULID();
    private static final RandomUtils randomUtils = RandomUtils.secure();

    public static String generateId() {
        return ULID.nextULID().toLowerCase();
    }

    public static String generateBoardId() {
        var ulid = generateId();
        var prefix = ulid.substring(ulid.length() - 10);
        var camelCasePrefix = new StringBuilder();
        for (char c : prefix.toCharArray()) {
            if (Character.isLetter(c)) {
                camelCasePrefix.append(randomUtils.randomBoolean()
                        ? Character.toLowerCase(c)
                        : Character.toUpperCase(c));
            } else {
                camelCasePrefix.append(c);
            }
        }
        int suffixLength = Constants.BOARD_ID_LENGTH - prefix.length();
        return camelCasePrefix + StringUtils.generate(suffixLength);
    }
}
