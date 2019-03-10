package com.exemple.ecommerce.resource.core.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonConstraintValidator implements ConstraintValidator<Json, JsonNode> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonConstraintValidator.class);

    private String table;

    private String messageTemplate;

    @Autowired
    private JsonValidator validator;

    @Override
    public boolean isValid(JsonNode source, ConstraintValidatorContext context) {

        boolean valid = true;

        try {
            validator.valid(source, this.table);
        } catch (JsonValidatorException e) {

            valid = false;

            LOG.trace(e.getMessage(messageTemplate), e);

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage(this.messageTemplate)).addPropertyNode(e.getNode()).addConstraintViolation();
        }

        return valid;
    }

    @Override
    public void initialize(Json constraintAnnotation) {

        this.table = constraintAnnotation.table();
        this.messageTemplate = constraintAnnotation.message();

    }

}
