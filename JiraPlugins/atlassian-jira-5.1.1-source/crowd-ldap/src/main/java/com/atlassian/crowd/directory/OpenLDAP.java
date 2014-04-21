package com.atlassian.crowd.directory;

import javax.naming.directory.Attributes;

import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;

public class OpenLDAP extends ConfigurablePasswordEncodingDirectory
{
    public OpenLDAP(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory, final PasswordEncoderFactory passwordEncoderFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory, passwordEncoderFactory);
    }

    public static String getStaticDirectoryType()
    {
        return "OpenLDAP";
    }

    public String getDescriptiveName()
    {
        return OpenLDAP.getStaticDirectoryType();
    }

    /**
     * OpenLDAP 2.3.35 in a default install requires the sn to be set before a user can be created.
     * @param user user
     * @param attributes directory attributes
     */
    @Override
    protected void getNewUserDirectorySpecificAttributes(final User user, final Attributes attributes)
    {
        addDefaultSnToUserAttributes(attributes, user.getName());
    }
}
