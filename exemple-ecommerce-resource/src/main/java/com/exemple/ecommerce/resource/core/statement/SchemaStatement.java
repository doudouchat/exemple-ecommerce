package com.exemple.ecommerce.resource.core.statement;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.exemple.ecommerce.resource.common.model.ResourceSchema;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;

@Component
public class SchemaStatement extends StatementResource {

    public static final String TABLE = "resource_schema";

    public static final String APP = "app";

    public static final String VERSION = "version";

    public static final String RESOURCE = "resource";

    public static final String RESOURCE_SCHEMA = "content";

    public static final String FILTER = "filter";

    public static final String TRANSFORM = "transform";

    public static final String RULE = "rule";

    public static final String SCHEMA_DEFAULT = "{\"$schema\": \"http://json-schema.org/draft-07/schema\",\"additionalProperties\": false}";

    private final Session session;

    public SchemaStatement(Cluster cluster, Session session) {
        super(cluster, TABLE);
        this.session = session;
    }

    @Cacheable("schema_resource")
    public ResourceSchema get(String app, String version, String resource) {

        MappingManager manager = new MappingManager(session);
        Mapper<ResourceSchema> mapper = manager.mapper(ResourceSchema.class, ResourceExecutionContext.get().keyspace());
        ResourceSchema resourceSchema = mapper.get(app, resource, version);

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

        String keyspace = ResourceExecutionContext.get().keyspace();
        MappingManager manager = new MappingManager(session);
        Mapper<ResourceSchema> mapper = manager.mapper(ResourceSchema.class, keyspace);

        Select select = QueryBuilder.select().from(keyspace, TABLE);
        select.where().and(QueryBuilder.eq(APP, app));

        return mapper.map(session.execute(select)).all();
    }

    public void insert(ResourceSchema resourceSchema) {

        MappingManager manager = new MappingManager(session);
        Mapper<ResourceSchema> mapper = manager.mapper(ResourceSchema.class, ResourceExecutionContext.get().keyspace());
        mapper.save(resourceSchema);

    }

}
