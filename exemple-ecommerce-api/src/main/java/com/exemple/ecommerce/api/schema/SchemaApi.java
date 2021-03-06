package com.exemple.ecommerce.api.schema;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.ecommerce.application.common.model.ApplicationDetail;
import com.exemple.ecommerce.application.detail.ApplicationDetailService;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.exemple.ecommerce.schema.description.SchemaDescription;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.Operation;

@Path("/v1/schemas")
@Component
public class SchemaApi {

    @Autowired
    private SchemaDescription service;

    @Autowired
    private ApplicationDetailService applicationDetailService;

    @GET
    @Path("/{resource}/{app}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(hidden = true)
    public JsonNode get(@NotNull @PathParam("resource") String resource, @NotNull @PathParam("app") String app,
            @NotNull @PathParam("version") String version) {

        ApplicationDetail applicationDetail = applicationDetailService.get(app);

        ResourceExecutionContext.get().setKeyspace(applicationDetail.getKeyspace());

        return service.get(app, version, resource);

    }

    @GET
    @Path("/patch")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(hidden = true)
    public JsonNode getPatch() {

        return service.getPatch();

    }
}
