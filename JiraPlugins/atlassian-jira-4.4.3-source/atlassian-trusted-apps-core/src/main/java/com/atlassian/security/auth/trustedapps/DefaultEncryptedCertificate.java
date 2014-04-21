package com.atlassian.security.auth.trustedapps;

public class DefaultEncryptedCertificate implements EncryptedCertificate
{
    private final String id;
    private final String key;
    private final String certificate;
    private final Integer protocolVersion;
    private final String magic;
    private final String signature;

    /**
     * Constructor for protocol '0' certificates
     *
     * @param id The application id
     * @param key The application public key
     * @param certificate The certificate string
     */
    public DefaultEncryptedCertificate(String id, String key, String certificate)
    {
        this(id, key, certificate, null, null, null);
    }

    /**
     * Constructor for protocol '1' certificates
     */
    public DefaultEncryptedCertificate(String id, String key, String certificate, Integer protocolVersion, String magic)
    {
        this(id, key, certificate, protocolVersion, magic, null);
    }

    /**
     * Constructor for protocol '2' certificates
     */
    public DefaultEncryptedCertificate(String id, String key, String certificate, Integer protocolVersion, String magic, String signature)
    {
        Null.not("id", id);
        Null.not("key", key);
        Null.not("certificate", certificate);

        this.id = id;
        this.key = key;
        this.certificate = certificate;
        this.protocolVersion = protocolVersion;
        this.magic = magic;
        
        this.signature = signature;
    }
    
    public String getCertificate()
    {
        return certificate;
    }

    public String getID()
    {
        return id;
    }

    public String getSecretKey()
    {
        return key;
    }

    public Integer getProtocolVersion()
    {
        return protocolVersion;
    }

    public String getMagicNumber()
    {
        return magic;
    }
    
    public String getSignature()
    {
        return signature;
    }
}