package com.atlassian.crowd.directory;

import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.event.api.EventPublisher;

/**
 * Generic LDAP connector.
 *
 * @author Justen Stepka <jstepka@atlassian.com>
 * @version 1.0
 */
public class GenericLDAP extends OpenLDAP
{
    public GenericLDAP(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory, PasswordEncoderFactory passwordEncoderFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory, passwordEncoderFactory);
    }

    public static String getStaticDirectoryType()
    {
        return "Generic Directory Server";
    }

    public String getDescriptiveName()
    {
        return GenericLDAP.getStaticDirectoryType();
    }
}
