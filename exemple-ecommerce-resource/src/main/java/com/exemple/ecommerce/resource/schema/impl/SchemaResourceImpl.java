package com.exemple.ecommerce.resource.schema.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.exemple.ecommerce.resource.common.model.ResourceSchema;
import com.exemple.ecommerce.resource.core.statement.SchemaStatement;
import com.exemple.ecommerce.resource.schema.SchemaResource;

@Service
@Validated
public class SchemaResourceImpl implements SchemaResource {

    private SchemaStatement schemaStatement;

    public SchemaResourceImpl(SchemaStatement schemaStatement) {
        this.schemaStatement = schemaStatement;
    }

    @Override
    public byte[] get(String app, String version, String resource) {

        return schemaStatement.get(app, version, resource).getContent();
    }

    @Override
    public Set<String> getFilter(String app, String version, String resource) {

        return schemaStatement.get(app, version, resource).getFilters();
    }

    @Override
    public Map<String, Set<String>> getTransform(String app, String version, String resource) {

        return schemaStatement.get(app, version, resource).getTransforms();
    }

    @Override
    public Map<String, Set<String>> getRule(String app, String version, String resource) {

        return schemaStatement.get(app, version, resource).getRules();
    }

    @Override
    public void save(ResourceSchema resourceSchema) {

        schemaStatement.insert(resourceSchema);

    }

    @Override
    public Map<String, List<String>> allVersions(String app) {

        Map<String, List<String>> versions = new HashMap<>();

        schemaStatement.findByApp(app).forEach((ResourceSchema resource) -> {
            versions.putIfAbsent(resource.getResource(), new ArrayList<>());
            versions.get(resource.getResource()).add(resource.getVersion());
        });

        return versions;
    }

}
