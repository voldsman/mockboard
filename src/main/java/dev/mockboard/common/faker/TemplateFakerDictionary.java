package dev.mockboard.common.faker;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.datafaker.Faker;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TemplateFakerDictionary {

    private static final Faker FAKER = new Faker(Locale.US);
    private static final Map<String, Supplier<String>> DICTIONARY = new ConcurrentHashMap<>();

    public static Map<String, Supplier<String>> getDictionary() {
        return DICTIONARY;
    }

    public static void initializeDictionary() {
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
