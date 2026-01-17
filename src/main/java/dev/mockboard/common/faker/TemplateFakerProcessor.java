package dev.mockboard.common.faker;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.springframework.stereotype.Component;
import tools.jackson.core.io.JsonStringEncoder;

@Slf4j
@Component
public class TemplateFakerProcessor {

    private static final int MAX_KEY_LENGTH = 48;

    private static final JsonStringEncoder ENCODER = JsonStringEncoder.getInstance();
    private final StringSubstitutor substitutor;

    public TemplateFakerProcessor() {
        log.info("Initializing faker...");
        TemplateFakerDictionary.initializeDictionary();

        StringLookup fakerLookup = key -> {
            if (key == null) return null;

            // should trim/remove whitespaces
            // {{   user.fullName    }} != {{user.fullName}}
            var trimmedKey = key.trim();
            if (isInvalidKey(trimmedKey)) return null;

            var supplier = TemplateFakerDictionary.getDictionary().get(trimmedKey);
            if (supplier != null) {
                var rawValue = supplier.get();
                char[] escaped = ENCODER.quoteAsCharArray(rawValue);
                return new String(escaped);
            }

            // not registered template
            return "[unknown: " + trimmedKey + "]";
        };

        this.substitutor = new StringSubstitutor(fakerLookup);
        this.substitutor.setVariablePrefix("{{");
        this.substitutor.setVariableSuffix("}}");
        this.substitutor.setEnableSubstitutionInVariables(false);
    }

    private boolean isInvalidKey(String key) {
        if (key.length() > MAX_KEY_LENGTH) return true;
        for (char c : key.toCharArray()) {
            // input body is json string, possbile case that needs to be handled:
            // { "username": "{{ test", "address": " test 2}}" }
            // {{{{user.fullName}}}}

            // possibly use BitSet, but should be good enough for now
            if (c == '"' || c == '\n' || c == '\r' || c == ':' || c == '{' || c == '}') {
                return true;
            }
        }
        return false;
    }

    public String applyFaker(String input) {
        if (input == null || input.isEmpty()) return input;
        return substitutor.replace(input);
    }
}
