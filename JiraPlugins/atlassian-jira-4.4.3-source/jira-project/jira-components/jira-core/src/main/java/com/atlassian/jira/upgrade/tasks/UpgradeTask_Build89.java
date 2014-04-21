package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.impl.ImportIdLinkCFType;
import com.atlassian.jira.issue.customfields.impl.ReadOnlyCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.searchers.TextSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldImpl;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.web.action.admin.customfields.CreateCustomField;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.List;

public class UpgradeTask_Build89 extends AbstractFieldScreenUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build89.class);

    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;
    private final CustomFieldManager customFieldManager;
    private final FieldConfigSchemeManager configSchemeManager;
    private final GenericConfigManager genericConfigManager;
    private final OfBizDelegator delegator;
    private final JiraContextTreeManager treeManager;

    public UpgradeTask_Build89(ProjectManager projectManager, ConstantsManager constantsManager, CustomFieldManager customFieldManager, FieldConfigSchemeManager configSchemeManager, GenericConfigManager genericConfigManager, OfBizDelegator delegator, JiraContextTreeManager treeManager)
    {
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.customFieldManager = customFieldManager;
        this.configSchemeManager = configSchemeManager;
        this.genericConfigManager = genericConfigManager;
        this.delegator = delegator;
        this.treeManager = treeManager;
    }

    public String getBuildNumber()
    {
        return "89";
    }

    public String getShortDescription()
    {
        return "Upgrade Bugzilla custom field from text to numeric (if present)";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        Long oldBugzillaCustomFieldId = getOldBugzillaCustomFieldId();
        if (oldBugzillaCustomFieldId != null)
        {
            log.info("Old Bugzilla Custom Field found; upgrading..");
            upgradeCustomFieldInstances(oldBugzillaCustomFieldId);
            upgradeCustomField(oldBugzillaCustomFieldId);
        }

        customFieldManager.refresh();
    }

    /**
     * Return ID of the old (text) bugzilla CF. Returns null if not found.
     */
    private Long getOldBugzillaCustomFieldId()
    {
        CustomField cf = customFieldManager.getCustomFieldObjectByName("Bugzilla Id");
        if (cf == null) return null;
        // These field values are the signature of the old (vs. new style) bugzilla custom field
        if (cf.getCustomFieldType() instanceof ReadOnlyCFType && cf.getCustomFieldSearcher() instanceof TextSearcher)
            return cf.getIdAsLong();
        return null;
    }

    /**
     * Change the CustomField type and searcher.
     */
    private void upgradeCustomField(final Long oldBugzillaCustomFieldId) throws GenericEntityException
    {
        log.info("Upgrading Bugzilla Id custom field type..");
        GenericValue cfGV = EntityUtil.getFirst(delegator.findByAnd("CustomField", EasyMap.build("id", oldBugzillaCustomFieldId)));
        cfGV.set(CustomFieldImpl.ENTITY_CF_TYPE_KEY, CreateCustomField.FIELD_TYPE_PREFIX + ImportIdLinkCFType.BUGZILLA_ID_TYPE);
        cfGV.set(CustomFieldImpl.ENTITY_CUSTOM_FIELD_SEARCHER, CreateCustomField.FIELD_TYPE_PREFIX + ImportIdLinkCFType.BUGZILLA_ID_SEARCHER);
        cfGV.store();
    }

    /**
     * Change CF instances from text to numeric.
     */
    private void upgradeCustomFieldInstances(final Long oldBugzillaCustomFieldId) throws GenericEntityException
    {
        log.info("Upgrading instances from text to numeric..");
        List values = delegator.findByAnd("CustomFieldValue", EasyMap.build("customfield", oldBugzillaCustomFieldId));
        Iterator iter = values.iterator();
        while (iter.hasNext())
        {
            GenericValue value = (GenericValue) iter.next();
            value.set("numbervalue", value.getString("stringvalue"));
            value.set("stringvalue", null);
            value.store();
        }
    }
}
