package com.atlassian.gadgets.renderer.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.Map;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import com.google.common.collect.Maps;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shindig.common.crypto.BlobCrypterException;
import org.apache.shindig.common.crypto.Crypto;

/** Simple implementation of BlobCrypter that doesn't check for expiration. */
public class NonExpirableBlobCrypterImpl extends BlobCrypterImpl
{
    // Labels for key derivation
    private static final byte CIPHER_KEY_LABEL = 0;
    private static final byte HMAC_KEY_LABEL = 1;

    /** minimum length of master key */
    public static final int MASTER_KEY_MIN_LEN = 16;

    private static final String UTF8 = "UTF-8";

    private byte[] cipherKey;
    private byte[] hmacKey;

    /**
     * Constructor.
     *
     * @param factory    the {@code PluginSettingsFactory} to use
     * @param txTemplate the {@code TransactionTemplate} to use
     */
    public NonExpirableBlobCrypterImpl(PluginSettingsFactory factory, TransactionTemplate txTemplate)
    {
        super(factory, txTemplate);
        init(getKey(factory).getBytes());
    }

    @Override
    public Map<String, String> unwrap(String in, int maxAgeSec) throws BlobCrypterException
    {
        try
        {
            byte[] bin = org.apache.commons.codec.binary.Base64.decodeBase64(in.getBytes());
            byte[] hmac = new byte[Crypto.HMAC_SHA1_LEN];
            byte[] cipherText = new byte[bin.length - Crypto.HMAC_SHA1_LEN];
            System.arraycopy(bin, 0, cipherText, 0, cipherText.length);
            System.arraycopy(bin, cipherText.length, hmac, 0, hmac.length);
            Crypto.hmacSha1Verify(hmacKey, cipherText, hmac);
            byte[] plain = Crypto.aes128cbcDecrypt(cipherKey, cipherText);
            Map<String, String> out = deserialize(plain);
            return out;
        }
        catch (GeneralSecurityException e)
        {
            throw new BlobCrypterException("Invalid token signature", e);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new BlobCrypterException("Invalid token format", e);
        }
        catch (NegativeArraySizeException e)
        {
            throw new BlobCrypterException("Invalid token format", e);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new BlobCrypterException(e);
        }
    }

    private void init(byte[] masterKey)
    {
        if (masterKey.length < MASTER_KEY_MIN_LEN)
        {
            throw new IllegalArgumentException("Master key needs at least " +
                                               MASTER_KEY_MIN_LEN + " bytes");
        }
        cipherKey = deriveKey(CIPHER_KEY_LABEL, masterKey, Crypto.CIPHER_KEY_LEN);
        hmacKey = deriveKey(HMAC_KEY_LABEL, masterKey, 0);
    }

    /**
     * Generates unique keys from a master key.
     *
     * @param label     type of key to derive
     * @param masterKey master key
     * @param len       length of key needed, less than 20 bytes.  20 bytes are returned if len is 0.
     * @return a derived key of the specified length
     */
    private byte[] deriveKey(byte label, byte[] masterKey, int len)
    {
        byte[] base = Crypto.concat(new byte[] { label }, masterKey);
        byte[] hash = DigestUtils.sha(base);
        if (len == 0)
        {
            return hash;
        }
        byte[] out = new byte[len];
        System.arraycopy(hash, 0, out, 0, out.length);
        return out;
    }

    private Map<String, String> deserialize(byte[] plain) throws UnsupportedEncodingException
    {
        String base = new String(plain, UTF8);
        String[] items = base.split("[&=]");
        Map<String, String> map = Maps.newHashMapWithExpectedSize(items.length);
        for (int i = 0; i < items.length;)
        {
            String key = URLDecoder.decode(items[i++], UTF8);
            String val = URLDecoder.decode(items[i++], UTF8);
            map.put(key, val);
        }
        return map;
    }
}