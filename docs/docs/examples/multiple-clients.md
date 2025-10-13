---
title: Multiple Clients Patterns
description: Managing multiple API clients with different configurations in MochaJSON v1.0.0.
keywords:
  - multiple clients
  - different configurations
  - client management
  - stateless design
---

# Multiple Clients Patterns

Managing multiple API clients with different configurations in MochaJSON v1.0.0.

## Basic Multiple Clients

```java
import io.mochaapi.client.*;
import java.time.Duration;

public class MultipleClientsExample {
    public static void main(String[] args) {
        try {
            // Different clients for different APIs
            ApiClient githubClient = new ApiClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .requestInterceptor(req -> {
                    req.header("Authorization", "Bearer " + getGithubToken());
                    req.header("Accept", "application/vnd.github.v3+json");
                    return req;
                })
                .build();
            
            ApiClient internalClient = new ApiClient.Builder()
                .allowLocalhost(true)
                .connectTimeout(Duration.ofSeconds(5))
                .requestInterceptor(req -> {
                    req.header("Authorization", "Bearer " + getInternalToken());
                    return req;
                })
                .build();
            
            ApiClient externalClient = new ApiClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .allowLocalhost(false)  // Production-safe
                .build();
            
            // Use them independently
            Map<String, Object> githubUser = githubClient.get("https://api.github.com/user")
                .execute()
                .toMap();
            
            Map<String, Object> internalData = internalClient.get("http://localhost:8080/api/data")
                .execute()
                .toMap();
            
            Map<String, Object> externalData = externalClient.get("https://api.external.com/data")
                .execute()
                .toMap();
            
            System.out.println("GitHub user: " + githubUser.get("login"));
            System.out.println("Internal data: " + internalData.get("value"));
            System.out.println("External data: " + externalData.get("value"));
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private static String getGithubToken() {
        return System.getenv("GITHUB_TOKEN");
    }
    
    private static String getInternalToken() {
        return System.getenv("INTERNAL_TOKEN");
    }
}
```

## Client Manager Pattern

```java
import io.mochaapi.client.*;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiClientManager {
    private final ApiClient githubClient;
    private final ApiClient internalClient;
    private final ApiClient externalClient;
    
    public ApiClientManager() {
        this.githubClient = createGithubClient();
        this.internalClient = createInternalClient();
        this.externalClient = createExternalClient();
    }
    
    private ApiClient createGithubClient() {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .requestInterceptor(req -> {
                req.header("Authorization", "Bearer " + getGithubToken());
                req.header("Accept", "application/vnd.github.v3+json");
                return req;
            })
            .build();
    }
    
    private ApiClient createInternalClient() {
        return new ApiClient.Builder()
            .allowLocalhost(true)
            .connectTimeout(Duration.ofSeconds(5))
            .requestInterceptor(req -> {
                req.header("Authorization", "Bearer " + getInternalToken());
                return req;
            })
            .build();
    }
    
    private ApiClient createExternalClient() {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .allowLocalhost(false)
            .build();
    }
    
    public CompletableFuture<Map<String, Object>> getGithubUser() {
        return githubClient.get("https://api.github.com/user")
            .executeAsync()
            .thenApply(response -> response.toMap());
    }
    
    public CompletableFuture<Map<String, Object>> getInternalData(String endpoint) {
        return internalClient.get("http://localhost:8080/api/" + endpoint)
            .executeAsync()
            .thenApply(response -> response.toMap());
    }
    
    public CompletableFuture<Map<String, Object>> getExternalData(String endpoint) {
        return externalClient.get("https://api.external.com/" + endpoint)
            .executeAsync()
            .thenApply(response -> response.toMap());
    }
    
    // Concurrent requests
    public CompletableFuture<Map<String, Object>> getAllData() {
        CompletableFuture<Map<String, Object>> githubFuture = getGithubUser();
        CompletableFuture<Map<String, Object>> internalFuture = getInternalData("data");
        CompletableFuture<Map<String, Object>> externalFuture = getExternalData("data");
        
        return CompletableFuture.allOf(githubFuture, internalFuture, externalFuture)
            .thenApply(v -> {
                Map<String, Object> result = new HashMap<>();
                result.put("github", githubFuture.join());
                result.put("internal", internalFuture.join());
                result.put("external", externalFuture.join());
                return result;
            });
    }
    
    private String getGithubToken() {
        return System.getenv("GITHUB_TOKEN");
    }
    
    private String getInternalToken() {
        return System.getenv("INTERNAL_TOKEN");
    }
}
```

## Spring Boot Configuration

