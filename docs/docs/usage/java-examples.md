# Java Examples

Complete Java usage examples for MochaJSON v1.0.0, showcasing the new stateless library design with no lifecycle management.

## v1.0.0 Stateless Design Examples

### Basic GET with Production-Safe Defaults

```java
import io.mochaapi.client.*;
import java.util.Map;

public class BasicExample {
    public static void main(String[] args) {
        try {
            // Simple static API - uses production defaults
            Map<String, Object> data = Api.get("https://jsonplaceholder.typicode.com/users/1")
                .execute()
                .toMap();
            
            System.out.println("Name: " + data.get("name"));
            System.out.println("Email: " + data.get("email"));
            
        } catch (ApiException | JsonException e) {
            e.printStackTrace();
        }
    }
}
```

### Development with Localhost Access

```java
import io.mochaapi.client.*;
import java.time.Duration;

public class DevelopmentExample {
    public static void main(String[] args) {
        try {
            // ApiClient for localhost development
            ApiClient devClient = new ApiClient.Builder()
                .allowLocalhost(true)           // Allow localhost for development
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .enableRetry()                 // Simple retry with 3 attempts
                .enableLogging()               // Console logging
                .build();
            
            // Use the client
            Map<String, Object> user = devClient.get("http://localhost:8080/api/users/1")
                .execute()
                .toMap();
            
            System.out.println("Local user: " + user.get("name"));
            
        } catch (ApiException | JsonException e) {
            e.printStackTrace();
        }
    }
}
```

### Stateless Resource Management

```java
import io.mochaapi.client.*;
import java.time.Duration;

public class StatelessExample {
    public static void main(String[] args) {
        try {
            // No shutdown needed - library is stateless
            // Create clients as needed
            ApiClient client1 = new ApiClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
                
            ApiClient client2 = new ApiClient.Builder()
                .allowLocalhost(true)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
            
            // Use them independently, no cleanup required
            Map<String, Object> data1 = client1.get("https://api1.example.com/data")
                .execute()
                .toMap();
                
            Map<String, Object> data2 = client2.get("http://localhost:8080/api/data")
                .execute()
                .toMap();
            
            System.out.println("External data: " + data1.get("value"));
            System.out.println("Local data: " + data2.get("value"));
            
        } catch (ApiException | JsonException e) {
            e.printStackTrace();
        }
    }
}
```

### Security Configuration Examples

```java
import io.mochaapi.client.*;
import java.time.Duration;

public class SecurityExample {
    public static void main(String[] args) {
        try {
            // Explicit security control
            ApiClient prodClient = new ApiClient.Builder()
                .allowLocalhost(false)  // Production-safe
                .connectTimeout(Duration.ofSeconds(10))
                .build();
            
            ApiClient devClient = new ApiClient.Builder()
                .allowLocalhost(true)   // Development-friendly
                .connectTimeout(Duration.ofSeconds(5))
                .build();
            
            // Production client - blocks localhost
            Map<String, Object> prodData = prodClient.get("https://api.example.com/data")
                .execute()
                .toMap();
            
            // Development client - allows localhost
            Map<String, Object> devData = devClient.get("http://localhost:8080/api/data")
                .execute()
                .toMap();
            
            System.out.println("Production data: " + prodData.get("value"));
            System.out.println("Development data: " + devData.get("value"));
            
        } catch (ApiException | JsonException e) {
            e.printStackTrace();
        }
    }
}
```

### Nested JSON Access with JsonMap

When working with deeply nested API responses, JsonMap eliminates the need for verbose casting and provides a clean, chainable API.

```java
import io.mochaapi.client.*;

public class JsonMapExample {
    public static void main(String[] args) {
        try {
            // Example: User profile API with nested data
            JsonMap response = Api.get("https://api.example.com/user/123")
                .execute()
                .toJsonMap();
            
            // Traditional approach (verbose with casting)
            Map<String, Object> data = response.toMap();
            Map<String, Object> user = (Map<String, Object>) data.get("data");
            Map<String, Object> name = (Map<String, Object>) user.get("name");
            Map<String, Object> location = (Map<String, Object>) user.get("location");
            Map<String, Object> street = (Map<String, Object>) location.get("street");
            Map<String, Object> coordinates = (Map<String, Object>) location.get("coordinates");
            
            String traditionalName = name.get("first") + " " + name.get("last");
            String traditionalAddress = street.get("number") + " " + street.get("name") + ", " + location.get("city");
            String traditionalLat = coordinates.get("latitude").toString();
            
            // JsonMap approach (clean chaining)
            String cleanName = response.get("data").get("name").get("first") + " " + 
                              response.get("data").get("name").get("last");
            String cleanAddress = response.get("data").get("location").get("street").get("number") + " " + 
                                 response.get("data").get("location").get("street").get("name") + ", " + 
                                 response.get("data").get("location").get("city");
            String cleanLat = response.get("data").get("location").get("coordinates").get("latitude").toString();
            
            System.out.println("Name: " + cleanName);
            System.out.println("Address: " + cleanAddress);
            System.out.println("Latitude: " + cleanLat);
            
            // Intermediate access for complex operations
            JsonMap userData = response.get("data");
            JsonMap locationData = userData.get("location");
            
            String email = userData.get("email").toString();
            String city = locationData.get("city").toString();
            String state = locationData.get("state").toString();
            
            System.out.println("Email: " + email);
            System.out.println("Location: " + city + ", " + state);
            
        } catch (ApiException | JsonException e) {
            e.printStackTrace();
        }
    }
}
```

