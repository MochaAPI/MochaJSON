---
title: Production-Ready Examples
description: Production-grade patterns for MochaJSON v1.0.0 with proper error handling, timeouts, retry, and security.
keywords:
  - production ready
  - error handling
  - timeouts
  - retry
  - security
---

# Production-Ready Examples

Production-grade patterns for MochaJSON v1.0.0 with proper error handling, timeouts, retry, and security.

## Production Service Class

```java
import io.mochaapi.client.*;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class UserService {
    private final ApiClient client;
    
    public UserService() {
        this.client = new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .enableRetry()                    // 3 attempts with exponential backoff
            .requestInterceptor(req -> {
                req.header("Authorization", "Bearer " + getToken());
                req.header("User-Agent", "MyApp/1.0");
                return req;
            })
            .responseInterceptor(resp -> {
                if (resp.code() >= 400) {
                    logError("API Error", resp.code(), resp.body());
                }
                return resp;
            })
            .enableLogging()
            .build();
    }
    
    public User getUser(int id) {
        try {
            return client.get("https://api.example.com/users/" + id)
                .execute()
                .to(User.class);
        } catch (ApiException e) {
            throw new UserServiceException("Failed to get user " + id, e);
        } catch (JsonException e) {
            throw new UserServiceException("Failed to parse user data", e);
        }
    }
    
    public CompletableFuture<User> getUserAsync(int id) {
        return client.get("https://api.example.com/users/" + id)
            .executeAsync()
            .thenApply(response -> {
                try {
                    return response.to(User.class);
                } catch (JsonException e) {
                    throw new RuntimeException("Failed to parse user data", e);
                }
            })
            .exceptionally(throwable -> {
                logError("Async API Error", throwable);
                throw new RuntimeException("Failed to get user " + id, throwable);
            });
    }
    
    public User createUser(User user) {
        try {
            return client.post("https://api.example.com/users")
                .body(user)
                .execute()
                .to(User.class);
        } catch (ApiException e) {
            throw new UserServiceException("Failed to create user", e);
        } catch (JsonException e) {
            throw new UserServiceException("Failed to parse created user", e);
        }
    }
    
    private String getToken() {
        return System.getenv("API_TOKEN");
    }
    
    private void logError(String message, int statusCode, String body) {
        System.err.println(message + ": " + statusCode + " - " + body);
    }
    
    private void logError(String message, Throwable throwable) {
        System.err.println(message + ": " + throwable.getMessage());
    }
    
    public static class User {
        public int id;
        public String name;
        public String email;
        
        public User() {}
        
        public User(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }
    
    public static class UserServiceException extends RuntimeException {
        public UserServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
```

## Spring Boot Integration

```java
import io.mochaapi.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class ExternalApiService {
    private final ApiClient client;
    
    public ExternalApiService(@Value("${api.base-url}") String baseUrl,
                             @Value("${api.token}") String token) {
        this.client = new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(15))
            .readTimeout(Duration.ofSeconds(60))
            .enableRetry()
            .requestInterceptor(req -> {
                req.header("Authorization", "Bearer " + token);
                req.header("Accept", "application/json");
                return req;
            })
            .responseInterceptor(resp -> {
                if (resp.code() >= 400) {
                    // Log to your logging framework
                    System.err.println("API Error: " + resp.code() + " - " + resp.body());
                }
                return resp;
            })
            .build();
    }
    
    public ApiResponse getData(String endpoint) {
        try {
            return client.get(baseUrl + endpoint)
                .execute();
        } catch (ApiException e) {
            throw new ExternalApiException("Failed to get data from " + endpoint, e);
        }
    }
    
    public <T> T getDataAs(String endpoint, Class<T> clazz) {
        try {
            return client.get(baseUrl + endpoint)
                .execute()
                .to(clazz);
        } catch (ApiException e) {
            throw new ExternalApiException("Failed to get data from " + endpoint, e);
        } catch (JsonException e) {
            throw new ExternalApiException("Failed to parse data from " + endpoint, e);
        }
    }
    
    private String baseUrl;
    
    public static class ExternalApiException extends RuntimeException {
        public ExternalApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
```

## Configuration Management

```java
import io.mochaapi.client.*;
import java.time.Duration;
import java.util.Map;

public class ApiClientFactory {
    
    public static ApiClient createProductionClient(String token) {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .enableRetry()
            .allowLocalhost(false)  // Production-safe
            .requestInterceptor(req -> {
                req.header("Authorization", "Bearer " + token);
                req.header("User-Agent", "MyApp/1.0");
                return req;
            })
            .build();
    }
    
    public static ApiClient createDevelopmentClient() {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(15))
            .enableRetry()
            .allowLocalhost(true)   // Development-friendly
            .enableLogging()
            .build();
    }
    
    public static ApiClient createTestClient() {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(1))
            .readTimeout(Duration.ofSeconds(5))
            .allowLocalhost(true)
            .build();
    }
    
    public static ApiClient createFromConfig(Map<String, Object> config) {
        ApiClient.Builder builder = new ApiClient.Builder();
        
        if (config.containsKey("connectTimeout")) {
            builder.connectTimeout(Duration.ofSeconds((Integer) config.get("connectTimeout")));
        }
        
        if (config.containsKey("readTimeout")) {
            builder.readTimeout(Duration.ofSeconds((Integer) config.get("readTimeout")));
        }
        
        if (config.containsKey("allowLocalhost")) {
            builder.allowLocalhost((Boolean) config.get("allowLocalhost"));
        }
        
        if (config.containsKey("enableRetry")) {
            builder.enableRetry();
        }
        
        if (config.containsKey("enableLogging")) {
            builder.enableLogging();
        }
        
        return builder.build();
    }
}
```