```java
import io.mochaapi.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class ApiClientConfig {
    
    @Value("${github.token}")
    private String githubToken;
    
    @Value("${internal.token}")
    private String internalToken;
    
    @Value("${external.api.key}")
    private String externalApiKey;
    
    @Bean
    public ApiClient githubClient() {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .requestInterceptor(req -> {
                req.header("Authorization", "Bearer " + githubToken);
                req.header("Accept", "application/vnd.github.v3+json");
                return req;
            })
            .build();
    }
    
    @Bean
    public ApiClient internalClient() {
        return new ApiClient.Builder()
            .allowLocalhost(true)
            .connectTimeout(Duration.ofSeconds(5))
            .requestInterceptor(req -> {
                req.header("Authorization", "Bearer " + internalToken);
                return req;
            })
            .build();
    }
    
    @Bean
    public ApiClient externalClient() {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .allowLocalhost(false)
            .requestInterceptor(req -> {
                req.header("X-API-Key", externalApiKey);
                return req;
            })
            .build();
    }
}

@Service
public class DataService {
    private final ApiClient githubClient;
    private final ApiClient internalClient;
    private final ApiClient externalClient;
    
    public DataService(ApiClient githubClient, 
                      ApiClient internalClient, 
                      ApiClient externalClient) {
        this.githubClient = githubClient;
        this.internalClient = internalClient;
        this.externalClient = externalClient;
    }
    
    public Map<String, Object> getGithubUser() {
        return githubClient.get("https://api.github.com/user")
            .execute()
            .toMap();
    }
    
    public Map<String, Object> getInternalData(String endpoint) {
        return internalClient.get("http://localhost:8080/api/" + endpoint)
            .execute()
            .toMap();
    }
    
    public Map<String, Object> getExternalData(String endpoint) {
        return externalClient.get("https://api.external.com/" + endpoint)
            .execute()
            .toMap();
    }
}
```

## Environment-Specific Clients

```java
import io.mochaapi.client.*;
import java.time.Duration;

public class EnvironmentSpecificClients {
    
    public static ApiClient createClient(Environment environment) {
        switch (environment) {
            case PRODUCTION:
                return createProductionClient();
            case STAGING:
                return createStagingClient();
            case DEVELOPMENT:
                return createDevelopmentClient();
            case TEST:
                return createTestClient();
            default:
                throw new IllegalArgumentException("Unknown environment: " + environment);
        }
    }
    
    private static ApiClient createProductionClient() {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(15))
            .readTimeout(Duration.ofSeconds(60))
            .enableRetry()
            .allowLocalhost(false)  // Production-safe
            .requestInterceptor(req -> {
                req.header("Authorization", "Bearer " + getProductionToken());
                req.header("User-Agent", "MyApp/1.0");
                return req;
            })
            .build();
    }
    
    private static ApiClient createStagingClient() {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .enableRetry()
            .allowLocalhost(false)  // Staging-safe
            .requestInterceptor(req -> {
                req.header("Authorization", "Bearer " + getStagingToken());
                req.header("User-Agent", "MyApp/1.0-staging");
                return req;
            })
            .build();
    }
    
    private static ApiClient createDevelopmentClient() {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(15))
            .enableRetry()
            .allowLocalhost(true)   // Development-friendly
            .enableLogging()
            .requestInterceptor(req -> {
                req.header("Authorization", "Bearer " + getDevelopmentToken());
                req.header("User-Agent", "MyApp/1.0-dev");
                return req;
            })
            .build();
    }
    
    private static ApiClient createTestClient() {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(1))
            .readTimeout(Duration.ofSeconds(5))
            .allowLocalhost(true)
            .build();
    }
    
    private static String getProductionToken() {
        return System.getenv("PRODUCTION_TOKEN");
    }
    
    private static String getStagingToken() {
        return System.getenv("STAGING_TOKEN");
    }
    
    private static String getDevelopmentToken() {
        return System.getenv("DEVELOPMENT_TOKEN");
    }
    
    public enum Environment {
        PRODUCTION, STAGING, DEVELOPMENT, TEST
    }
}
```

## Kotlin Multiple Clients

### Kotlin Client Manager

