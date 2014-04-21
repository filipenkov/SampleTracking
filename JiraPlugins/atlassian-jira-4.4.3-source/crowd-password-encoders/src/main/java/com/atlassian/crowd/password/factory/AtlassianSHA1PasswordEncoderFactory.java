package com.atlassian.crowd.password.factory;

import com.atlassian.crowd.exception.PasswordEncoderException;
import com.atlassian.crowd.exception.PasswordEncoderNotFoundException;
import com.atlassian.crowd.password.encoder.AtlassianSHA1PasswordEncoder;
import com.atlassian.crowd.password.encoder.PasswordEncoder;

import java.util.Collections;
import java.util.Set;

/**
 * Always returns a single {@link com.atlassian.crowd.password.factory.AtlassianSHA1PasswordEncoderFactory}.
 */
public class AtlassianSHA1PasswordEncoderFactory implements PasswordEncoderFactory
{
    private final AtlassianSHA1PasswordEncoder passwordEncoder;

    public AtlassianSHA1PasswordEncoderFactory()
    {
        this.passwordEncoder = new AtlassianSHA1PasswordEncoder();
    }

    public PasswordEncoder getInternalEncoder(String encoder) throws PasswordEncoderNotFoundException
    {
        return passwordEncoder;
    }

    public PasswordEncoder getLdapEncoder(String encoder) throws PasswordEncoderNotFoundException
    {
        throw new PasswordEncoderNotFoundException("LDAP password encoder not configured");
    }

    public PasswordEncoder getEncoder(String encoder) throws PasswordEncoderNotFoundException
    {
        return passwordEncoder;
    }

    public Set<String> getSupportedInternalEncoders()
    {
        return Collections.singleton(passwordEncoder.getKey());
    }

    public Set<String> getSupportedLdapEncoders()
    {
        return Collections.emptySet();
    }

    public void addEncoder(PasswordEncoder passwordEncoder) throws PasswordEncoderException
    {
        // no-op we only use the pre-configured encoder
    }

    public void removeEncoder(PasswordEncoder passwordEncoder)
    {
        // no-op we only use the pre-configured encoder
    }
}
