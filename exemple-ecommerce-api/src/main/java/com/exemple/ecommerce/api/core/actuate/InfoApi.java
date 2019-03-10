package com.exemple.ecommerce.api.core.actuate;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Template;
import org.springframework.stereotype.Component;

import com.exemple.ecommerce.api.common.ManifestUtils;
import com.exemple.ecommerce.api.core.actuate.model.Info;

@Path("/")
@Component
public class InfoApi {

    @Context
    private ServletContext servletContext;

    @GET
    @Template(name = "/info")
    @Produces({ MediaType.TEXT_XML, MediaType.TEXT_HTML })
    public Info info() throws IOException {

        Info result = new Info();
        result.setVersion(ManifestUtils.version(servletContext));
        result.setBuildTime(ManifestUtils.buildTime(servletContext));

        return result;
    }
}
