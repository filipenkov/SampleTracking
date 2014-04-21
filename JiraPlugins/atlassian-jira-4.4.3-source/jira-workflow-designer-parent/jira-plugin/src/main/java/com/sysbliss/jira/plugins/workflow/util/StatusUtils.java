/**
 *
 */
package com.sysbliss.jira.plugins.workflow.util;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author jdoklovic
 */
public class StatusUtils {

    public static final String STATUS_ENTITY_NAME = "Status";
    private static final String NEW_STATUS_DEFAULT_ICON = "/images/icons/status_generic.gif";
    private static final Long NEW_STATUS_START_ID = new Long(10000);

    private Map fields;

    public GenericValue addStatus(String name, String description, final String iconurl) throws GenericEntityException {

        if (new Long(EntityUtils.getNextStringId(STATUS_ENTITY_NAME)).longValue() < NEW_STATUS_START_ID.longValue()) {
            addField("id", NEW_STATUS_START_ID.toString());
        } else {
            addField("id", EntityUtils.getNextStringId(STATUS_ENTITY_NAME));
        }

        // populate the rest of the fields to create the new entity
        addField("name", name);
        addField("description", description);
        addField("iconurl", iconurl);
        addField("sequence", new Long(getMaxSequenceNo() + 1));

        final GenericValue createdValue = EntityUtils.createValue(STATUS_ENTITY_NAME, getFields());

        // reset data
        name = null;
        description = null;

        final ConstantsManager cman = ManagerFactory.getConstantsManager();
        cman.refreshStatuses();

        return createdValue;
    }

    public GenericValue updateStatus(final String id, final String name, final String description, final String iconurl) throws GenericEntityException {
        final GenericValue gv = getStatus(id);
        gv.set("name", name);
        gv.set("description", description);
        gv.set("iconurl", iconurl);
        gv.store();

        final ConstantsManager cman = ManagerFactory.getConstantsManager();
        cman.refreshStatuses();

        return gv;
    }

    public GenericValue deleteStatus(final String id) throws GenericEntityException {
        final GenericValue gv = getStatus(id);

        gv.remove();

        final ConstantsManager cman = ManagerFactory.getConstantsManager();
        cman.refreshStatuses();

        return gv;
    }

    public static boolean isActive(final GenericValue statusGV) {
        boolean active = false;
        final WorkflowManager workflowManager = ComponentManager.getInstance().getWorkflowManager();
        final Collection workflows = workflowManager.getWorkflows();

        for (final Iterator iterator = workflows.iterator(); iterator.hasNext();) {
            final JiraWorkflow workflow = (JiraWorkflow) iterator.next();
            final Collection linkStatuses = workflow.getLinkedStatuses();
            if (linkStatuses.contains(statusGV)) {
                active = true;
                break;
            }
        }
        return active;
    }

    protected GenericValue getStatus(final String id) {
        final ConstantsManager cman = ManagerFactory.getConstantsManager();

        return cman.getStatus(id);
    }

    protected void addField(final String key, final Object value) {
        if (getFields() == null) {
            fields = new HashMap();
        }
        getFields().put(key, value);
    }

    private Map getFields() {
        return fields;
    }

    private long getMaxSequenceNo() {
        final ConstantsManager cman = ManagerFactory.getConstantsManager();

        final Collection constants = cman.getConstantObjects(ConstantsManager.STATUS_CONSTANT_TYPE);
        long maxSequence = 0;
        for (final Iterator iterator = constants.iterator(); iterator.hasNext();) {
            final Status status = (Status) iterator.next();

            final long thisSequence = status.getGenericValue().getLong("sequence").longValue();
            if (thisSequence > maxSequence)
                maxSequence = thisSequence;
        }
        return maxSequence;
    }
}
