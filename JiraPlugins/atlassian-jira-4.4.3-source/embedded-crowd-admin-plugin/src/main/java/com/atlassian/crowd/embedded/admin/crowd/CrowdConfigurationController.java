package com.atlassian.crowd.embedded.admin.crowd;

import com.atlassian.crowd.embedded.admin.ConfigurationController;
import com.atlassian.crowd.embedded.api.Directory;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public final class CrowdConfigurationController extends ConfigurationController
{
    protected Map referenceData(HttpServletRequest request) throws Exception
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("crowdPermissionOptions", getCrowdPermissionOptions());
        if (request.getPathInfo().endsWith("/crowd/"))
        {
            model.put("serverType", "crowd");
        }
        else
        {
            model.put("serverType", "jira");
        }
        return model;
    }

    protected Directory createDirectory(Object command)
    {
        return directoryMapper.buildCrowdDirectory((CrowdDirectoryConfiguration) command);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception
    {
        if (directoryRetriever.hasDirectoryId(request))
        {
            Directory directory = directoryRetriever.getDirectory(request);
            return directoryMapper.toCrowdConfiguration(directory);
        }

        CrowdDirectoryConfiguration configuration = (CrowdDirectoryConfiguration) createCommand();

        // Set the name appropriately.
        if (request.getPathInfo().endsWith("/crowd/"))
        {
            configuration.setName("Crowd Server");
        }
        else
        {
            configuration.setName("JIRA Server");
        }
        return configuration;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
    {
        super.initBinder(request, binder);
        binder.setRequiredFields(new String[] {
            "name", "crowdServerUrl", "applicationName", "applicationPassword", "crowdPermissionOption", "crowdServerSynchroniseIntervalInMin"
        });
    }

    private List<String> getCrowdPermissionOptions()
    {
        List<String> options = new ArrayList<String>();
        for (CrowdPermissionOption option : CrowdPermissionOption.values())
        {
            options.add(option.name());
        }
        return options;
    }


}
