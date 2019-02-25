package com.jonnymatts.jzonbie.jackson.body;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.jonnymatts.jzonbie.Body;

import static com.jonnymatts.jzonbie.jackson.body.BodyContent.TYPE_IDENTIFIER;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = TYPE_IDENTIFIER)
@JsonSubTypes({
        @Type(value = LiteralBodyContent.class, name = "J_LITERAL"),
        @Type(value = ObjectBodyContent.class, name = "J_OBJECT"),
        @Type(value = StringBodyContent.class, name = "J_STRING"),
        @Type(value = ArrayBodyContent.class, name = "J_ARRAY")
})
public abstract class BodyContent<T> implements Body<T> {

    public static final String TYPE_IDENTIFIER = "JZONBIE_CONTENT_TYPE";

    protected BodyContent() {}

    @JsonValue public abstract T getContent();
    @JsonProperty(TYPE_IDENTIFIER) public abstract BodyContentType getType();

}