package com.exemple.ecommerce.customer.account.validation.impl

import org.springframework.stereotype.Component

import com.exemple.ecommerce.customer.account.validation.AccountValidationCustom

@Component
class AccountValidationCustomImpl implements AccountValidationCustom {

    @Override
    void validate(Map<String, ?> form, Map<String, ?> old) {
    }
}
