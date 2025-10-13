---
title: Library vs Framework: Why MochaJSON is 1000% Library
description: Understanding the philosophy behind MochaJSON v1.0.0's pure library design and why it's better than framework-like approaches.
keywords:
  - library vs framework
  - stateless design
  - MochaJSON philosophy
  - pure library
  - HTTP client design
---

# Library vs Framework: Why MochaJSON is 1000% Library

## The Philosophy

MochaJSON is designed as a **pure library**, not a framework. This distinction is crucial for understanding how to use it effectively and why it's better than framework-like approaches.

## Library Characteristics ✅

### 1. No Lifecycle Management

```java
// ✅ Just use it - no initialization or shutdown
Map<String, Object> data = Api.get("https://api.example.com/data")
    .execute()
    .toMap();
```

**What this means:**
- No `init()` or `startup()` methods
- No `shutdown()` or `cleanup()` methods
- No application lifecycle hooks
- Just import and use

### 2. No Global State

```java
// ✅ Each client is independent
ApiClient client1 = new ApiClient.Builder().build();
ApiClient client2 = new ApiClient.Builder().build();
// No conflicts, no shared state
```

**What this means:**
- No global configuration
- No shared mutable state
- Each client is completely isolated
- Thread-safe by design

### 3. Explicit Configuration

```java
// ✅ Configuration is per-client, explicit
ApiClient client = new ApiClient.Builder()
    .allowLocalhost(true)
    .connectTimeout(Duration.ofSeconds(10))
    .build();
```

**What this means:**
- All configuration is explicit
- No hidden defaults or magic
- Configuration is per-instance
- Clear ownership of settings

### 4. User Controls Everything

```java
// ✅ You decide when to create, use, and discard
void processRequests() {
    ApiClient client = new ApiClient.Builder().build();
    // Use client
    // No cleanup needed - client is stateless
}
```

**What this means:**
- You control object lifecycle
- You control resource management
- You control when things happen
- No hidden dependencies

## Framework Anti-Patterns ❌ (Removed in v1.0.0)

### ❌ Lifecycle Hooks (REMOVED)

```java
// ❌ v1.0.0 had this (removed in v1.0.0)
Runtime.getRuntime().addShutdownHook(...);
Api.shutdown();
```

**Why this was bad:**
- Libraries shouldn't manage application lifecycle
- Creates hidden dependencies
- Makes testing harder
- Violates single responsibility principle

### ❌ Global Configuration (REMOVED)

```java
// ❌ v1.0.0 had this (removed in v1.0.0)
Utils.setDefaultSecurityConfig(config);
```

**Why this was bad:**
- Global state is hard to reason about
- Makes testing difficult
- Creates hidden dependencies
- Violates explicit is better than implicit

### ❌ Hidden State Management (REMOVED)

```java
// ❌ v1.0.0 tracked state (removed in v1.0.0)
if (Api.isShutdown()) { ... }
```

**Why this was bad:**
- Hidden state is unpredictable
- Makes debugging difficult
- Creates race conditions
- Violates transparency principle

## Benefits of Library Design

### 1. Predictability

**No surprise side effects or hidden state changes.**

```java
// ✅ Predictable behavior
ApiClient client1 = new ApiClient.Builder().build();
ApiClient client2 = new ApiClient.Builder().build();

// These are completely independent
Map<String, Object> data1 = client1.get(url1).execute().toMap();
Map<String, Object> data2 = client2.get(url2).execute().toMap();
```

### 2. Testability

**Easy to mock and test because each client is independent.**

```java
@Test
public void testApiCall() {
    // ✅ Easy to test - no global state to worry about
    ApiClient client = new ApiClient.Builder()
        .allowLocalhost(true)
        .build();
    
    Map<String, Object> result = client.get("http://localhost:8080/test")
        .execute()
        .toMap();
    
    assertNotNull(result);
    // No cleanup needed
}
```

### 3. Flexibility

**Use multiple clients with different configurations.**

```java
// ✅ Different configurations for different needs
ApiClient externalAPI = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(30))
    .allowLocalhost(false)  // Production-safe
    .build();

ApiClient internalAPI = new ApiClient.Builder()
    .allowLocalhost(true)   // Development-friendly
    .connectTimeout(Duration.ofSeconds(5))
    .build();

ApiClient testAPI = new ApiClient.Builder()
    .allowLocalhost(true)
    .connectTimeout(Duration.ofSeconds(1))
    .build();
```

