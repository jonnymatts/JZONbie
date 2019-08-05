package com.jonnymatts.jzonbie.body;

import com.jonnymatts.jzonbie.Body;

/**
 * Request/response body defining a literal response. This will not be recognized as
 * JSON in requests and responses.
 * <p>
 * The body content:
 * <p>
 * {@code
 * literalBody("<data>1</data>")
 * }
 * <p>
 * would be the same as the response:
 * <pre>
 * {@code
 * <data>1</data>
 * }
 * </pre>
 */
public class LiteralBodyContent extends BodyContent<String> {

    private final String content;

    private LiteralBodyContent(Object content) {
        this.content = String.valueOf(content);
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public boolean matches(Body<?> other) {
        return other instanceof LiteralBodyContent && content.matches(((LiteralBodyContent)other).getContent());
    }

    @Override
    public LiteralBodyContent copy() {
        return new LiteralBodyContent(content);
    }

    @Override
    public BodyContentType getType() {
        return BodyContentType.LITERAL;
    }

    /**
     * Creates a {@link LiteralBodyContent} body content containing the literal content.
     *
     * @param content literal content
     * @return literal body content
     */
    public static LiteralBodyContent literalBody(Object content) {
        return new LiteralBodyContent(content);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LiteralBodyContent that = (LiteralBodyContent) o;

        return content != null ? content.equals(that.content) : that.content == null;
    }

    @Override
    public int hashCode() {
        return content != null ? content.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "LiteralBodyContent{" +
                "content='" + content + '\'' +
                '}';
    }
}