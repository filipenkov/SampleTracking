package com.atlassian.upm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.upm.impl.JarPluginArtifactInstallHandler;
import com.atlassian.upm.impl.ObrPluginInstallHandler;
import com.atlassian.upm.impl.XmlPluginArtifactInstallHandler;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.rest.representations.PluginRepresentation;
import com.atlassian.upm.rest.representations.RepresentationFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Installs plugins
 */
public class PluginInstaller
{
    private static final Predicate<String> SUPPORTS_LEGACY_DYNAMIC_INSTALLS =
        Predicates.in(ImmutableSet.of("confluence"));
    private static final Predicate<String> SUPPORTS_XML_PLUGIN_INSTALLS =
        Predicates.in(ImmutableSet.of("jira", "confluence", "bamboo"));

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObrPluginInstallHandler obrPluginInstallHandler;
    private final PluginAccessorAndController pluginAccessorAndController;
    private final RepresentationFactory representationFactory;
    private final ApplicationProperties applicationProperties;
    private final AuditLogService auditLogger;

    public PluginInstaller(ObrPluginInstallHandler obrPluginInstallHandler,
        PluginAccessorAndController pluginAccessorAndController,
        RepresentationFactory representationFactory,
        ApplicationProperties applicationProperties,
        AuditLogService auditLogger)
    {
        this.obrPluginInstallHandler = checkNotNull(obrPluginInstallHandler, "obrPluginInstallHandler");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.auditLogger = checkNotNull(auditLogger, "auditLogger");
    }

    /**
     * Installs the passed plugin {@code File}. The plugin install type will be determined based on the
     * {@code plugin}'s {@code URI} filename's extension
     *
     * @param plugin the plugin {@code File} to install
     * @param source a textual version of the plugin source, to be displayed in audit log messages
     * @return the representation of the installed plugin
     * @throws UnknownPluginTypeException if plugin install type was not found for the supplied plugin file
     * @throws LegacyPluginsUnsupportedException if the plugin is a v1 plugin
     * @throws UnrecognisedPluginVersionException if the plugin version is not recognised (e.g. a future version)
     * @throws XmlPluginsUnsupportedException if the plugin is an xml plugin and application does not support it
     * @throws SafeModeException if trying to install a plugin when system is in safe mode
     */
    public PluginRepresentation install(File plugin, String source)
    {
        return execute(checkNotNull(plugin, "plugin"), source, ExecutionType.INSTALL);
    }

    /**
     * Updates the passed plugin {@code File}. The plugin install type will be determined based on the
     * {@code plugin}'s {@code URI} filename's extension. This is the same thing as installing, except that
     * possible error messages will be tailored to updates.
     *
     * @param plugin the plugin {@code File} to install
     * @param source a textual version of the plugin source, to be displayed in audit log messages
     * @return the representation of the installed plugin
     * @throws UnknownPluginTypeException if plugin install type was not found for the supplied plugin file
     * @throws LegacyPluginsUnsupportedException if the plugin is a v1 plugin
     * @throws UnrecognisedPluginVersionException if the plugin version is not recognised (e.g. a future version) 
     * @throws XmlPluginsUnsupportedException if the plugin is an xml plugin and application does not support it
     * @throws SafeModeException if trying to install a plugin when system is in safe mode
     */
    public PluginRepresentation update(File plugin, String source)
    {
        return execute(checkNotNull(plugin, "plugin"), source, ExecutionType.UPDATE);
    }

