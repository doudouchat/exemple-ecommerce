package com.exemple.ecommerce.customer.subcription;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.ecommerce.customer.subcription.exception.SubscriptionServiceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;

public interface SubscriptionService {

    boolean save(@NotBlank String email, @NotNull JsonNode source, @NotBlank String app, @NotBlank String version);

    JsonNode get(@NotBlank String email, @NotBlank String app, @NotBlank String version) throws SubscriptionServiceNotFoundException;
}
