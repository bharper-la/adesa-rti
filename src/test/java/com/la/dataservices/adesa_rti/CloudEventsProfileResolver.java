package com.la.dataservices.adesa_rti;

import org.springframework.test.context.ActiveProfilesResolver;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Resolves active profiles for tests:
 * - Always includes "test"
 * - Merges anything provided via system property or env var:
 *     -Dspring.profiles.active=legacy-json
 *     (or SPRING_PROFILES_ACTIVE)
 */
public class CloudEventsProfileResolver implements ActiveProfilesResolver {
    @Override
    public String[] resolve(Class<?> testClass) {
        String fromSys = System.getProperty("spring.profiles.active");
        if (fromSys == null || fromSys.isBlank()) {
            fromSys = System.getenv("SPRING_PROFILES_ACTIVE");
        }

        Set<String> profiles = new LinkedHashSet<>();
        profiles.add("test"); // ensure H2/test config is always on

        if (fromSys != null && !fromSys.isBlank()) {
            Stream.of(fromSys.split("[,\\s]+"))
                    .filter(s -> !s.isBlank())
                    .forEach(profiles::add);
        }
        return profiles.toArray(String[]::new);
    }
}
