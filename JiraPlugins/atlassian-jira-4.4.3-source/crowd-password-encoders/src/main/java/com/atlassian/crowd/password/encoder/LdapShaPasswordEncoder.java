package com.atlassian.crowd.password.encoder;

/**
 * An LDAP based SHA encoder that extends {@link org.acegisecurity.providers.ldap.authenticator.LdapShaPasswordEncoder}
 */
public class LdapShaPasswordEncoder extends org.springframework.security.providers.ldap.authenticator.LdapShaPasswordEncoder
        implements LdapPasswordEncoder, InternalPasswordEncoder
{

    public LdapShaPasswordEncoder()
    {
        setForceLowerCasePrefix(false);
    }

    public String getKey()
    {
        return "sha";
    }
}
