package com.atlassian.jira.collector.plugin.web.admin;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.collector.plugin.components.Collector;
import com.atlassian.jira.collector.plugin.components.CollectorService;
import com.atlassian.jira.collector.plugin.components.ScriptletRenderer;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.seraph.util.RedirectUtils;

import javax.servlet.http.HttpServletRequest;

public class InsertCollectorHelp extends AbstractProjectAdminAction
{
    private String collectorId;
    private Collector collector;
    private final CollectorService collectorService;
    private final ScriptletRenderer scriptletRenderer;

    public InsertCollectorHelp(final CollectorService collectorService, final ScriptletRenderer scriptletRenderer)
    {
        this.collectorService = collectorService;
        this.scriptletRenderer = scriptletRenderer;
    }

    @Override
    public String doDefault() throws Exception
    {
        initRequest();

		if (getLoggedInUser() == null) {
			final HttpServletRequest request = ExecutingHttpRequest.get();
			return forceRedirect(RedirectUtils.getLoginUrl(request));
		}

        ServiceOutcome<Collector> result = collectorService.getCollector(collectorId);
        if (result.isValid() && result.getReturnedValue() != null)
        {
            collector = result.getReturnedValue();
            return SUCCESS;
        }

        addErrorMessage("Collector with id " + collectorId + " not found!");
        return ERROR;
    }

    public String getCollectorId()
    {
        return collectorId;
    }

    public void setCollectorId(final String collectorId)
    {
        this.collectorId = collectorId;
    }

    public String getScriptSource()
    {
        return scriptletRenderer.render(collector);
    }
    
    public String getScriptSourceJavascript()
    {
        return scriptletRenderer.renderJavascript(collector);
    }
}
