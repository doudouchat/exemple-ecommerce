package com.exemple.ecommerce.customer.connexion.resource.impl

import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Component

import com.exemple.ecommerce.customer.connexion.resource.LoginServiceResource

@Component
class LoginServiceResourceImpl implements LoginServiceResource {

    private static final String PASSWORD = "password"
    
    private static final String ROLES = "roles"
    
    private static final String SCOPES = "scopes"
    
    @Override
    Map<String, ?> saveLogin(Map<String, ?> source) {

        Map<String, ?> login = updateLogin(source)
        login.put(ROLES, ["ROLE_ACCOUNT"])
        login.put(SCOPES, ["account:read", "account:update", "login:update", "login:delete"])
       
        return login
    }
    
    @Override
    Map<String, ?> updateLogin(Map<String, ?> source) {

        if( source.containsKey(PASSWORD)) {
            source.put(PASSWORD, "{bcrypt}" + BCrypt.hashpw(source.get(PASSWORD), BCrypt.gensalt()))
        }
        return source
    }
}
