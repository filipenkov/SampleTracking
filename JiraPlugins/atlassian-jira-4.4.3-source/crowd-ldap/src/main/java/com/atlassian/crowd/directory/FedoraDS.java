package com.atlassian.crowd.directory;

import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.event.api.EventPublisher;

/**
 * Read-only directory connector for FedoraDS running the Posix schema.
 */
public class FedoraDS extends Rfc2307
{
    public FedoraDS(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory, PasswordEncoderFactory passwordEncoderFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory, passwordEncoderFactory);
    }

    public static String getStaticDirectoryType()
    {
        return "FedoraDS (Read-Only Posix Schema)";
    }

    public String getDescriptiveName()
    {
        return FedoraDS.getStaticDirectoryType();
    }
}
