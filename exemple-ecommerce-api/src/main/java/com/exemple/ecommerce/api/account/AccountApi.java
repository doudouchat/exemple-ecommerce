package com.exemple.ecommerce.api.account;

import java.net.URI;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.ecommerce.api.common.PatchUtils;
import com.exemple.ecommerce.api.common.model.SchemaBeanParam;
import com.exemple.ecommerce.api.common.security.ApiSecurityContext;
import com.exemple.ecommerce.api.common.security.ApiSecurityContext.Resource;
import com.exemple.ecommerce.api.core.swagger.DocumentApiResource;
import com.exemple.ecommerce.customer.account.AccountService;
import com.exemple.ecommerce.customer.account.exception.AccountServiceException;
import com.exemple.ecommerce.customer.account.exception.AccountServiceNotFoundException;
import com.exemple.ecommerce.resource.core.statement.AccountStatement;
import com.exemple.ecommerce.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/v1/account")
@OpenAPIDefinition(tags = @Tag(name = "account"))
@Component
public class AccountApi {

    private static final Logger LOG = LoggerFactory.getLogger(AccountApi.class);

    private static final String ACCOUNT_SCHEMA = "Account";

    @Autowired
    private AccountService service;

    @Autowired
    private SchemaValidation schemaValidation;

    @Context
    private ContainerRequestContext servletContext;

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = { "account" }, security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Account Data", responseCode = "200", content = @Content(schema = @Schema(ref = ACCOUNT_SCHEMA))),
            @ApiResponse(description = "Account is not found", responseCode = "404"),
            @ApiResponse(description = "Account is not found accessible", responseCode = "403")

    })
    @RolesAllowed("account:read")
    public JsonNode get(@NotNull @PathParam("id") UUID id, @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam)
            throws AccountServiceException {

        checkAuthorization(id);

        return service.get(id);

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = { "account" }, security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Account is created", responseCode = "201", headers = {
                    @Header(name = "Location", description = "Links to Account Data", schema = @Schema(type = "string")) })

    })
    @RolesAllowed("account:create")
    public Response create(@NotNull @Parameter(schema = @Schema(ref = ACCOUNT_SCHEMA)) JsonNode account,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam, @Context UriInfo uriInfo)
            throws AccountServiceException {

        JsonNode source = service.save(account);

        return Response.status(Status.CREATED).location(URI.create(uriInfo.getAbsolutePath() + "/" + source.get(AccountStatement.ID).textValue()))
                .build();

    }

    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = { "account" }, security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Account is updated", responseCode = "204"),
            @ApiResponse(description = "Account is not found", responseCode = "404"),
            @ApiResponse(description = "Account is not found accessible", responseCode = "403")

    })
    @RolesAllowed("account:update")
    public Response update(@NotNull @PathParam("id") UUID id, @NotNull @Parameter(schema = @Schema(name = "Patch")) ArrayNode patch,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam) throws AccountServiceException {

        checkAuthorization(id);

        schemaValidation.validatePatch(patch);

        JsonNode source = service.get(id);
        JsonNode account = PatchUtils.diff(patch, source);
        LOG.debug("account update {}", account);

        service.save(id, account);

        return Response.status(Status.NO_CONTENT).build();

    }

    private void checkAuthorization(UUID id) throws AccountServiceNotAuthorizedException {

        if (!((ApiSecurityContext) servletContext.getSecurityContext()).isAuthorized(id.toString(), Resource.ACCOUNT)) {

            throw new AccountServiceNotAuthorizedException();
        }
    }

    @Provider
    public static class AccountServiceNotFoundExceptionMapper implements ExceptionMapper<AccountServiceNotFoundException> {

        @Override
        public Response toResponse(AccountServiceNotFoundException ex) {

            return Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build();

        }

    }

    @Provider
    public static class AccountServiceNotAuthorizedExceptionMapper implements ExceptionMapper<AccountServiceNotAuthorizedException> {

        @Override
        public Response toResponse(AccountServiceNotAuthorizedException ex) {

            return Response.status(Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).build();

        }

    }

    private static class AccountServiceNotAuthorizedException extends AccountServiceException {

        private static final long serialVersionUID = 1L;

    }
}