```kotlin
import io.mochaapi.client.*
import java.time.Duration

class ApiClientManager {
    val githubClient by lazy {
        ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .requestInterceptor { req ->
                req.header("Authorization", "Bearer ${getGithubToken()}")
                req.header("Accept", "application/vnd.github.v3+json")
                req
            }
            .build()
    }
    
    val internalClient by lazy {
        ApiClient.Builder()
            .allowLocalhost(true)
            .connectTimeout(Duration.ofSeconds(5))
            .requestInterceptor { req ->
                req.header("Authorization", "Bearer ${getInternalToken()}")
                req
            }
            .build()
    }
    
    val externalClient by lazy {
        ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .allowLocalhost(false)
            .build()
    }
    
    suspend fun getGithubUser(): Map<String, Any> = withContext(Dispatchers.IO) {
        githubClient.get("https://api.github.com/user")
            .execute()
            .toMap()
    }
    
    suspend fun getInternalData(endpoint: String): Map<String, Any> = withContext(Dispatchers.IO) {
        internalClient.get("http://localhost:8080/api/$endpoint")
            .execute()
            .toMap()
    }
    
    suspend fun getExternalData(endpoint: String): Map<String, Any> = withContext(Dispatchers.IO) {
        externalClient.get("https://api.external.com/$endpoint")
            .execute()
            .toMap()
    }
    
    suspend fun getAllData(): Map<String, Any> = coroutineScope {
        val githubDeferred = async { getGithubUser() }
        val internalDeferred = async { getInternalData("data") }
        val externalDeferred = async { getExternalData("data") }
        
        mapOf(
            "github" to githubDeferred.await(),
            "internal" to internalDeferred.await(),
            "external" to externalDeferred.await()
        )
    }
    
    private fun getGithubToken(): String = System.getenv("GITHUB_TOKEN") ?: ""
    private fun getInternalToken(): String = System.getenv("INTERNAL_TOKEN") ?: ""
}
```

### Kotlin Object Pattern

```kotlin
import io.mochaapi.client.*
import java.time.Duration

object ApiClients {
    val github by lazy {
        ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .requestInterceptor { req ->
                req.header("Authorization", "Bearer ${getGithubToken()}")
                req.header("Accept", "application/vnd.github.v3+json")
                req
            }
            .build()
    }
    
    val internal by lazy {
        ApiClient.Builder()
            .allowLocalhost(true)
            .connectTimeout(Duration.ofSeconds(5))
            .requestInterceptor { req ->
                req.header("Authorization", "Bearer ${getInternalToken()}")
                req
            }
            .build()
    }
    
    val external by lazy {
        ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .allowLocalhost(false)
            .build()
    }
    
    private fun getGithubToken(): String = System.getenv("GITHUB_TOKEN") ?: ""
    private fun getInternalToken(): String = System.getenv("INTERNAL_TOKEN") ?: ""
}

// Usage
fun main() = runBlocking {
    try {
        val githubUser = ApiClients.github.get("https://api.github.com/user")
            .execute()
            .toMap()
        
        val internalData = ApiClients.internal.get("http://localhost:8080/api/data")
            .execute()
            .toMap()
        
        val externalData = ApiClients.external.get("https://api.external.com/data")
            .execute()
            .toMap()
        
        println("GitHub user: ${githubUser["login"]}")
        println("Internal data: ${internalData["value"]}")
        println("External data: ${externalData["value"]}")
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
```

## Testing Multiple Clients

```java
import io.mochaapi.client.*;
import java.time.Duration;

public class MultipleClientsTest {
    
    @Test
    public void testMultipleClients() {
        // Create test clients
        ApiClient testClient1 = new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(1))
            .allowLocalhost(true)
            .build();
        
        ApiClient testClient2 = new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(1))
            .allowLocalhost(true)
            .build();
        
        // Test that they're independent
        Map<String, Object> data1 = testClient1.get("http://localhost:8080/api/data1")
            .execute()
            .toMap();
        
        Map<String, Object> data2 = testClient2.get("http://localhost:8080/api/data2")
            .execute()
            .toMap();
        
        assertNotNull(data1);
        assertNotNull(data2);
        // Clients are independent - no conflicts
    }
    
    @Test
    public void testConcurrentClients() throws Exception {
        ApiClient client1 = new ApiClient.Builder().build();
        ApiClient client2 = new ApiClient.Builder().build();
        
        CompletableFuture<Map<String, Object>> future1 = client1.get("http://localhost:8080/api/data1")
            .executeAsync()
            .thenApply(response -> response.toMap());
        
        CompletableFuture<Map<String, Object>> future2 = client2.get("http://localhost:8080/api/data2")
            .executeAsync()
            .thenApply(response -> response.toMap());
        
        Map<String, Object> result1 = future1.get();
        Map<String, Object> result2 = future2.get();
        
        assertNotNull(result1);
        assertNotNull(result2);
    }
}
```

## Key Benefits

- ✅ **Independent Configuration** - Each client has its own settings
- ✅ **No Conflicts** - Clients don't interfere with each other
- ✅ **Thread Safety** - Multiple clients can be used concurrently
- ✅ **Easy Testing** - Each client can be tested independently
- ✅ **Flexible Architecture** - Different clients for different APIs
- ✅ **Environment Support** - Easy to create environment-specific clients
- ✅ **Dependency Injection** - Works well with Spring Boot and other frameworks
- ✅ **Resource Management** - No cleanup needed - clients are stateless
