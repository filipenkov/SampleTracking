package com.atlassian.crowd.directory;

import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;

public class OpenLDAPRfc2307 extends Rfc2307
{
    public OpenLDAPRfc2307(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory, PasswordEncoderFactory passwordEncoderFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory, passwordEncoderFactory);
    }

    public static String getStaticDirectoryType()
    {
        return "OpenLDAP (Read-Only Posix Schema)";
    }

    public String getDescriptiveName()
    {
        return OpenLDAPRfc2307.getStaticDirectoryType();
    }
}
