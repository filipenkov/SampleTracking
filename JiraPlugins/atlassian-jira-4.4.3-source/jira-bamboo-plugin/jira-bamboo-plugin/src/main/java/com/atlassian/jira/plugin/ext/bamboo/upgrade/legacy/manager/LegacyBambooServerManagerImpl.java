package com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.manager;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.LegacyBambooServer;
import com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.LegacyBambooServerImpl;
import com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.util.LegacyBambooPropertyManager;
import com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.util.LegacyBambooServerIdGenerator;

import com.atlassian.util.concurrent.LazyReference;
import com.google.common.collect.ImmutableList;
import com.opensymphony.module.propertyset.PropertySet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Implementation for the legacy Bamboo server manager. This should only be used by the upgrade tasks.
 */
public class LegacyBambooServerManagerImpl implements LegacyBambooServerManager
{
    private static final Logger log = Logger.getLogger(LegacyBambooServerManagerImpl.class);

    public static final String CFG_ROOT               = "bamboo.config";
    public static final String CFG_VERSION_NUMBER     = CFG_ROOT + ".version";
    public static final String CFG_SERVER_DEFS        = CFG_ROOT + ".server.definitions";
    public static final String CFG_SERVER_DEFAULT     = CFG_ROOT + ".server.default";

    public static final String CFG_KEY_ID          = "id";
    public static final String CFG_KEY_NAME        = "serverName";
    public static final String CFG_KEY_DESCRIPTION = "description";
    public static final String CFG_KEY_USERNAME    = "username";
    public static final String CFG_KEY_PASSWORD    = "password";
    public static final String CFG_KEY_HOST        = "host";
    public static final String CFG_KEY_PROJECTS    = "projects";

    public static final int CFG_VERSION_22 = 22;

    private static final String PROJECT_KEYS_SEPARATOR = " ,;:";

    private final Map<Integer, LegacyBambooServer> servers = new HashMap<Integer, LegacyBambooServer>();
    private final Map<String, Integer> projectMapping = new HashMap<String, Integer>();
    private LegacyBambooServer defaultServer;

    private final PropertySet propertySet;
    private final LegacyBambooServerIdGenerator idGenerator;
    private final LazyReference<BambooStringEncrypter> stringEncrypter = new LazyReference<BambooStringEncrypter>()
    {
        @Override
        protected BambooStringEncrypter create()
        {
            return new BambooStringEncrypter();
        }
    };

    public LegacyBambooServerManagerImpl(LegacyBambooPropertyManager propertyManager, LegacyBambooServerIdGenerator bambooServerIdGenerator)
    {
        this.idGenerator = bambooServerIdGenerator;
        this.propertySet = propertyManager.getPropertySet();

        checkUpgradeConfig();
        loadAllServers();
        initDefaultServer();
    }

    private void initDefaultServer()
    {
        int defaultServerId = 0;
        if (propertySet.exists(CFG_SERVER_DEFAULT))
        {
            defaultServerId = propertySet.getInt(CFG_SERVER_DEFAULT);
        }
        if (defaultServerId != 0)
        {
            LegacyBambooServer cfgDefaultServer = getServer(defaultServerId);
            if (cfgDefaultServer != null)
            {
                setDefaultServer(cfgDefaultServer);
            }
        }
        if (defaultServer == null)
        {
            pickDefaultServer();
        }
    }

    /**
     * Test if server passed as an argument is default "catch-all" server.
     *
     * @param server Bamboo server definition to be tested
     * @return True if Bamboo server definition represents default "catch-all" server
     */
    public boolean isDefaultServer(LegacyBambooServer server)
    {
        if (null == server)
        {
            throw new IllegalArgumentException("Server must not be null.");
        }
        if(defaultServer == null)
        {
            pickDefaultServer();
        }
        return defaultServer != null && defaultServer.getId() == server.getId();
    }

    private void setDefaultServer(LegacyBambooServer server)
    {
        if (null == server)
        {
            throw new IllegalArgumentException("Cannot set default server to null.");
        }
        if (!servers.containsValue(server))
        {
            throw new IllegalArgumentException("Default server must be a known server");
        }

        defaultServer = server;
        propertySet.setInt(CFG_SERVER_DEFAULT, server.getId());        
    }

    /**
     * Retrieves ids of Bamboo server definitions.
     *
     * @return Collection of Strings representing names of Bamboo server definitions.
     */
    private Collection<String> getServerConfPaths()
    {
        Collection<String> serverNames = new HashSet<String>();

        Collection<String> keys = propertySet.getKeys(CFG_SERVER_DEFS);
        for (String key : keys)
        {
            serverNames.add(CFG_SERVER_DEFS + "." + StringUtils.substringBetween(key, CFG_SERVER_DEFS + ".", "."));
        }

        return serverNames;
    }

    public Iterable<LegacyBambooServer> getServers()
    {
        return ImmutableList.copyOf(this.servers.values());
    }

    public boolean hasServers()
    {
        return !servers.isEmpty();
    }

    private LegacyBambooServer getServer(int serverId)
    {
        return servers.get(serverId);
    }

    private void addServer(LegacyBambooServer server)
    {
        LegacyBambooServerImpl newServer = new LegacyBambooServerImpl(server);
        if (newServer.getId() == 0)
        {
            newServer.setId(idGenerator.next());
        }

        // remove projectKey mapping for a server if it is no longer specified
        removeOldKeys(newServer);

        // manage Jira project mappings
        for (String projectKey : newServer.getAssociatedProjectKeys())
        {
            Integer bambooServerId = projectMapping.get(projectKey);
            if(bambooServerId != null)
            {
                LegacyBambooServer conflictingServer = getServer(bambooServerId);
                if(conflictingServer != null && conflictingServer instanceof LegacyBambooServerImpl)
                {
                    ((LegacyBambooServerImpl)conflictingServer).removeAssociatedProjectKey(projectKey);
                }
                projectMapping.remove(projectKey);
            }

            projectMapping.put(projectKey, newServer.getId());
        }

        // store server
        servers.put(newServer.getId(), newServer);

        // update defaults
        if (defaultServer != null && newServer.getId() == defaultServer.getId())
        {
            defaultServer = newServer;
        }

        // serialize server
        store(newServer);

        if (defaultServer == null)
        {
            setDefaultServer(newServer);
        }
    }

