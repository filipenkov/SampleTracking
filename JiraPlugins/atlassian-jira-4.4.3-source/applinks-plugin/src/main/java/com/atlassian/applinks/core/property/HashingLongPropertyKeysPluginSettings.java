package com.atlassian.applinks.core.property;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation of PluginSettings prevents keys from getting bigger than 100 characters.
 * If a key exceeds this limit, it will hash the whole key and replace the last characters in the original
 * key with the hashed value and store this key.
 *
 * @since v3.3
 */
class HashingLongPropertyKeysPluginSettings implements PluginSettings
{
    private final PluginSettings pluginSettings;
    private static final int MAX_KEY_LENGTH = 100;
    private static final Logger LOG = LoggerFactory.getLogger(HashingLongPropertyKeysPluginSettings.class);

    HashingLongPropertyKeysPluginSettings(PluginSettings pluginSettings)
    {
        this.pluginSettings = pluginSettings;
    }

    public Object get(final String key)
    {
       return pluginSettings.get(hashKeyIfTooLong(key));
    }

    private String hashKeyIfTooLong(final String key)
    {
        if (key.length() > MAX_KEY_LENGTH)
        {
            final String keyHash = DigestUtils.md5Hex(key);
            final String keptOriginalKey = key.substring(0, MAX_KEY_LENGTH - keyHash.length());
            LOG.debug("Key '" + key + "' exceeds " + MAX_KEY_LENGTH + " characters. Key length is: '" + key.length() + "'. Hashed key value is: '" + keyHash + "'. Using combined original key and hash value '" + keptOriginalKey + keyHash + " as the key.");
            final String hashedKey = keptOriginalKey + keyHash;
            migrateKey(key, hashedKey);
            return hashedKey;
        }
        return key;
    }

    private void migrateKey(final String oldkey, final String newKey)
    {
        /* Unlikely but possible. */
        if (oldkey.equals(newKey))
        {
            return;
        }
        
        try
        {
            Object o = pluginSettings.get(oldkey);
            if (o != null)
            {
                pluginSettings.put(newKey, o);
                pluginSettings.remove(oldkey);
            }
        }
        catch (Exception ex)
        {
            LOG.debug("Exception thrown when attempting to migrate key '" + oldkey + "' to new key '" + newKey + "', application did never support keys > " + MAX_KEY_LENGTH, ex);
        }
    }

    public Object put(final String key, final Object value)
    {
        return pluginSettings.put(hashKeyIfTooLong(key), value);
    }

    public Object remove(final String key)
    {
        return pluginSettings.remove(hashKeyIfTooLong(key));
    }
}
