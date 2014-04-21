package com.atlassian.crowd.service.client;

import java.io.File;

/**
 * Will find the location of the given resourceName based off a set of rules to
 * locate the given resourceFileName:
 * <ol>
 * <li>System property</li>
 * <li>Configuration directory</li>
 * <li>Classpath</li>
 * </ol>
 */
public class ClientResourceLocator extends BaseResourceLocator
{

    public ClientResourceLocator(String resourceName)
    {
        this(resourceName, null);
    }

    public ClientResourceLocator(String resourceName, String configurationDir)
    {
        super(resourceName);
        this.propertyFileLocation = findPropertyFileLocation(configurationDir);
    }

    private String findPropertyFileLocation(String directory)
    {
        String location = getResourceLocationFromSystemProperty();

        if (location == null)
        {
            location = getResourceLocationFromDirectory(directory);
        }

        if (location == null)
        {
            location = getResourceLocationFromClassPath();
        }

        return location;

    }

    private String getResourceLocationFromDirectory(String directory)
    {
        if (directory == null)
        {
            return null;
        }

        // Create what 'should' be the location of the resource
        final String fileLocation = new File(directory, getResourceName()).getPath();

        return formatFileLocation(fileLocation);
    }
}
