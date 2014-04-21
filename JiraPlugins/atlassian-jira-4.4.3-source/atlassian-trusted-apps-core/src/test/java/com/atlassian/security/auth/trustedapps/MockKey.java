package com.atlassian.security.auth.trustedapps;

import java.security.PrivateKey;
import java.security.PublicKey;

class MockKey implements PublicKey, PrivateKey
{
    public String getAlgorithm()
    {
        return "ALGY";
    }

    public byte[] getEncoded()
    {
        return new byte[] { 1, 2, 3, 4 };
    }

    public String getFormat()
    {
        return "format";
    }
}
