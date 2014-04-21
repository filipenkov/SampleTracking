package com.atlassian.modzdetector;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Implements a mapping for streams that are only ever loaded from a filesystem rooted at a specific configured
 * directory.
 * <p/>
 * // TODO can has remove filter?
 */
public class FilesystemStreamMapper implements StreamMapper
{
    private static final char REGISTRY_SEPARATOR_CHAR = '/';
    private final File base;
    private final int basePathLength;
    private FileFilter filter;

    /**
     * Constructor that provides the base directory. All registrations are expected to be relative to this base. The
     * filter is used to restrict the specific scope of the stream mapping.
     *
     * @param base   the base of the filesystem mapping.
     * @param filter a filter to restrict what resources are visible.
     */
    public FilesystemStreamMapper(File base, FileFilter filter)
    {
        if (!base.isDirectory() && base.canRead())
        {
            throw new IllegalArgumentException("base must be a readable directory");
        }
        this.base = base;
        this.basePathLength = (base.getAbsolutePath() + "/").length();
        this.filter = filter;
    }

    public InputStream mapStream(String prefix, String resourceName)
    {
        if (HashRegistry.PREFIX_FILESYSTEM.equals(prefix))
        {
            try
            {
                File file = new File(base, resourceName);
                if (filter.accept(file))
                {
                    return new FileInputStream(file);
                }
                else
                {
                    throw new IllegalStateException("Non matching file cannot be mapped");
                }

            }
            catch (FileNotFoundException ignore)
            {
                return null;
            }
        }
        else
        {
            throw new IllegalStateException("Only filesystems supported.");
        }
    }

    public String getResourcePath(String resourceKey)
    {
        // TODO resource path is being reported in filesystem native notation, unlike the rest of modz detector - better way or better docs?
        if (resourceKey.startsWith(HashRegistry.PREFIX_FILESYSTEM))
        {
            String unixPath = resourceKey.substring(HashRegistry.PREFIX_FILESYSTEM.length());
            // we use unix paths in the registry
            return unixPath.replace(REGISTRY_SEPARATOR_CHAR, File.separatorChar);
        }
        throw new IllegalArgumentException("only filesystem resources are supported: " + resourceKey);
    }

    public String getResourceKey(File file)
    {
        return HashRegistry.PREFIX_FILESYSTEM + file.getAbsolutePath().substring(basePathLength);
    }
}
