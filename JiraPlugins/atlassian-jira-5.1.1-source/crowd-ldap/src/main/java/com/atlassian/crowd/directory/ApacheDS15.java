package com.atlassian.crowd.directory;

import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.directory.Attributes;

public class ApacheDS15 extends ApacheDS
{
    private static final Logger logger = Logger.getLogger(ApacheDS15.class);

    public ApacheDS15(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory, final PasswordEncoderFactory passwordEncoderFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory, passwordEncoderFactory);
    }

    public static String getStaticDirectoryType()
    {
        return "Apache Directory Server 1.5.x";
    }

    public String getDescriptiveName()
    {
        return ApacheDS15.getStaticDirectoryType();
    }

    /**
     * ApacheDS 1.5.x requires a non-blank uniqueMember when
     * adding a group. The bind user (usually the admin user)
     * is added as a member of the group.
     *
     * @return bind user DN.
     */
    @Override
    protected String getInitialGroupMemberDN()
    {
        return ldapPropertiesMapper.getUsername();
    }

    /**
     * ApacheDS in a default install requires the sn to be set before a user can be created.
     * @param user user to add
     * @param attributes representing the user
     */
    protected void getNewUserDirectorySpecificAttributes(final User user, final Attributes attributes)
    {
        // If no SN exists for the user add a blank SN
        if (StringUtils.isBlank(user.getLastName()))
        {
            addDefaultSnToUserAttributes(attributes, " ");
        }

        // If email is empty or null adda blank email
        if (StringUtils.isBlank(user.getEmailAddress()))
        {
            addDefaultValueToUserAttributesForAttribute(ldapPropertiesMapper.getUserEmailAttribute(), attributes, " ");
        }
    }

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

