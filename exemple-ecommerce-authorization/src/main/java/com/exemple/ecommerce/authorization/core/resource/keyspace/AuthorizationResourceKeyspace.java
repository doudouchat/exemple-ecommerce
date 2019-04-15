package com.exemple.ecommerce.authorization.core.resource.keyspace;

import java.util.Collection;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Component;

import com.exemple.ecommerce.application.common.model.ApplicationDetail;
import com.exemple.ecommerce.application.detail.ApplicationDetailService;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;

@Component
public class AuthorizationResourceKeyspace {

    private final ApplicationDetailService applicationDetailService;

    public AuthorizationResourceKeyspace(ApplicationDetailService applicationDetailService) {
        this.applicationDetailService = applicationDetailService;
    }

    public void initKeyspace(String application) {

        ApplicationDetail applicationDetail = applicationDetailService.get(application);

        ResourceExecutionContext.get().setKeyspace(applicationDetail.getKeyspace());
    }

    private void initKeyspace(Collection<String> resourceIds) {

        initKeyspace(resourceIds.stream().findFirst().orElseThrow(IllegalStateException::new));
    }

    public void initKeyspace(OAuth2Request oAuth2Request) {

        initKeyspace(oAuth2Request.getResourceIds());

    }

    public void initKeyspace(ClientDetails client) {

        initKeyspace(client.getResourceIds());

    }

}
