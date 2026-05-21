# JSON Fields

A library for filtering JSON response fields based on a `fields` query parameter. Supports both Spring WebMVC and Spring WebFlux with Spring Boot auto-configuration.

## Quick Start

Add the dependency for your framework:

**Spring WebMVC (Servlet)**
```xml
<dependency>
    <groupId>net.niebes</groupId>
    <artifactId>jsonfields-webmvc</artifactId>
    <version>0.5.4-SNAPSHOT</version>
</dependency>
```

**Spring WebFlux (Reactive)**
```xml
<dependency>
    <groupId>net.niebes</groupId>
    <artifactId>jsonfields-webflux</artifactId>
    <version>0.5.4-SNAPSHOT</version>
</dependency>
```

That's it. Spring Boot auto-configuration registers everything automatically — no `@Bean` declarations needed. Any endpoint that returns JSON will support the `?fields=` query parameter.

## How It Works

Given a REST endpoint:

```kotlin
@RestController
class UserController {
    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: Long): User = userService.findById(id)
}
```

Clients can request only the fields they need:

```
GET /users/1?fields=(id,name,email)
```

Response (only selected fields):
```json
{"id": 1, "name": "Alice", "email": "alice@example.com"}
```

Instead of the full response:
```json
{"id": 1, "name": "Alice", "email": "alice@example.com", "address": {"street": "...", "city": "...", "country": "..."}, "createdAt": "..."}
```

### Auto-Configuration

Both `jsonfields-webmvc` and `jsonfields-webflux` include Spring Boot auto-configuration that:

1. Registers a servlet `Filter` (WebMVC) or `WebFilter` (WebFlux) that parses the `?fields=` parameter
2. Registers a Jackson `Module` that filters serialized fields based on the parsed expression
3. Configures everything with sensible defaults — works out of the box

To customize behavior or override beans, define your own:

```kotlin
@Bean
fun jsonFieldsFilter(): JsonFieldsFilter = JsonFieldsFilter(paramName = "select")
```

The auto-configuration backs off when it detects your custom bean (`@ConditionalOnMissingBean`).

## Configuration

```yaml
spring:
  json-fields:
    enabled: true          # set to false to disable filtering entirely
    parameter-name: fields # customize the query parameter name
```

## Fields Expression Syntax

**Whitelist** — include only specified fields:
```
GET /api/users?fields=(id,name)
```

**Blacklist** — exclude specified fields:
```
GET /api/users?fields=!(password,secret)
```

**Nested selection** — select fields within nested objects:
```
GET /api/users?fields=(id,address(city,country))
```

**Nested blacklist** — exclude specific nested fields:
```
GET /api/users?fields=(id,profile!(internal_notes))
```

**Combined** — mix whitelist and nested expressions:
```
GET /api/users?fields=(id,name,address(city),profile!(age))
```

## Modules

| Module | Description |
|--------|-------------|
| `jsonfields-grammar` | ANTLR4 grammar definition ([JsonFields.g4](grammar/src/main/antlr4/JsonFields.g4)) |
| `jsonfields-core` | Parser and `FieldPredicate` model (framework-agnostic) |
| `jsonfields-jackson` | Jackson `Module` and `FilterProvider` (framework-agnostic) |
| `jsonfields-webmvc` | Servlet filter + Spring Boot auto-configuration |
| `jsonfields-webflux` | WebFilter + Reactor context-propagation + Spring Boot auto-configuration |

For most users, only `jsonfields-webmvc` or `jsonfields-webflux` is needed — they transitively pull in `jsonfields-jackson` and `jsonfields-core`.

## Requirements

- Java 17+
- Spring Boot 3.2+

## License

[Apache License 2.0](LICENSE)
