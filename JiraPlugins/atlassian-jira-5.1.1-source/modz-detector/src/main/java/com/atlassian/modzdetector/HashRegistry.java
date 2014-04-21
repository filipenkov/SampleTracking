package com.atlassian.modzdetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Represents a stateful hash registry to which resources can be cumulatively added
 * using {@link #register(String, java.io.InputStream)}. Its operation is modal. By default
 * resources are registered as filesystem loadable resources. Calling {@link #setClasspathMode()}
 * means subsequent registrations indicate that resources are to be loaded from the claspath
 * when modifications or removals are to be detected.
 */
public class HashRegistry
{
    private static final Logger log = LoggerFactory.getLogger(HashRegistry.class);

    /**
     * TODO: make this private again
     */
    static final String FILE_NAME_HASH_REGISTRY_PROPERTIES = "hash-registry.properties";

    /**
     * Indicates that the name is registered as a classpath resource.
     */
    public static final String PREFIX_CLASSPATH = "cp.";

    /**
     * Indicates that the name is registered as a filesystem resource.
     */
    public static final String PREFIX_FILESYSTEM = "fs.";

    private Properties properties;
    private HashAlgorithm algorithm;
    private String registryFilename;
    private String currentPrefix;

    /**
     * Constructor using default algorithm and file name.
     */
    public HashRegistry()
    {
        this(new MD5HashAlgorithm(), FILE_NAME_HASH_REGISTRY_PROPERTIES);
    }

    public HashRegistry(String fileName)
    {
        this(new MD5HashAlgorithm(), fileName);
    }

    /**
     * Creates a HashRegistry that uses the given algorithm and filename.
     *
     * @param algorithm the hashing algorithm.
     * @param fileName  the filename to write the registry to.
     */
    public HashRegistry(HashAlgorithm algorithm, String fileName)
    {
        this.algorithm = algorithm;
        this.registryFilename = fileName;
        this.properties = new Properties();
        setFilesystemMode();
    }

    public void setClasspathMode()
    {
        currentPrefix = PREFIX_CLASSPATH;
    }

    public void setFilesystemMode()
    {
        currentPrefix = PREFIX_FILESYSTEM;
    }

    /**
     * Registers a hash for the resource with the given name and contents.
     *
     * @param name     the name of the resource relative to a cwd or docroot.
     * @param contents will be closed before this method completes.
     */
    public void register(String name, InputStream contents)
    {
        properties.setProperty(currentPrefix + name, algorithm.getHash(contents));
        IOUtils.closeQuietly(contents);
    }

    /**
     * Registers a whole filesystem under the given root. Sets the mode to filesystem mode.
     *
     * @param root the root of the filesystem to register.
     */
    public void registerFilesystm(File root)
    {
        registerFilesystem(root, new FileFilter()
        {
            public boolean accept(File file)
            {
                return true;
            }
        });
    }

    /**
     * Registers a whole filesystem under the given root for each file that matches filter. Directories
     * that do not match the filter will not be entered.
     *
     * @param root   the root of the filesystem to register.
     * @param filter the filter that all files must match in order to be registered.
     */
    public void registerFilesystem(File root, FileFilter filter)
    {
        setFilesystemMode();
        if (!filter.accept(root))
        {
            return;
        }
        int stripTo = root.getAbsolutePath().length() + "/".length();
        registerFilesystem(stripTo, root, filter);
    }

    private void registerFilesystem(int stripTo, File file, FileFilter filter)
    {
        if (file.isDirectory())
        {
            for (File f : file.listFiles(filter))
            {
                registerFilesystem(stripTo, f, filter);
            }
        }
        else
        {
            String relativePath = file.getAbsolutePath().substring(stripTo);
            try
            {
                register(relativePath, new FileInputStream(file));
            }
            catch (FileNotFoundException e)
            {
                log.warn("Weird, file not found: '{}'", file.getAbsolutePath());
            }
        }
    }

    /**
     * Writes the registry to a file with the configured name.
     *
     * @return the {@link java.io.File} written to.
     * @throws IOException if the file cannot be written.
     */
    public File store() throws IOException
    {
        File registryFile = new File(registryFilename);
        FileOutputStream propertiesOut = new FileOutputStream(registryFile);
        properties.store(propertiesOut, " THIS FILE IS GENERATED - DO NOT MODIFY. algorithm: " + algorithm);
        propertiesOut.close();
        log.info("wrote " + properties.keySet().size() + " hashes to " + registryFile.getAbsolutePath());
        return registryFile;
    }

}
