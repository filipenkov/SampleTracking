package com.atlassian.upm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.impl.ObrPluginInstallHandler;
import com.atlassian.upm.rest.representations.RepresentationFactory;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.upm.PluginInstaller.PluginInstallType.LEGACY;
import static com.atlassian.upm.PluginInstaller.PluginInstallType.OBR;
import static com.atlassian.upm.PluginInstaller.PluginInstallType.OSGI;
import static com.atlassian.upm.PluginInstaller.PluginInstallType.XML;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginInstallerTest
{
    @Mock RepresentationFactory representationFactory;
    @Mock PluginAccessorAndController accessor;
    @Mock ObrPluginInstallHandler obrPluginInstallHandler;
    @Mock ApplicationProperties applicationProperties;
    @Mock AuditLogService auditLogger;

    PluginInstaller pluginInstaller;

    static File PLUGIN_JAR_FILE;
    static File LEGACY_PLUGIN_JAR_FILE;
    static File FUTURE_PLUGIN_JAR_FILE;
    static File OBR_FILE;
    static File NON_PLUGIN_JAR_FILE;
    static File NON_JAR_FILE;
    static File XML_PLUGIN_FILE;

    @BeforeClass
    public static void createPluginJarFile() throws IOException
    {
        PLUGIN_JAR_FILE = createTmpJarContaining(
            file("atlassian-plugin.xml", "<atlassian-plugin plugins-version='2'></atlassian-plugin>"), ".jar");
    }

    @BeforeClass
    public static void createLegacyPluginJarFile() throws IOException
    {
        LEGACY_PLUGIN_JAR_FILE = createTmpJarContaining(
            file("atlassian-plugin.xml", "<atlassian-plugin></atlassian-plugin>"), ".jar");
    }
    
    @BeforeClass
    public static void createFuturePluginJarFile() throws IOException
    {
        FUTURE_PLUGIN_JAR_FILE = createTmpJarContaining(
            file("atlassian-plugin.xml", "<atlassian-plugin plugins-version='3'></atlassian-plugin>"), ".jar");
    }
    

    @BeforeClass
    public static void createObrFile() throws IOException
    {
        OBR_FILE = createTmpJarContaining("obr.xml", ".obr");
    }

    @BeforeClass
    public static void createNonPluginJarFile() throws IOException
    {
        NON_PLUGIN_JAR_FILE = createTmpJarContaining("junk", ".jar");
    }

    @BeforeClass
    public static void createNonJarFile() throws IOException
    {
        NON_JAR_FILE = File.createTempFile("something", "different");
    }

    @BeforeClass
    public static void createXmlPluginFile() throws IOException
    {
        XML_PLUGIN_FILE = createTmpXmlPluginFile(file("xml-plugin", "<atlassian-plugin plugins-version='2'></atlassian-plugin>"));
    }

    @Before
    public void setUp()
    {
        when(applicationProperties.getDisplayName()).thenReturn("notconfluence");
        pluginInstaller = new PluginInstaller(obrPluginInstallHandler, accessor, representationFactory, applicationProperties, auditLogger);
    }

    @Test
    public void assertThatPluginInstallTypeIsOsgiForJarFilesWithDescriptorDeclaringPluginsVersion2() throws URISyntaxException
    {
        assertThat(pluginInstaller.getPluginInstallType(PLUGIN_JAR_FILE), is(equalTo(OSGI)));
    }

    @Test
    public void assertThatPluginInstallTypeIsLegacyForJarFilesWithDescriptorNotDeclaringPluginsVersion2()
    {
        assertThat(pluginInstaller.getPluginInstallType(LEGACY_PLUGIN_JAR_FILE), is(equalTo(LEGACY)));
    }

    @Test
    public void assertThatPluginInstallTypeIsObrForObrFiles() throws URISyntaxException
    {
        assertThat(pluginInstaller.getPluginInstallType(OBR_FILE), is(equalTo(OBR)));
    }

    @Test
    public void assertThatPluginInstallTypeIsXmlForXmlFilesWithDescriptorDeclaringPluginsVersion2()
    {
        assertThat(pluginInstaller.getPluginInstallType(XML_PLUGIN_FILE), is(equalTo(XML)));
    }

    @Test(expected = UnknownPluginTypeException.class)
    public void assertThatUnsupportedPluginTypeExceptionIsThrownForJarsThatAreNotPlugins() throws URISyntaxException
    {
        pluginInstaller.getPluginInstallType(NON_PLUGIN_JAR_FILE);
    }

    @Test(expected = UnknownPluginTypeException.class)
    public void assertThatUnsupportedPluginTypeExceptionIsThrownForFilesThatAreNotJars() throws URISyntaxException
    {
        pluginInstaller.getPluginInstallType(NON_JAR_FILE);
    }

    @Test(expected = LegacyPluginsUnsupportedException.class)
    public void assertThatLegacyPluginUnsupportedExceptionIsThrownForApplicationsThatAreNotConfluence()
    {
        pluginInstaller.install(LEGACY_PLUGIN_JAR_FILE, LEGACY_PLUGIN_JAR_FILE.getName());
    }

    @Test(expected = UnrecognisedPluginVersionException.class)
    public void assertThatUnrecognisedPluginVersionExceptionIsThrownForBadPluginVersions()
    {
        pluginInstaller.install(FUTURE_PLUGIN_JAR_FILE, FUTURE_PLUGIN_JAR_FILE.getName());
    }
    
    
    @Test(expected = XmlPluginsUnsupportedException.class)
    public void assertThatXmlPluginUnsupportedExceptionIsThrownForApplicationsThatAreNotConfluence()
    {
        pluginInstaller.install(XML_PLUGIN_FILE, XML_PLUGIN_FILE.getName());
    }

    private static File createTmpJarContaining(String fileName, String extension) throws IOException
    {
        return createTmpJarContaining(file(fileName, ""), extension);
    }

    private static File createTmpJarContaining(FileNameAndContent fileNameAndContent, String extension) throws IOException
    {
        File file = File.createTempFile("plugin", extension);
        FileOutputStream fos = new FileOutputStream(file);
        JarOutputStream jos = null;
        try
        {
            jos = new JarOutputStream(fos);
            Writer writer = new OutputStreamWriter(jos);
            jos.putNextEntry(new JarEntry(fileNameAndContent.name));
            writer.write(fileNameAndContent.content);
            writer.flush();
        }
        finally
        {
            IOUtils.closeQuietly(jos);
            IOUtils.closeQuietly(fos);
        }
        return file;
    }

    private static File createTmpXmlPluginFile(FileNameAndContent fileNameAndContent) throws IOException
    {
        File file = File.createTempFile(fileNameAndContent.name, ".xml");
        FileOutputStream fos = new FileOutputStream(file);
        try
        {
            Writer writer = new OutputStreamWriter(fos);
            writer.write(fileNameAndContent.content);
            writer.flush();
        }
        finally
        {
            IOUtils.closeQuietly(fos);
        }
        return file;
    }

    private static FileNameAndContent file(String name, String content)
    {
        return new FileNameAndContent(name, content);
    }

    private static class FileNameAndContent
    {
        final String name;
        final String content;

        FileNameAndContent(String name, String content)
        {
            this.name = name;
            this.content = content;
        }
    }
}
