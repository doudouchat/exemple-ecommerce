package com.exemple.ecommerce.api.common.model;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.HeaderParam;

import com.exemple.ecommerce.customer.core.CustomerExecutionContext;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

public class SchemaBeanParam extends ApplicationBeanParam {

    public static final String VERSION_HEADER = "version";

    @NotBlank
    @Parameter(name = VERSION_HEADER, in = ParameterIn.HEADER)
    private final String version;

    public SchemaBeanParam(@HeaderParam(APP_HEADER) String app, @HeaderParam(VERSION_HEADER) String version) {

        super(app);

        this.version = version;

        CustomerExecutionContext context = CustomerExecutionContext.get();
        context.setApp(app);
        context.setVersion(this.version);

    }

    public String getVersion() {
        return version;
    }

}
