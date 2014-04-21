package com.atlassian.crowd.directory;

import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.event.api.EventPublisher;
import org.apache.log4j.Logger;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

public class OpenDS extends GenericLDAP
{
    private static final Logger logger = Logger.getLogger(OpenDS.class);

    public OpenDS(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory, PasswordEncoderFactory passwordEncoderFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory, passwordEncoderFactory);
    }

    public static String getStaticDirectoryType()
    {
        return "OpenDS";
    }

    public String getDescriptiveName()
    {
        return OpenDS.getStaticDirectoryType();
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
            // TODO: This doesn't look very fatal?
            logger.fatal(e.getMessage(), e);
        }

        return new LdapTemplate(contextSource);
    }
}
