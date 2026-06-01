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
./mvnw test -pl core
./mvnw test -pl jackson
./mvnw test -pl webmvc
./mvnw test -pl webflux

# Run a single test class
./mvnw test -pl core -Dtest=ParserFrameworkTest
./mvnw test -pl jackson -Dtest=JsonFieldsModuleTest

# Build with coverage report (core and jackson modules only)
./mvnw verify -Pcoverage
```

Java 17 target, Kotlin 2.3. Uses Maven wrapper (`./mvnw`). No linter configured.

## Architecture

Multi-module Maven library (`net.niebes:jsonfields`) that parses a `fields` query parameter into predicates for filtering JSON serialization output.

```
grammar  → ANTLR4 grammar definition
core     → Parser + FieldPredicate model (framework-agnostic)
jackson  → Jackson Module + FilterProvider (framework-agnostic)
webmvc   → Servlet filter + Spring Boot auto-configuration
webflux  → WebFilter + Reactor context-propagation + Spring Boot auto-configuration
```

**grammar** → ANTLR4 grammar (`JsonFields.g4`) defining the fields expression syntax. Ships the `.g4` file as its artifact; the ANTLR-generated parser is only in test scope here.

**core** → Unpacks the grammar artifact and generates the ANTLR parser into `target/generated-sources/antlr`. Contains the core domain model: `FieldPredicate` (a `Predicate<List<String>>` where the list represents a nested field path like `["foo", "bar"]`), `FieldPredicates` (combinators: `and`, `or`, `not`, `matchIndex`, `depthLessThan`), and `ParserFramework` (entry point that parses expression strings into `FieldPredicate`s via `FieldPredicateVisitor`). Package: `net.niebes.jsonfields.core`.

**jackson** → Framework-agnostic Jackson integration. `JsonFieldsModule` is a Jackson `SimpleModule` that takes a `Supplier<FieldPredicate>` and registers a `JsonFieldsFilterProvider`. During serialization, the filter provider intercepts every property write, builds the current field path via an internal ThreadLocal context stack, and tests it against the active predicate. No servlet or Spring dependencies. Package: `net.niebes.jsonfields.jackson`.

**webmvc** → Servlet integration. `JsonFieldsFilter` implements both `jakarta.servlet.Filter` and `Supplier<FieldPredicate>` — it parses the `?fields=` query parameter, stores the predicate in a ThreadLocal, and cleans up after the request. `JsonFieldsWebMvcAutoConfiguration` auto-registers the filter and module beans in Spring Boot. Integration tests use `TestRestTemplate`. Package: `net.niebes.jsonfields.webmvc`.

**webflux** → Reactive integration. `JsonFieldsWebFilter` implements both `WebFilter` and `Supplier<FieldPredicate>`. Stores the predicate in both a ThreadLocal (for Jackson serialization) and Reactor Context (for propagation). `JsonFieldsThreadLocalAccessor` bridges Reactor Context to ThreadLocal via `io.micrometer:context-propagation`. `JsonFieldsWebFluxAutoConfiguration` auto-registers the filter and module beans. Integration tests use `WebTestClient` with `Mono<T>` endpoints. Package: `net.niebes.jsonfields.webflux`.

## Usage

Add `jsonfields-webmvc` (servlet) or `jsonfields-webflux` (reactive) as a dependency. Spring Boot auto-configuration registers everything — no `@Bean` declarations needed. Requests with `?fields=(id,name)` automatically filter the JSON response.

## Language & Conventions

Source is Kotlin (production) and Java (tests in grammar/core modules, some tests in jackson). The grammar and core modules use `src/main/java/` for Kotlin sources. The webmvc and webflux modules use `src/main/kotlin/`. JVM target is 17.

## Releasing to Maven Central

Publishing uses `central-publishing-maven-plugin` (Sonatype Central Portal), GPG-signed at deploy phase.

Prerequisites: GPG key `927993B7B82F199016055909E7347F989651794C` in your keyring, `<server id="central">` in `~/.m2/settings.xml` with a Sonatype Central token.

```bash
# 1. Prepare: bumps versions, commits, and tags locally (does NOT push)
./mvnw release:prepare -DreleaseVersion=X.Y.Z -DdevelopmentVersion=X.Y.W-SNAPSHOT -B

# 2. Re-tag with meaningful release notes (the plugin generates a generic message)
#    Verify the two commits: HEAD is "Prepare ...-SNAPSHOT", HEAD~1 is "Release X.Y.Z"
git log --oneline -2
git tag -d X.Y.Z
git tag -a X.Y.Z $(git log --oneline --grep="Release X.Y.Z" -1 --format=%H) -m "Release X.Y.Z

- Added foo
- Fixed bar"

# 3. Push commits and tag to remote
git push origin master --tags

# 4. Deploy the tagged release to Maven Central
./mvnw release:perform
```

If something goes wrong between steps, clean up before retrying:

```bash
# Delete remote and local tag
git push origin :refs/tags/X.Y.Z
git tag -d X.Y.Z

# Remove release plugin state
rm -f release.properties

# If release commits were pushed, revert them (master is force-push protected)
git revert HEAD HEAD~1
git push origin master
```

## Fields Expression Syntax

Whitelist: `(field1,field2)` — include only these fields.
Blacklist: `!(field1,field2)` — exclude these fields.
Nested: `(field1(sub1,sub2),field2)` — field1 includes only sub1 and sub2.
Mixed: `(field1!(sub1),field2)` — field1 excludes sub1, all other sub-fields of field1 are included.