    private void removeOldKeys(LegacyBambooServerImpl server)
    {
        Collection<String> removables = new HashSet<String>();
        for(Map.Entry<String, Integer> entry : projectMapping.entrySet())
        {
            final Integer value = entry.getValue();
            if(value != null && server.getId() == value)
            {
                if(!server.getAssociatedProjectKeys().contains(entry.getKey()))
                {
                    removables.add(entry.getKey());
                }
            }
        }

        for(String s : removables)
        {
            projectMapping.remove(s);
        }
    }

    private void pickDefaultServer()
    {
        if (servers.isEmpty())
        {
            defaultServer = null;
            if (propertySet.exists(CFG_SERVER_DEFAULT))
            {
                propertySet.remove(CFG_SERVER_DEFAULT);
            }
        }
        else
        {
            setDefaultServer(servers.values().iterator().next());
        }
    }

    private void upgradeConfigToVersion22()
    {
        // load oldschool configuration
        LegacyBambooServerImpl server = load(CFG_ROOT);
        if (server.getName() != null)
        {
            server.setId(idGenerator.next());
            // store in CFG_SERVER_DEFS.<name>
            store(server);
            // set it as default configuration
            propertySet.setInt(CFG_SERVER_DEFAULT, server.getId());

            // remove old keys
            propertySet.remove(CFG_ROOT + "." + CFG_KEY_NAME);
            propertySet.remove(CFG_ROOT + "." + CFG_KEY_HOST);
        }

        propertySet.setInt(CFG_VERSION_NUMBER, CFG_VERSION_22);
    }

    /**
     * Validate if configuration needs format upgrade.
     */
    private void checkUpgradeConfig()
    {
        int configVersionNumber = getConfigVersionNumber();

        if (configVersionNumber < CFG_VERSION_22)
        {
            upgradeConfigToVersion22();
            checkUpgradeConfig();
        }
    }

    /**
     * Retrieve information on Bamboo config version number.
     *
     * @return
     */
    private int getConfigVersionNumber()
    {
        int result = 0;

        if (propertySet.exists(CFG_VERSION_NUMBER))
        {
            result = propertySet.getInt(CFG_VERSION_NUMBER);
        }

        return result;
    }

    private void loadAllServers()
    {
        Collection<String> keys = getServerConfPaths();

        for (String serverConfPath : keys)
        {
            LegacyBambooServer server = load(serverConfPath);
            addServer(server);
        }
    }

    /**
     * Load Bamboo server definition from PropertySet.
     *
     * @param configurationPath Configuration path to read server properties from.
     * @return Newly constructed LegacyBambooServer object.
     */
    private LegacyBambooServerImpl load(String configurationPath)
    {
        final LegacyBambooServerImpl server = new LegacyBambooServerImpl();

        server.setHost(propertySet.getString(configurationPath + "." + CFG_KEY_HOST));
        server.setId(propertySet.getInt(configurationPath + "." + CFG_KEY_ID));        
        server.setName(propertySet.getString(configurationPath + "." + CFG_KEY_NAME));
        server.setDescription(propertySet.getString(configurationPath + "." + CFG_KEY_DESCRIPTION));
        server.setUsername(propertySet.getString(configurationPath + "." + CFG_KEY_USERNAME));
        server.setPassword(stringEncrypter.get().decrypt(propertySet.getString(configurationPath + "." + CFG_KEY_PASSWORD)));

        String projectKeys[] = StringUtils.split(propertySet.getString(configurationPath + "." + CFG_KEY_PROJECTS), PROJECT_KEYS_SEPARATOR);
        if (projectKeys != null)
        {
            server.setAssociatedProjectKeys(new HashSet<String>(Arrays.asList(projectKeys)));
        }

        return server;
    }

    /**
     * Store Bamboo server definition in default path in PropertySet.
     *
     * @param server Bamboo server definition to be stored
     */
    private void store(LegacyBambooServer server)
    {
        store(server, CFG_SERVER_DEFS + "." + String.valueOf(server.getId()));
    }

    /**
     * Store Bamboo server definition in a given path in PropertySet.
     *
     * @param server Bamboo server definition to be stored
     * @param configurationPath Configuration path where to store properties
     */
    private void store(LegacyBambooServer server, String configurationPath)
    {
        propertySet.setInt(configurationPath + "." + CFG_KEY_ID, server.getId());
        propertySet.setString(configurationPath + "." + CFG_KEY_NAME, server.getName());
        propertySet.setString(configurationPath + "." + CFG_KEY_DESCRIPTION, server.getDescription());
        propertySet.setString(configurationPath + "." + CFG_KEY_HOST, server.getHost());
        propertySet.setString(configurationPath + "." + CFG_KEY_USERNAME, server.getUsername());
        propertySet.setString(configurationPath + "." + CFG_KEY_PASSWORD, stringEncrypter.get().encrypt(server.getPassword()));

        final Object projects[] = server.getAssociatedProjectKeys().toArray();
        Arrays.sort(projects);
        propertySet.setString(configurationPath + "." + CFG_KEY_PROJECTS, StringUtils.join(projects, ' '));
    }
}