    /**
     * Installs/updates the passed plugin {@code File} with type {@code pluginInstallType}. The {@code ExecutionType}
     * only changes possible error messages.
     *
     * @param plugin the plugin {@code File} to install
     * @param source a textual version of the plugin source
     * @param executionType either INSTALL or UPDATE
     * @return the representation of the installed plugin
     * @throws UnknownPluginTypeException if plugin install type was not found for the supplied plugin uri
     * @throws LegacyPluginsUnsupportedException if the plugin is a v1 plugin
     * @throws UnrecognisedPluginVersionException if the plugin version is not recognised (e.g. a future version)
     * @throws XmlPluginsUnsupportedException if the plugin is an xml plugin and application does not support it
     * @throws SafeModeException if trying to install a plugin when system is in safe mode
     */
    private PluginRepresentation execute(File plugin, String source, ExecutionType executionType)
    {
        String i18nFailureMessage = "upm.auditLog." + executionType.name + ".plugin.failure";
        try
        {
            PluginInstallType pluginInstallType = getPluginInstallType(plugin);
            PluginInstallHandler handler = getHandler(checkNotNull(pluginInstallType, "pluginInstallType"));
            return handler.installPlugin(checkNotNull(plugin, "plugin"));
        }
        catch (SafeModeException sme)
        {
            logger.debug("System is in safe mode", sme);
            auditLogger.logI18nMessage(i18nFailureMessage, plugin.getName(), source);
            throw sme;
        }
        catch (PluginInstallException pie)
        {
            logger.error("Failed to install plugin", pie);
            auditLogger.logI18nMessage(i18nFailureMessage, plugin.getName(), source);
            throw pie;
        }
        catch (UnrecognisedPluginVersionException upve)
        {
            logger.error("The plugin '" + source + "' with unrecognised version '" + upve.getVersion() + "' cannot be installed");
            auditLogger.logI18nMessage(i18nFailureMessage, plugin.getName(), source);
            throw upve;
        }        
        catch (UnknownPluginTypeException upte)
        {
            logger.error("File '" + source + "' is not a valid plugin: " + upte.getMessage());
            auditLogger.logI18nMessage(i18nFailureMessage, plugin.getName(), source);
            throw upte;
        }
        catch (LegacyPluginsUnsupportedException lpue)
        {
            logger.error("The legacy plugin '" + source + "' cannot be dynamically installed");
            auditLogger.logI18nMessage(i18nFailureMessage, plugin.getName(), source);
            throw lpue;
        }
        catch (XmlPluginsUnsupportedException xpue)
        {
            logger.error("The xml plugin '" + source + "' cannot be dynamically installed");
            auditLogger.logI18nMessage(i18nFailureMessage, plugin.getName(), source);
            throw xpue;
        }
        catch (Throwable t)
        {
            logger.error("Failed to install plugin", t);
            auditLogger.logI18nMessage("upm.auditLog.install.plugin.failure", plugin.getName(), source);
            throw new RuntimeException(t);
        }
    }

    private enum ExecutionType
    {
        INSTALL("install"),
        UPDATE("update");

        private final String name;

        private ExecutionType(String name)
        {
            this.name = name;
        }
    }

    public enum PluginInstallType
    {
        OSGI,
        LEGACY,
        OBR,
        XML;

        /**
         * Returns the {@code PluginInstallType} based on the supplied plugin file. If the file is an xml file and is
         * a valid atlassian-plugin artifact, then {@code XML} is returned. If the file is a jar and contains
         * an {@code obr.xml} file, then {@code OBR} is returned.  If it's a jar and contains an
         * {@code atlassian-plugin.xml} file, then {@code OSGI} is returned. If it is not an xml or not a jar or does
         * not contain either an {@code obr.xml} or a {@code atlassian-plugin.xml} file, then an
         * {@code UnknownPluginTypeException} is thrown.
         *
         * @param plugin the plugin file to inspect to get the {@code PluginInstallType}
         * @return the {@code PluginInstallType} based on the supplied plugin file
         * @throws UnknownPluginTypeException if the file is not a jar or if it doesn't contain an {@code obr.xml} or an
         * {@code atlassian-plugin.xml} file
         */
        public static PluginInstallType from(File plugin)
        {
            if (plugin.getName().endsWith(".xml"))
            {
                try
                {
                    FileInputStream fis = new FileInputStream(plugin);
                    try
                    {
                        String pluginsVersion = getPluginsVersionFromXmlInputStream(fis);
                        return resolveInstallTypeFromVersion(pluginsVersion, XML);
                    }
                    finally
                    {
                        closeQuietly(fis);
                    }
                }
                catch (IOException e)
                {
                    throw new UnknownPluginTypeException("Could not open the file as an XML");
                }
            }
            return getPluginInstallTypeFromJar(plugin);
        }

