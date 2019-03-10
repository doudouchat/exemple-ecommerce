package com.exemple.ecommerce.resource.common;

import java.text.ParseException;
import java.util.Arrays;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import com.datastax.driver.core.ParseUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public final class JsonNodeTransformUtils {

    private static final Configuration CONFIGURATION;

    private JsonNodeTransformUtils() {

    }

    static {

        CONFIGURATION = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider()).mappingProvider(new JacksonMappingProvider()).build()
                .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

    }

    public static JsonNode transform(JsonNode form, UnaryOperator<String> function, String... paths) {

        DocumentContext context = JsonPath.using(CONFIGURATION).parse(form);

        return Arrays.stream(paths).reduce(context, (DocumentContext c, String p) -> {

            JsonNode value = c.read(p);

            if (value instanceof TextNode) {
                c.set(p, function.apply(value.textValue()));
            }

            if (value instanceof ArrayNode) {
                IntStream.range(0, value.size()).forEach(i -> c.set(p.replace("*", Integer.toString(i)), function.apply(value.get(i).textValue())));
            }

            if (value instanceof ObjectNode) {
                JsonNodeUtils.stream(JsonNodeUtils.clone(value).fields()).forEach(e -> c.renameKey(p, e.getKey(), function.apply(e.getKey())));
            }

            return c;
        }, (n1, n2) -> n2).json();

    }

    public static JsonNode transformDate(JsonNode form, String... paths) {

        return transform(form, (String value) -> {
            try {
                return ParseUtils.parseDate(value).toInstant().toString();
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }, paths);

    }

}
