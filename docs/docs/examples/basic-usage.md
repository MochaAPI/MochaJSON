---
title: Basic Usage Examples
description: Dead simple examples showing MochaJSON v1.0.0's stateless design with no setup required.
keywords:
  - basic usage
  - simple examples
  - stateless design
  - no setup
---

# Basic Usage Examples

Dead simple examples showing MochaJSON v1.0.0's stateless design with no setup required.

## Hello World

```java
import io.mochaapi.client.*;

public class HelloWorld {
    public static void main(String[] args) {
        try {
            // Just use it - no setup needed
            Map<String, Object> data = Api.get("https://jsonplaceholder.typicode.com/users/1")
                .execute()
                .toMap();
            
            System.out.println("Hello, " + data.get("name") + "!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

## Simple GET Request

```java
import io.mochaapi.client.*;

public class SimpleGet {
    public static void main(String[] args) {
        try {
            // GET request with automatic JSON parsing
            Map<String, Object> user = Api.get("https://jsonplaceholder.typicode.com/users/1")
                .execute()
                .toMap();
            
            System.out.println("User ID: " + user.get("id"));
            System.out.println("Name: " + user.get("name"));
            System.out.println("Email: " + user.get("email"));
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

## Simple POST Request

```java
import io.mochaapi.client.*;
import java.util.Map;

public class SimplePost {
    public static void main(String[] args) {
        try {
            // POST request with JSON body
            Map<String, Object> newPost = Map.of(
                "title", "My Post",
                "body", "This is the content of my post",
                "userId", 1
            );
            
            Map<String, Object> result = Api.post("https://jsonplaceholder.typicode.com/posts")
                .body(newPost)
                .execute()
                .toMap();
            
            System.out.println("Created post with ID: " + result.get("id"));
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

## With POJO Classes

```java
import io.mochaapi.client.*;

public class PojoExample {
    public static void main(String[] args) {
        try {
            // GET request → POJO
            User user = Api.get("https://jsonplaceholder.typicode.com/users/1")
                .execute()
                .to(User.class);
            
            System.out.println("User: " + user.name);
            System.out.println("Email: " + user.email);
            System.out.println("Website: " + user.website);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    public static class User {
        public int id;
        public String name;
        public String email;
        public String website;
        
        public User() {}
    }
}
```

## Error Handling

```java
import io.mochaapi.client.*;

public class ErrorHandling {
    public static void main(String[] args) {
        try {
            Map<String, Object> data = Api.get("https://jsonplaceholder.typicode.com/users/1")
                .execute()
                .toMap();
            
            System.out.println("Success: " + data.get("name"));
            
        } catch (ApiException e) {
            System.err.println("API Error: " + e.getMessage());
            System.err.println("Status Code: " + e.getStatusCode());
        } catch (JsonException e) {
            System.err.println("JSON Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
        }
    }
}
```

## Kotlin Examples

### Basic Kotlin

```kotlin
import io.mochaapi.client.*

fun main() {
    try {
        val data = Api.get("https://jsonplaceholder.typicode.com/users/1")
            .execute()
            .toMap()
        
        println("Hello, ${data["name"]}!")
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
```

### Kotlin with Data Classes

```kotlin
import io.mochaapi.client.*

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val website: String
)

fun main() {
    try {
        val user = Api.get("https://jsonplaceholder.typicode.com/users/1")
            .execute()
            .to(User::class.java)
        
        println("User: ${user.name}")
        println("Email: ${user.email}")
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
```

### Kotlin Extension Functions

```kotlin
import io.mochaapi.client.*

// Extension function for cleaner syntax
fun <T> ApiRequest.fetchAs(clazz: Class<T>): T = 
    execute().to(clazz)

fun main() {
    try {
        val user = Api.get("https://jsonplaceholder.typicode.com/users/1")
            .fetchAs(User::class.java)
        
        println("User: ${user.name}")
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}

data class User(
    val id: Int,
    val name: String,
    val email: String
)
```

## Key Points

- ✅ **No Setup Required** - Just import and use
- ✅ **No Shutdown Needed** - Library is stateless
- ✅ **Automatic JSON Parsing** - No manual parsing required
- ✅ **Simple Error Handling** - Clear exception types
- ✅ **Works with POJOs** - Automatic object mapping
- ✅ **Kotlin Friendly** - Great Kotlin support with data classes
