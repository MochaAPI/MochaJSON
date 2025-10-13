# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-01-11 - First Production Release

### Stable Features
- Pure stateless library design (no lifecycle management)
- Full Java 21+ and Kotlin 1.9.22+ support
- HTTP methods: GET, POST, PUT, DELETE, PATCH
- Automatic JSON parsing with Jackson and Kotlinx.serialization
- Fluent API with builder pattern
- Virtual threads support (Java 21+) with automatic fallback
- Configurable timeouts (connect, read, write)
- Simple retry mechanism (3 attempts, exponential backoff)
- Request/Response interceptors
- Security controls (localhost access control, URL validation)
- Async support (CompletableFuture, callbacks)
- Production-safe defaults

### API Guarantees
- Public API is stable for 1.x.x series
- No breaking changes in minor/patch releases
- Semantic versioning strictly followed

### Dependencies
- Jackson Databind 2.17.2 (JSON for Java)
- Kotlinx Serialization 1.7.2 (JSON for Kotlin)
- SLF4J 2.0.9 (optional logging)

### Tested Against
- Java 21
- Kotlin 1.9.22
- Ubuntu, Windows, macOS

### Namespace
- Group ID: `io.github.mochaapi`
- Repository: https://github.com/MochaAPI/MochaJSON
