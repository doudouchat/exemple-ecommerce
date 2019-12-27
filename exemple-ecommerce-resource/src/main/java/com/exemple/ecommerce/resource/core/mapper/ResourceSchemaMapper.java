package com.exemple.ecommerce.resource.core.mapper;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.exemple.ecommerce.resource.core.dao.ResourceSchemaDao;

@Mapper
public interface ResourceSchemaMapper {

    @DaoFactory
    ResourceSchemaDao resourceSchemaDao();

    static MapperBuilder<ResourceSchemaMapper> builder(CqlSession session) {
        return new ResourceSchemaMapperBuilder(session);
    }
}
