package dev.mockboard.engine;

import lombok.Getter;

public class PathPattern {

    private static final char SEPARATOR = '/';

    @Getter
    private final String pattern;

    @Getter
    private final String mockId;

    private final String[] segments;
    private final boolean[] wildcardSegments;
    private final int segmentCount;

    public PathPattern(String pattern, String mockId) {
        this.pattern = pattern;
        this.mockId = mockId;

        var parts = pattern.split("/", -1);
        int startIdx = (parts.length > 0 && parts[0].isEmpty()) ? 1 : 0;

        this.segmentCount = parts.length - startIdx;
        this.segments = new String[segmentCount];
        this.wildcardSegments = new boolean[segmentCount];

        for (int i = 0; i < segmentCount; i++) {
            var segment = parts[startIdx + i];
            segments[i] = segment;
            wildcardSegments[i] = "*".equals(segment);
        }
    }

    boolean matches(String requestPath) {
        if (requestPath.isEmpty()) {
            return pattern.isEmpty();
        }

        int pathLength = requestPath.length();
        int segmentIdx = 0;
        int charIdx = (requestPath.charAt(0) == SEPARATOR) ? 1 : 0;

        while (segmentIdx < segmentCount) {
            if (charIdx >= pathLength) {
                return false;
            }

            if (wildcardSegments[segmentIdx]) {
                while (charIdx < pathLength && requestPath.charAt(charIdx) != SEPARATOR) {
                    charIdx++;
                }
                charIdx++;
            } else {
                var expectedSegment = segments[segmentIdx];
                int expectedLength = expectedSegment.length();

                int segmentEnd = charIdx;
                while (segmentEnd < pathLength && requestPath.charAt(segmentEnd) != SEPARATOR) {
                    segmentEnd++;
                }

                int actualLength = segmentEnd - charIdx;

                if (actualLength != expectedLength) {
                    return false;
                }

                for (int i = 0; i < expectedLength; i++) {
                    if (requestPath.charAt(charIdx + i) != expectedSegment.charAt(i)) {
                        return false;
                    }
                }

                charIdx = segmentEnd + 1;
            }

            segmentIdx++;
        }
        return charIdx >= pathLength || charIdx == pathLength - 1;
    }
}
