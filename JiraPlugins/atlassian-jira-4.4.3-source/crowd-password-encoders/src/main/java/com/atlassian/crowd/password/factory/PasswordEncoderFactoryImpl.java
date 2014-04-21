package com.atlassian.crowd.password.factory;

import com.atlassian.crowd.exception.PasswordEncoderException;
import com.atlassian.crowd.exception.PasswordEncoderNotFoundException;
import com.atlassian.crowd.password.encoder.InternalPasswordEncoder;
import com.atlassian.crowd.password.encoder.LdapPasswordEncoder;
import com.atlassian.crowd.password.encoder.PasswordEncoder;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @see PasswordEncoderFactory
 */
public class PasswordEncoderFactoryImpl implements PasswordEncoderFactory
{
    private Map<String, PasswordEncoder> internalEncoders = new HashMap<String, PasswordEncoder>();
    private Map<String, PasswordEncoder> ldapEncoders = new HashMap<String, PasswordEncoder>();

    private Set<String> supportedInternalEncoders = null;
    private Set<String> supportedLdapEncoders = null;

    private static final Logger LOGGER = Logger.getLogger(PasswordEncoderFactoryImpl.class);

    public void setEncoders(List<PasswordEncoder> encoders)
    {
        for (PasswordEncoder encoder : encoders)
            addEncoder(encoder);
    }

    public PasswordEncoder getInternalEncoder(String encoder)
    {
        PasswordEncoder passwordEncoder = internalEncoders.get(encoder);

        if (passwordEncoder == null)
        {
            throw new PasswordEncoderNotFoundException("The PasswordEncoder '" + encoder + "' was not found in the Internal Encoders list by the PasswordEncoderFactory");
        }

        return passwordEncoder;
    }

    public PasswordEncoder getLdapEncoder(String encoder)
    {
        PasswordEncoder passwordEncoder = ldapEncoders.get(encoder);

        if (passwordEncoder == null)
        {
            throw new PasswordEncoderNotFoundException("The PasswordEncoder '" + encoder + "' was not found in the LDAP Encoders list by the PasswordEncoderFactory");
        }

        return passwordEncoder;
    }

    public PasswordEncoder getEncoder(String encoder) throws PasswordEncoderNotFoundException
    {
        PasswordEncoder passwordEncoder = ldapEncoders.get(encoder);

        if (passwordEncoder == null)
        {
            passwordEncoder = internalEncoders.get(encoder);
        }

        if (passwordEncoder == null)
        {
            throw new PasswordEncoderNotFoundException("The PasswordEncoder '" + encoder + "' was not found in the encoders list by the PasswordEncoderFactory");
        }

        return passwordEncoder;
    }

    public Set<String> getSupportedInternalEncoders()
    {
        if (supportedInternalEncoders == null)
        {
            supportedInternalEncoders = Collections.unmodifiableSet(new TreeSet<String>(internalEncoders.keySet()));
        }

        return supportedInternalEncoders;
    }

    public Set<String> getSupportedLdapEncoders()
    {
        if (supportedLdapEncoders == null)
        {
            supportedLdapEncoders = Collections.unmodifiableSet(new TreeSet<String>(ldapEncoders.keySet()));
        }

        return supportedLdapEncoders;
    }

    public void addEncoder(PasswordEncoder passwordEncoder) throws PasswordEncoderException
    {
        if (passwordEncoder == null)
        {
            throw new PasswordEncoderException("You cannot add a null password encoder to the factory");
        }

        if (passwordEncoder.getKey() == null)
        {
            throw new PasswordEncoderException("Your password encoder must contain a 'key' value");
        }

        if (passwordEncoder instanceof LdapPasswordEncoder)
        {
            LOGGER.debug("Adding LDAP Password Encoder to Factory: " + passwordEncoder.getKey());
            ldapEncoders.put(passwordEncoder.getKey().toLowerCase(Locale.ENGLISH), passwordEncoder);
        }

        if (passwordEncoder instanceof InternalPasswordEncoder)
        {
            LOGGER.debug("Adding Internal Password Encoder to Factory: " + passwordEncoder.getKey());
            internalEncoders.put(passwordEncoder.getKey().toLowerCase(Locale.ENGLISH), passwordEncoder);
        }

        try
        {
            getEncoder(passwordEncoder.getKey());
        }
        catch (PasswordEncoderNotFoundException e)
        {
            throw new PasswordEncoderException("Your password encoder does not support a valid encoder type.");
        }
    }

    public void removeEncoder(PasswordEncoder passwordEncoder)
    {
        synchronized (this)
        {
            internalEncoders.remove(passwordEncoder.getKey().toLowerCase(Locale.ENGLISH));
            ldapEncoders.remove(passwordEncoder.getKey().toLowerCase(Locale.ENGLISH));
        }
    }
}
