package com.atlassian.mail.server.managers;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class MockAuthenticator extends Authenticator
{
    private String username;
    private String password;

    public MockAuthenticator(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    public PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication(getUsername(), getPassword());
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }
}
