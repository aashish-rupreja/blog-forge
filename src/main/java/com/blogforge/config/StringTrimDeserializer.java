package com.blogforge.config;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class StringTrimDeserializer extends StdDeserializer<String> {

    public StringTrimDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        return (p.getValueAsString() == null) ? null : p.getValueAsString().trim();
    }
}
