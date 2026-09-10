/**
 * Module: experiment
 *
 * Purpose: A/B testing of listing variants (Experiment, ExperimentVariant,
 * MetricEvent). Full design deferred to Phase 10.
 *
 * Dependency rule: depends on shared, identity, and listing (an experiment
 * is about listing versions). Nothing depends on experiment — it is a
 * downstream consumer of listing's published state, not a dependency of it.
 */
package com.platform.ecom.experiment;
