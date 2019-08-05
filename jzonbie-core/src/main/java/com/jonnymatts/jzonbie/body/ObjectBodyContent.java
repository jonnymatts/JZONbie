package com.jonnymatts.jzonbie.body;


import com.jonnymatts.jzonbie.Body;

import java.util.HashMap;
import java.util.Map;

import static com.jonnymatts.jzonbie.util.Matching.mapValuesMatchWithRegex;

/**
 * Request/response body containing a {@link Map}. This will be recognized as
 * a JSON object in requests and responses.
 * <p>
 * The body content:
 * <p>
 * {@code
 * objectBody(singletonMap("key", "value"))
 * }
 * <p>
 * would be the same as the JSON:
 * <p>
 * <pre>
 * {"key": "value"}
 * </pre>
 */
public class ObjectBodyContent extends BodyContent<Map<String, Object>> {

    private Map<String, Object> content;

    private ObjectBodyContent(Map<String, ?> content) {
        this.content = content == null ? null : new HashMap<>(content);
    }

    @Override
    public Map<String, Object> getContent() {
        return content;
    }

    @Override
    public boolean matches(Body<?> other) {
        return other instanceof ObjectBodyContent && mapValuesMatchWithRegex(content, ((ObjectBodyContent)other).getContent());
    }

    @Override
    public ObjectBodyContent copy() {
        return new ObjectBodyContent(new HashMap<>(content));
    }

    @Override
    public BodyContentType getType() {
        return BodyContentType.OBJECT;
    }

    /**
     * Creates an {@link ObjectBodyContent} body content containing the map content.
     *
     * @param content map content
     * @return map body content
     */
    public static ObjectBodyContent objectBody(Map<String, ?> content) {
        return new ObjectBodyContent(content);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectBodyContent that = (ObjectBodyContent) o;

        return content != null ? content.equals(that.content) : that.content == null;
    }

    @Override
    public int hashCode() {
        return content != null ? content.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ObjectBodyContent{" +
                "content=" + content +
                '}';
    }
}