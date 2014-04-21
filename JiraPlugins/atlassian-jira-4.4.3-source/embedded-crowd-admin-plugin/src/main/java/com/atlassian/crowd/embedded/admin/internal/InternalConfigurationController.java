package com.atlassian.crowd.embedded.admin.internal;

import com.atlassian.crowd.embedded.admin.ConfigurationController;
import com.atlassian.crowd.embedded.admin.crowd.CrowdDirectoryConfiguration;
import com.atlassian.crowd.embedded.admin.crowd.CrowdPermissionOption;
import com.atlassian.crowd.embedded.api.Directory;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InternalConfigurationController extends ConfigurationController
{
    protected Directory createDirectory(Object command)
    {
        return directoryMapper.buildInternalDirectory((InternalDirectoryConfiguration) command);

    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception
    {
        if (directoryRetriever.hasDirectoryId(request))
        {
            Directory directory = directoryRetriever.getDirectory(request);
            return directoryMapper.toInternalConfiguration(directory);
        }
        return createCommand();
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
    {
        super.initBinder(request, binder);
    }

}
