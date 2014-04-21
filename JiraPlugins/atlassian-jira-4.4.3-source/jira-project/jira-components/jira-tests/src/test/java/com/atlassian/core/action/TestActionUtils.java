package com.atlassian.core.action;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.AtlassianCoreException;
import com.atlassian.jira.local.ListeningTestCase;
import webwork.action.Action;
import webwork.action.ActionSupport;
import webwork.dispatcher.ActionResult;

import java.util.ArrayList;
import java.util.List;

public class TestActionUtils extends ListeningTestCase
{
    @Test
    public void testGoodResult()
    {
        ActionResult result = new ActionResult(Action.SUCCESS, null, new ArrayList(), null);

        try
        {
            ActionUtils.checkForErrors(result);
        }
        catch (Exception e)
        {
            fail("Should not throw exception!");
        }
    }

    @Test
    public void testBadResultError() throws Exception
    {
        ActionResult result = new ActionResult(Action.ERROR, null, new ArrayList(), null);

        try
        {
            ActionUtils.checkForErrors(result);
            fail("Should throw exception!");
        }
        catch (AtlassianCoreException e)
        {
            assertEquals("Error in action: null, result: error", e.getMessage());
        }
    }

    @Test
    public void testBadResultException() throws Exception
    {
        ActionSupport as = new SimpleAction();
        as.addErrorMessage("action error message");
        Exception expectedException = new AtlassianCoreException("exception error message");
        List list = new ArrayList(1);
        list.add(as);
        ActionResult result = new ActionResult(Action.ERROR, null, list, expectedException);

        try
        {
            ActionUtils.checkForErrors(result);
            fail("Should throw exception!");
        }
        catch (AtlassianCoreException e)
        {
            assertEquals(expectedException, e);
        }
    }
}

class SimpleAction extends ActionSupport
{

}

