package com.atlassian.crowd.service.client;

import java.util.Properties;

/**
 * Will provide information about the location of the Crowd resource used to configure a Crowd
 * Client. 
 */
public interface ResourceLocator
{
    /**
     * Will return the location of the resource on the file system.
     * This will be in URI format similar to "file:/crowd/temp/crowd.properties"
     * @return location of the resource
     */
    String getResourceLocation();

    /**
     * The configured name of the resource
     * @return name of resource
     */
    String getResourceName();

    /**
     * The Properties present within the given resource.
     * @return Properties or null if no properties exist 
     */
    Properties getProperties();
}