## Error Handling Patterns

```java
import io.mochaapi.client.*;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class RobustApiClient {
    private final ApiClient client;
    
    public RobustApiClient() {
        this.client = new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .enableRetry()
            .build();
    }
    
    public <T> T getWithFallback(String url, Class<T> clazz, T fallback) {
        try {
            return client.get(url)
                .execute()
                .to(clazz);
        } catch (ApiException e) {
            System.err.println("API Error: " + e.getMessage());
            return fallback;
        } catch (JsonException e) {
            System.err.println("JSON Error: " + e.getMessage());
            return fallback;
        }
    }
    
    public <T> CompletableFuture<T> getAsyncWithFallback(String url, Class<T> clazz, T fallback) {
        return client.get(url)
            .executeAsync()
            .thenApply(response -> {
                try {
                    return response.to(clazz);
                } catch (JsonException e) {
                    System.err.println("JSON Error: " + e.getMessage());
                    return fallback;
                }
            })
            .exceptionally(throwable -> {
                System.err.println("Async Error: " + throwable.getMessage());
                return fallback;
            });
    }
    
    public boolean isHealthy(String healthCheckUrl) {
        try {
            ApiResponse response = client.get(healthCheckUrl)
                .execute();
            return response.code() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
```

## Circuit Breaker Pattern

```java
import io.mochaapi.client.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CircuitBreakerApiClient {
    private final ApiClient client;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final int failureThreshold = 5;
    private final long timeoutMs = 60000; // 1 minute
    
    public CircuitBreakerApiClient() {
        this.client = new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .enableRetry()
            .build();
    }
    
    public <T> T get(String url, Class<T> clazz) {
        if (isCircuitOpen()) {
            throw new CircuitBreakerOpenException("Circuit breaker is open");
        }
        
        try {
            T result = client.get(url)
                .execute()
                .to(clazz);
            
            // Success - reset failure count
            failureCount.set(0);
            return result;
            
        } catch (Exception e) {
            // Failure - increment count and record time
            failureCount.incrementAndGet();
            lastFailureTime.set(System.currentTimeMillis());
            throw e;
        }
    }
    
    private boolean isCircuitOpen() {
        if (failureCount.get() >= failureThreshold) {
            long timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime.get();
            return timeSinceLastFailure < timeoutMs;
        }
        return false;
    }
    
    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}
```

## Kotlin Production Examples

### Kotlin Service Class

```kotlin
import io.mochaapi.client.*
import java.time.Duration

class UserService {
    private val client = ApiClient.Builder()
        .connectTimeout(Duration.ofSeconds(10))
        .readTimeout(Duration.ofSeconds(30))
        .enableRetry()
        .requestInterceptor { req ->
            req.header("Authorization", "Bearer ${getToken()}")
            req.header("User-Agent", "MyApp/1.0")
            req
        }
        .responseInterceptor { resp ->
            if (resp.code() >= 400) {
                logError("API Error", resp.code(), resp.body())
            }
            resp
        }
        .enableLogging()
        .build()
    
    fun getUser(id: Int): User {
        return try {
            client.get("https://api.example.com/users/$id")
                .execute()
                .to(User::class.java)
        } catch (e: ApiException) {
            throw UserServiceException("Failed to get user $id", e)
        } catch (e: JsonException) {
            throw UserServiceException("Failed to parse user data", e)
        }
    }
    
    suspend fun getUserAsync(id: Int): User = withContext(Dispatchers.IO) {
        try {
            client.get("https://api.example.com/users/$id")
                .execute()
                .to(User::class.java)
        } catch (e: Exception) {
            throw UserServiceException("Failed to get user $id", e)
        }
    }
    
    private fun getToken(): String = System.getenv("API_TOKEN") ?: ""
    
    private fun logError(message: String, statusCode: Int, body: String) {
        System.err.println("$message: $statusCode - $body")
    }
    
    private fun logError(message: String, throwable: Throwable) {
        System.err.println("$message: ${throwable.message}")
    }
}

data class User(
    val id: Int,
    val name: String,
    val email: String
)

class UserServiceException(message: String, cause: Throwable) : RuntimeException(message, cause)
```

### Kotlin Configuration Factory

```kotlin
import io.mochaapi.client.*
import java.time.Duration

object ApiClientFactory {
    
    fun createProductionClient(token: String): ApiClient {
        return ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .enableRetry()
            .allowLocalhost(false)  // Production-safe
            .requestInterceptor { req ->
                req.header("Authorization", "Bearer $token")
                req.header("User-Agent", "MyApp/1.0")
                req
            }
            .build()
    }
    
    fun createDevelopmentClient(): ApiClient {
        return ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(15))
            .enableRetry()
            .allowLocalhost(true)   // Development-friendly
            .enableLogging()
            .build()
    }
    
    fun createTestClient(): ApiClient {
        return ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(1))
            .readTimeout(Duration.ofSeconds(5))
            .allowLocalhost(true)
            .build()
    }
}
```

## Key Production Principles

- ✅ **Proper Timeouts** - Set appropriate connection and read timeouts
- ✅ **Retry Logic** - Enable retry for transient failures
- ✅ **Error Handling** - Catch and handle specific exception types
- ✅ **Logging** - Log errors and important events
- ✅ **Security** - Use proper authentication headers
- ✅ **Circuit Breaker** - Implement circuit breaker pattern for resilience
- ✅ **Configuration** - Externalize configuration for different environments
- ✅ **Monitoring** - Add health checks and monitoring
- ✅ **Async Support** - Use async methods for better performance
- ✅ **Fallback Values** - Provide fallback values for critical operations
