package com.netby.evaluacion.arquitectura;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Entity;
import jakarta.transaction.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * CA-08 verificable: estas reglas fallan la compilación si alguien rompe la arquitectura por capas
 * (Resource -> Service -> Repository) o las convenciones del proyecto.
 */
@AnalyzeClasses(packages = "com.netby.evaluacion", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    private static final String RESOURCE = "..resource..";
    private static final String SERVICE = "..service..";
    private static final String REPOSITORY = "..repository..";
    private static final String ENTITY = "..entity..";

    @ArchTest
    static final ArchRule capas = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Resource").definedBy(RESOURCE)
            .layer("Service").definedBy(SERVICE)
            .layer("Repository").definedBy(REPOSITORY)
            .layer("Entity").definedBy(ENTITY)
            .whereLayer("Resource").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Resource")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
            .because("los controladores solo delegan en servicios y solo los servicios acceden a datos");

    @ArchTest
    static final ArchRule losResourcesNoExponenEntidades = noClasses()
            .that().resideInAPackage(RESOURCE)
            .should().dependOnClassesThat().areAnnotatedWith(Entity.class)
            .because("la API trabaja con DTOs; las entidades JPA no salen de la capa de servicio");

    @ArchTest
    static final ArchRule laLogicaDeNegocioNoConoceHttp = noClasses()
            .that().resideInAnyPackage(SERVICE, REPOSITORY, ENTITY)
            .should().dependOnClassesThat().resideInAPackage("jakarta.ws.rs..")
            .because("las reglas de negocio no dependen del protocolo de transporte");

    @ArchTest
    static final ArchRule lasTransaccionesSeDefinenEnLosServicios = noMethods()
            .that().areDeclaredInClassesThat().resideOutsideOfPackage(SERVICE)
            .should().beAnnotatedWith(Transactional.class)
            .because("el límite transaccional pertenece a la capa de servicio");

    @ArchTest
    static final ArchRule lasEntidadesVivenEnSuPaquete = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage(ENTITY);

    @ArchTest
    static final ArchRule losRepositoriosSiguenLaConvencion = classes()
            .that().resideInAPackage(REPOSITORY).and().areAnnotatedWith(ApplicationScoped.class)
            .should().haveSimpleNameEndingWith("Repository");

    @ArchTest
    static final ArchRule losResourcesImplementanSuContrato = classes()
            .that().resideInAPackage("com.netby.evaluacion.resource")
            .and().haveSimpleNameEndingWith("Resource")
            .should().implement(com.tngtech.archunit.base.DescribedPredicate.describe(
                    "una interfaz de contrato *Api",
                    tipo -> tipo.getSimpleName().endsWith("Api")));

    @ArchTest
    static final ArchRule sinCiclosEntrePaquetes = slices()
            .matching("com.netby.evaluacion.(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule inyeccionSoloPorConstructor = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    static final ArchRule sinSystemOutNiPrintStackTrace = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    static final ArchRule sinJavaUtilLogging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    @ArchTest
    static final ArchRule sinExcepcionesGenericas = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
}