**Benefits of JsonMap:**

- **No casting boilerplate**: Eliminates `(Map<String, Object>)` casts
- **Type safety**: Prevents ClassCastException errors
- **Readable code**: Chainable syntax reads naturally
- **Flexible access**: Supports both direct chaining and intermediate variables

### Multiple Clients Pattern

```java
import io.mochaapi.client.*;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class MultipleClientsExample {
    public static void main(String[] args) {
        try {
            // Different clients for different APIs - no conflicts
            ApiClient githubClient = new ApiClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .requestInterceptor(req -> {
                    req.header("Authorization", "Bearer " + getGithubToken());
                    return req;
                })
                .build();
            
            ApiClient internalClient = new ApiClient.Builder()
                .allowLocalhost(true)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
            
            // Use them simultaneously without conflicts
            CompletableFuture<Map<String, Object>> githubFuture = githubClient
                .get("https://api.github.com/user")
                .executeAsync()
                .thenApply(response -> response.toMap());
                
            CompletableFuture<Map<String, Object>> internalFuture = internalClient
                .get("http://localhost:8080/api/user")
                .executeAsync()
                .thenApply(response -> response.toMap());
            
            // Wait for both
            Map<String, Object> githubUser = githubFuture.get();
            Map<String, Object> internalUser = internalFuture.get();
            
            System.out.println("GitHub user: " + githubUser.get("login"));
            System.out.println("Internal user: " + internalUser.get("name"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String getGithubToken() {
        return System.getenv("GITHUB_TOKEN");
    }
}
```

### Complete Real-World Example

```java
import io.mochaapi.client.*;
import java.time.Duration;

public class UserService {
    private final ApiClient client;
    
    public UserService() {
        this.client = new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .enableRetry()
            .requestInterceptor(req -> {
                req.header("Authorization", "Bearer " + getToken());
                return req;
            })
            .enableLogging()
            .build();
    }
    
    public User getUser(int id) {
        try {
            return client.get("https://api.example.com/users/" + id)
                .execute()
                .to(User.class);
        } catch (ApiException | JsonException e) {
            throw new RuntimeException("Failed to get user " + id, e);
        }
    }
    
    public User createUser(User user) {
        try {
            return client.post("https://api.example.com/users")
                .body(user)
                .execute()
                .to(User.class);
        } catch (ApiException | JsonException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }
    
    private String getToken() {
        return System.getenv("API_TOKEN");
    }
    
    // POJO class
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
}
```

## Traditional Examples (Still Work in v1.0.0)

### GET Request with Map Parsing

```java
import io.mochaapi.client.*;
import java.util.Map;

public class GetExample {
    public static void main(String[] args) {
        // GET request → Map
        Map<String, Object> post = Api.get("https://jsonplaceholder.typicode.com/posts/1")
            .execute()
            .toMap();
        
        System.out.println("Post ID: " + post.get("id"));
        System.out.println("Post Title: " + post.get("title"));
        System.out.println("Post Body: " + post.get("body"));
    }
}
```

**Sample JSON Response:**

| Field | Type | Sample Value |
|-------|------|--------------|
| `userId` | `int` | `1` |
| `id` | `int` | `1` |
| `title` | `String` | `"sunt aut facere repellat provident occaecati excepturi optio reprehenderit"` |
| `body` | `String` | `"quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"` |

## GET Request with POJO Parsing

```java
import io.mochaapi.client.*;

public class PojoExample {
    public static void main(String[] args) {
        // GET request → POJO
        Post post = Api.get("https://jsonplaceholder.typicode.com/posts/1")
            .execute()
            .to(Post.class);
        
        System.out.println("Post ID: " + post.id);
        System.out.println("Post Title: " + post.title);
        System.out.println("Post Body: " + post.body);
    }
    
    // POJO class for JSON deserialization
    public static class Post {
        public int id;
        public int userId;
        public String title;
        public String body;
    }
}
```

## POST Request with JSON Body

