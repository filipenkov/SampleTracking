package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenStore;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: amazkovoi
 * Date: 1/09/2004
 * Time: 16:23:16
 */
public class UpgradeTask_Build83 extends AbstractFieldScreenUpgradeTask
{
    private static final String DEFAULT_TAB_NAME="Field Tab";

    private final FieldManager fieldManager;
    private final FieldScreenManager fieldScreenManager;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;
    private final FieldLayoutManager fieldLayoutManager;

    public UpgradeTask_Build83(FieldManager fieldManager, FieldScreenManager fieldScreenManager, FieldScreenSchemeManager fieldScreenSchemeManager, IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, ProjectManager projectManager, ConstantsManager constantsManager, FieldLayoutManager fieldLayoutManager)
    {
        this.fieldManager = fieldManager;
        this.fieldScreenManager = fieldScreenManager;
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.fieldLayoutManager = fieldLayoutManager;
    }

    public String getBuildNumber()
    {
        return "83";
    }

    public String getShortDescription()
    {
        return "Create default screens.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        // Default screen to be used for Create and Edit issue operations
        FieldScreen defaultFieldScreen = new FieldScreenImpl(fieldScreenManager);
        setupDefaultFieldScreen(defaultFieldScreen);
        FieldScreenTab fieldScreenTab = defaultFieldScreen.addTab(DEFAULT_TAB_NAME);

        // Create a default screen based on the default Field Layout
        FieldLayout defaultFieldLayout = fieldLayoutManager.getFieldLayout();

        // Test if the field layout has been saved to the database
        if (defaultFieldLayout.getGenericValue() != null)
        {
            // If the default field layout has been saved to the database then convert it to a field screen normally
            populateFieldScreenTab(fieldManager, defaultFieldLayout, fieldScreenTab);
        }
        else
        {
            // Otherwise, create a default field screen
            createDefaultFieldScreen(fieldManager, fieldScreenTab);
        }


        // Create a Field Screen Scheme for this field screen
        FieldScreenScheme fieldScreenScheme = new FieldScreenSchemeImpl(fieldScreenSchemeManager);
        setupDefaultFieldScreenScheme(fieldScreenScheme);

        FieldScreenSchemeItem fieldScreenSchemeItem = new FieldScreenSchemeItemImpl(fieldScreenSchemeManager, fieldScreenManager);
        fieldScreenSchemeItem.setIssueOperation(null);
        fieldScreenSchemeItem.setFieldScreen(defaultFieldScreen);
        fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItem);

