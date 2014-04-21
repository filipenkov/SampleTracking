package com.atlassian.crowd.directory;

import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.password.encoder.PasswordEncoder;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;
import org.apache.commons.lang.StringUtils;

import javax.naming.directory.Attributes;

public class OpenLDAP extends RFC4519Directory
{
    private final PasswordEncoderFactory passwordEncoderFactory;

    public OpenLDAP(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory, final PasswordEncoderFactory passwordEncoderFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory);
        this.passwordEncoderFactory = passwordEncoderFactory;
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
     * Translates a clear-text password into an encrypted one, based on the directory settings.
     * @param unencodedPassword password
     * @return encoded password
     */
    protected String encodePassword(String unencodedPassword)
    {
        if (unencodedPassword == null)
        {
            return null;
        }

        String encryptionAlgorithm = ldapPropertiesMapper.getUserEncryptionMethod();
        if (!StringUtils.isBlank(encryptionAlgorithm))
        {
            PasswordEncoder passwordEncoder = passwordEncoderFactory.getLdapEncoder(encryptionAlgorithm);
            return passwordEncoder.encodePassword(unencodedPassword, null);
        }
        else
        {
            return unencodedPassword;
        }

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
