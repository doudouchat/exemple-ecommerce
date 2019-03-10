package com.exemple.ecommerce.resource.common;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.fd.hamcrest.jackson.IsJsonArray;

public class JsonNodeTransformUtilsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void transform() {

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupont@gmail.com");
        model.put("creation_date", "2010-06-30 01:20:30.000Z");
        model.put("update_date", null);

        Map<String, Object> notes = new HashMap<>();
        notes.put("2001-01-01 00:00:00.000Z", "note 1");
        notes.put("2002-01-01 00:00:00.000Z", "note 2");
        model.put("notes", notes);

        List<Object> preferences = new ArrayList<>();
        preferences.add(Arrays.asList("pref1", "2003-01-01 00:00:00.000Z"));
        preferences.add(Arrays.asList("pref2", "2004-01-01 00:00:00.000Z"));
        model.put("preferences", preferences);

        model.put("preference", Arrays.asList("pref1", "2005-01-01 00:00:00.000Z"));

        Map<String, Object> cgu0 = new HashMap<>();
        cgu0.put("version", "v1");
        cgu0.put("date", "2006-01-01 00:00:00.000Z");

        Map<String, Object> cgu1 = new HashMap<>();
        cgu1.put("version", "v2");
        cgu1.put("date", "2007-01-01 00:00:00.000Z");

        model.put("cgus", Arrays.asList(cgu0, cgu1));

        JsonNode newData = JsonNodeTransformUtils.transformDate(MAPPER.convertValue(model, JsonNode.class),
                // exist
                "$.creation_date",
                // null
                "$.update_date",
                // missing
                "$.last_date",
                // keys
                "$.notes",
                // tuple
                "$.preference[1]",
                // list tuple
                "$.preferences[*][1]",
                // list map
                "$.cgus[*].date");

        assertThat(newData, hasJsonField("creation_date", "2010-06-30T01:20:30Z"));
        assertThat(newData, hasJsonField("update_date", "null"));
        assertThat(newData, hasJsonField("notes", hasJsonField("2001-01-01T00:00:00Z", "note 1"), hasJsonField("2002-01-01T00:00:00Z", "note 2")));
        assertThat(newData, hasJsonField("preferences", IsJsonArray.isJsonArray(IsJsonArray.isJsonArray("pref1", "2003-01-01T00:00:00Z"),
                IsJsonArray.isJsonArray("pref2", "2004-01-01T00:00:00Z"))));
        assertThat(newData, hasJsonField("preference", IsJsonArray.isJsonArray("pref1", "2005-01-01T00:00:00Z")));
        assertThat(newData, hasJsonField("cgus",
                IsJsonArray.isJsonArray(hasJsonField("date", "2006-01-01T00:00:00Z"), hasJsonField("date", "2007-01-01T00:00:00Z"))));

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void transformFailure() {

        Map<String, Object> data = new HashMap<>();
        data.put("date_creation", "bad");

        JsonNodeTransformUtils.transformDate(MAPPER.convertValue(data, JsonNode.class), "$.date_creation");
    }

}
