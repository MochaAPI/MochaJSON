package io.mochaapi.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Chainable wrapper for JSON data that eliminates casting boilerplate when accessing nested objects.
 * 
 * <p>JsonMap provides a fluent API for accessing nested JSON data without the need for explicit casting
 * to {@code Map<String, Object>}. This makes code more readable and eliminates unchecked cast warnings.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * ApiResponse response = Api.get("https://api.example.com/user").execute();
 * 
 * // Traditional approach (verbose with casting)
 * Map&lt;String, Object&gt; data = response.toMap();
 * String city = ((Map&lt;String, Object&gt;)((Map&lt;String, Object&gt;)data.get("user")).get("address")).get("city").toString();
 * 
 * // JsonMap approach (clean chaining)
 * String city = response.toJsonMap().get("user").get("address").get("city").toString();
 * </pre>
 * 
 * <p>The JsonMap automatically handles both nested objects (returning new JsonMap instances) and 
 * primitive values (wrapping them for seamless string conversion). This makes it ideal for working 
 * with complex nested JSON responses from APIs.</p>
 * 
 * @since 1.0.0
 */
public class JsonMap extends HashMap<String, Object> {

    /** Internal field to wrap primitive values for chaining. */
    private Object wrappedValue = null;

    /**
     * Creates a new JsonMap from a Map.
     * 
     * @param map the map to wrap
     */
    public JsonMap(Map<String, Object> map) {
        super(map);
    }

    /**
     * Creates a new JsonMap wrapping a primitive value.
     * This constructor is used internally to wrap non-Map values for chaining.
     * 
     * @param value the primitive value to wrap
     */
    private JsonMap(Object value) {
        this.wrappedValue = value;
    }

    /**
     * Gets a value from the map, returning a chainable JsonMap.
     * 
     * <p>If the value is a Map, returns a new JsonMap for further chaining.
     * If the value is primitive (String, Number, Boolean, etc.), returns a JsonMap
     * that wraps the value and can be converted to string via toString().</p>
     * 
     * <p>Example:</p>
     * <pre>
     * JsonMap json = response.toJsonMap();
     * 
     * // Access nested objects (returns JsonMap for chaining)
     * JsonMap user = json.get("data").get("user");
     * 
     * // Access primitive values (returns wrapped JsonMap)
     * String name = json.get("data").get("user").get("name").toString();
     * </pre>
     * 
     * @param key the key to retrieve
     * @return JsonMap for chaining or wrapping primitive values
     */
    public JsonMap get(String key) {
        Object value = super.get(key);

        if (value instanceof Map) {
            return new JsonMap((Map<String, Object>) value);
        }

        return new JsonMap(value);
    }

    /**
     * Returns the string representation of the wrapped value.
     * 
     * <p>For primitive values, returns the string representation of the value.
     * For Map objects, returns the standard HashMap toString() representation.</p>
     * 
     * <p>This method is automatically called when the JsonMap is used in string contexts,
     * making it seamless to use in System.out.println() or string concatenation.</p>
     * 
     * @return string representation of the value
     */
    @Override
    public String toString() {
        if (wrappedValue != null) {
            return wrappedValue.toString();
        }
        return super.toString();
    }
}