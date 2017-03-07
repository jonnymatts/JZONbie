package com.jonnymatts.jzonbie.model.content;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.ArrayList;
import java.util.List;

import static com.jonnymatts.jzonbie.model.content.BodyContentType.J_ARRAY;
import static com.jonnymatts.jzonbie.util.Matching.listsMatchesRegex;

public class ArrayBodyContent extends BodyContent<List<Object>> {

    private List<Object> content;

    ArrayBodyContent(List<?> content) {
        this.content = content == null ? null : new ArrayList<>(content);
    }

    @Override
    public List<Object> getContent() {
        return content;
    }

    @Override
    public BodyContentType getType() {
        return J_ARRAY;
    }

    @Override
    public boolean matches(BodyContent that) {
        return that instanceof ArrayBodyContent && listsMatchesRegex(content, ((ArrayBodyContent)that).getContent());
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
}