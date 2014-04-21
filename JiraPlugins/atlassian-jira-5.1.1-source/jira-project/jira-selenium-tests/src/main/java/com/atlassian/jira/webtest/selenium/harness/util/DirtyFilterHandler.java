package com.atlassian.jira.webtest.selenium.harness.util;

import com.atlassian.jira.functest.framework.log.FuncTestOut;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContextAware;
import com.atlassian.jira.webtest.framework.impl.selenium.util.JqueryExecutor;

/**
 * Dirty hack for JIRA dirty filter.
 *
 * @since v4.2
 */
public final class DirtyFilterHandler extends SeleniumContextAware
{
    private static final String DIRTY_HACK = "jQuery(\"body\").isDirty({clear : true});";
    private static final String DIRTY_DEFINED = "if (jQuery.fn.isDirty) true; else false;";

    private final JqueryExecutor scriptExecutor;

    public DirtyFilterHandler(SeleniumContext ctx)
    {
        super(ctx);
        this.scriptExecutor = new JqueryExecutor(context);
    }

    /**
     * Reset dirty filter state to prevent popups at page unload.
     *
     */
    public void resetDirtyFilter()
    {
        JqueryExecutor.JqueryState state = scriptExecutor.jqueryState();
        if (!state.canExecute())
        {
            FuncTestOut.log("Cannot execute dirty filter reset, reason: " + state + ", jQuery check: "
                    + scriptExecutor.checkJquery());
            return;
        }
        if (!dirtyFilterDefined())
        {
            FuncTestOut.log("Cannot execute dirty filter reset, dirty filter function not defined");
            return;
        }
        scriptExecutor.execute(DIRTY_HACK);
    }

    private boolean dirtyFilterDefined()
    {
        return Boolean.parseBoolean(scriptExecutor.execute(DIRTY_DEFINED).byDefaultTimeout());
    }
}