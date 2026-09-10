package com.platform.ecom.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Mechanical enforcement of the module dependency graph decided in Phase 2.
 *
 * Each rule below is deliberately written out explicitly, one per module,
 * rather than generated from a shared map/loop. This is a conscious
 * exception to "don't repeat yourself": this file IS the architecture
 * contract, changes to it should be rare and reviewed line-by-line, and a
 * failing rule should tell you immediately and unambiguously which module
 * boundary was crossed — a clever generic loop would obscure exactly that.
 *
 * NOTE: as of Phase 3, every module contains only a package-info.java —
 * these rules currently pass vacuously (there's nothing to violate yet).
 * They start doing real work the moment Phase 4 adds actual classes. That's
 * intentional: the fence goes up before there's anything to fence in.
 */
@AnalyzeClasses(
        packages = "com.platform.ecom",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ModuleBoundaryTest {

    private static final String BASE = "com.platform.ecom";

    @ArchTest
    static final ArchRule shared_must_not_depend_on_any_business_module =
            noClasses().that().resideInAPackage(BASE + ".shared..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".identity..", BASE + ".supplier..", BASE + ".catalog..",
                            BASE + ".listing..", BASE + ".ai..", BASE + ".marketplace..",
                            BASE + ".experiment.."
                    )
                    .because("shared is the dependency-free foundation kernel (Phase 2, module doc: shared)");

    @ArchTest
    static final ArchRule identity_must_only_depend_on_shared =
            noClasses().that().resideInAPackage(BASE + ".identity..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".supplier..", BASE + ".catalog..", BASE + ".listing..",
                            BASE + ".ai..", BASE + ".marketplace..", BASE + ".experiment.."
                    )
                    .because("identity is the foundation layer; everyone depends on it, it depends on no business module (Phase 2)");

    @ArchTest
    static final ArchRule supplier_must_not_depend_on_downstream_modules =
            noClasses().that().resideInAPackage(BASE + ".supplier..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".catalog..", BASE + ".listing..", BASE + ".ai..",
                            BASE + ".marketplace..", BASE + ".experiment.."
                    )
                    .because("supplier feeds catalog, never the reverse (Phase 2 context map)");

    @ArchTest
    static final ArchRule catalog_must_not_depend_on_downstream_modules =
            noClasses().that().resideInAPackage(BASE + ".catalog..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".listing..", BASE + ".ai..", BASE + ".marketplace..",
                            BASE + ".experiment.."
                    )
                    .because("catalog feeds listing, never the reverse (Phase 2 context map)");

    @ArchTest
    static final ArchRule ai_must_stay_business_agnostic =
            noClasses().that().resideInAPackage(BASE + ".ai..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".identity..", BASE + ".supplier..", BASE + ".catalog..",
                            BASE + ".listing..", BASE + ".marketplace..", BASE + ".experiment.."
                    )
                    .because("ai must know nothing about business domains — it exposes a generic generation port (Phase 2, ADR-004)");

    @ArchTest
    static final ArchRule marketplace_must_stay_business_agnostic =
            noClasses().that().resideInAPackage(BASE + ".marketplace..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".supplier..", BASE + ".catalog..", BASE + ".listing..",
                            BASE + ".ai..", BASE + ".experiment.."
                    )
                    .because("listing depends on marketplace's port, never the reverse (Phase 2, module doc: marketplace)");

    @ArchTest
    static final ArchRule listing_must_not_depend_on_supplier_or_experiment =
            noClasses().that().resideInAPackage(BASE + ".listing..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".supplier..", BASE + ".experiment.."
                    )
                    .because("listing depends on catalog/ai/marketplace only, not raw supplier data, and never on experiment (Phase 2)");

    @ArchTest
    static final ArchRule experiment_must_not_depend_on_unrelated_modules =
            noClasses().that().resideInAPackage(BASE + ".experiment..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".supplier..", BASE + ".catalog..", BASE + ".ai..",
                            BASE + ".marketplace.."
                    )
                    .because("experiment depends on listing only (via shared/identity); nothing depends on experiment (Phase 2)");

    /**
     * Complementary check: even where a dependency is allowed above, no two
     * modules may depend on each other in both directions. A cycle would
     * mean the "module" boundary is fictional regardless of what the rules
     * above say.
     */
    @ArchTest
    static final ArchRule modules_must_be_free_of_cycles =
            slices().matching(BASE + ".(*)..")
                    .should().beFreeOfCycles();
}