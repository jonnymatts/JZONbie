package com.jonnymatts.jzonbie.jackson.body;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.jonnymatts.jzonbie.body.*;

import static com.jonnymatts.jzonbie.jackson.body.BodyContentMixIn.TYPE_IDENTIFIER;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.WRAPPER_OBJECT,
        property = TYPE_IDENTIFIER)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LiteralBodyContent.class, name = "literal"),
        @JsonSubTypes.Type(value = ObjectBodyContent.class, name = "object"),
        @JsonSubTypes.Type(value = StringBodyContent.class, name = "string"),
        @JsonSubTypes.Type(value = ArrayBodyContent.class, name = "array")
})
public abstract class BodyContentMixIn<T> {

    static final String TYPE_IDENTIFIER = "JZONBIE_CONTENT_TYPE";

    @JsonValue public abstract T getContent();
    @JsonProperty(TYPE_IDENTIFIER) public BodyContentType getType() {return null;}

}