package com.jonnymatts.jzonbie.body;


import com.jonnymatts.jzonbie.Body;

import java.util.ArrayList;
import java.util.List;

import static com.jonnymatts.jzonbie.util.Matching.listsMatchesRegex;

/**
 * Request/response body containing a {@link List}. This will be recognized as
 * a JSON array in requests and responses.
 * <p>
 * The body content:
 * <p>
 * {@code
 * arrayBody(asList(1, 2, 3))
 * }
 * <p>
 * would be the same as the JSON:
 * <pre>
 * [1, 2, 3]
 * </pre>
 */
public class ArrayBodyContent extends BodyContent<List<Object>> {

    private List<Object> content;

    ArrayBodyContent(List<?> content) {
        this.content = content == null ? null : new ArrayList<>(content);
    }

    @Override
    public boolean matches(Body<?> other) {
        return other instanceof ArrayBodyContent && listsMatchesRegex(content, ((ArrayBodyContent)other).getContent());
    }

    @Override
    public ArrayBodyContent copy() {
        return new ArrayBodyContent(new ArrayList<>(content));
    }

    @Override
    public List<Object> getContent() {
        return content;
    }

    @Override
    public BodyContentType getType() {
        return BodyContentType.ARRAY;
    }

    /**
     * Creates an {@link ArrayBodyContent} body content containing the list content.
     *
     * @param content list content
     * @return list body content
     */
    public static ArrayBodyContent arrayBody(List<?> content) {
        return new ArrayBodyContent(content);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArrayBodyContent that = (ArrayBodyContent) o;

        return content != null ? content.equals(that.content) : that.content == null;
    }

    @Override
    public int hashCode() {
        return content != null ? content.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ArrayBodyContent{" +
                "content=" + content +
                '}';
    }
}