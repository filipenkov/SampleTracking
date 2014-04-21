/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.appconsistency.integrity.IntegrityCheckManager;
import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebSudoRequired
public class IntegrityChecker extends JiraWebActionSupport
{
    private final IntegrityCheckManager integrityCheckManager;
    private final com.atlassian.jira.appconsistency.integrity.IntegrityChecker integrityChecker;
    public static final String INTEGRITY_CHECK_PREFIX = "integrity_";
    public static final String CHECK_PREFIX = "check_";
    private String check;
    private String fix;
    private String back;
    private Map results = null;
    private static final String RESULT_NOT_POPULATED = "Check result has not been populated yet.";

    public IntegrityChecker(IntegrityCheckManager integrityCheckManager, com.atlassian.jira.appconsistency.integrity.IntegrityChecker integrityChecker)
    {
        this.integrityCheckManager = integrityCheckManager;
        this.integrityChecker = integrityChecker;
    }

    protected void doValidation()
    {
        // Check that a function type was selected
        if (!TextUtils.stringSet(getCheck()) && !TextUtils.stringSet(getFix()) && !TextUtils.stringSet(getBack()) )
        {
            addErrorMessage(getText("admin.integritychecker.error.no.function"));
        }
        else
        {
            // Check that it is either Preview or Correct
            if (!(isCheck() || isFix() || isBack()))
            {
                addErrorMessage(getText("admin.integritychecker.error.no.function"));
            }
        }

        // Check that atleast one integrity check was selected.
        List checkIds = getCheckIds();
        if (isCheck() && checkIds.isEmpty())
        {
            addErrorMessage(getText("admin.integritychecker.error.one.check"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (isCheck())
        {
            results = integrityChecker.previewWithIds(getCheckIds());
            return "preview";
        }
        else if (isFix())
        {
            results = integrityChecker.correctWithIds(getCheckIds());
            return "correct";
        }
        else if (isBack())
        {
            return getRedirect("IntegrityChecker!default.jspa");
        }
        else
        {
            return INPUT;
        }
    }

    public int getTotalResults()
    {
        int total = 0;
        final Map results = getResults();
        if (results != null)
        {
            for (Iterator iterator = results.keySet().iterator(); iterator.hasNext();)
            {
                List result = (List) results.get(iterator.next());
                if (result != null)
                {
                    for (int i = 0; i < result.size(); i++)
                    {
                        Amendment amendment = (Amendment) result.get(i);
                        if (amendment.isCorrection())
                        {
                            total++;
                        }
                    }
                }
            }
        }

        return total;
    }

    public List getIntegrityChecks()
    {
        return integrityCheckManager.getIntegrityChecks();
    }

    public String getCheck()
    {
        return check;
    }

    public void setCheck(String check)
    {
        this.check = check;
    }

    public String getFix()
    {
        return fix;
    }

    public void setFix(String fix)
    {
        this.fix = fix;
    }

    public String getBack()
    {
        return back;
    }

    public void setBack(String back)
    {
        this.back = back;
    }

    public Map getResults()
    {
        if (results == null)
        {
            throw new IllegalStateException(RESULT_NOT_POPULATED);
        }
        else
        {
            return results;
        }
    }

    public boolean isHasCorrectableResults()
    {
        // Go through all the results and see if there are any correctable results
        final Map results = getResults();
        for (Iterator iterator = results.keySet().iterator(); iterator.hasNext();)
        {
            // Check if there are non-emty results
            if (isHasCorrectableResults((Check) iterator.next()))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isHasWarningResults(Check check)
    {
        List result = (List) results.get(check);

        // Check if there are non-emty resulsts
        if (!(result == null || result.isEmpty()))
        {
            // we have to check if the results are WARNINGS
            for (int i = 0; i < result.size(); i++)
            {
                Amendment amendment = (Amendment) result.get(i);
                if (amendment.isWarning())
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isHasCorrectableResults(Check check)
    {
        List result = (List) results.get(check);

        // Check if there are non-emty resulsts
        if (!(result == null || result.isEmpty()))
        {
            // we have to check if the results are ERRORS (WARNINGS cannot be corrected)
            for (int i = 0; i < result.size(); i++)
            {
                Amendment amendment = (Amendment) result.get(i);
                if (amendment.isError())
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isIntegrityCheckAvailable(IntegrityCheck integrityCheck)
    {
        if (!integrityCheck.isAvailable())
        {
            return false;
        }

        if (isBack())
        {
            // If this is a new view or the view has been reset make this integrity check availaable
            return true;
        }

        if (isCheck())
        {
            // Check if there are any correctable results for this integrity check
            return isHasCorrectableResults(integrityCheck);
        }

        if (isFix())
        {
            // No need to make integrity function available if there are things being fixed
            return false;
        }

        throw new IllegalStateException("Invalid function was selected.");
    }

    public boolean isHasCorrectableResults(IntegrityCheck integrityCheck)
    {
        final List checks = integrityCheck.getChecks();
        for (int i = 0; i < checks.size(); i++)
        {
            Check check = (Check) checks.get(i);
            if (isHasCorrectableResults(check))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Determine whether to chow a check box for the check
     *
     * @param check
     */
    public boolean isCheckAvailable(Check check)
    {
        if (!check.isAvailable())
        {
            // If the check is not available itself, do not show the check box
            return false;
        }

        if (isBack())
        {
            // If this is a new view or the view has been reset show the check box
            return true;
        }

        if (isCheck())
        {
            // Only make the check available if it has correctable results
            return isHasCorrectableResults(check);
        }

        if (isFix())
        {
            // The correct has been executed. No check boxes should be shown
            return false;
        }

        throw new IllegalStateException("Invalid function was selected.");
    }

    public String getIntegrityCheckPrefix()
    {
        return INTEGRITY_CHECK_PREFIX;
    }

    public String getCheckPrefix()
    {
        return getIntegrityCheckPrefix() + CHECK_PREFIX;
    }

    public String getCheckId(Check check)
    {
        return getCheckPrefix() + check.getIntegrityCheck().getId().toString() + "_" + check.getId().toString();
    }

    public boolean isChecked(Check check)
    {
        return ActionContext.getParameters().containsKey(getCheckId(check));
    }

    private List getCheckIds()
    {
        List checkIds = new ArrayList();
        Map parameters = ActionContext.getParameters();
        Set keys = parameters.keySet();
        for (Iterator iterator = keys.iterator(); iterator.hasNext();)
        {
            String key = (String) iterator.next();
            if (key.startsWith(getCheckPrefix()))
            {
                try
                {
                    checkIds.add(Long.valueOf(((String[]) parameters.get(key))[0]));
                }
                catch (NumberFormatException e)
                {
                    log.error(e, e);
                }
            }
        }
        return checkIds;
    }

    private boolean isCheck()
    {
        return check != null;
    }

    private boolean isFix()
    {
        return fix != null;
    }

    private boolean isBack()
    {
        return back != null;
    }

}
