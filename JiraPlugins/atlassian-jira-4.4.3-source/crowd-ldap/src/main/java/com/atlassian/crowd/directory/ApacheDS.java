package com.atlassian.crowd.directory;

import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;

import javax.naming.directory.Attributes;

public class ApacheDS extends RFC4519Directory
{
    public ApacheDS(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory);
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
     * ApacheDS doesn't want passwords encoded before they're passed to the directory. At least, we don't currently
     * support it.
     * @param unencodedPassword
     * @return
     */
    protected String encodePassword(String unencodedPassword)
    {
        return unencodedPassword;
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
