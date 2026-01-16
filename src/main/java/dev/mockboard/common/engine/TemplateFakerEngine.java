package dev.mockboard.common.engine;

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@Component
public class TemplateFakerEngine {

    private static final Faker FAKER = new Faker(Locale.US);
    private static final Map<String, Supplier<String>> DICTIONARY = new ConcurrentHashMap<>();
    private final StringSubstitutor substitutor;

    public TemplateFakerEngine() {
        log.info("Initializing faker dictionary...");
        initializeDictionary();
        StringLookup fakerLookup = key -> {
            var supplier = DICTIONARY.get(key);
            if (supplier != null) return supplier.get();

            // not registered template, return original for now
            return "{{" + key + "}}";
        };

        this.substitutor = new StringSubstitutor(fakerLookup);
        this.substitutor.setVariablePrefix("{{");
        this.substitutor.setVariableSuffix("}}");
        this.substitutor.setEnableSubstitutionInVariables(false);
    }

    public String applyFaker(String input) {
        if (input == null || input.isEmpty()) return input;
        return substitutor.replace(input);
    }

    private void initializeDictionary() {
        DICTIONARY.clear();

        // user
        DICTIONARY.put("user.fullName", () -> FAKER.name().fullName());
        DICTIONARY.put("user.firstName", () -> FAKER.name().firstName());
        DICTIONARY.put("user.lastName", () -> FAKER.name().lastName());
        DICTIONARY.put("user.email", () -> FAKER.internet().emailAddress());
    }
}
