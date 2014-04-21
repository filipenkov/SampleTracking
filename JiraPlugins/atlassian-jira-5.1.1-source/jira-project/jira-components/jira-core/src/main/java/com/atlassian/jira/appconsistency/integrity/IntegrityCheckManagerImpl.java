/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.appconsistency.integrity.check.EntityCheck;
import com.atlassian.jira.appconsistency.integrity.check.FieldLayoutCheck;
import com.atlassian.jira.appconsistency.integrity.check.FilterSubscriptionsSavedFilterCheck;
import com.atlassian.jira.appconsistency.integrity.check.FilterSubscriptionsTriggerCheck;
import com.atlassian.jira.appconsistency.integrity.check.IssueLinkCheck;
import com.atlassian.jira.appconsistency.integrity.check.PrimaryEntityRelationCreate;
import com.atlassian.jira.appconsistency.integrity.check.PrimaryEntityRelationDelete;
import com.atlassian.jira.appconsistency.integrity.check.SchemePermissionCheck;
import com.atlassian.jira.appconsistency.integrity.check.SearchRequestRelationCheck;
import com.atlassian.jira.appconsistency.integrity.check.SimpleTriggerCheck;
import com.atlassian.jira.appconsistency.integrity.check.WorkflowCurrentStepCheck;
import com.atlassian.jira.appconsistency.integrity.check.WorkflowIssueStatusNull;
import com.atlassian.jira.appconsistency.integrity.check.WorkflowStateCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.EntityIntegrityCheckImpl;
import com.atlassian.jira.appconsistency.integrity.integritycheck.FieldLayoutIntegrityCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.FilterSubscriptionIntegrityCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.SchemePermissionIntegrityCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.SearchRequestRelationIntegrityCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.WorkflowStateIntegrityCheck;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.I18nHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntegrityCheckManagerImpl implements IntegrityCheckManager
{
    private final Map<Long, IntegrityCheck> integrityChecks = new HashMap<Long, IntegrityCheck>();
    private final Map<Long, Check> checks = new HashMap<Long, Check>();
    private int checkId = 1;
    private int integrityCheckId = 1;
    private final I18nHelper i18nBean;

    public IntegrityCheckManagerImpl(final OfBizDelegator ofbizDelegator, final I18nHelper.BeanFactory i18nFactory, final ApplicationProperties applicationProperties)
    {
        this.i18nBean = i18nFactory.getInstance(applicationProperties.getDefaultLocale());

        // Currently hard code the checks because this is easiest.
        final IntegrityCheck issueRelationsCheck = getIssueRelationsCheck(ofbizDelegator);
        final IntegrityCheck searchRequestRelationsCheck = getSearchRequestRelationsCheck(ofbizDelegator);
        final IntegrityCheck schemePermissionCheck = getSchemePermissionCheck(ofbizDelegator);
        final IntegrityCheck workflowStateCheck = getWorkflowIntegrityCheck(ofbizDelegator);
        final IntegrityCheck fieldLayoutCheck = getFieldLayoutIntegrityCheck(ofbizDelegator);
        final IntegrityCheck filterSubscriptionCheck = getFilterSubscriptionIntegrityCheck(ofbizDelegator);

        integrityChecks.put(issueRelationsCheck.getId(), issueRelationsCheck);
        integrityChecks.put(searchRequestRelationsCheck.getId(), searchRequestRelationsCheck);
        integrityChecks.put(schemePermissionCheck.getId(), schemePermissionCheck);
        integrityChecks.put(workflowStateCheck.getId(), workflowStateCheck);
        integrityChecks.put(fieldLayoutCheck.getId(), fieldLayoutCheck);
        integrityChecks.put(filterSubscriptionCheck.getId(), filterSubscriptionCheck);
    }

    public List<IntegrityCheck> getIntegrityChecks()
    {
        final List<IntegrityCheck> result = new ArrayList<IntegrityCheck>(integrityChecks.values());
        Collections.sort(result);
        return result;
    }

    public Check getCheck(final Long checkId)
    {
        return checks.get(checkId);
    }

    public IntegrityCheck getIntegrityCheck(final Long id)
    {
        return integrityChecks.get(id);
    }

    private IntegrityCheck getIssueRelationsCheck(final OfBizDelegator ofBizDelegator)
    {
        final List<EntityCheck> issueChecks = new ArrayList<EntityCheck>();

        // Create Checks for (Issue Relations)
        EntityCheck check = new PrimaryEntityRelationDelete(ofBizDelegator, checkId++, "Parent", "Project");
        issueChecks.add(check);
        checks.put(check.getId(), check);
        check = new PrimaryEntityRelationCreate(ofBizDelegator, checkId++, "Related", "OSWorkflowEntry", "workflowId", EasyMap.build("name", "jira",
            "state", new Integer(0)));
        issueChecks.add(check);
        checks.put(check.getId(), check);
        check = new IssueLinkCheck(ofBizDelegator, checkId++);
        issueChecks.add(check);
        checks.put(check.getId(), check);

        // Create Issue Relations Checks
        return new EntityIntegrityCheckImpl(integrityCheckId++, i18nBean.getText("admin.integrity.check.entity.relation.desc"), "Issue", issueChecks);
    }

    private IntegrityCheck getFilterSubscriptionIntegrityCheck(final OfBizDelegator ofBizDelegator)
    {
        final List<Check> issueChecks = new ArrayList<Check>();
        Check check = new FilterSubscriptionsTriggerCheck(ofBizDelegator, checkId++);
        issueChecks.add(check);
        checks.put(check.getId(), check);
        check = new FilterSubscriptionsSavedFilterCheck(ofBizDelegator, checkId++);
        issueChecks.add(check);
        checks.put(check.getId(), check);
        check = new SimpleTriggerCheck(ofBizDelegator, checkId++);
        issueChecks.add(check);
        checks.put(check.getId(), check);

        return new FilterSubscriptionIntegrityCheck(integrityCheckId++, i18nBean.getText("admin.integrity.check.filter.subscriptions.desc"),
            issueChecks);
    }

    private IntegrityCheck getWorkflowIntegrityCheck(final OfBizDelegator ofBizDelegator)
    {
        final Check check = new WorkflowStateCheck(ofBizDelegator, checkId++);
        checks.put(check.getId(), check);
        final Check check2 = new WorkflowCurrentStepCheck(ofBizDelegator, checkId++);
        checks.put(check2.getId(), check2);
        final Check check3 = new WorkflowIssueStatusNull(ofBizDelegator, checkId++);
        checks.put(check3.getId(), check3);

        return new WorkflowStateIntegrityCheck(integrityCheckId++, i18nBean.getText("admin.integrity.check.workflow.state.desc"), check, check2,
            check3);
    }

    private IntegrityCheck getFieldLayoutIntegrityCheck(final OfBizDelegator ofBizDelegator)
    {
        final Check check = new FieldLayoutCheck(ofBizDelegator, checkId++);
        checks.put(check.getId(), check);

        return new FieldLayoutIntegrityCheck(integrityCheckId++, i18nBean.getText("admin.integrity.check.field.layout.desc"), check);
    }

    private IntegrityCheck getSearchRequestRelationsCheck(final OfBizDelegator ofBizDelegator)
    {
        final Check check = new SearchRequestRelationCheck(ofBizDelegator, checkId++);
        checks.put(check.getId(), check);

        return new SearchRequestRelationIntegrityCheck(integrityCheckId++, i18nBean.getText("admin.integrity.check.search.request.relation.desc"),
            check);
    }

    private IntegrityCheck getSchemePermissionCheck(final OfBizDelegator ofBizDelegator)
    {
        final Check check = new SchemePermissionCheck(ofBizDelegator, checkId++);
        checks.put(check.getId(), check);

        return new SchemePermissionIntegrityCheck(integrityCheckId++, i18nBean.getText("admin.integrity.check.scheme.permission.desc"), check);
    }
}
