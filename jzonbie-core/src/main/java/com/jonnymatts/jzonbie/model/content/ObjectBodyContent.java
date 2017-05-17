package com.jonnymatts.jzonbie.model.content;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

import static com.jonnymatts.jzonbie.model.content.BodyContentType.J_OBJECT;
import static com.jonnymatts.jzonbie.util.Matching.mapValuesMatchWithRegex;

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
    public BodyContentType getType() {
        return J_OBJECT;
    }

    @Override
    public boolean matches(BodyContent that) {
        return that instanceof ObjectBodyContent && mapValuesMatchWithRegex(content, ((ObjectBodyContent)that).getContent());
    }

    @JsonCreator
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