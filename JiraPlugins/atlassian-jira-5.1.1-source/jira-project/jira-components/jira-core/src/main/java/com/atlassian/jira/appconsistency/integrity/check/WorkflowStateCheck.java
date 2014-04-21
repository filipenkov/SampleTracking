package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.opensymphony.workflow.spi.WorkflowEntry;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Ensure all issues have a valid workflow state.
public class WorkflowStateCheck extends CheckImpl
{
    public WorkflowStateCheck(OfBizDelegator ofBizDelegator, int id)
    {
        super(ofBizDelegator, id);
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.workflow.state.check.desc");
    }

    public List preview() throws IntegrityException
    {
        return doCheck(false);
    }

    public List correct() throws IntegrityException
    {
        return doCheck(true);
    }

    public boolean isAvailable()
    {
        return true;
    }

    public String getUnavailableMessage()
    {
        return "";
    }

    private List doCheck(boolean correct) throws IntegrityException
    {
        List results = new ArrayList();
        String message;
        try
        {
            Map map = getWorkflowIdToKeyMap();
            for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();)
            {
                Map.Entry entry = (Map.Entry) iterator.next();
                Long workflowId = (Long) entry.getKey();
                String issueKey = (String) entry.getValue();

                List workflowEntries = ofBizDelegator.findByAnd("OSWorkflowEntry", EasyMap.build("id", workflowId));

                for (int i = 0; i < workflowEntries.size(); i++)
                {
                    GenericValue workflowEntry = (GenericValue) workflowEntries.get(i);

                    // Ensure the 'state' column is not null or WorkflowEntry.CREATED
                    if (workflowEntry.getInteger("state") == null || "0".equals(workflowEntry.getInteger("state").toString()))
                    {
                        if (correct)
                        {
                            // Fix the problem
                            workflowEntry.set("state", new Integer(WorkflowEntry.ACTIVATED));
                            // Persist the changes
                            workflowEntry.store();

                            message = getI18NBean().getText("admin.integrity.check.workflow.state.check.message", issueKey, workflowEntry.getLong("id").toString());
                            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4241"));
                        }
                        else
                        {
                            // If we are just previewing then simply record the message
                            message = getI18NBean().getText("admin.integrity.check.workflow.state.check.preview", issueKey, workflowEntry.getLong("id").toString());
                            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4241"));
                        }
                    }
                }
            }
        }
        catch (GenericEntityException e)
        {
            throw new IntegrityException("Error occurred while performing check.", e);
        }

        return results;
    }

    private Map getWorkflowIdToKeyMap()
    {
        Map map = new HashMap();

        OfBizListIterator listIterator = null;
        try
        {
            listIterator = ofBizDelegator.findListIteratorByCondition("Issue", null, null, EasyList.build("workflowId", "key"), null, null);
            GenericValue issue = (GenericValue) listIterator.next();
            // Retrieve all issues
            // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
            // if there are any results left in the iterator is to iterate over it until null is returned
            // (i.e. not use hasNext() method)
            // The documentation mentions efficiency only - but the functionality is totally broken when using
            // hsqldb JDBC drivers (hasNext() always returns true).
            // So listen to the OfBiz folk and iterate until null is returned.
            while (issue != null)
            {
                map.put(issue.getLong("workflowId"), issue.getString("key"));
                issue = (GenericValue) listIterator.next();
            }
        }
        finally
        {
            if (listIterator != null)
            {
                // Close the iterator
                listIterator.close();
            }

        }

        return map;
    }
}