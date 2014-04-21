package com.atlassian.jira.upgrade.tasks.enterprise;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.upgrade.tasks.AbstractFieldScreenUpgradeTask;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: amazkovoi
 * Date: 1/09/2004
 * Time: 16:23:16
 */
public class UpgradeTask_Build84 extends AbstractFieldScreenUpgradeTask
{
    private final FieldManager fieldManager;
    private final FieldScreenManager fieldScreenManager;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final ConstantsManager constantsManager;

    public UpgradeTask_Build84(FieldManager fieldManager, FieldScreenManager fieldScreenManager, FieldScreenSchemeManager fieldScreenSchemeManager, IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, ConstantsManager constantsManager)
    {
        this.fieldManager = fieldManager;
        this.fieldScreenManager = fieldScreenManager;
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.constantsManager = constantsManager;
    }

    public String getBuildNumber()
    {
        return "84";
    }

    public String getShortDescription()
    {
        return "Create screen scheme for each field layout.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        Map fieldLayouts = new HashMap();
        // Default field layout shoud be mapped to the default field screen scheme
        fieldLayouts.put(null, fieldScreenSchemeManager.getFieldScreenScheme(FieldScreenSchemeManager.DEFAULT_FIELD_SCREEN_SCHEME_ID));


        FieldLayoutManager fieldLayoutManager = fieldManager.getFieldLayoutManager();
        for (Iterator iterator = fieldLayoutManager.getEditableFieldLayouts().iterator(); iterator.hasNext();)
        {
            EditableFieldLayout editableFieldLayout = (EditableFieldLayout) iterator.next();

            // DO not build a field screen for default field layout as it this should have been done in a previous upgrade task
            if (!FieldLayoutManager.TYPE_DEFAULT.equals(editableFieldLayout.getType()))
            {
                // Default screen to be used for all issue operations
                FieldScreen fieldScreen = new FieldScreenImpl(fieldScreenManager);
                fieldScreen.setName(editableFieldLayout.getName());
                fieldScreen.setDescription(editableFieldLayout.getDescription());
                fieldScreen.store();
                FieldScreenTab fieldScreenTab = fieldScreen.addTab("Field Tab");


                populateFieldScreenTab(fieldManager, editableFieldLayout, fieldScreenTab);

                // Create a Field Screen Scheme for this field screen
                FieldScreenScheme fieldScreenScheme = new FieldScreenSchemeImpl(fieldScreenSchemeManager);
                fieldScreenScheme.setName(fieldScreen.getName());
                fieldScreenScheme.setDescription("Scheme for " + fieldScreen.getName());
                fieldScreenScheme.store();

                FieldScreenSchemeItem fieldScreenSchemeItem = new FieldScreenSchemeItemImpl(fieldScreenSchemeManager, fieldScreenManager);
                fieldScreenSchemeItem.setIssueOperation(null);
                fieldScreenSchemeItem.setFieldScreen(fieldScreen);
                fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItem);

                fieldLayouts.put(editableFieldLayout.getId(), fieldScreenScheme);
            }
        }

        // Go throug each field layout scheme and create a issue type field screen for each one
        for (Iterator iterator = fieldLayoutManager.getFieldLayoutSchemes().iterator(); iterator.hasNext();)
        {
            FieldLayoutScheme fieldLayoutScheme = (FieldLayoutScheme) iterator.next();

            IssueTypeScreenScheme issueTypeScreenScheme = new IssueTypeScreenSchemeImpl(issueTypeScreenSchemeManager, null);
            issueTypeScreenScheme.setName("Scheme for " + fieldLayoutScheme.getName());
            issueTypeScreenScheme.store();

            for (Iterator iterator1 = fieldLayoutScheme.getEntities().iterator(); iterator1.hasNext();)
            {
                FieldLayoutSchemeEntity fieldLayoutSchemeEntity = (FieldLayoutSchemeEntity) iterator1.next();
                // Create entity for the new issue type scheme to mirror the confoguration of the field layout scheme
                IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(issueTypeScreenSchemeManager, (GenericValue) null, fieldScreenSchemeManager, constantsManager);
                issueTypeScreenSchemeEntity.setIssueTypeId(fieldLayoutSchemeEntity.getIssueTypeId());
                issueTypeScreenSchemeEntity.setFieldScreenScheme((FieldScreenScheme) fieldLayouts.get(fieldLayoutSchemeEntity.getFieldLayoutId()));
                issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);
            }

            // Associate the new issue type screen scheme with the same projects as the corresponding field layout scheme
            for (Iterator iterator1 = fieldLayoutScheme.getProjects().iterator(); iterator1.hasNext();)
            {
                GenericValue projectGV = (GenericValue) iterator1.next();
                issueTypeScreenSchemeManager.addSchemeAssociation(projectGV, issueTypeScreenScheme);
            }
        }
    }
}
