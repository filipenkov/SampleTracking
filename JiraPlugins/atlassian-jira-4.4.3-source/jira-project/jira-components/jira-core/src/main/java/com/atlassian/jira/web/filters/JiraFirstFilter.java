package com.atlassian.jira.web.filters;

import com.atlassian.jira.web.filters.steps.ChainedFilterStepRunner;
import com.atlassian.jira.web.filters.steps.FilterStep;
import com.atlassian.jira.web.filters.steps.i18n.I18nTranslationsModeThreadlocaleStep;
import com.atlassian.jira.web.filters.steps.requestinfo.RequestInfoFirstStep;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * This is the first filter that is run during a web request to JIRA.  At this point you know that the request is
 * pristine.
 * <p/>
 * You are at the outer most entry point for the request for the filter chain.
 * <p/>
 * After extensive market research, this filter has been carefully named to indicate that its the "first" filter and it
 * should remain that way.
 *
 * @since v4.2
 */
public class JiraFirstFilter extends ChainedFilterStepRunner
{

    @Override
    protected List<FilterStep> getFilterSteps()
    {
        return Lists.newArrayList(
                new RequestInfoFirstStep(),
                new I18nTranslationsModeThreadlocaleStep()
        );
    }
}
