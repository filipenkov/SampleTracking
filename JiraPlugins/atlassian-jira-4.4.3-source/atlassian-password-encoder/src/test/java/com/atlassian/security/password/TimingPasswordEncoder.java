package com.atlassian.security.password;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TimingPasswordEncoder implements PasswordEncoder
{
    private static final Logger log = LoggerFactory.getLogger(TimingPasswordEncoder.class);

    private final PasswordEncoder delegate;

    public TimingPasswordEncoder(PasswordEncoder delegate)
    {
        this.delegate = delegate;
    }

    public String encodePassword(String rawPassword) throws IllegalArgumentException
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return delegate.encodePassword(rawPassword);
        }
        finally
        {
            log.info("Took {} ms to encode password", (System.currentTimeMillis() - startTime));
        }
    }

    public boolean isValidPassword(String rawPassword, String encodedPassword) throws IllegalArgumentException
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return delegate.isValidPassword(rawPassword, encodedPassword);
        }
        finally
        {
            log.info("Took {} ms to validate password", (System.currentTimeMillis() - startTime));
        }
    }

    public boolean canDecodePassword(String encodedPassword)
    {
        return delegate.canDecodePassword(encodedPassword);
    }
}
