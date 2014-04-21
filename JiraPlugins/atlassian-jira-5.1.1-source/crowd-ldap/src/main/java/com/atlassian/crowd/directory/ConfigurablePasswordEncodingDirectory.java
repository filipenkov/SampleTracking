package com.atlassian.crowd.directory;

import com.atlassian.crowd.password.encoder.PasswordEncoder;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;

import org.apache.commons.lang.StringUtils;

abstract class ConfigurablePasswordEncodingDirectory extends RFC4519Directory
{
    private final PasswordEncoderFactory passwordEncoderFactory;

    public ConfigurablePasswordEncodingDirectory(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory, final PasswordEncoderFactory passwordEncoderFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory);
        this.passwordEncoderFactory = passwordEncoderFactory;
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
}
