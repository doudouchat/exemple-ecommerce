package com.exemple.ecommerce.api.login;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.BeanParam;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.ecommerce.api.common.model.ApplicationBeanParam;
import com.exemple.ecommerce.api.core.swagger.DocumentApiResource;
import com.exemple.ecommerce.customer.login.LoginService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/v1/login")
@OpenAPIDefinition(tags = @Tag(name = "login"))
@Component
public class LoginApi {

    @Autowired
    private LoginService loginService;

    @HEAD
    @Path("/{login}")
    @Operation(tags = { "login" }, security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @RolesAllowed("ROLE_APP")
    public Response check(@NotBlank @PathParam("login") String login,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) ApplicationBeanParam applicationBeanParam) {

        if (!loginService.exist(login)) {

            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.status(Status.NO_CONTENT).build();

    }
}