```java
import io.mochaapi.client.*;
import java.util.Map;
import java.util.HashMap;

public class PostExample {
    public static void main(String[] args) {
        // Create request body
        Map<String, Object> newPost = new HashMap<>();
        newPost.put("title", "My New Post");
        newPost.put("body", "This is the content of my new post.");
        newPost.put("userId", 1);
        
        // POST request
        ApiResponse response = Api.post("https://jsonplaceholder.typicode.com/posts")
            .body(newPost)
            .execute();
        
        System.out.println("Status Code: " + response.code());
        
        // Parse response to Map
        Map<String, Object> responseData = response.toMap();
        System.out.println("Created Post ID: " + responseData.get("id"));
        System.out.println("Created Post Title: " + responseData.get("title"));
        
        // Parse response to POJO
        Post createdPost = response.to(Post.class);
        System.out.println("Created Post: " + createdPost.title);
    }
    
    public static class Post {
        public int id;
        public int userId;
        public String title;
        public String body;
    }
}
```

**Sample Response JSON:**

| Field | Type | Sample Value |
|-------|------|--------------|
| `id` | `int` | `101` |
| `title` | `String` | `"My New Post"` |
| `body` | `String` | `"This is the content of my new post."` |
| `userId` | `int` | `1` |

## Query Parameters and Headers

```java
import io.mochaapi.client.*;

public class AdvancedExample {
    public static void main(String[] args) {
        ApiResponse response = Api.get("https://jsonplaceholder.typicode.com/posts")
            .query("userId", 1)
            .query("_limit", 5)
            .header("Authorization", "Bearer token123")
            .header("User-Agent", "MyApp/1.0")
            .execute();
        
        var posts = response.toList();
        System.out.println("Found " + posts.size() + " posts");
        
        // Process each post
        for (Object postObj : posts) {
            Map<String, Object> post = (Map<String, Object>) postObj;
            System.out.println("Post " + post.get("id") + ": " + post.get("title"));
        }
    }
}
```

## Async Requests

```java
import io.mochaapi.client.*;

public class AsyncExample {
    public static void main(String[] args) {
        Api.get("https://jsonplaceholder.typicode.com/posts/1")
            .async(response -> {
                System.out.println("Async response: " + response.body());
                System.out.println("Status: " + response.code());
            });
        
        // Main thread continues...
        System.out.println("Request sent asynchronously");
    }
}
```

## Complex Nested Objects

```java
import io.mochaapi.client.*;

public class NestedObjectExample {
    public static void main(String[] args) {
        User user = Api.get("https://jsonplaceholder.typicode.com/users/1")
            .execute()
            .to(User.class);
        
        System.out.println("User Name: " + user.name);
        System.out.println("User Email: " + user.email);
        System.out.println("User Address: " + user.address.street + ", " + user.address.city);
        System.out.println("User Company: " + user.company.name);
    }
    
    public static class User {
        public int id;
        public String name;
        public String username;
        public String email;
        public String phone;
        public String website;
        public Address address;
        public Company company;
        
        public static class Address {
            public String street;
            public String suite;
            public String city;
            public String zipcode;
            public Geo geo;
            
            public static class Geo {
                public String lat;
                public String lng;
            }
        }
        
        public static class Company {
            public String name;
            public String catchPhrase;
            public String bs;
        }
    }
}
```

**Sample User JSON Response:**

| Field | Type | Sample Value |
|-------|------|--------------|
| `id` | `int` | `1` |
| `name` | `String` | `"Leanne Graham"` |
| `email` | `String` | `"Sincere@april.biz"` |
| `phone` | `String` | `"1-770-736-8031 x56442"` |
| `website` | `String` | `"hildegard.org"` |
| `address.street` | `String` | `"Kulas Light"` |
| `address.city` | `String` | `"Gwenborough"` |
| `company.name` | `String` | `"Romaguera-Crona"` |

## Error Handling

```java
import io.mochaapi.client.*;
import io.mochaapi.client.exception.*;

public class ErrorHandlingExample {
    public static void main(String[] args) {
        try {
            ApiResponse response = Api.get("https://jsonplaceholder.typicode.com/posts/1")
                .execute();
            
            if (response.isError()) {
                System.err.println("HTTP Error: " + response.code());
                return;
            }
            
            Post post = response.to(Post.class);
            System.out.println("Success: " + post.title);
            
        } catch (ApiException e) {
            System.err.println("Network/HTTP Error: " + e.getMessage());
        } catch (JsonException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
        }
    }
    
    public static class Post {
        public int id;
        public int userId;
        public String title;
        public String body;
    }
}
```

## Next Steps

- **[Kotlin Examples](/MochaJSON/usage/kotlin-examples)** - See equivalent Kotlin code
- **[JSON Handling](/MochaJSON/usage/json-handling)** - Advanced JSON parsing techniques
- **[API Reference](/MochaJSON/api/overview)** - Complete API documentation
