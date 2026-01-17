package dev.mockboard.common.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateFakerEngineTest {

    private final TemplateFakerEngine engine = new TemplateFakerEngine();

    @Test
    void replaceValidTokens() {
        var input = "{\"name\": \"{{user.fullName}}\", \"email\": \"{{user.email}}\"}";
        var result = engine.applyFaker(input);
        assertThat(result)
                .doesNotContain("{{user.fullName}}")
                .doesNotContain("{{user.email}}")
                .contains("\"name\": \"")
                .contains("@");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{{user.fullName}}",
            "{{  user.fullName  }}",
            "{{\tuser.fullName\n}}",
            "{{       user.fullName       }}"
    })
    void handleWhitespaces(String token) {
        var result = engine.applyFaker(token);

        assertThat(result)
                .doesNotContain("{{")
                .doesNotContain("unknown")
                .isNotEmpty();
    }

    @Test
    void preventNotPairMatching() {
        var input = """
            {
              "username": "{{ test",
              "address": " test 2}}"
            }
            """;

        var result = engine.applyFaker(input);
        assertThat(result).isEqualTo(input);
    }

    @Test
    void unknownTokens() {
        var input = "Hello {{non.existent}}";
        var result = engine.applyFaker(input);
        assertThat(result).isEqualTo("Hello [unknown: non.existent]");
    }

    @Test
    void edgeCases() {
        assertThat(engine.applyFaker(null)).isNull();
        assertThat(engine.applyFaker("")).isEmpty();
        assertThat(engine.applyFaker("   ")).isEqualTo("   ");
    }

    @Test
    void ignoreLongKeys() {
        var longKey = "z".repeat(50);
        var input = "{{" + longKey + "}}";

        var result = engine.applyFaker(input);
        assertThat(result).isEqualTo(input);
    }

    @Test
    void multipleIdenticalTokens() {
        var input = "{{user.fullName}}{{user.fullName}}";
        var result = engine.applyFaker(input);
        assertThat(result)
                .doesNotContain("{")
                .doesNotContain("}")
                .doesNotContain("unknown");
    }

    @Test
    void extraCurlyBraces() {
        var input = "{{{user.fullName}}}";
        var result = engine.applyFaker(input);
        assertThat(result)
                .isEqualTo(input);
    }

    @Test
    void systemTemplates() {
        var input = "{{system.int}}, {{system.long}}, {{system.double}}, {{system.bool}}, {{system.uuid}}";
        var result = engine.applyFaker(input);
        assertThat(result)
                .doesNotContain("{{")
                .doesNotContain("}}")
                .doesNotContain("unknown");

        var parts = result.split(",");
        assertThat(parts).hasSize(5);
        assertThat(Integer.parseInt(parts[0].trim())).isBetween(0, 10000).isInstanceOf(Integer.class);
        assertThat(Long.parseLong(parts[1].trim())).isInstanceOf(Long.class);
        assertThat(Double.parseDouble(parts[2].trim())).isInstanceOf(Double.class);
        assertThat(parts[3].trim()).matches("true|false");
        assertThat(UUID.fromString(parts[4].trim())).isNotNull();
    }
}