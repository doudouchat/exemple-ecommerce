package com.exemple.ecommerce.authorization.core.resource.keyspace;

import java.util.Collection;

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

    public void initKeyspace(Collection<String> resourceIds) {

        ApplicationDetail applicationDetail = applicationDetailService.get(resourceIds.stream().findFirst().orElseThrow(IllegalStateException::new));

        ResourceExecutionContext.get().setKeyspace(applicationDetail.getKeyspace());
    }

}
