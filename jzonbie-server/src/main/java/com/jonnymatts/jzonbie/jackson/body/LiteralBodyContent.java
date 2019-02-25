package com.jonnymatts.jzonbie.jackson.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jonnymatts.jzonbie.Body;

import static com.jonnymatts.jzonbie.jackson.body.BodyContentType.J_LITERAL;

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
        return J_LITERAL;
    }

    @JsonCreator
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