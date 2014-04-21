package com.atlassian.security.auth.trustedapps;

import org.bouncycastle.util.encoders.Base64;

import java.io.UnsupportedEncodingException;

/**
 * Encode/decode byte[] to Strings.
 */
interface Transcoder
{
    String encode(byte[] data);

    byte[] decode(String encoded);

    byte[] getBytes(String data);

    /**
     * Standard implemetation.
     */
    static class Base64Transcoder implements Transcoder
    {
        public String encode(byte[] data)
        {
            try
            {
                return new String(Base64.encode(data), TrustedApplicationUtils.Constant.CHARSET_NAME);
            }
            // /CLOVER:OFF
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
            // /CLOVER:ON
        }

        public byte[] decode(String encoded)
        {
            return decode(getBytes(encoded));
        }

        byte[] decode(byte[] encoded)
        {
            return Base64.decode(encoded);
        }

        public byte[] getBytes(String data)
        {
            try
            {
                return data.getBytes(TrustedApplicationUtils.Constant.CHARSET_NAME);
            }
            // /CLOVER:OFF
            catch (UnsupportedEncodingException e)
            {
                throw new AssertionError(e);
            }
            // /CLOVER:ON
        }
    }
}