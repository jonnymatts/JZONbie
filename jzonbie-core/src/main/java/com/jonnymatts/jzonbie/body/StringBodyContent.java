package com.jonnymatts.jzonbie.body;

import com.jonnymatts.jzonbie.Body;

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
    public boolean matches(Body<?> other) {
        return other instanceof StringBodyContent && content.matches(((StringBodyContent)other).getContent());
    }

    @Override
    public StringBodyContent copy() {
        return new StringBodyContent(content);
    }

    @Override
    public BodyContentType getType() {
        return BodyContentType.STRING;
    }

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