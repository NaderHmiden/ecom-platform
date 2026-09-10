package com.platform.ecom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Composition root for the modular monolith.
 *
 * This class deliberately contains no logic. Its only job is to live at the
 * package root (com.platform.ecom) so Spring's component scan covers every
 * module below it (identity, catalog, listing, ...) without each module
 * needing its own scan configuration.
 */
@SpringBootApplication
public class PlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }
}
