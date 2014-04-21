package com.atlassian.crowd.directory;

import javax.naming.directory.Attributes;

import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;

public class ApacheDS extends ConfigurablePasswordEncodingDirectory
{
    public ApacheDS(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory, final PasswordEncoderFactory passwordEncoderFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory, passwordEncoderFactory);
    }

    public static String getStaticDirectoryType()
    {
        return "Apache Directory Server 1.0.x";
    }

    public String getDescriptiveName()
    {
        return ApacheDS.getStaticDirectoryType();
    }

    /**
     * ApacheDS in a default install requires the sn to be set before a user can be created.
     * @param user user to add
     * @param attributes representing the user
     */
    protected void getNewUserDirectorySpecificAttributes(final User user, final Attributes attributes)
    {
        // If no SN exists for the user add a blank SN
        addDefaultSnToUserAttributes(attributes, "");
    }
}
