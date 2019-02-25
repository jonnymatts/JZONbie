package com.jonnymatts.jzonbie.jackson.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jonnymatts.jzonbie.Body;

import java.util.ArrayList;
import java.util.List;

import static com.jonnymatts.jzonbie.jackson.body.BodyContentType.J_ARRAY;
import static com.jonnymatts.jzonbie.util.Matching.listsMatchesRegex;

public class ArrayBodyContent extends BodyContent<List<?>> {

    private List<Object> content;

    ArrayBodyContent(List<?> content) {
        this.content = content == null ? null : new ArrayList<>(content);
    }

    @Override
    public List<Object> getContent() {
        return content;
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
    public BodyContentType getType() {
        return J_ARRAY;
    }

    @JsonCreator
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