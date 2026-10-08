# 🛒 AI-Powered Ecommerce Operations & Listing Optimization Platform

> A production-grade, multi-tenant SaaS backend that takes resellers from a messy supplier CSV to a live, high-converting, continuously-improving marketplace listing — with AI doing the heavy lifting on language and a deterministic system guaranteeing correctness, safety, and auditability.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Module Structure](#module-structure)
- [Core Features](#core-features)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
- [Getting Started](#getting-started)
- [Running Tests](#running-tests)
- [Design Decisions](#design-decisions)
- [Project Roadmap](#project-roadmap)

---

## Overview

Resellers who source from suppliers face a chain of high-friction problems:

- Supplier data is inconsistent, incomplete, and non-standard
- Writing marketplace-compliant listing copy at scale is slow and repetitive
- Nobody systematically knows *which* listings are underperforming or *why*
- Listings are disconnected from inventory reality, order fulfillment, and logistics

This platform solves these problems through a **modular monolith** that implements the full product lifecycle:

```
Supplier CSV → Ingestion → Catalog → AI Enrichment → Listing → Review → Publish
                                                                    ↓
                                              A/B Experiment ← Analytics ← Orders/Inventory
```

The core architectural insight: **listing generation is a pipeline with a probabilistic step in the middle, surrounded by deterministic guarantees.**

```
Deterministic Validation → AI Generation → Deterministic Validation → Human Review → Publish
```

AI is one subsystem among several — not the product.

---

## Architecture

### Modular Monolith with DDD

The system is a **modular monolith** (not microservices) with strict Domain-Driven Design boundaries. Each bounded context is a self-contained Java package with its own:

- `domain/` — pure domain model (entities, value objects, domain services) — zero framework dependencies
- `application/` — use cases, orchestration, `@Transactional` services
- `infrastructure/` — JPA repositories, external adapters, Spring beans
- `api/` — REST controllers, DTOs, request/response mapping

Inter-module dependencies are enforced automatically at build time by **ArchUnit** (9 rules, 0 violations).

### Dependency Rules

```
shared  → (no business module)
ai      → shared only
catalog → shared, ai
listing → shared, catalog, ai
marketplace → shared, listing
experiment  → shared, listing
order       → shared, catalog
inventory   → shared, catalog
fulfillment → shared, inventory, order
identity    → shared
supplier    → shared, catalog
```

### Multi-Tenancy

Every database table has a `tenant_id` column. A Hibernate `@FilterDef` (`tenantFilter`) is enabled automatically before every repository call via an **AOP aspect** (`TenantFilterAspect`). No data leaks between tenants — enforced at the ORM layer, not just the service layer.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Security | Spring Security · JJWT 0.12.6 (stateless JWT) |
| Persistence | Spring Data JPA · Hibernate 6 · PostgreSQL |
| Migrations | Flyway (V1 → V9) |
| Messaging | Apache Kafka |
| Cache | Redis |
| CSV Processing | Apache Commons CSV |
| Architecture Tests | ArchUnit 1.3.0 |
| Integration Tests | Testcontainers 1.20.1 |
| Unit Tests | JUnit 5 · Mockito |
| Build | Maven 3 |
| JDK | OpenJDK 21 |

---

## Module Structure

```
com.platform.ecom
├── shared/                   # Cross-cutting: Money VO, TenantContext, AOP, exceptions
│   ├── domain/
│   │   └── Money.java        # Value object: amount + currency, immutable
│   ├── persistence/
│   │   └── TenantFilterAspect.java   # AOP: enables Hibernate tenant filter on every repo call
│   └── security/
│       └── TenantContext.java
│
├── identity/                 # Auth, JWT, user & tenant management
│   ├── domain/               # Tenant, AppUser, Role
│   ├── application/          # AuthService, UserService
│   ├── infrastructure/       # JwtService, JwtAuthenticationFilter
│   └── api/                  # AuthController
│
├── supplier/                 # CSV ingestion pipeline
│   ├── domain/               # SupplierImportJob, SupplierRow, SupplierRowValidator
│   ├── application/          # SupplierImportService, SupplierRowProcessor
│   │   └── SupplierRowTransactionOps.java  # Split bean: avoids @Transactional self-invocation
│   └── api/                  # SupplierController
│
├── catalog/                  # Normalized product catalog
│   ├── domain/               # Product, ProductVariant, Category, ProductStatus
│   ├── application/          # ProductService, CatalogEnrichmentOrchestrator
│   └── api/                  # ProductController
│
├── listing/                  # Marketplace listing lifecycle
│   ├── domain/               # Listing, ListingVersion, ListingValidationEngine, RuleSet
│   ├── application/          # ListingService, ListingTransactionOps, ListingAiGenerationService
│   └── api/                  # ListingController
│
├── ai/                       # AI generation port + adapters
│   ├── port/                 # GenerationPort (interface), GenerationRequest, GenerationResult
│   └── adapter/              # MockGenerationAdapter (deterministic stub)
│
├── experiment/               # A/B testing engine
│   ├── domain/               # Experiment, ExperimentVariant, MetricEvent
│   ├── application/          # ExperimentService
│   └── api/                  # ExperimentController
│
├── marketplace/              # Marketplace publishing
│   ├── domain/               # MarketplaceAccount, PublishJob, PublishStatus
│   ├── application/          # PublishService
│   ├── port/                 # MarketplacePort (interface)
│   ├── adapter/              # MockMarketplaceAdapter
│   └── api/                  # MarketplaceController
│
├── order/                    # Order ingestion & management
│   ├── domain/               # Order, OrderItem, OrderStatus
│   ├── application/          # OrderService
│   └── api/                  # OrderController
│
├── inventory/                # Stock management
│   ├── domain/               # StockLevel, StockReservation
│   ├── application/          # InventoryService
│   └── infrastructure/       # StockLevelRepository (pessimistic lock)
│
└── fulfillment/              # Fulfillment request processing
    ├── domain/               # FulfillmentRequest, FulfillmentStatus
    ├── application/          # FulfillmentService
    └── api/                  # FulfillmentController
```

---

## Core Features

### 🔐 Identity & Multi-Tenancy
- Stateless JWT authentication (`Authorization: Bearer <token>`)
- Per-tenant data isolation via Hibernate `@FilterDef` + AOP activation
- `401 Unauthorized` vs `400 Bad Request` semantically correct error mapping

### 📦 Supplier CSV Ingestion
- Async import with `@Async` (non-blocking HTTP response)
- Transactional row-by-row processing (`SupplierRowTransactionOps` pattern avoids Spring self-invocation trap)
- Atomic counters for success/failure per import job
- Idempotent: re-importing the same file is safe

### 🤖 AI-Powered Content Generation
- Abstract `GenerationPort` (Adapter pattern) — swappable between `MockGenerationAdapter` and any real LLM (OpenAI, Anthropic, Gemini)
- Post-generation deterministic validation via `ListingValidationEngine` + configurable `RuleSet` (character limits, required fields, banned claims)
- AI and validation are strictly decoupled — the system is correct even if the AI output is wrong

### 📋 Listing State Machine
```
DRAFT → READY_FOR_REVIEW → APPROVED → PUBLISHED
                                ↓
                           REJECTED → DRAFT
```
- Invariants enforced at the domain method level (`InvariantViolationException` if preconditions not met)
- `ListingVersion` tracks every state change with reviewer ID and timestamp
- `approve()` rejects if current state is not `READY_FOR_REVIEW` or if last validation failed

### 🧪 A/B Experimentation Engine
- `Experiment` aggregate owns `ExperimentVariant` list
- `MetricEvent` is a separate aggregate (high-volume events don't lock the parent experiment)
- Conversion rate calculation per variant
- Experiment lifecycle: `DRAFT → RUNNING → CONCLUDED`

### 🏬 Marketplace Publishing
- `MarketplacePort` / `MockMarketplaceAdapter` — pluggable per marketplace (Amazon, Shopify, eBay)
- Partial index on `publish_jobs`: `UNIQUE (listing_id) WHERE status = 'IN_PROGRESS'` — prevents concurrent duplicate publishes at the DB level
- `PublishJob` lifecycle: `PENDING → IN_PROGRESS → SUCCEEDED / FAILED`

### 📊 Inventory Management
- `StockLevel` per `(tenant_id, sku)` — pessimistic write lock (`SELECT ... FOR UPDATE`) on reservation path
- `StockReservation` tracks per-order holds
- Explicit pessimistic locking justified: high contention on stock writes, optimistic retry cost is unacceptable

### 🛍️ Order Management
- Idempotent ingestion by `externalOrderId` (unique constraint on `(tenant_id, external_order_id)`)
- Create-or-retrieve pattern: safe to ingest the same order twice (e.g. webhook replay)
- `OrderStatus`: `PENDING → CONFIRMED → SHIPPED → DELIVERED / CANCELLED`

### 🏗️ Architecture Enforcement (ArchUnit)
9 rules verified at every build:
- `ai` cannot import from `identity`, `supplier`, `catalog`, `listing`, `marketplace`, `experiment`
- `catalog` cannot import from `listing`, `ai`, `marketplace`, `experiment`
- `listing` cannot import from `supplier`, `experiment`
- `shared` cannot import from any business module

---

## Database Schema

| Migration | Tables |
|---|---|
| `V1__init.sql` | `flyway_schema_history`, base setup |
| `V2__identity.sql` | `tenants`, `app_users` |
| `V3__supplier.sql` | `supplier_import_jobs`, `supplier_rows` |
| `V4__catalog.sql` | `products`, `product_variants`, `categories` |
| `V5__listing.sql` | `listings`, `listing_versions` |
| `V6__experiment.sql` | `experiments`, `experiment_variants`, `metric_events` |
| `V7__marketplace.sql` | `marketplace_accounts`, `publish_jobs` |
| `V8__order.sql` | `orders`, `order_items` |
| `V9__inventory_fulfillment.sql` | `stock_levels`, `stock_reservations`, `fulfillment_requests` |

---

## API Reference

### Auth — `/api/v1/auth`
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/register` | Register a new tenant + admin user |
| `POST` | `/login` | Obtain a JWT token |

### Supplier — `/api/v1/suppliers`
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/imports` | Start async CSV import |
| `GET` | `/imports/{jobId}` | Poll import job status |
| `GET` | `/imports/{jobId}/rows` | List processed rows |

### Catalog — `/api/v1/catalog`
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/products` | Create product |
| `GET` | `/products` | List products (paginated) |
| `GET` | `/products/{id}` | Get product detail |
| `PUT` | `/products/{id}` | Update product |
| `DELETE` | `/products/{id}` | Delete product |
| `POST` | `/products/{id}/enrich` | Trigger AI catalog enrichment |

### Listings — `/api/v1/listings`
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/` | Create draft listing |
| `GET` | `/` | List listings |
| `GET` | `/{id}` | Get listing detail |
| `POST` | `/{id}/generate` | Trigger AI generation |
| `POST` | `/{id}/submit` | Submit for review |
| `POST` | `/{id}/approve` | Approve listing |
| `POST` | `/{id}/reject` | Reject listing |
| `GET` | `/{id}/versions` | List all versions |

### Experiments — `/api/v1/experiments`
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/` | Create experiment |
| `GET` | `/` | List experiments |
| `GET` | `/{id}` | Get experiment |
| `POST` | `/{id}/start` | Start experiment |
| `POST` | `/{id}/conclude` | Conclude experiment |
| `POST` | `/{id}/events` | Record metric event |
| `GET` | `/{id}/results` | Get conversion rates per variant |

### Marketplace — `/api/v1/marketplace`
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/accounts` | Register marketplace account |
| `GET` | `/accounts` | List connected accounts |
| `POST` | `/publish` | Publish listing to marketplace |
| `GET` | `/publish/{jobId}` | Get publish job status |

### Orders — `/api/v1/orders`
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/` | Ingest order (idempotent) |
| `GET` | `/` | List orders |
| `GET` | `/{id}` | Get order detail |
| `PUT` | `/{id}/status` | Update order status |

### Inventory — `/api/v1/inventory`
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/stock` | List stock levels |
| `PUT` | `/stock/{sku}` | Update stock level |
| `POST` | `/reserve` | Reserve stock for order |
| `POST` | `/release` | Release stock reservation |

### Fulfillment — `/api/v1/fulfillment`
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/requests` | Create fulfillment request |
| `GET` | `/requests` | List fulfillment requests |
| `GET` | `/requests/{id}` | Get fulfillment request |
| `PUT` | `/requests/{id}/status` | Update fulfillment status |

---

## Getting Started

### Prerequisites

- Java 21+
- PostgreSQL 15+
- Apache Kafka (optional for async features)
- Redis (optional, reserved for future rate-limiting/caching)
- Maven 3.9+

### 1. Clone the repository

```bash
git clone https://github.com/your-org/ecom-platform.git
cd ecom-platform
```

### 2. Configure the database

Create a PostgreSQL database:

```sql
CREATE DATABASE ecom_platform;
CREATE USER ecom_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE ecom_platform TO ecom_user;
```

### 3. Configure `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecom_platform
    username: ecom_user
    password: your_password
  flyway:
    enabled: true
    locations: classpath:db/migration

app:
  security:
    jwt:
      secret: your-256-bit-secret-key-here-minimum-32-chars
      expiration-seconds: 86400
```

### 4. Run the application

```bash
mvn spring-boot:run
```

Flyway will apply all 9 migrations automatically on startup.

### 5. Register your first tenant

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"tenantName": "MyShop", "email": "admin@myshop.com", "password": "secret"}'
```

---

## Running Tests

```bash
mvn test
```

**Expected output:**
```
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Test Coverage

| Test Class | What it tests |
|---|---|
| `ModuleBoundaryTest` | 9 ArchUnit inter-module dependency rules |
| `ListingValidationEngineTest` | Deterministic validation: character limits, required fields |
| `ListingStateMachineTest` | State machine invariants: invalid transitions throw `InvariantViolationException` |
| `ExperimentServiceTest` | Conversion rate calculation, experiment lifecycle |
| `SupplierRowValidatorTest` | CSV row validation rules |

---

## Design Decisions

### Why a modular monolith, not microservices?
At this stage, the cost of distributed systems (network failures, eventual consistency, service discovery, distributed tracing) outweighs the benefits. Module boundaries are enforced by ArchUnit — migrating a bounded context to a microservice later is a packaging change, not a redesign.

### Why pessimistic locking on stock reservations?
Stock reservation is a high-contention write path. Optimistic locking (`@Version`) would cause frequent `OptimisticLockException` → retry storms under load. A `SELECT ... FOR UPDATE` is cheaper when conflicts are expected to be frequent. All other aggregates use optimistic locking.

### Why a separate `*TransactionOps` bean?
Spring `@Transactional` works through a proxy. When a method calls another `@Transactional` method on the **same bean**, the proxy is bypassed — the inner transaction boundary is silently ignored. Splitting into a dedicated `XxxTransactionOps` bean forces all calls to go through the proxy. This pattern is used in `SupplierRowTransactionOps` and `ListingTransactionOps`.

### Why `GenerationPort` and not a direct LLM client?
Decoupling the AI provider from the domain lets us:
1. Run all tests with `MockGenerationAdapter` (no API key, no cost, deterministic)
2. Switch providers (OpenAI → Anthropic → Gemini) with zero domain code change
3. Validate LLM output deterministically after generation, regardless of which LLM produced it

### Why `MetricEvent` is a separate aggregate?
`Experiment` would become a write bottleneck if every event acquisition locked the experiment row. `MetricEvent` as an independent aggregate allows high-throughput event writes without contention on `Experiment`.

---

## Project Roadmap

| Status | Phase | Scope |
|---|---|---|
| ✅ Done | Phase 1–3 | Project setup, Maven, ArchUnit baseline |
| ✅ Done | Phase 4 | Multi-tenancy, AOP filter, identity module |
| ✅ Done | Phase 5 | Supplier CSV ingestion pipeline |
| ✅ Done | Phase 6 | Catalog domain (Product, Category, Variant) |
| ✅ Done | Phase 7 | Listing domain & state machine |
| ✅ Done | Phase 8 | AI generation port + mock adapter |
| ✅ Done | Phase 9 | Catalog enrichment orchestrator |
| ✅ Done | Phase 10 | A/B experimentation engine |
| ✅ Done | Phase 11 | Marketplace publishing |
| ✅ Done | Phase 12 | Order management |
| ✅ Done | Phase 13 | Inventory & fulfillment |
| 🔲 Next | Phase 14 | Angular standalone frontend |
| 🔲 Next | Phase 15 | Real LLM adapter (OpenAI / Gemini) |
| 🔲 Next | Phase 16 | Integration tests (Testcontainers) |
| 🔲 Next | Phase 17 | Docker Compose + deployment |

---

## License

MIT License — see [LICENSE](LICENSE) for details.

---

<div align="center">
  <sub>Built with ☕ Java 21 · Spring Boot 3.3 · Domain-Driven Design</sub>
</div>
