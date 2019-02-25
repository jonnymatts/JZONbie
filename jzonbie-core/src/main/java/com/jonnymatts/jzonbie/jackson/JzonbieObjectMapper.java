package com.jonnymatts.jzonbie.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

public class JzonbieObjectMapper extends ObjectMapper {

    public JzonbieObjectMapper() {
        super();
        registerModule(new Jdk8Module());
        registerModule(new JavaTimeModule());
        enable(INDENT_OUTPUT);
        setSerializationInclusion(NON_NULL);
    }
}
