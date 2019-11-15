package com.exemple.ecommerce.resource.core.statement;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.exemple.ecommerce.resource.core.dao.ResourceSchemaDao;
import com.exemple.ecommerce.resource.core.mapper.ResourceSchemaMapper;
import com.exemple.ecommerce.resource.schema.model.ResourceSchema;

@Component
public class SchemaStatement {

    public static final String SCHEMA_DEFAULT = "{\"$schema\": \"http://json-schema.org/draft-07/schema\",\"additionalProperties\": false}";

    private final CqlSession session;

    public SchemaStatement(CqlSession session) {
        this.session = session;
    }

    @Cacheable("schema_resource")
    public ResourceSchema get(String app, String version, String resource) {

        ResourceSchemaMapper mapper = ResourceSchemaMapper.builder(session).withDefaultKeyspace(ResourceExecutionContext.get().keyspace()).build();
        ResourceSchemaDao resourceSchemaDao = mapper.resourceSchemaDao();

        ResourceSchema resourceSchema = resourceSchemaDao.findByApplicationAndResourceAndVersion(app, resource, version);

        if (resourceSchema == null) {
            resourceSchema = new ResourceSchema();
        }

        if (resourceSchema.getContent() == null) {
            resourceSchema.setContent(SCHEMA_DEFAULT.getBytes(StandardCharsets.UTF_8));
        }

        return resourceSchema;
    }

    @Cacheable("schema_resources")
    public List<ResourceSchema> findByApp(String app) {

        ResourceSchemaMapper mapper = ResourceSchemaMapper.builder(session).withDefaultKeyspace(ResourceExecutionContext.get().keyspace()).build();
        ResourceSchemaDao resourceSchemaDao = mapper.resourceSchemaDao();

        return resourceSchemaDao.findByApplication(app).all();
    }

    public void insert(ResourceSchema resourceSchema) {

        ResourceSchemaMapper mapper = ResourceSchemaMapper.builder(session).withDefaultKeyspace(ResourceExecutionContext.get().keyspace()).build();
        ResourceSchemaDao resourceSchemaDao = mapper.resourceSchemaDao();

        resourceSchemaDao.create(resourceSchema);
    }

    @CacheEvict(cacheNames = { "schema_resource", "schema_resources" }, allEntries = true)
    public void update(ResourceSchema resourceSchema) {

        ResourceSchemaMapper mapper = ResourceSchemaMapper.builder(session).withDefaultKeyspace(ResourceExecutionContext.get().keyspace()).build();
        ResourceSchemaDao resourceSchemaDao = mapper.resourceSchemaDao();

        resourceSchemaDao.update(resourceSchema);
    }

}
