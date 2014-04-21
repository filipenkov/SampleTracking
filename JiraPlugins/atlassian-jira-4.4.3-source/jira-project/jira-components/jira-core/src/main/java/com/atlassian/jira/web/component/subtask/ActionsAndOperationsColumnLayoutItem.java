package com.atlassian.jira.web.component.subtask;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.web.component.SimpleColumnLayoutItem;
import com.atlassian.velocity.VelocityManager;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.util.HashMap;
import java.util.Map;

/**
 * ColumnLayoutItem that displays an AJAX dropdrown of all available actions and operations.
 * This replaces the previous ActionsLink column layout that showed 3 actions.
 *
 * @since 4.0
 */
public class ActionsAndOperationsColumnLayoutItem extends SimpleColumnLayoutItem
{
    private static final Logger log = Logger.getLogger(ActionsAndOperationsColumnLayoutItem.class);

    private static final String SUBTASK_OPERATION_TEMPLATE = "issue-operations.vm";
    private static final String CSS_CLASS = "issue_actions";
    private static final String TEMPLATE_DIRECTORY_PATH = "templates/jira/issue/field/";

    private final VelocityManager velocityManager;
    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext authenticationContext;
    private final XsrfTokenGenerator xsrfTokenGenerator;

    public ActionsAndOperationsColumnLayoutItem(final VelocityManager velocityManager, final ApplicationProperties applicationProperties, final JiraAuthenticationContext authenticationContext, final XsrfTokenGenerator xsrfTokenGenerator)
    {
        this.velocityManager = velocityManager;
        this.applicationProperties = applicationProperties;
        this.authenticationContext = authenticationContext;
        this.xsrfTokenGenerator = xsrfTokenGenerator;
    }

    @Override
    protected String getColumnCssClass()
    {
        return CSS_CLASS;
    }

    @Override
    public String getHtml(final Map displayParams, final Issue issue)
    {
        final Map<String, Object> localParams = new HashMap<String, Object>();
        localParams.put("issue", issue);
        localParams.put("atl_token", xsrfTokenGenerator.generateToken());
        localParams.put("displayParams", displayParams);
        localParams.put("i18n", authenticationContext.getI18nHelper());
        final Map<String, Object> velocityParams = CompositeMap.of(localParams, JiraVelocityUtils.getDefaultVelocityParams(authenticationContext));

        try
        {
            return velocityManager.getEncodedBody(TEMPLATE_DIRECTORY_PATH, SUBTASK_OPERATION_TEMPLATE, applicationProperties.getEncoding(),
                velocityParams);
        }
        catch (final VelocityException e)
        {
            log.error("Error occurred while rendering velocity template for '" + TEMPLATE_DIRECTORY_PATH + "/" + SUBTASK_OPERATION_TEMPLATE + "'.", e);
        }

        return "";
    }

    @Override
    public String getColumnHeadingKey()
    {
        return "";
    }
}
