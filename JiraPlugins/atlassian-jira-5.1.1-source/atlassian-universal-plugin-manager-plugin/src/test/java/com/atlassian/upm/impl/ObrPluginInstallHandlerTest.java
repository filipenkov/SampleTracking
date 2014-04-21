package com.atlassian.upm.impl;

import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class ObrPluginInstallHandlerTest
{

    @Mock RepresentationFactory representationFactory;
    @Mock PluginAccessorAndController accessor;

    @Test(expected = IllegalArgumentException.class)
    public void assertThatPluginInstallExceptionIsThrownForMissingBundleSymbolicLink() throws IOException
    {
        File jar = createTmpJarWithManifest("obrHandlerTestMissingSymbolicLink", createManifest());
        ObrPluginInstallHandler obrHandler = new ObrPluginInstallHandler(representationFactory, accessor);
        obrHandler.extractBundleName(jar);
    }

    @Test
    public void assertThatNoExceptionIsThrownWhenBundleSymbolicLinkEntryExists() throws IOException
    {
        final String bundleName = "com.example.plugins.test.BundleName";

        File jar = createTmpJarWithManifest("obrHandlerTestExistingSymbolicLink", createManifest(bundleName));
        ObrPluginInstallHandler obrHandler = new ObrPluginInstallHandler(representationFactory, accessor);
        String extractedBundleName = obrHandler.extractBundleName(jar);
        assertThat(bundleName, is(equalTo(extractedBundleName)));
    }

    private Manifest createManifest()
    {
        return createManifest(null);
    }

    private Manifest createManifest(String bundleName)
    {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        if (bundleName != null)
        {
            attributes.putValue(Constants.BUNDLE_SYMBOLICNAME, bundleName);
        }

        return manifest;
    }

    private File createTmpJarWithManifest(String filename, Manifest manifest) throws IOException
    {
        File file = File.createTempFile(filename, ".jar");
        file.deleteOnExit();

        FileOutputStream fos = null;
        JarOutputStream jos = null;
        try
        {
            fos = new FileOutputStream(file);
            jos = new JarOutputStream(fos, manifest);
        }
        finally
        {
            IOUtils.closeQuietly(jos);
            IOUtils.closeQuietly(fos);
        }
        return file;
    }
}
