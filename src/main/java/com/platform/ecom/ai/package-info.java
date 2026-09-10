/**
 * Module: ai
 *
 * Purpose: LLM provider abstraction and generation infrastructure
 * (PromptTemplate, GenerationRequest, GenerationResult, ModelConfig).
 *
 * CRITICAL RULE (Phase 2, ADR-004): this module knows NOTHING about
 * "listings," "products," or any other business concept. It exposes a
 * generic generate(prompt, schema) -> structuredResult port. This is what
 * keeps the AI provider swappable and keeps business modules from leaking
 * prompt/model concerns into their domain logic.
 *
 * Dependency rule: depends only on shared. Nothing in this module may
 * import from listing, catalog, or any other business module — that
 * dependency direction always points the other way (listing depends on
 * ai, never the reverse).
 */
package com.platform.ecom.ai;
