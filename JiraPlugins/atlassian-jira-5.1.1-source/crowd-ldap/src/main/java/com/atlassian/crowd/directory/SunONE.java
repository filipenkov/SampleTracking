package com.atlassian.crowd.directory;

import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;
import org.apache.log4j.Logger;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.directory.Attributes;

/**
 * Sun ONE / Sun DSEE Directory connector.
 */
public class SunONE extends RFC4519Directory
{
    private static final Logger logger = Logger.getLogger(SunONE.class);

    public SunONE(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory);
    }

    public static String getStaticDirectoryType()
    {
        return "Sun Directory Server Enterprise Edition";
    }

    public String getDescriptiveName()
    {
        return SunONE.getStaticDirectoryType();
    }

    /**
     * Sun DSEE doesn't want passwords encoded before they're passed to the directory.
     * @param unencodedPassword
     * @return
     */
    protected String encodePassword(String unencodedPassword)
    {
        return unencodedPassword;
    }

    /**
     * Sun DSEE 6.2 in a default install requires the sn to be set before a user can be created.
     * @param user
     * @param attributes
     */
    @Override
    protected void getNewUserDirectorySpecificAttributes(final User user, final Attributes attributes)
    {
        addDefaultSnToUserAttributes(attributes, "");
    }


    // CHANGE LISTENER STUFF

    protected LdapTemplate createChangeListenerTemplate()
    {
        // create a spring connection context object
        LdapContextSource contextSource = new LdapContextSource();

        contextSource.setUrl(ldapPropertiesMapper.getConnectionURL());
        contextSource.setUserDn(ldapPropertiesMapper.getUsername());
        contextSource.setPassword(ldapPropertiesMapper.getPassword());

        // let spring know of our connection attributes
        contextSource.setBaseEnvironmentProperties(getBaseEnvironmentProperties());

        // create a pool for when doing multiple calls.
        contextSource.setPooled(true);

        // by default, all results are converted to a DirContextAdapter using the
        // dirObjectFactory property of the ContextSource (we need to disable that)
        contextSource.setDirObjectFactory(null);

        try
        {
            // we need to tell the context source to configure up our ldap server
            contextSource.afterPropertiesSet();
        }
        catch (Exception e)
        {
            logger.fatal(e.getMessage(), e);
        }

        return new LdapTemplate(contextSource);
    }
}