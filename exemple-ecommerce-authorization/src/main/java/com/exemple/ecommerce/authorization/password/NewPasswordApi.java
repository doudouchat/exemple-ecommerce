package com.exemple.ecommerce.authorization.password;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.exemple.ecommerce.application.common.model.ApplicationDetail;
import com.exemple.ecommerce.application.detail.ApplicationDetailService;
import com.exemple.ecommerce.authorization.common.security.AuthorizationContextSecurity;
import com.exemple.ecommerce.authorization.core.feature.FeatureConfiguration;
import com.exemple.ecommerce.authorization.password.model.NewPassword;
import com.exemple.ecommerce.authorization.password.properties.PasswordProperties;
import com.exemple.ecommerce.resource.account.AccountResource;
import com.exemple.ecommerce.resource.core.statement.LoginStatement;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;

@Path("/v1/new_password")
@Component
public class NewPasswordApi {

    @Context
    private ContainerRequestContext servletContext;

    @Autowired
    private LoginResource loginResource;

    @Autowired
    private AccountResource accountResource;

    @Autowired
    private Algorithm algorithm;

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> template;

    @Autowired
    private ApplicationDetailService applicationDetailService;

    @Autowired
    private PasswordProperties passwordProperties;

    @Autowired
    private Clock clock;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "ROLE_APP", "ROLE_TRUSTED_CLIENT" })
    public Response post(@NotNull @Valid NewPassword newPassword, @NotBlank @HeaderParam(FeatureConfiguration.APP_HEADER) String app) {

        AuthorizationContextSecurity securityContext = (AuthorizationContextSecurity) servletContext.getSecurityContext();

        ApplicationDetail applicationDetail = applicationDetailService.get(app);

        Date expiresAt = Date.from(Instant.now(clock)
                .plus(ObjectUtils.defaultIfNull(applicationDetail.getExpiryTimePassword(), passwordProperties.getExpiryTime()), ChronoUnit.SECONDS));

        Map<String, Object> data = new HashMap<>();

        loginResource.get(newPassword.getLogin()).ifPresent((JsonNode login) -> {

            UUID id = UUID.fromString(login.get(LoginStatement.ID).textValue());

            String accessToken = JWT.create().withArrayClaim("scope", new String[] { "login:update", "login:read" })
                    .withClaim("user_name", newPassword.getLogin()).withExpiresAt(expiresAt)
                    .withClaim("client_id", securityContext.getAuthentication().getOAuth2Request().getClientId())
                    .withAudience(securityContext.getAuthentication().getOAuth2Request().getResourceIds().stream().toArray(String[]::new))
                    .withArrayClaim("authorities", securityContext.getAuthentication().getOAuth2Request().getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority).toArray(String[]::new))
                    .sign(algorithm);

            data.put("token", accessToken);

            accountResource.get(id).ifPresent((JsonNode account) -> data.put("account", account));

            if (!securityContext.isUserInRole("ROLE_TRUSTED_CLIENT")) {

                Message<Map<String, Object>> message = MessageBuilder.withPayload(data).setHeader(KafkaHeaders.TOPIC, "new_password").build();
                template.send(message);

            }

        });

        if (!securityContext.isUserInRole("ROLE_TRUSTED_CLIENT")) {

            return Response.status(Status.NO_CONTENT).build();
        }

        return Response.status(Status.OK).entity(data).build();

    }

}
