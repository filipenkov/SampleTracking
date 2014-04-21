package com.atlassian.crowd.directory;

import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;

import javax.naming.directory.Attributes;

/**
 * Novell eDirectory LDAP connector.
 *
 */
public class NovelleDirectory extends RFC4519Directory
{
    public NovelleDirectory(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory);
    }

    public static String getStaticDirectoryType()
    {
        return "Novell eDirectory Server";
    }

    public String getDescriptiveName()
    {
        return NovelleDirectory.getStaticDirectoryType();
    }
    
     /**
     * Novell eDirectory doesn't want passwords encoded before they're passed to the directory. At least, we don't currently
     * support it.
     * @param unencodedPassword
     * @return
     */
    protected String encodePassword(String unencodedPassword)
    {
        return unencodedPassword;
    }

    /**
     * Novell eDirectory in a default install requires the sn to be set before a user can be created.
     * @param user
     * @param attributes
     */
    @Override
    protected void getNewUserDirectorySpecificAttributes(final User user, final Attributes attributes)
    {
        addDefaultSnToUserAttributes(attributes, user.getName());
    }
}