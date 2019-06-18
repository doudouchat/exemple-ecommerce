package com.exemple.ecommerce.customer.connexion.resource

interface LoginServiceResource  {
    
    Map<String , ?> saveLogin(Map<String , ?> source)
    
    Map<String , ?> updateLogin(Map<String , ?> source)
}
