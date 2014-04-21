package com.atlassian.crowd.password.encoder;

import org.springframework.security.providers.encoding.Md5PasswordEncoder;

import java.util.Locale;

/**
 * A version of {@link Md5PasswordEncoder} which supports an Ldap version via having a label of "{MD5}"
 * preappended to the encoded hash. This can be made lower-case
 * in the encoded password, if required, by setting the <tt>forceLowerCasePrefix</tt> property to true.
 */
public class LdapMd5PasswordEncoder extends Md5PasswordEncoder implements InternalPasswordEncoder, LdapPasswordEncoder
{
    private boolean forceLowerCasePrefix;

    protected static final String MD5_PREFIX = "{MD5}";

    protected static final String MD5_PREFIX_LC = MD5_PREFIX.toLowerCase(Locale.ENGLISH);

    public LdapMd5PasswordEncoder()
    {
        this.forceLowerCasePrefix = false;
        setEncodeHashAsBase64(true);
    }

    public String encodePassword(String rawPass, Object salt)
    {
        String encodedPassword = super.encodePassword(rawPass, salt);

        String prefix = forceLowerCasePrefix ? MD5_PREFIX_LC : MD5_PREFIX;

        return prefix + encodedPassword;
    }

    public boolean isPasswordValid(String encPass, String rawPass, Object salt)
    {
        String encPassWithoutPrefix;

        if (encPass.startsWith(MD5_PREFIX) || encPass.startsWith(MD5_PREFIX_LC))
        {
            encPassWithoutPrefix = encPass.substring(5);
        }
        else
        {
            encPassWithoutPrefix = encPass;            
        }

        // Compare the encoded passwords without the prefix
        return encodePassword(rawPass, salt).endsWith(encPassWithoutPrefix);
    }

    public String getKey()
    {
        return "md5";
    }

    public void setForceLowerCasePrefix(boolean forceLowerCasePrefix)
    {
        this.forceLowerCasePrefix = forceLowerCasePrefix;
    }
}
