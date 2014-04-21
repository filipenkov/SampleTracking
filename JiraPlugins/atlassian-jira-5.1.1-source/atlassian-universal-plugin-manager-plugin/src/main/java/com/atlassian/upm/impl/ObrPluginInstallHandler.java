package com.atlassian.upm.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.jar.JarFile;

import com.atlassian.plugin.DefaultPluginArtifactFactory;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginArtifactFactory;
import com.atlassian.plugin.util.zip.FileUnzipper;
import com.atlassian.plugin.util.zip.Unzipper;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.PluginInstallException;
import com.atlassian.upm.PluginInstallHandler;
import com.atlassian.upm.rest.representations.PluginRepresentation;
import com.atlassian.upm.rest.representations.RepresentationFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import org.osgi.framework.Constants;
import org.osgi.service.obr.RepositoryAdmin;
import org.osgi.service.obr.Resolver;
import org.osgi.service.obr.Resource;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.osgi.context.BundleContextAware;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.deleteDirectory;

/**
 * An implementation of the {@code PluginInstallHandler} that handles OBR plugin artifact installation.
 */
public class ObrPluginInstallHandler implements PluginInstallHandler, BundleContextAware, DisposableBean
{
    private final RepresentationFactory representationFactory;
    private final PluginAccessorAndController pluginAccessorAndController;
    private final PluginArtifactFactory pluginArtifactFactory = new DefaultPluginArtifactFactory();
    private static final Logger log = LoggerFactory.getLogger(ObrPluginInstallHandler.class);
    private ServiceTracker repositoryAdminServiceTracker;

    public ObrPluginInstallHandler(RepresentationFactory representationFactory, PluginAccessorAndController pluginAccessorAndController)
    {
        this.representationFactory = representationFactory;
        this.pluginAccessorAndController = pluginAccessorAndController;
    }

    public PluginRepresentation installPlugin(File plugin)
    {
        RepositoryAdmin repositoryAdmin = (RepositoryAdmin) checkNotNull(repositoryAdminServiceTracker.getService(), "couldn't locate RepositoryAdmin service");
        File obrDir = expandObrFile(plugin);
        URI repoUri = new File(obrDir, "obr.xml").toURI();

        try
        {
            repositoryAdmin.addRepository(repoUri.toURL());
            Resolver resolver = repositoryAdmin.resolver();

            for (File pluginJar : findPluginsToInstall(obrDir))
            {
                String bundleName = extractBundleName(pluginJar);
                Resource resource = repositoryAdmin.discoverResources("(symbolicname=" + bundleName + ")")[0];
                resolver.add(resource);
            }
            if (resolver.resolve())
            {
                return representationFactory.createPluginRepresentation(pluginAccessorAndController.getPlugin(installResources(resolver)));
            }
        }
        catch (Exception e)
        {
            throw new PluginInstallException("Failed to install OBR jar artifact", e);
        }
        finally
        {
            try
            {
                repositoryAdmin.removeRepository(repoUri.toURL());
                deleteDirectory(obrDir);
            }
            catch (Exception e)
            {
                log.warn("Failed to remove local OBR repository resources");
            }
        }
        return null;
    }

    private String installResources(Resolver resolver) throws URISyntaxException
    {
        Iterable<PluginArtifact> requiredArtifacts = transform(asList(resolver.getRequiredResources()), pluginArtifactFromResource);
        Iterable<PluginArtifact> optionalArtifacts = transform(asList(resolver.getOptionalResources()), pluginArtifactFromResource);

        // The OBR should contain a single "main" plugin that is being installed. This is the plugin whose key will be returned
        // to the client
        checkState(resolver.getAddedResources().length == 1, "Attempted to install an OBR that does not have exactly one main plugin");

        pluginAccessorAndController.installPlugins(concat(requiredArtifacts, optionalArtifacts));

        Resource mainPluginResource = resolver.getAddedResources()[0];
        return pluginAccessorAndController.installPlugin(pluginArtifactFromResource.apply(mainPluginResource));
    }

    private final Function<Resource, PluginArtifact> pluginArtifactFromResource = new Function<Resource, PluginArtifact>()
    {
        public PluginArtifact apply(Resource res)
        {
            return pluginArtifactFactory.create(URI.create(res.getURL().toString()));
        }
    };


    private File expandObrFile(File plugin)
    {
        try
        {
            File dir = File.createTempFile("obr-", plugin.getName());
            dir.delete();
            dir.mkdir();

            Unzipper unzipper = new FileUnzipper(plugin, dir);
            unzipper.unzip();
            return dir;
        }
        catch (IOException ioe)
        {
            log.error("Failed to expand OBR jar artifact", ioe);
            throw new PluginInstallException("Failed to expand OBR jar artifact", ioe);
        }
    }

    private Iterable<File> findPluginsToInstall(File obrDir)
    {
        return ImmutableList.of(obrDir.listFiles(JarFilenameFilter.INSTANCE));
    }

    private enum JarFilenameFilter implements FilenameFilter
    {
        INSTANCE;

        public boolean accept(File dir, String name)
        {
            return name.endsWith(".jar");
        }
    }

    String extractBundleName(File pluginJar)
    {
        JarFile jar = null;
        try
        {
            jar = new JarFile(pluginJar);
            String bundleName = jar.getManifest().getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME);
            if (bundleName == null)
            {
                throw new IllegalArgumentException("Missing " + Constants.BUNDLE_SYMBOLICNAME + " entry in OBR jar manifest");
            }
            return bundleName;
        }
        catch (IOException ioe)
        {
            throw new PluginInstallException("Failed to extract bundle name from OBR jar artifact", ioe);
        }
        finally
        {
            try
            {
                if (jar != null)
                {
                    jar.close();
                }
            }
            catch (IOException e)
            {
                log.warn("Failed to close OBR jar archive");
            }
        }
    }

    public void setBundleContext(org.osgi.framework.BundleContext bundleContext)
    {
        repositoryAdminServiceTracker = new ServiceTracker(bundleContext, RepositoryAdmin.class.getName(), null);
        repositoryAdminServiceTracker.open();
    }

    public void destroy()
    {
        repositoryAdminServiceTracker.close();
    }
}