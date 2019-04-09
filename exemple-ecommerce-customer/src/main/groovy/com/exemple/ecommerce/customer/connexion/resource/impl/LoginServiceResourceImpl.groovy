package com.exemple.ecommerce.customer.connexion.resource.impl

import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Component

import com.exemple.ecommerce.customer.connexion.resource.LoginServiceResource

@Component
class LoginServiceResourceImpl implements LoginServiceResource {

    private static final String ID = "id"

    private static final String PASSWORD = "password"

    private static final String EMAIL = "email"
    
    private static final String ROLES = "roles"
    
    private static final String SCOPES = "scopes"
    
    @Override
    Map<String, ?> saveLogin(UUID id, Map<String, ?> account) {

        Map<String, ?> login = updateLogin(id, account)
        login.put(ROLES, ["ROLE_ACCOUNT"])
        login.put(SCOPES, ["account:read", "account:update"])
       
        return login
    }

    @Override
    Map<String, ?> updateLogin(UUID id, Map<String, ?> account) {

        Map<String, ?> login = new HashMap()
        if( account.containsKey(EMAIL)) {
            login.put(EMAIL, account.get(EMAIL))
        }
        if( account.containsKey(PASSWORD)) {
            login.put(PASSWORD, "{bcrypt}" + BCrypt.hashpw(account.get(PASSWORD), BCrypt.gensalt()))
        }
        if( account.containsKey(ID)) {
            login.put(ID, id)
        }
        return login
    }
    
    @Override
    Map<String, ?> updateLogin(Map<String, ?> source) {

        Map<String, ?> login = new HashMap()
        if( source.containsKey(PASSWORD)) {
            login.put(PASSWORD, "{bcrypt}" + BCrypt.hashpw(source.get(PASSWORD), BCrypt.gensalt()))
        }
        return login
    }
}
