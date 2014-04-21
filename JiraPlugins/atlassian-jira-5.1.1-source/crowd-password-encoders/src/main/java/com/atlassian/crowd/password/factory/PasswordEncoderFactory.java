package com.atlassian.crowd.password.factory;

import com.atlassian.crowd.exception.PasswordEncoderException;
import com.atlassian.crowd.exception.PasswordEncoderNotFoundException;
import com.atlassian.crowd.password.encoder.PasswordEncoder;

import java.util.Set;

/**
 * This factory manages the getting of a PasswordEncoder, based on given encoder key
 * such as 'MD5', 'SSHA', 'SHA', "PLAINTEXT, "DES"
 */
public interface PasswordEncoderFactory
{
    public static final String DES_ENCODER = "des";
    public static final String SSHA_ENCODER = "ssha";
    public static final String SHA_ENCODER = "sha";
    public static final String PLAINTEXT_ENCODER = "plaintext";
    public static final String MD5_ENCODER = "md5";
    public static final String ATLASSIAN_SECURITY_ENCODER = "atlassian-security";

    /**
     * This will return an Internal encoder for the given key. If no encoder is not found
     * a PasswordEncoderNotFoundException will be returned.
     *
     * @param encoder the encoder requested, eg "ssha"
     * @return the password encoder for a given type
     * @throws com.atlassian.crowd.exception.PasswordEncoderNotFoundException if no encoder is found matching the given key
     */
    public PasswordEncoder getInternalEncoder(String encoder) throws PasswordEncoderNotFoundException;

    /**
     * This will return an LDAP encoder for the given key. If no encoder is not found
     * a PasswordEncoderNotFoundException will be returned.
     *
     * @param encoder the encoder requested, eg "ssha"
     * @return the password encoder for a given type
     * @throws com.atlassian.crowd.exception.PasswordEncoderNotFoundException if no encoder is found matching the given key
     */
    public PasswordEncoder getLdapEncoder(String encoder) throws PasswordEncoderNotFoundException;

    /**
     * This will return an LDAP encoder for the given key. If no encoder is not found
     * a PasswordEncoderNotFoundException will be returned.
     *
     * @param encoder the encoder requested, eg "ssha"
     * @return the password encoder for a given type
     * @throws com.atlassian.crowd.exception.PasswordEncoderNotFoundException if no encoder is found matching the given key
     */
    public PasswordEncoder getEncoder(String encoder) throws PasswordEncoderNotFoundException;
    /**
     * This will return a list of supported encoders suitable for use in an Internal Directory, this will be the values
     * held by {@link com.atlassian.crowd.password.encoder.PasswordEncoder#getKey()}
     *
     * @return Set<String>'s of supported encoders
     */
    public Set<String> getSupportedInternalEncoders();

    /**
     * This will return a list of supported encoders for a Remote Directory using LDAP, this will be the values
     * held by {@link com.atlassian.crowd.password.encoder.PasswordEncoder#getKey()}
     *
     * @return Set<String> of supported encoders
     */
    public Set<String> getSupportedLdapEncoders();

    /**
     * Hook to add encoders to the factory
     * @param passwordEncoder the password encoder to have available in the factory
     * @throws com.atlassian.crowd.exception.PasswordEncoderException if there was an issue add the encoder to the factory
     */
    public void addEncoder(PasswordEncoder passwordEncoder) throws PasswordEncoderException;

    /**
     * Will remove a given encoder from the available encoders in the PasswordEncoder factory
     * @param passwordEncoder
     */
    void removeEncoder(PasswordEncoder passwordEncoder);
}
