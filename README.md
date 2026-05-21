# JSON Fields

A library for filtering JSON response fields based on a `fields` query parameter. Supports both Spring WebMVC and Spring WebFlux with Spring Boot auto-configuration.

## Usage

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

Spring Boot auto-configuration registers everything automatically. No `@Bean` declarations needed.

## Configuration

```yaml
spring:
  json-fields:
    enabled: true          # set to false to disable filtering
    parameter-name: fields # customize the query parameter name
```

## Fields Expression Syntax

Whitelist fields:
```
GET /api/users?fields=(id,name)
```

Blacklist fields:
```
GET /api/users?fields=!(password,secret)
```

Nested selection:
```
GET /api/users?fields=(id,address(city,country))
```

Nested blacklist:
```
GET /api/users?fields=(id,profile!(internal_notes))
```

## Modules

| Module | Description |
|--------|-------------|
| `jsonfields-grammar` | ANTLR4 grammar definition ([JsonFields.g4](grammar/src/main/antlr4/JsonFields.g4)) |
| `jsonfields-core` | Parser and `FieldPredicate` model (framework-agnostic) |
| `jsonfields-jackson` | Jackson `Module` and `FilterProvider` (framework-agnostic) |
| `jsonfields-webmvc` | Servlet filter + Spring Boot auto-configuration |
| `jsonfields-webflux` | WebFilter + Reactor context-propagation + Spring Boot auto-configuration |

## Requirements

- Java 17+
- Spring Boot 3.2+

## License

[Apache License 2.0](LICENSE)
