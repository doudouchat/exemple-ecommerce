package com.exemple.ecommerce.customer.account.resource.impl

import java.time.OffsetDateTime

import org.springframework.stereotype.Component

import com.exemple.ecommerce.customer.account.resource.AccountServiceResource

@Component
class AccountServiceResourceImpl implements AccountServiceResource {

    private static final String CREATION_DATE = "creation_date"

    @Override
    Map<String, ?> save(UUID id, Map<String, ?> account) {

        account.put(CREATION_DATE, OffsetDateTime.now().toString())

        return account
    }

    @Override
    Map<String, ?> saveOrUpdateAccount(UUID id, Map<String, ?> account) {

        return account
    }
}
