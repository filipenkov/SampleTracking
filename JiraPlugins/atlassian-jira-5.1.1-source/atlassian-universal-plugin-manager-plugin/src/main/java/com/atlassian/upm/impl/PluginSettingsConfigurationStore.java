package com.atlassian.upm.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.upm.Configuration;
import com.atlassian.upm.ConfigurationStore;
import com.atlassian.upm.ConfigurationStoreException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * Provides access to the stored configurations using SAL {@code PluginSettings}.
 */
public class PluginSettingsConfigurationStore implements ConfigurationStore
{
    private static final String KEY_PREFIX = ConfigurationStore.class.getName() + ":configuration:";
    private static final String UPM_CONFIGURATION_KEY = "upm_configuration";
    private final Base64 base64;

    private final PluginSettingsFactory pluginSettingsFactory;
    private final ObjectMapper mapper;

    /**
     * Constructor
     *
     * @param pluginSettingsFactory the {@code PluginSettingsFactory} to use
     */
    public PluginSettingsConfigurationStore(PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = checkNotNull(pluginSettingsFactory, "pluginSettingsFactory");
        this.mapper = new ObjectMapper(new MappingJsonFactory());
        this.base64 = new Base64();
    }

    /**
     * Gets the saved configuration value.
     *
     * @return The {@code Configuration} value. May be {@code null}
     * @throws IOException
     */
    public Configuration getSavedConfiguration() throws IOException
    {
        return getConfigurationFromString(getConfigurationString());
    }

    /**
     * Saves the configuration.
     *
     * @param configuration {@code Configuration} value. {@code value} cannot be {@code null}.
     * @throws ConfigurationStoreException when there is a previously saved {@code Configuration}.
     * @throws IOException
     */
    public void saveConfiguration(Configuration configuration) throws IOException
    {
        checkNotNull(configuration, "configuration");

        String configString = getConfigurationString();
        if (!isEmpty(configString))
        {
            throw new ConfigurationStoreException("Configurations are read-only");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = null;

        try
        {
            osw = new OutputStreamWriter(new GZIPOutputStream(bos), "UTF-8");
            osw.write(mapper.writeValueAsString(configuration));
        }
        finally
        {
            closeQuietly(osw);
        }

        getPluginSettings().put(UPM_CONFIGURATION_KEY, new String(base64.encodeBase64(bos.toByteArray()), "UTF-8"));
    }

    /**
     * Removes the saved configuration.
     *
     * @return The {@code Configuration} value that was removed. Null if nothing was removed.
     * @throws ConfigurationStoreException if you try to delete the "current" and "safe" configuration.
     * @throws IOException
     */
    public Configuration removeSavedConfiguration() throws IOException
    {
        Object val = getPluginSettings().remove(UPM_CONFIGURATION_KEY);

        if (val != null && val instanceof String)
        {
            return getConfigurationFromString(String.valueOf(val));
        }

        return null;
    }

    private String getConfigurationString()
    {
        Object val = getPluginSettings().get(UPM_CONFIGURATION_KEY);

        if (val != null && val instanceof String)
        {
            return String.valueOf(val);
        }

        return null;
    }

    private Configuration getConfigurationFromString(final String value) throws IOException
    {
        if (value != null)
        {
            // First try to decompress configuration
            byte[] bytes = base64.decodeBase64(value.getBytes("UTF-8"));

            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            GZIPInputStream gzipIn = null;
            String decompressedValue;

            try
            {
                gzipIn = new GZIPInputStream(bis);
                decompressedValue = IOUtils.toString(gzipIn, "UTF-8");
            }
            catch(IOException error)
            {
                // Decompression failed, fall back to reading uncompressed configuration
                return mapper.readValue(value, Configuration.class);
            }
            finally
            {
                closeQuietly(gzipIn);
                bis.close();
            }
            // Any exception decoding the value will be propagated
            return mapper.readValue(decompressedValue, Configuration.class);
        }

        return null;
    }

    private PluginSettings getPluginSettings()
    {
        //never cache our plugin settings
        return new NamespacedPluginSettings(pluginSettingsFactory.createGlobalSettings(), KEY_PREFIX);
    }
}
