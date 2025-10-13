# MochaJSON

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mochaapi/MochaJSON.svg)](https://search.maven.org/artifact/io.github.mochaapi/MochaJSON)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/MochaAPI/MochaJSON)
[![MIT License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://openjdk.java.net/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22+-purple.svg)](https://kotlinlang.org/)
[![GitHub Sponsors](https://img.shields.io/github/sponsors/MochaAPI?color=ea4aaa)](https://github.com/sponsors/MochaAPI)

**Production-ready HTTP + JSON client for Java 21+ and Kotlin**

A pure, stateless library that eliminates boilerplate while providing automatic JSON parsing, virtual threads support, and production-safe defaults.

## Quick Start

```java
// Installation
implementation("io.github.mochaapi:MochaJSON:1.0.0")

// Usage - that's it!
Map<String, Object> data = Api.get("https://api.example.com/users/1")
    .execute()
    .toMap();
```

## Why MochaJSON?

**Problem Statement:**
- Tired of OkHttp + Gson boilerplate?
- Need simple HTTP requests without framework overhead?
- Want production-ready stability guarantees?

**Solution:**
- One import, zero configuration
- Automatic JSON parsing
- Production-safe defaults
- API stability guarantees

## Key Features

- ✅ **Zero Boilerplate** - One line API calls
- ✅ **Production Ready** - Stability guarantees for 1.x.x
- ✅ **Pure Library** - No lifecycle management
- ✅ **Java 21+ & Kotlin** - Full support for both
- ✅ **Virtual Threads** - Built-in async support
- ✅ **Type Safe** - Automatic POJO mapping
- ✅ **Interceptors** - Request/Response hooks
- ✅ **Retry Logic** - Exponential backoff
- ✅ **Security** - URL validation, localhost control
- ✅ **Lightweight** - Minimal dependencies

## Installation

**Gradle (Kotlin DSL):**
```kotlin
dependencies {
    implementation("io.github.mochaapi:MochaJSON:1.0.0")
}
```

**Gradle (Groovy):**
```groovy
dependencies {
    implementation 'io.github.mochaapi:MochaJSON:1.0.0'
}
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.mochaapi</groupId>
    <artifactId>MochaJSON</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Getting Started

**Java Example:**
```java
import io.mochaapi.client.*;

public class QuickStart {
    public static void main(String[] args) {
        // Simple GET request
        Map<String, Object> user = Api.get("https://jsonplaceholder.typicode.com/users/1")
                           .execute()
            .toMap();
        
        System.out.println("Name: " + user.get("name"));
        
        // POST request with body
        ApiResponse response = Api.post("https://jsonplaceholder.typicode.com/posts")
            .body(Map.of("title", "Hello", "body", "World", "userId", 1))
            .execute();
        
        System.out.println("Status: " + response.code());
    }
}
```

**Kotlin Example:**
```kotlin
import io.mochaapi.client.*

fun main() {
    // Simple GET request
    val user = Api.get("https://jsonplaceholder.typicode.com/users/1")
        .execute()
        .toMap()
    
    println("Name: ${user["name"]}")
    
    // POST with data class
    data class Post(val title: String, val body: String, val userId: Int)
    
    val response = Api.post("https://jsonplaceholder.typicode.com/posts")
        .body(Post("Hello", "World", 1))
        .execute()
    
    println("Status: ${response.code()}")
}
```

## Core Capabilities

**Production-Ready Features:**

### Stateless Design
```java
// No initialization required
// No shutdown needed
// Each client is independent
ApiClient client1 = new ApiClient.Builder().build();
ApiClient client2 = new ApiClient.Builder().build();
// Use them independently - no conflicts!
```

### Type-Safe POJOs
```java
public class User {
    public int id;
    public String name;
    public String email;
}

User user = Api.get("https://api.example.com/users/1")
    .execute()
    .to(User.class);
```

### Async Operations
```java
// CompletableFuture
CompletableFuture<ApiResponse> future = Api.get(url).executeAsync();

// Callbacks
Api.get(url).executeAsync(response -> {
    System.out.println("Got: " + response.code());
    });
```

### Advanced Configuration
```java
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .enableRetry()  // 3 attempts with exponential backoff
    .allowLocalhost(true)  // For development
    .requestInterceptor(req -> {
        req.header("Authorization", "Bearer " + token);
        return req;
    })
    .build();
```

## Documentation

- 📖 **[Full Documentation](https://mochaapi.github.io/MochaJSON/)** - Complete guide
- 🚀 **[Getting Started](https://mochaapi.github.io/MochaJSON/getting-started)** - Quick setup
- 📚 **[Java Examples](https://mochaapi.github.io/MochaJSON/usage/java-examples)** - Java code samples
- 📚 **[Kotlin Examples](https://mochaapi.github.io/MochaJSON/usage/kotlin-examples)** - Kotlin code samples
- 🔧 **[API Reference](https://mochaapi.github.io/MochaJSON/api/api-reference)** - Complete API docs
- 🔒 **[Security](SECURITY.md)** - Security policy
- 📋 **[Changelog](CHANGELOG.md)** - Version history

## 💖 Sponsor This Project

MochaJSON is open source and free to use. If it helps your project, consider sponsoring to support ongoing development and maintenance.

**Why Sponsor?**
- ✅ Ensure long-term maintenance and updates
- ✅ Priority support for issues and feature requests
- ✅ Influence roadmap and feature priorities
- ✅ Recognition in README and documentation
- ✅ Support open source software


**[Become a Sponsor on GitHub Sponsors](https://github.com/sponsors/MochaAPI)**

**Other Ways to Support:**
- ⭐ Star the repository
- 🐛 Report bugs and suggest features
- 📝 Contribute code or documentation
- 💬 Share on social media
- 📢 Write a blog post or tutorial

### 🏢 Corporate Sponsorship

For enterprise support, custom features, or bulk licensing, contact us at:
- 📧 Email: sponsors@mochaapi.org
- 💼 GitHub: [Enterprise Contact](https://github.com/MochaAPI)

## Comparison with Alternatives

| Feature | MochaJSON | OkHttp + Gson | Retrofit |
|---------|-----------|---------------|----------|
| Setup Lines | 1 | 20+ | 30+ |
| Dependencies | 1 | 3+ | 4+ |
| JSON Parsing | Automatic | Manual | Converter setup |
| Kotlin Support | Native | Limited | Requires setup |
| Virtual Threads | ✅ Built-in | ❌ Manual | ❌ Manual |
| Stateless | ✅ Yes | ⚠️ Shared | ⚠️ Shared |
| Retry Logic | ✅ Built-in | ❌ Manual | ❌ Manual |
| API Stability | ✅ Guaranteed | ⚠️ Breaking changes | ⚠️ Breaking changes |

## Requirements

- **Java**: 21+ (LTS recommended)
- **Kotlin**: 1.9.22+ (optional, for Kotlin projects)
- **Dependencies**: Minimal
  - Jackson 2.17.2 (JSON for Java)
  - Kotlinx Serialization 1.7.2 (JSON for Kotlin)
  - SLF4J 2.0.9 (optional logging)

## API Stability Guarantee

MochaJSON v1.0.0 provides strict API stability guarantees:

✅ **No breaking changes in 1.x.x releases**
✅ **Semantic versioning strictly followed**
✅ **Deprecation warnings 6+ months before removal**
✅ **Long-term support commitment**

## Community & Contributing

**Get Involved:**
- 🐛 [Report Issues](https://github.com/MochaAPI/MochaJSON/issues)
- 💡 [Request Features](https://github.com/MochaAPI/MochaJSON/issues/new)
- 🤝 [Contribute Code](CONTRIBUTING.md)
- 💬 [Join Discussions](https://github.com/MochaAPI/MochaJSON/discussions)
- 📖 [Read Contributing Guide](CONTRIBUTING.md)

**Code of Conduct:**
We follow the [Contributor Covenant](https://www.contributor-covenant.org/) to ensure a welcoming community.

## License

MochaJSON is released under the [MIT License](LICENSE).

```
MIT License - Free to use, modify, and distribute
✅ Commercial use allowed
✅ Modification allowed
✅ Distribution allowed
✅ Private use allowed
```

## Acknowledgments

**Built With:**
- Java 21 Virtual Threads
- Jackson for JSON processing
- Kotlinx Serialization for Kotlin
- Love for clean, simple APIs

**Inspired By:**
- The need for simpler HTTP clients
- Community feedback and requests
- Production use cases and requirements

## Contact & Support

**Need Help?**
- 📖 [Documentation](https://mochaapi.github.io/MochaJSON/)
- 💬 [GitHub Discussions](https://github.com/MochaAPI/MochaJSON/discussions)
- 🐛 [Issue Tracker](https://github.com/MochaAPI/MochaJSON/issues)
- 📧 Email: support@mochaapi.org

**Follow Updates:**
- 🌟 Star on GitHub
- 👀 Watch repository for releases
- 📢 Follow @MochaAPI (if available)

---

**Made with ❤️ by the MochaAPI Team**

**Star ⭐ this repository if you find it useful!**