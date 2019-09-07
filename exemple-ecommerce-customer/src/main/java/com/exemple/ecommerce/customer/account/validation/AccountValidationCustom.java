package com.exemple.ecommerce.customer.account.validation;

import java.util.Map;

public interface AccountValidationCustom {

    void validate(Map<String, Object> form, Map<String, Object> old);

}