        private static PluginInstallType getPluginInstallTypeFromJar(File jarFile)
        {
            try
            {
                JarFile jar = new JarFile(jarFile);
                if (jar.getJarEntry("atlassian-plugin.xml") != null)
                {
                    InputStream xis = jar.getInputStream(jar.getJarEntry("atlassian-plugin.xml"));
                    try
                    {
                        String pluginsVersion = getPluginsVersionFromXmlInputStream(xis);
                        return resolveInstallTypeFromVersion(pluginsVersion, OSGI);
                    }
                    finally
                    {
                        closeQuietly(xis);
                    }
                }
                else if (jar.getEntry("obr.xml") != null)
                {
                    return OBR;
                }
                else if (jar.getEntry("META-INF/MANIFEST.MF") != null)
                {
                    InputStream xis = jar.getInputStream(jar.getEntry("META-INF/MANIFEST.MF"));
                    try
                    {
                        Manifest manifest = new Manifest(xis);
                        if (manifest.getMainAttributes().getValue("Bundle-SymbolicName") != null)
                        {
                            return OSGI;
                        }
                    }
                    finally
                    {
                        closeQuietly(xis);
                    }

                    throw new UnknownPluginTypeException("Manifest does not contain required Bundle-SymbolicName header");
                }
                throw new UnknownPluginTypeException("File does not contain an atlassian-plugin.xml, obr.xml, or MANIFEST.MF file");
            }
            catch (IOException ioe)
            {
                throw new UnknownPluginTypeException("Could not open the file as a jar");
            }
        }
        
        private static String getPluginsVersionFromXmlInputStream(InputStream xis) throws IOException
        {
            try
            {
                Document doc = parseXml(xis);
                if (!"atlassian-plugin".equals(doc.getDocumentElement().getNodeName()))
                {
                    throw new UnknownPluginTypeException("xml file is not a valid Atlassian plugin descriptor");
                }
                return getPluginsVersion(doc);
            }
            catch (SAXException e)
            {
                throw new UnknownPluginTypeException("xml file is not a valid Atlassian plugin descriptor");
            }
            catch (ParserConfigurationException e)
            {
                throw new RuntimeException("Unable to parse xml file", e);
            }
        }

        private static PluginInstallType resolveInstallTypeFromVersion(String pluginsVersion, PluginInstallType version2InstallType)
        {
            if ("1".equals(pluginsVersion))
            {
                return LEGACY;
            }
            else if ("2".equals(pluginsVersion))
            {
                // May be XML or OSGI, depending on plugin file type. 
                return version2InstallType;
            }
            else
            {
                throw new UnrecognisedPluginVersionException(pluginsVersion);
            }
        }         
        
        private static Document parseXml(InputStream xis) throws SAXException, IOException, ParserConfigurationException
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder().parse(new InputSource(xis));
        }

        private static String getPluginsVersion(Document doc)
        {
            NamedNodeMap attributes = doc.getDocumentElement().getAttributes();
            Node pluginsVersion = attributes.getNamedItem("plugins-version") != null ?
                attributes.getNamedItem("plugins-version") :
                attributes.getNamedItem("pluginsVersion");
            return pluginsVersion != null ? pluginsVersion.getTextContent() : "1";
        }
    }

    /**
     * Returns the {@code PluginInstallType} based on the supplied plugin uri.
     *
     * @param plugin the plugin file to check the {@code PluginInstallType} of.
     * @return the {@code PluginInstallType} based on the supplied plugin uri.
     * @throws UnknownPluginTypeException if plugin install type was not found for the supplied plugin uri.
     */
    public PluginInstallType getPluginInstallType(File plugin)
    {
        return PluginInstallType.from(plugin);
    }

    private PluginInstallHandler getHandler(PluginInstallType pluginInstallType)
    {
        switch (pluginInstallType)
        {
            case LEGACY:
                if (!applicationSupportsDynamicLegacyPlugins())
                {
                    throw new LegacyPluginsUnsupportedException();
                }
            case OSGI:
                return new JarPluginArtifactInstallHandler(pluginAccessorAndController, representationFactory);
            case OBR:
                return obrPluginInstallHandler;
            case XML:
                if (!applicationSupportsXmlPlugins())
                {
                    throw new XmlPluginsUnsupportedException();
                }
                return new XmlPluginArtifactInstallHandler(pluginAccessorAndController, representationFactory);
            default:
                throw new UnknownPluginTypeException("No handler found for plugin install type: " + pluginInstallType);
        }
    }

    private boolean applicationSupportsDynamicLegacyPlugins()
    {
        return SUPPORTS_LEGACY_DYNAMIC_INSTALLS.apply(applicationProperties.getDisplayName().toLowerCase());
    }

    private boolean applicationSupportsXmlPlugins()
    {
        return SUPPORTS_XML_PLUGIN_INSTALLS.apply(applicationProperties.getDisplayName().toLowerCase());
    }
}
