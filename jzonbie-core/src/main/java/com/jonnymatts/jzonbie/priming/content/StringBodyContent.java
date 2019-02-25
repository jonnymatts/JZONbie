package com.jonnymatts.jzonbie.priming.content;

import com.fasterxml.jackson.annotation.JsonCreator;

import static com.jonnymatts.jzonbie.priming.content.BodyContentType.J_STRING;

public class StringBodyContent extends BodyContent<String> {

    private String content;

    StringBodyContent(String content) {
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public BodyContentType getType() {
        return J_STRING;
    }

    @Override
    public boolean matches(BodyContent that) {
        return that instanceof StringBodyContent && content.matches(((StringBodyContent)that).getContent());
    }

    @JsonCreator
    public static StringBodyContent stringBody(String content) {
        return new StringBodyContent(content);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringBodyContent that = (StringBodyContent) o;

        return content != null ? content.equals(that.content) : that.content == null;
    }

    @Override
    public int hashCode() {
        return content != null ? content.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "StringBodyContent{" +
                "content='" + content + '\'' +
                '}';
    }
}