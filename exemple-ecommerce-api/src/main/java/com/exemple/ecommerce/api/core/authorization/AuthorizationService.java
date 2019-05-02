package com.exemple.ecommerce.api.core.authorization;

import javax.ws.rs.core.Response;

public interface AuthorizationService {

    Response checkToken(String token, String username, String password);

}
