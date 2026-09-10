# ecom-platform

AI-powered ecommerce operations & listing optimization platform — modular
monolith (Java 21 / Spring Boot / Gradle).

## Module structure

Logical (package-based) module boundaries, enforced by an ArchUnit test
(added in Phase 3, Step 4) rather than physical Gradle multi-module
separation — see ADR-001 and the Phase 3 discussion of why.

```
com.platform.ecom
├── shared        (no dependencies — value objects, base types)
├── identity      (foundation: auth, tenancy)
├── supplier      (raw external data ingestion)
├── catalog       (canonical product truth)
├── listing       (CORE DOMAIN: generation, validation, lifecycle)
├── ai            (LLM infra, business-agnostic port)
├── marketplace   (publish adapters, business-agnostic port)
└── experiment    (A/B testing of listing versions)
```

Each module's `package-info.java` states its purpose and allowed
dependencies — read those before adding a new cross-module import.

## Running locally

This project targets Java 21 and Maven (Spring Boot's
`spring-boot-starter-parent` as the parent POM, handling dependency
version alignment for the Spring-managed libraries; ArchUnit and
Testcontainers are pinned independently since they're outside that BOM).

```bash
mvn clean install
```

This sandbox's network access doesn't include Maven Central, so this
command hasn't been run here — run it locally to confirm it resolves and
builds cleanly.

Docker Compose infrastructure (PostgreSQL, Kafka, Redis) is added in
Phase 3, Step 2 — required before running the application, not required
to compile it.
