package com.exemple.ecommerce.api.core.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import com.exemple.ecommerce.resource.core.ResourceExecutionContext;

public class ExecutionContextResponseFilter implements ContainerResponseFilter {

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        ResourceExecutionContext.destroy();

    }

}
