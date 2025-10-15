package io.mochaapi.client;

import io.mochaapi.client.internal.DefaultHttpClientEngine;
import io.mochaapi.client.exception.ApiException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Main entry point for HTTP API requests.
 * Provides a simple, chainable API for making HTTP calls with automatic JSON handling.
 * 
 * <p>Example usage:</p>
 * <pre>
 * User user = Api.get("https://api.example.com/user")
 *                .query("id", 12)
 *                .execute()
 *                .json()
 *                .to(User.class);
 * </pre>
 * 
 * @since 1.0.0
 */
public class Api {
    
    /** Private constructor to prevent instantiation. */
    private Api() {}
    
    private static final HttpClientEngine DEFAULT_ENGINE = new DefaultHttpClientEngine();
    private static final Executor DEFAULT_EXECUTOR = createDefaultExecutor();
    
    /**
     * Creates a GET request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public static ApiRequest get(String url) {
        return new ApiRequest(url, "GET");
    }
    
    /**
     * Creates a POST request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public static ApiRequest post(String url) {
        return new ApiRequest(url, "POST");
    }
    
    /**
     * Creates a PUT request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public static ApiRequest put(String url) {
        return new ApiRequest(url, "PUT");
    }
    
    /**
     * Creates a DELETE request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public static ApiRequest delete(String url) {
        return new ApiRequest(url, "DELETE");
    }
    
    /**
     * Creates a PATCH request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public static ApiRequest patch(String url) {
        return new ApiRequest(url, "PATCH");
    }
    
    /**
     * Executes a request synchronously and returns the response.
     * 
     * @param request the configured request
     * @return ApiResponse containing the result
     * @throws ApiException if the request fails
     */
    public static ApiResponse execute(ApiRequest request) {
        return DEFAULT_ENGINE.execute(request);
    }
    
    /**
     * Executes a request asynchronously and returns a CompletableFuture.
     * 
     * @param request the configured request
     * @return CompletableFuture containing the response
     */
    public static CompletableFuture<ApiResponse> executeAsync(ApiRequest request) {
        return CompletableFuture.supplyAsync(() -> execute(request), DEFAULT_EXECUTOR);
    }
    
    /**
     * Executes a request asynchronously with a callback.
     * 
     * @param request the configured request
     * @param callback function to handle the response
     * @throws IllegalArgumentException if request or callback is null
     */
    public static void executeAsync(ApiRequest request, Consumer<ApiResponse> callback) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        
        executeAsync(request)
            .thenAccept(callback)
            .exceptionally(throwable -> {
                // Error handled by callback - no logging needed
                return null;
            });
    }
    
    /**
     * Executes a request asynchronously with both success and error callbacks.
     * 
     * @param request the configured request
     * @param successCallback function to handle successful responses
     * @param errorCallback function to handle errors
     * @throws IllegalArgumentException if request, successCallback, or errorCallback is null
     */
    public static void executeAsync(ApiRequest request, Consumer<ApiResponse> successCallback, Consumer<Throwable> errorCallback) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (successCallback == null) {
            throw new IllegalArgumentException("Success callback cannot be null");
        }
        if (errorCallback == null) {
            throw new IllegalArgumentException("Error callback cannot be null");
        }
        
        executeAsync(request)
            .thenAccept(successCallback)
            .exceptionally(throwable -> {
                try {
                    errorCallback.accept(throwable);
                } catch (Exception e) {
                    // Error in error callback - silently ignore to prevent cascading failures
                }
                return null;
            });
    }
    
    /**
     * Creates the default executor using virtual threads if available (Java 21+).
     * Falls back to cached thread pool for older Java versions.
     * 
     * @return the default executor
     * @since 1.0.0
     */
    private static Executor createDefaultExecutor() {
        try {
            // Try to use virtual threads (Java 21+)
            return Executors.newVirtualThreadPerTaskExecutor();
        } catch (Exception e) {
            // Fallback to cached thread pool for older Java versions
            return Executors.newCachedThreadPool();
        }
    }
    
}
