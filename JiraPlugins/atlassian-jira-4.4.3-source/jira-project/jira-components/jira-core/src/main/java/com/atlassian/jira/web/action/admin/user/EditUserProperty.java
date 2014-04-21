package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraUrlCodec;
import org.apache.commons.lang.StringUtils;

public class EditUserProperty extends UserProperty
{
    public EditUserProperty(CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, UserPropertyManager userPropertyManager, UserManager userManager)
    {
        super(crowdService, crowdDirectoryService, userPropertyManager, userManager);
    }

    protected String doExecute() throws Exception
    {
        if (key != null)
        {
            setValue(userPropertyManager.getPropertySet(getUser()).getString(getTrueKey()));
        }
        return getResult();
    }

    @RequiresXsrfCheck
    public String doUpdate()
    {
        if (StringUtils.isBlank(value))
        {
            addError("value", getText("admin.errors.userproperty.value.empty"));
        }
        else if (value.length() > 250)
        {
            addError("value", getText("admin.errors.userproperty.value.too.long"));
        }

        // Check if we found any errors
        if (invalidInput())
        {
            // If we did
            retrieveUserMetaProperties();
            return ERROR;
        }
        else
        {
            userPropertyManager.getPropertySet(getUser()).setString(getTrueKey(), value);
            return redirectToView();
        }
    }

    private String redirectToView()
    {
        return getRedirect("EditUserProperties.jspa?name=" + JiraUrlCodec.encode(getName()));
    }
}
