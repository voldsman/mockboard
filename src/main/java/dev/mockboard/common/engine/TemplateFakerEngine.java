package dev.mockboard.common.engine;

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.springframework.stereotype.Component;
import tools.jackson.core.io.JsonStringEncoder;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@Component
public class TemplateFakerEngine {

    private static final int MAX_KEY_LENGTH = 48;

    private static final Faker FAKER = new Faker(Locale.US);
    private static final Map<String, Supplier<String>> DICTIONARY = new ConcurrentHashMap<>();
    private static final JsonStringEncoder ENCODER = JsonStringEncoder.getInstance();
    private final StringSubstitutor substitutor;

    public TemplateFakerEngine() {
        log.info("Initializing faker dictionary...");
        initializeDictionary();
        StringLookup fakerLookup = key -> {
            if (key == null) return null;

            // should trim/remove whitespaces
            // {{   user.fullName    }} != {{user.fullName}}
            var trimmedKey = key.trim();
            if (isInvalidKey(trimmedKey)) return null;

            var supplier = DICTIONARY.get(trimmedKey);
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

    private void initializeDictionary() {
        // personal data
        DICTIONARY.put("user.fullName", () -> FAKER.name().fullName());
        DICTIONARY.put("user.firstName", () -> FAKER.name().firstName());
        DICTIONARY.put("user.lastName", () -> FAKER.name().lastName());
        DICTIONARY.put("user.email", () -> FAKER.internet().emailAddress());
        DICTIONARY.put("user.username", () -> FAKER.credentials().username());
        DICTIONARY.put("user.phoneNumber", () -> FAKER.phoneNumber().cellPhone());
        DICTIONARY.put("user.avatar", () -> FAKER.avatar().image());

        // address
        DICTIONARY.put("address.full", () -> FAKER.address().fullAddress());
        DICTIONARY.put("address.city", () -> FAKER.address().city());
        DICTIONARY.put("address.street", () -> FAKER.address().streetAddress());
        DICTIONARY.put("address.zipCode", () -> FAKER.address().zipCode());
        DICTIONARY.put("address.country", () -> FAKER.address().country());
        DICTIONARY.put("address.countryCode", () -> FAKER.address().countryCode());

        // content
        DICTIONARY.put("content.char", () -> String.valueOf(FAKER.lorem().character()));
        DICTIONARY.put("content.word", () -> FAKER.lorem().word());
        DICTIONARY.put("content.sentence", () -> FAKER.lorem().sentence());
        DICTIONARY.put("content.paragraph", () -> FAKER.lorem().paragraph());

        // system
        DICTIONARY.put("system.int", () -> String.valueOf(FAKER.random().nextInt(0, 10000)));
        DICTIONARY.put("system.long", () -> String.valueOf(FAKER.random().nextLong()));
        DICTIONARY.put("system.double", () -> String.valueOf(FAKER.random().nextDouble()));
        DICTIONARY.put("system.bool", () -> String.valueOf(FAKER.random().nextBoolean()));
        DICTIONARY.put("system.uuid", () -> UUID.randomUUID().toString());
    }
}
