package com.exemple.ecommerce.resource.account.exception;

import java.text.MessageFormat;

public class AccountLoginResourceExistException extends AccountLoginResourceException {

    protected static final String EXCEPTION_MESSAGE = "Login {0} already exists";

    private static final long serialVersionUID = 1L;

    private final String login;

    public AccountLoginResourceExistException(String login) {
        super(MessageFormat.format(EXCEPTION_MESSAGE, login));
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

}