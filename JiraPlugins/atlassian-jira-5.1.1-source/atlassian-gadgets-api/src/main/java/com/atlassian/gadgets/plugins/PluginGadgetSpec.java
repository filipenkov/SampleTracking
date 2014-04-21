package com.atlassian.gadgets.plugins;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.Plugin;

import static com.atlassian.plugin.util.Assertions.notNull;

/**
 * A gadget spec provided by a plugin.
 * <p/>
 * There are two major categories of plugin gadgets: published gadgets and external gadgets.
 * <p/>
 * Published gadgets are gadget specs that are packaged as resources within the plugin.  They are served by the
 * Atlassian Gadgets Publisher plugin at a URL of the form {@code http://<hostname>[:<port>]/[<context>/]<path/to/gadget/location.xml>}.
 * The location of published gadgets returned from {@link #getLocation()} is a relative path to the gadget spec file
 * within the plugin.
 * <p/>
 * External gadgets are gadget specs that are hosted on an external web site.  Declaring external gadgets within a
 * plugin makes the application aware of these gadgets, so they can be displayed in a directory of available gadgets,
 * for example.  The location of external gadgets returned from {@link #getLocation()} is the absolute URL of the gadget
 * spec file, beginning with {@code http} or {@code https}.  External gadgets served through protocols other than HTTP
 * are <em>not</em> supported.
 */
public final class PluginGadgetSpec
{
    private final Plugin plugin;
    private final String location;
    private final String moduleKey;
    private final Map<String, String> params;

    /**
     * Constructs a new {@code PluginGadgetSpec} from the specified plugin and location.
     *
     * @param plugin   the plugin that contains this gadget spec.  Must not be {@code null}, or a {@code
     *                 NullPointerException} will be thrown.
     * @param moduleKey the module key of the gadget. Must not be {@code null}, or a {@code
     *                 NullPointerException} will be thrown.
     * @param location the location of the plugin.  Must not be {@code null}, or a {@code NullPointerException} will be
     *                 thrown.
     * @throws NullPointerException if any argument is {@code null}
     */
    public PluginGadgetSpec(Plugin plugin, String moduleKey, String location, Map<String, String> params)
    {
        this.plugin = notNull("plugin", plugin);
        this.moduleKey = notNull("moduleKey", moduleKey);
        this.location = notNull("location", location);
        this.params = unmodifiableCopy(notNull("params", params));
    }
    
    private Map<String, String> unmodifiableCopy(Map<String, String> map)
    {
        return Collections.unmodifiableMap(new HashMap<String, String>(map));
    }

    /**
     * Returns a unique identifier for this spec.
     * @return the unique identifier
     */
    public Key getKey()
    {
        return new Key(plugin.getKey(), location);
    }

    /**
     * Returns the module key for this spec.
     * @return the module key
     */
    public String getModuleKey()
    {
        return moduleKey;
    }

    /**
     * Returns the plugin key for this spec.
     * @return the plugin key
     */
    public String getPluginKey()
    {
        return plugin.getKey();
    }

    /**
     * If {@code isHostedExternally()} returns true, this method returns the absolute URL of the gadget spec file,
     * beginning with http or https. If {@code isHostedExternally()} returns false, this method returns a relative
     * location -- the path of the spec file within its plugin.
     * @return the location string
     */
    public String getLocation()
    {
        return location;
    }

    /**
     * Returns the spec resource as an input stream, for processing.
     * @return the input stream
     */
    public InputStream getInputStream()
    {
        return plugin.getResourceAsStream(location);
    }

    /**
     * Returns true if this spec is hosted externally (meaning it is not part of a plugin served by the Gadgets
     * Publisher plugin), false otherwise.
     * @return true if the spec is hosted externally, false otherwise
     */
    public boolean isHostedExternally()
    {
        return location.startsWith("http://") || location.startsWith("https://");
    }

    public boolean hasParameter(String name)
    {
        return params.containsKey(name);
    }

    public String getParameter(String name)
    {
        return params.get(name);
    }
    
    public Date getDateLoaded()
    {
        return plugin.getDateLoaded();
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((plugin == null) ? 0 : plugin.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        PluginGadgetSpec other = (PluginGadgetSpec) obj;
        return plugin.equals(other.plugin) && location.equals(other.location);
    }

    @Override public String toString()
    {
        return "PluginGadgetSpec{" +
               "plugin=" + plugin +
               ", location='" + location + '\'' +
               '}';
    }

    /**
     * An immutable representation of a unique identifier for plugin gadget specs, composed of a plugin key and resource
     * location path name.
     */
    public static final class Key
    {
        private final String pluginKey;
        private final String location;


        /**
         * Constructs a new {@code PluginGadgetSpec.Key} from the specified plugin key and resource location.
         *
         * @param pluginKey the plugin key of the {@link Plugin} that contains the resource.  Must not be {@code null},
         *                  or a {@code NullPointerException} will be thrown.
         * @param location  the location of the resource within the plugin or the external location of the resource.
         *                  Must not be {@code null}, or a {@code NullPointerException} will be thrown.
         * @throws NullPointerException if any argument is {@code null}
         */
        public Key(String pluginKey, String location)
        {
            this.pluginKey = notNull("pluginKey", pluginKey);
            this.location = notNull("location", location);
        }

        /**
         * Returns the key of the plugin that the spec this key is for is contained in.
         * @return the key of the plugin
         */
        public String getPluginKey()
        {
            return pluginKey;
        }

        /**
         * Returns the location of the spec this key is for.
         * @return the location of the spec this key is for
         */
        public String getLocation()
        {
            return location;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            Key that = (Key) o;

            return location.equals(that.location) && pluginKey.equals(that.pluginKey);

        }

        @Override
        public int hashCode()
        {
            return 31 * pluginKey.hashCode() + location.hashCode();
        }

        @Override
        public String toString()
        {
            return "Key{" +
                   "pluginKey='" + pluginKey + '\'' +
                   ", location='" + location + '\'' +
                   '}';
        }
    }
}
