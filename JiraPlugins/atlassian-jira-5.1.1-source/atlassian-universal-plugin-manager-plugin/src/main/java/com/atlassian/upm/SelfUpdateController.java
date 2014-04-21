package com.atlassian.upm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.upm.api.util.Either;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.osgi.Version;
import com.atlassian.upm.osgi.impl.Versions;
import com.atlassian.upm.rest.UpmUriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.upm.api.util.Either.left;
import static com.atlassian.upm.api.util.Either.right;
import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.io.File.createTempFile;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

/**
 * Encapsulates the mechanism that lets UPM update itself using a second temporary plugin.
 * <p>
 * This works as follows:  1. We temporarily install a "self-update plugin", which has been
 * built in a separate module and embedded within the main plugin's jar.  2. We tell the self-update
 * plugin the location of the UPM jar that's to be installed.  3. The self-update plugin gives us a
 * REST URI which we return to the front end.  4. The front end does a POST to this URI, triggering
 * the self-update plugin to complete the installation of the previously configured jar.  5. When
 * complete, the front end calls the main UPM plugin again (in the newly installed version) and it
 * uninstalls the self-update plugin.
 */
public class SelfUpdateController
{
    private static final Logger logger = LoggerFactory.getLogger(SelfUpdateController.class);

    private static final String SELFUPDATE_PLUGIN_JAR_RESOURCE = "atlassian-universal-plugin-manager-selfupdate-plugin.jar";
    private static final String SELFUPDATE_PLUGIN_KEY = "com.atlassian.upm.atlassian-universal-plugin-manager-selfupdate-plugin";
    
    private final PluginAccessorAndController pluginAccessorAndController;
    private final SelfUpdatePluginAccessor selfUpdatePluginAccessor;
    private final UpmUriBuilder uriBuilder;
    private final UpmVersionTracker upmVersionTracker;
    
    public SelfUpdateController(PluginAccessorAndController pluginAccessorAndController,
                                SelfUpdatePluginAccessor selfUpdatePluginAccessor,
                                UpmUriBuilder uriBuilder,
                                UpmVersionTracker upmVersionTracker)
    {
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        this.selfUpdatePluginAccessor = checkNotNull(selfUpdatePluginAccessor, "selfUpdatePluginAccessor");
        this.uriBuilder = checkNotNull(uriBuilder, "uriBuilder");
        this.upmVersionTracker = checkNotNull(upmVersionTracker, "upmVersionTracker");
    }
    
    public boolean isUpmPlugin(File plugin)
    {
        String pluginKey = getBundleAttribute(plugin, "Bundle-SymbolicName").getOrElse("");
        return pluginAccessorAndController.getUpmPluginKey().equals(pluginKey);
    }
    
    /**
     * Prepares but does not execute the self-update.
     * @param upmJar  the new UPM jar that will be installed
     * @return  an {@link Either} containing either a REST URI which can be POSTed to to trigger
     *   completion of the update, or, if preparation was unsuccessful, an error string.
     */
    public Either<String, URI> prepareSelfUpdate(File upmJar)
    {
        // Is the version of the new jar lower than the current version?  If so, we should stop
        // right now-- the plugin framework will refuse to do a downgrade anyway, but we can avoid
        // the unnecessary step of installing the self-update plugin.
        String newVersionStr = getBundleAttribute(upmJar, "Bundle-Version").getOrElse("").trim();
        if (newVersionStr.equals(""))
        {
            logger.warn("Could not get version string from jar");
            return left("upm.plugin.error.unexpected.error");
        }
        Version newVersion = Versions.fromString(newVersionStr);
        if (newVersion.compareTo(pluginAccessorAndController.getUpmVersion()) < 0)
        {
            return left("upm.update.error.downgrade");
        }
        
        // Extract the self-update plugin to its own jar file
        File selfUpdateJar;
        InputStream fromJarStream = null;
        OutputStream toJarStream = null;
        try
        {
            selfUpdateJar = createTempFile("upm-selfupdate", ".jar");
            fromJarStream = getClass().getClassLoader().getResourceAsStream(SELFUPDATE_PLUGIN_JAR_RESOURCE);
            toJarStream = new FileOutputStream(selfUpdateJar);
            copy(fromJarStream, toJarStream);
            toJarStream.close();
            logger.info("Extracted self-update plugin to " + selfUpdateJar.getAbsolutePath());
        }
        catch (IOException e)
        {
            logger.warn("Unable to extract self-update plugin: " + e);
            return left("upm.plugin.error.unexpected.error");
        }
        finally
        {
            closeQuietly(fromJarStream);
            closeQuietly(toJarStream);
        }

        logger.info("Installing self-update plugin");
        try
        {
            String pluginKey = pluginAccessorAndController.installPlugin(new JarPluginArtifact(selfUpdateJar));
            if (!pluginKey.equals(SELFUPDATE_PLUGIN_KEY))
            {
                logger.warn("Self-update plugin had incorrect key \"" + pluginKey + "\"; not updating");
                return left("upm.plugin.error.unexpected.error");
            }
        }
        catch (Exception e)
        {
            logger.warn("Unable to install self-update plugin: " + e, e);
            return left("upm.plugin.error.unexpected.error");
        }
        
        // Store configuration properties that the self-update plugin will need, and return the
        // URI of the self-update plugin's REST resource for executing the update.
        String upmPluginKey = pluginAccessorAndController.getUpmPluginKey();
        URI pluginUriWillBe = uriBuilder.makeAbsolute(uriBuilder.buildPluginUri(upmPluginKey));
        URI selfUpdatePluginUri = uriBuilder.makeAbsolute(uriBuilder.buildPluginUri(SELFUPDATE_PLUGIN_KEY));
        URI updateUri = selfUpdatePluginAccessor.prepareUpdate(upmJar, upmPluginKey, pluginUriWillBe, selfUpdatePluginUri);

        upmVersionTracker.setCurrentUpmVersionAsMostRecentlyUpdated();
        return right(updateUri);
    }
    
    private Option<String> getBundleAttribute(File plugin, String attributeName)
    {
        try
        {
            Manifest manifest = new JarFile(plugin).getManifest();
            if (manifest == null)
            {
                return none();
            }
            return option(manifest.getMainAttributes().getValue(attributeName));
        }
        catch (IOException e)
        {
            return none();
        }
    }
}
