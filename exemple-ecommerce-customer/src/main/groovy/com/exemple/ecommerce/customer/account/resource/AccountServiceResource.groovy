package com.exemple.ecommerce.customer.account.resource

interface AccountServiceResource  {
    
    Map<String , ?> save(UUID id, Map<String , ?> account)

    Map<String , ?> saveOrUpdateAccount(UUID id, Map<String , ?> account)
}