### 4. Simplicity

**No complex lifecycle to understand or manage.**

```java
// ✅ Simple and straightforward
public class UserService {
    private final ApiClient client;
    
    public UserService() {
        this.client = new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    public User getUser(int id) {
        return client.get("https://api.example.com/users/" + id)
            .execute()
            .to(User.class);
    }
    
    // No cleanup method needed
}
```

### 5. Integration

**Works seamlessly with any framework or architecture.**

```java
// ✅ Works with Spring Boot
@Configuration
public class ApiConfig {
    @Bean
    public ApiClient externalApiClient() {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    @Bean
    public ApiClient internalApiClient() {
        return new ApiClient.Builder()
            .allowLocalhost(true)
            .build();
    }
}

// ✅ Works standalone
public class Main {
    public static void main(String[] args) {
        Map<String, Object> data = Api.get("https://api.example.com/data")
            .execute()
            .toMap();
        System.out.println(data);
    }
}
```

## Real-World Comparison

### Spring Boot Integration

```java
@Configuration
public class ApiConfig {
    @Bean
    @Primary
    public ApiClient externalApiClient() {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .allowLocalhost(false)  // Production-safe
            .requestInterceptor(req -> {
                req.header("Authorization", "Bearer " + getExternalToken());
                return req;
            })
            .build();
    }
    
    @Bean
    public ApiClient internalApiClient() {
        return new ApiClient.Builder()
            .allowLocalhost(true)   // Development-friendly
            .connectTimeout(Duration.ofSeconds(5))
            .requestInterceptor(req -> {
                req.header("Authorization", "Bearer " + getInternalToken());
                return req;
            })
            .build();
    }
}

@Service
public class UserService {
    private final ApiClient externalClient;
    private final ApiClient internalClient;
    
    public UserService(ApiClient externalApiClient, ApiClient internalApiClient) {
        this.externalClient = externalApiClient;
        this.internalClient = internalApiClient;
    }
    
    public User getExternalUser(int id) {
        return externalClient.get("https://api.external.com/users/" + id)
            .execute()
            .to(User.class);
    }
    
    public User getInternalUser(int id) {
        return internalClient.get("http://localhost:8080/users/" + id)
            .execute()
            .to(User.class);
    }
}
```

### Micronaut Integration

```java
@Factory
public class ApiClientFactory {
    @Bean
    @Singleton
    public ApiClient externalApiClient() {
        return new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    @Bean
    @Singleton
    public ApiClient internalApiClient() {
        return new ApiClient.Builder()
            .allowLocalhost(true)
            .build();
    }
}

@Controller("/api")
public class ApiController {
    private final ApiClient externalClient;
    private final ApiClient internalClient;
    
    public ApiController(ApiClient externalApiClient, ApiClient internalApiClient) {
        this.externalClient = externalApiClient;
        this.internalClient = internalApiClient;
    }
    
    @Get("/external/{id}")
    public User getExternalUser(int id) {
        return externalClient.get("https://api.external.com/users/" + id)
            .execute()
            .to(User.class);
    }
    
    @Get("/internal/{id}")
    public User getInternalUser(int id) {
        return internalClient.get("http://localhost:8080/users/" + id)
            .execute()
            .to(User.class);
    }
}
```

### Standalone Application

```java
public class Main {
    public static void main(String[] args) {
        // ✅ No framework needed
        Map<String, Object> data = Api.get("https://api.example.com/data")
            .execute()
            .toMap();
        
        System.out.println("Data: " + data);
        
        // ✅ Or use configured client
        ApiClient client = new ApiClient.Builder()
            .allowLocalhost(true)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        
        User user = client.get("http://localhost:8080/users/1")
            .execute()
            .to(User.class);
        
        System.out.println("User: " + user.name);
    }
}
```

## Testing Benefits

### Easy Unit Testing

```java
public class UserServiceTest {
    @Test
    public void testGetUser() {
        // ✅ Easy to test - no global state
        ApiClient client = new ApiClient.Builder()
            .allowLocalhost(true)
            .build();
        
        UserService service = new UserService(client);
        User user = service.getUser(1);
        
        assertNotNull(user);
        assertEquals(1, user.id);
    }
}
```

### Easy Integration Testing

