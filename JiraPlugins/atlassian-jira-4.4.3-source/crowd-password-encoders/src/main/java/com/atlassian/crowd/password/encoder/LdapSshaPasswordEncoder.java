package com.atlassian.crowd.password.encoder;

import com.atlassian.crowd.exception.PasswordEncoderException;
import com.atlassian.crowd.manager.property.PropertyManager;
import com.atlassian.crowd.manager.property.PropertyManagerException;

/**
 * This class overrides the {@link org.springframework.security.providers.ldap.authenticator.LdapShaPasswordEncoder} to specifically add salt to the SSHA
 * if it has not been provided
 */
public class LdapSshaPasswordEncoder extends org.springframework.security.providers.ldap.authenticator.LdapShaPasswordEncoder implements LdapPasswordEncoder, InternalPasswordEncoder
{
    private PropertyManager propertyManager;

    public LdapSshaPasswordEncoder()
    {
        setForceLowerCasePrefix(false);
    }

    /**
     * This method delgates to {@link org.springframework.security.providers.ldap.authenticator.LdapShaPasswordEncoder#encodePassword}, but if the passed in salt is null
     * Crowd will use the propertyManager to find the salt used for Token's and pass that along to the underlying implementation
     *
     * @param rawPass the password to encode
     * @param salt    the salt needs to be of type byte[], if null a Crowd salt value will be used
     * @return String the encoded password
     */
    public String encodePassword(String rawPass, Object salt)
    {
        String encodedPassword = null;

        if (salt == null)
        {
            try
            {
                salt = propertyManager.getTokenSeed().getBytes();

                encodedPassword = super.encodePassword(rawPass, salt);
            }
            catch (PropertyManagerException e)
            {
                throw new PasswordEncoderException(e.getMessage(), e);
            }
        }
        else
        {
            encodedPassword = super.encodePassword(rawPass, salt);
        }

        return encodedPassword;
    }

    public String getKey()
    {
        return "ssha";
    }

    ///CLOVER:OFF
    public void setPropertyManager(PropertyManager propertyManager)
    {
        this.propertyManager = propertyManager;
    }

}
