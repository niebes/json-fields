# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test

```bash
# Build and run all tests (CI command)
./mvnw verify --no-transfer-progress

# Build without tests
./mvnw package -DskipTests

# Run tests for a single module
./mvnw test -pl grammar
./mvnw test -pl java
./mvnw test -pl jackson
./mvnw test -pl webmvc
./mvnw test -pl webflux

# Run a single test class
./mvnw test -pl java -Dtest=ParserFrameworkTest
./mvnw test -pl jackson -Dtest=JsonFieldsModuleTest

# Build with coverage report (java and jackson modules only)
./mvnw verify -Pcoverage
```

Java 17 target, Kotlin 2.1. Uses Maven wrapper (`./mvnw`). No linter configured.

## Architecture

Multi-module Maven library (`org.zalando.guild.api:json-fields`) that parses a `fields` query parameter into predicates for filtering JSON serialization output.

```
grammar  → ANTLR4 grammar definition
java     → Parser + FieldPredicate model (framework-agnostic)
jackson  → Jackson Module + FilterProvider (framework-agnostic)
webmvc   → Servlet filter + Spring Boot auto-configuration
webflux  → WebFilter + Reactor context-propagation + Spring Boot auto-configuration
```

**grammar** → ANTLR4 grammar (`JsonFields.g4`) defining the fields expression syntax. Ships the `.g4` file as its artifact; the ANTLR-generated parser is only in test scope here.

**java** → Unpacks the grammar artifact and generates the ANTLR parser into `target/generated-sources/antlr`. Contains the core domain model: `FieldPredicate` (a `Predicate<List<String>>` where the list represents a nested field path like `["foo", "bar"]`), `FieldPredicates` (combinators: `and`, `or`, `not`, `matchIndex`, `depthLessThan`), and `ParserFramework` (entry point that parses expression strings into `FieldPredicate`s via `FieldPredicateVisitor`).

**jackson** → Framework-agnostic Jackson integration. `JsonFieldsModule` is a Jackson `SimpleModule` that takes a `Supplier<FieldPredicate>` and registers a `JsonFieldsFilterProvider`. During serialization, the filter provider intercepts every property write, builds the current field path via an internal ThreadLocal context stack, and tests it against the active predicate. No servlet or Spring dependencies.

**webmvc** → Servlet integration. `JsonFieldsFilter` implements both `jakarta.servlet.Filter` and `Supplier<FieldPredicate>` — it parses the `?fields=` query parameter, stores the predicate in a ThreadLocal, and cleans up after the request. `JsonFieldsWebMvcAutoConfiguration` auto-registers the filter and module beans in Spring Boot. Integration tests use `TestRestTemplate`.

**webflux** → Reactive integration. `JsonFieldsWebFilter` implements both `WebFilter` and `Supplier<FieldPredicate>`. Stores the predicate in both a ThreadLocal (for Jackson serialization) and Reactor Context (for propagation). `JsonFieldsThreadLocalAccessor` bridges Reactor Context to ThreadLocal via `io.micrometer:context-propagation`. `JsonFieldsWebFluxAutoConfiguration` auto-registers the filter and module beans. Integration tests use `WebTestClient` with `Mono<T>` endpoints.

## Usage

Add `json-fields-webmvc` (servlet) or `json-fields-webflux` (reactive) as a dependency. Spring Boot auto-configuration registers everything — no `@Bean` declarations needed. Requests with `?fields=(id,name)` automatically filter the JSON response.

## Language & Conventions

Source is Kotlin (production) and Java (tests in grammar/java modules, some tests in jackson). The grammar and java modules use `src/main/java/` for Kotlin sources. The webmvc and webflux modules use `src/main/kotlin/`. JVM target is 17.

## Fields Expression Syntax

Whitelist: `(field1,field2)` — include only these fields.
Blacklist: `!(field1,field2)` — exclude these fields.
Nested: `(field1(sub1,sub2),field2)` — field1 includes only sub1 and sub2.
Mixed: `(field1!(sub1),field2)` — field1 excludes sub1, all other sub-fields of field1 are included.
