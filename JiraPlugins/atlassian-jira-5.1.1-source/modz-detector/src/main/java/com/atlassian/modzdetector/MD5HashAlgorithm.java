package com.atlassian.modzdetector;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 hash algorithm.
 */
public class MD5HashAlgorithm implements HashAlgorithm
{
    public String getHash(final InputStream stream)
    {
        try
        {
            final MessageDigest md5 = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(stream, md5);
            IOUtils.copy(dis, new NullOutputStream());
            return new String(Hex.encodeHex(dis.getMessageDigest().digest()));
        }
        catch (IOException e)
        {
            return null;
        }
        catch (NoSuchAlgorithmException shouldNotHappen)
        {
            throw new RuntimeException(shouldNotHappen);
        }
    }

    public String getHash(final byte[] bytes)
    {
        return DigestUtils.md5Hex(bytes);
    }

    public String toString()
    {
        return "MD5 HEX";
    }

}

