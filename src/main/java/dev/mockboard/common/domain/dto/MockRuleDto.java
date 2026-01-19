package dev.mockboard.common.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.mockboard.Constants;
import dev.mockboard.common.utils.StringUtils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.regex.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockRuleDto implements Serializable {

    private String id;
    @JsonIgnore private String boardId;
    @NotEmpty private String method;
    @NotEmpty private String path;
    private String headers;
    private String body;
    @NotNull @Positive private Integer statusCode;
    @NotNull @PositiveOrZero private Integer delay;
    private Instant timestamp;
    @JsonIgnore private transient Pattern compiledPattern;
    @JsonIgnore private transient Integer wildcardCount;
    @JsonIgnore private transient Integer pathLength;

    public void compilePattern() {
        if (this.path == null) return;

        var escaped = escapePath(this.path);
        this.compiledPattern = Pattern.compile("^" + escaped + "$");
        this.wildcardCount = StringUtils.countWildcards(this.path);
        this.pathLength = this.path.length();
    }

    public boolean matches(String requestPath) {
        if (requestPath == null) return false;
        if (this.compiledPattern == null) {
            throw new IllegalStateException("Pattern not compiled. Call compilePattern() first.");
        };
        return this.compiledPattern.matcher(requestPath).matches();
    }

    private String escapePath(String requestPath) {
        var builder = new StringBuilder();
        int start = 0;

        for (int i = 0; i < requestPath.length(); i++) {
            if (requestPath.charAt(i) == Constants.WILDCARD.charAt(0)) {
                if (i > start) {
                    builder.append(Pattern.quote(requestPath.substring(start, i)));
                }
                builder.append("[^/]+");
                start = i + 1;
            }
        }

        if (start < requestPath.length()) {
            builder.append(Pattern.quote(requestPath.substring(start)));
        }
        return builder.toString();
    }
}
