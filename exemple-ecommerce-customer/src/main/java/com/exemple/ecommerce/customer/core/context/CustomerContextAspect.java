package com.exemple.ecommerce.customer.core.context;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.exemple.ecommerce.customer.common.CustomerExecutionContext;

@Aspect
@Component
public class CustomerContextAspect {

    @Before("@target(org.springframework.stereotype.Service) "
            + "&& execution(public * com.exemple.ecommerce.customer..*(.., *, *)) && args(.., app, version)")
    public void beforeValidate(String app, String version) {

        CustomerExecutionContext context = CustomerExecutionContext.get();

        context.setApp(app);
        context.setVersion(version);
    }

    @After("@target(org.springframework.stereotype.Service) "
            + "&& execution(public * com.exemple.ecommerce.customer..*(.., *, *)) && args(.., app, version)")
    public void afterValidate(String app, String version) {

        CustomerExecutionContext.destroy();
    }

    @AfterThrowing("@target(org.springframework.stereotype.Service) "
            + "&& execution(public * com.exemple.ecommerce.customer..*(.., *, *)) && args(.., app, version)")
    public void afterValidateException(String app, String version) {

        CustomerExecutionContext.destroy();
    }

}
