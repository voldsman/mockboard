package dev.mockboard.common.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.mockboard.common.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

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
    private String method;
    private String path;
    private String headers;
    private String body;
    private int statusCode;
    private long delay;
    private Instant timestamp;
    @JsonIgnore
    @Transient private Pattern compiledPattern;

    public void compilePattern() {
        if (this.path != null && this.compiledPattern == null) {
            var normalizedPath = StringUtils.removeTrailingSlash(this.path);
            this.compiledPattern = Pattern.compile("^" + escape(normalizedPath) + "$");
        }
    }

    public boolean matches(String requestPath) {
        if (this.compiledPattern == null) compilePattern();
        var normalizedPath = StringUtils.removeTrailingSlash(requestPath);
        return this.compiledPattern.matcher(normalizedPath).matches();
    }

    private String escape(String requestPath) {
        var builder = new StringBuilder();
        var parts = requestPath.split("\\*", -1);

        for (int i = 0; i < parts.length; i++) {
            builder.append(Pattern.quote(parts[i]));
            if (i < parts.length - 1) {
                builder.append("[^/]+");
            }
        }
        return builder.toString();
    }
}
