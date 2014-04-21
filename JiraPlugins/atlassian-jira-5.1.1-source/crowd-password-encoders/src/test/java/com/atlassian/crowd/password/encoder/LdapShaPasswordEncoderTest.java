package com.atlassian.crowd.password.encoder;

import org.junit.Test;
import org.springframework.security.providers.ldap.authenticator.LdapShaPasswordEncoder;

import static org.junit.Assert.assertEquals;

public class LdapShaPasswordEncoderTest
{
    @Test
    public void confirmThatHashedFormMatchesApacheDsSample()
    {
        assertEquals("{SHA}nU4eI71bcnBGqeO0t9tXvY1u5oQ=",
                new LdapShaPasswordEncoder().encodePassword("pass", null));
    }
}
