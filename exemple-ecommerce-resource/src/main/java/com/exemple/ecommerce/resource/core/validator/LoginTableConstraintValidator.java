package com.exemple.ecommerce.resource.core.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.statement.ParameterStatement;
import com.fasterxml.jackson.databind.JsonNode;

public class LoginTableConstraintValidator implements ConstraintValidator<LoginTable, JsonNode> {

    private static final Logger LOG = LoggerFactory.getLogger(LoginTableConstraintValidator.class);

    private String table;

    private String messageTemplate;

    @Autowired
    private JsonValidator validator;

    @Autowired
    private ParameterStatement parameterStatement;

    @Override
    public boolean isValid(JsonNode source, ConstraintValidatorContext context) {

        boolean valid = true;

        if (source != null) {

            JsonNode loginNode = JsonNodeUtils.clone(source, parameterStatement.getLogins().stream().toArray(String[]::new));
            try {
                validator.valid(loginNode, this.table);
            } catch (JsonValidatorException e) {

                valid = false;

                LOG.trace(e.getMessage(messageTemplate), e);

                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(e.getMessage(this.messageTemplate)).addPropertyNode(e.getNode())
                        .addConstraintViolation();
            }
        }

        return valid;
    }

    @Override
    public void initialize(LoginTable constraintAnnotation) {

        this.table = constraintAnnotation.table();
        this.messageTemplate = constraintAnnotation.message();

    }
}