        // Create an Assign Issue screen - used in workflow transitions
        FieldScreen assignIssueScreen = new FieldScreenImpl(fieldScreenManager);
        setupAssignIssueScreen(assignIssueScreen);
        fieldScreenTab = assignIssueScreen.addTab(DEFAULT_TAB_NAME);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.ASSIGNEE);
        fieldScreenTab.store();

        // Create a Resolve Issue screen - used in workflow transitions
        FieldScreen resolveIssueScreen = new FieldScreenImpl(fieldScreenManager);
        setupResolveIssueScreen(resolveIssueScreen);
        fieldScreenTab = resolveIssueScreen.addTab(DEFAULT_TAB_NAME);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.RESOLUTION);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.FIX_FOR_VERSIONS);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.ASSIGNEE);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.WORKLOG);
        fieldScreenTab.store();

        // Create Issue Type Screen Scheme
        IssueTypeScreenScheme issueTypeScreenScheme = new IssueTypeScreenSchemeImpl(issueTypeScreenSchemeManager, null);
        setupIssueTypeScreenScheme(issueTypeScreenScheme);

        // Create a default scheme entity
        IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(issueTypeScreenSchemeManager, (GenericValue) null, fieldScreenSchemeManager, constantsManager);
        issueTypeScreenSchemeEntity.setIssueTypeId(null);
        issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenScheme);
        issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);

        // Go through all projects an associate them with the default Issue Type Screen Scheme
        for (Iterator iterator = projectManager.getProjects().iterator(); iterator.hasNext();)
        {
            GenericValue projectGV = (GenericValue) iterator.next();
            issueTypeScreenSchemeManager.addSchemeAssociation(projectGV, issueTypeScreenScheme);
        }

        // Ensure that the non system field screens are created with id of 10,000 or more
        setNextId(FieldScreenStore.FIELD_SCREEN_ENTITY_NAME, new Long(10000));
        // Ensure that the non system field screen schemes are created with id of 10,000 or more
        setNextId(FieldScreenSchemeManager.FIELD_SCREEN_SCHEME_ENTITY_NAME, new Long(10000));
    }

    void setupIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        issueTypeScreenScheme.setId(IssueTypeScreenScheme.DEFAULT_SCHEME_ID);
        issueTypeScreenScheme.setName(getI18nTextWithDefault("admin.field.screens.default.issue.type.screen.scheme.name", "Default Issue Type Screen Scheme"));
        issueTypeScreenScheme.setDescription(getI18nTextWithDefault("admin.field.screens.default.issue.type.screen.scheme.description", "The default issue type screen scheme"));
        issueTypeScreenScheme.store();
    }

    void setupResolveIssueScreen(final FieldScreen resolveIssueScreen)
    {
        resolveIssueScreen.setId(WorkflowTransitionUtil.VIEW_RESOLVE_ID);
        resolveIssueScreen.setName(getI18nTextWithDefault("admin.field.screens.resolve.issue.name", "Resolve Issue Screen"));
        resolveIssueScreen.setDescription(getI18nTextWithDefault("admin.field.screens.resolve.issue.description", "Allows to set resolution, change fix versions and assign an issue."));
        resolveIssueScreen.store();
    }

    void setupAssignIssueScreen(final FieldScreen assignIssueScreen)
    {
        assignIssueScreen.setId(WorkflowTransitionUtil.VIEW_COMMENTASSIGN_ID);
        assignIssueScreen.setName(getI18nTextWithDefault("admin.field.screens.assign.issue.name", "Assign Issue Screen"));
        assignIssueScreen.setDescription(getI18nTextWithDefault("admin.field.screens.assign.issue.description", "Allows to assign an issue."));
        assignIssueScreen.store();
    }

    void setupDefaultFieldScreenScheme(final FieldScreenScheme fieldScreenScheme)
    {
        fieldScreenScheme.setId(FieldScreenSchemeManager.DEFAULT_FIELD_SCREEN_SCHEME_ID);
        fieldScreenScheme.setName(getI18nTextWithDefault("admin.field.screens.default.screen.scheme.name", "Default Screen Scheme"));
        fieldScreenScheme.setDescription(getI18nTextWithDefault("admin.field.screens.default.screen.scheme.description", "Default Screen Scheme"));
        fieldScreenScheme.store();
    }

    void setupDefaultFieldScreen(final FieldScreen defaultFieldScreen)
    {
        defaultFieldScreen.setId(FieldScreen.DEFAULT_SCREEN_ID);
        defaultFieldScreen.setName(getI18nTextWithDefault("admin.field.screens.default.name", "Default Screen"));
        defaultFieldScreen.setDescription(getI18nTextWithDefault("admin.field.screens.default.description", "Allows to update all system fields."));
        defaultFieldScreen.store();
    }

    private void createDefaultFieldScreen(FieldManager fieldManager, FieldScreenTab fieldScreenTab)
    {
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.SUMMARY);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.ISSUE_TYPE);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.SECURITY);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.PRIORITY);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.DUE_DATE);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.COMPONENTS);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.AFFECTED_VERSIONS);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.FIX_FOR_VERSIONS);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.ASSIGNEE);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.REPORTER);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.ENVIRONMENT);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.DESCRIPTION);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.TIMETRACKING);
        fieldScreenTab.addFieldScreenLayoutItem(IssueFieldConstants.ATTACHMENT);

        // Get all custom fields
        List customFieldObjects = fieldManager.getCustomFieldManager().getCustomFieldObjects();
        for (int i = 0; i < customFieldObjects.size(); i++)
        {
            CustomField customField = (CustomField) customFieldObjects.get(i);
            fieldScreenTab.addFieldScreenLayoutItem(customField.getId());
        }
    }

    private void setNextId(String entityName, Long nextId) throws GenericEntityException
    {
        // First ensure we have an entry in SequenecValueItem table
        getDelegator().getNextSeqId(entityName);
        // Now set it to nextId
        GenericValue sequeneceItem = EntityUtil.getOnly(getDelegator().findByAnd("SequenceValueItem", EasyMap.build("seqName", entityName)));
        if (sequeneceItem != null)
        {
            sequeneceItem.set("seqId", nextId);
            sequeneceItem.store();
            getDelegator().refreshSequencer();
        }
    }

    private String getI18nTextWithDefault(String key, String defaultResult)
    {
        String result = getApplicationI18n().getText(key);
        if (result.equals(key))
        {
            return defaultResult;
        }
        else
        {
            return result;
        }
    }

    I18nHelper getApplicationI18n()
    {
        return new I18nBean();
    }
}
