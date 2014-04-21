package com.atlassian.crowd.password.encoder;

/**
 * A plaintext password encoder
 */
public class PlaintextPasswordEncoder extends org.springframework.security.providers.encoding.PlaintextPasswordEncoder
        implements InternalPasswordEncoder, LdapPasswordEncoder
{
    public String getKey()
    {
        return "plaintext";
    }
}
