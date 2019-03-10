package com.exemple.ecommerce.customer.connexion.resource

interface LoginServiceResource  {
    
    Map<String , ?> saveLogin(UUID id, Map<String , ?> account)

    Map<String , ?> updateLogin(UUID id, Map<String , ?> account)
}
