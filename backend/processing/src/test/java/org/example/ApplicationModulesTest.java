package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationModulesTest {

    private final ApplicationModules modules = ApplicationModules.of(ProcessingApplication.class);

    @Test
    void verifyModules() {
        modules.verify();
    }

    @Test
    void printModules() {
        System.out.println("Discovered Application Modules:");
        modules.forEach(System.out::println);
    }
}
