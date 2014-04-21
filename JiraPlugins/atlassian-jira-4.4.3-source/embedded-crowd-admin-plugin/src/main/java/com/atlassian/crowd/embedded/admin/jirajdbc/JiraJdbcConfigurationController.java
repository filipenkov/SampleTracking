package com.atlassian.crowd.embedded.admin.jirajdbc;

import com.atlassian.crowd.embedded.admin.ConfigurationController;
import com.atlassian.crowd.embedded.api.Directory;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;

public class JiraJdbcConfigurationController extends ConfigurationController
{
    protected Directory createDirectory(Object command)
    {
        return directoryMapper.buildJiraJdbcDirectory((JiraJdbcDirectoryConfiguration) command);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception
    {
        if (directoryRetriever.hasDirectoryId(request))
        {
            Directory directory = directoryRetriever.getDirectory(request);
            return directoryMapper.toJiraJdbcConfiguration(directory);
        }
        return createCommand();
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
    {
        super.initBinder(request, binder);
        binder.setRequiredFields(new String[] {
            "name", "datasourceJndiName"
        });
    }
}