```java
@SpringBootTest
public class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;
    
    @Test
    public void testGetUser() {
        // ✅ Works with Spring Boot testing
        User user = userService.getUser(1);
        
        assertNotNull(user);
        assertEquals(1, user.id);
    }
}
```

### Easy Mocking

```java
@Test
public void testWithMockClient() {
    // ✅ Easy to mock - no global state
    ApiClient mockClient = mock(ApiClient.class);
    when(mockClient.get(anyString())).thenReturn(mockRequest);
    
    UserService service = new UserService(mockClient);
    // Test with mocked client
}
```

## Performance Benefits

### No Global State Overhead

```java
// ✅ No global state to manage
ApiClient client1 = new ApiClient.Builder().build();
ApiClient client2 = new ApiClient.Builder().build();
// Each client is independent and efficient
```

### Better Memory Management

```java
// ✅ Clients can be garbage collected when not needed
void processRequests() {
    ApiClient client = new ApiClient.Builder().build();
    // Use client
    // Client is automatically cleaned up when method ends
}
```

### Thread Safety

```java
// ✅ Thread-safe by design
CompletableFuture<Map<String, Object>> future1 = client1.get(url1).executeAsync();
CompletableFuture<Map<String, Object>> future2 = client2.get(url2).executeAsync();
// No shared state, no race conditions
```

## Migration from Framework-like Libraries

### From OkHttp (Framework-like)

```java
// ❌ OkHttp (framework-like)
OkHttpClient client = new OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .build();

Request request = new Request.Builder()
    .url("https://api.example.com/data")
    .build();

Response response = client.newCall(request).execute();
String json = response.body().string();
// Manual JSON parsing needed
```

```java
// ✅ MochaJSON (pure library)
Map<String, Object> data = Api.get("https://api.example.com/data")
    .execute()
    .toMap();
// Automatic JSON parsing
```

### From Retrofit (Framework-like)

```java
// ❌ Retrofit (framework-like)
public interface ApiService {
    @GET("users/{id}")
    Call<User> getUser(@Path("id") int id);
}

Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build();

ApiService service = retrofit.create(ApiService.class);
Call<User> call = service.getUser(1);
User user = call.execute().body();
```

```java
// ✅ MochaJSON (pure library)
ApiClient client = new ApiClient.Builder().build();
User user = client.get("https://api.example.com/users/1")
    .execute()
    .to(User.class);
```

## Best Practices

### 1. Create Clients as Needed

```java
// ✅ Create clients when you need them
public class DataService {
    private final ApiClient client;
    
    public DataService() {
        this.client = new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    public Data getData(String id) {
        return client.get("https://api.example.com/data/" + id)
            .execute()
            .to(Data.class);
    }
}
```

### 2. Use Dependency Injection

```java
// ✅ Inject clients for better testability
@Service
public class UserService {
    private final ApiClient externalClient;
    private final ApiClient internalClient;
    
    public UserService(ApiClient externalClient, ApiClient internalClient) {
        this.externalClient = externalClient;
        this.internalClient = internalClient;
    }
}
```

### 3. Configure Per Environment

```java
// ✅ Different configurations for different environments
@Configuration
public class ApiConfig {
    @Bean
    @Profile("production")
    public ApiClient productionClient() {
        return new ApiClient.Builder()
            .allowLocalhost(false)
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }
    
    @Bean
    @Profile("development")
    public ApiClient developmentClient() {
        return new ApiClient.Builder()
            .allowLocalhost(true)
            .connectTimeout(Duration.ofSeconds(5))
            .enableLogging()
            .build();
    }
}
```

## Summary

MochaJSON v1.0.0 is a **pure library** because it:

- ✅ **No Lifecycle Management** - Just use it
- ✅ **No Global State** - Each client is independent
- ✅ **Explicit Configuration** - All settings are per-client
- ✅ **User Control** - You decide when to create and use clients
- ✅ **Predictable** - No hidden side effects
- ✅ **Testable** - Easy to mock and test
- ✅ **Flexible** - Multiple clients with different configs
- ✅ **Simple** - No complex lifecycle to manage
- ✅ **Framework Agnostic** - Works with any architecture

This makes MochaJSON more reliable, testable, and easier to use than framework-like HTTP clients. You get all the power of a modern HTTP client without the complexity of lifecycle management or global state.
