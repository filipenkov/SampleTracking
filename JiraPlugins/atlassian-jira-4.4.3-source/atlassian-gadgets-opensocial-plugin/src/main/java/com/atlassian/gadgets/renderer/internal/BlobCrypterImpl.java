package com.atlassian.gadgets.renderer.internal;

import java.util.Map;
import java.util.Random;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import org.apache.shindig.common.crypto.BasicBlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypterException;
import org.bouncycastle.util.encoders.Base64;

public class BlobCrypterImpl implements BlobCrypter
{
    private static final String KEY_PREFIX = BlobCrypter.class.getName() + ":";
    private static final int KEY_BYTE_ARRAY_SIZE = 32;

    private final BlobCrypter crypter;
    private final TransactionTemplate txTemplate;

    /**
     * Constructor.
     * @param factory the {@code PluginSettingsFactory} to use
     * @param txTemplate the {@code TransactionTemplate} to use
     */
    public BlobCrypterImpl(PluginSettingsFactory factory, TransactionTemplate txTemplate)
    {
        this.txTemplate = txTemplate;        
        crypter = new BasicBlobCrypter(getKey(factory).getBytes());
    }

    protected String getKey(PluginSettingsFactory factory)
    {
        final PluginSettings pluginSettings = factory.createGlobalSettings();
        return (String) txTemplate.execute(new TransactionCallback()
        {
            public Object doInTransaction()
            {
                String key = (String) pluginSettings.get(KEY_PREFIX + "key");
                if (key == null)
                {
                    Random random = new Random();
                    byte[] keyBytes = new byte[KEY_BYTE_ARRAY_SIZE];
                    random.nextBytes(keyBytes);
                    key = new String(Base64.encode(keyBytes));
                    pluginSettings.put(KEY_PREFIX + "key", key);
                }
                return key;
            }
        });
    }
    
    public Map<String, String> unwrap(String paramString, int paramInt) throws BlobCrypterException
    {
        return crypter.unwrap(paramString, paramInt);
    }

    public String wrap(Map<String, String> paramMap) throws BlobCrypterException
    {
        return crypter.wrap(paramMap);
    }

}
