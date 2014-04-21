package com.atlassian.jira.collector.plugin.web.admin;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.collector.plugin.components.Collector;
import com.atlassian.jira.collector.plugin.components.CollectorService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.atlassian.seraph.util.RedirectUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays a global admin section that users can navigate to with GG.  Displays all collectors globally.
 *
 * @since v1.17-beta
 */
@WebSudoRequired
public class ViewGlobalCollectors extends JiraWebActionSupport
{
    private final CollectorService collectorService;
    private Map<Long, List<Collector>> projectCollectors;
    private Map<Long, List<Collector>> leftColumn = new LinkedHashMap<Long, List<Collector>>();
    private Map<Long, List<Collector>> rightColumn = new LinkedHashMap<Long, List<Collector>>();

    public ViewGlobalCollectors(final CollectorService collectorService)
    {
        this.collectorService = collectorService;
    }

    @Override
    public String doDefault() throws Exception
    {
		if (getLoggedInUser() == null) {
			final HttpServletRequest request = ExecutingHttpRequest.get();
			return forceRedirect(RedirectUtils.getLoginUrl(request));
		}

		final ServiceOutcome<Map<Long, List<Collector>>> result = collectorService.getCollectorsPerProject(getLoggedInUser());
        this.projectCollectors = result.getReturnedValue();

        int count = 0;
        for (Map.Entry<Long, List<Collector>> entry : projectCollectors.entrySet())
        {
            if (count % 2 == 0)
            {
                this.leftColumn.put(entry.getKey(), entry.getValue());
            }
            else
            {
                this.rightColumn.put(entry.getKey(), entry.getValue());
            }
            count++;
        }

        return SUCCESS;
    }

    public Map<Long, List<Collector>> getProjectCollectors()
    {
        return projectCollectors;
    }

    public Map<Long, List<Collector>> getLeftColumn()
    {
        return leftColumn;
    }

    public Map<Long, List<Collector>> getRightColumn()
    {
        return rightColumn;
    }

    public Project getProject(final Long projectId)
    {
        return getProjectManager().getProjectObj(projectId);
    }
}
