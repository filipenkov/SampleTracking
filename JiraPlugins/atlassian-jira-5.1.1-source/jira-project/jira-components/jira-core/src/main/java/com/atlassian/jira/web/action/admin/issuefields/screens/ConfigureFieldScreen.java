package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.action.screen.AddFieldToScreenUtil;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.ProjectFieldScreenHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.comparators.ReverseComparator;
import webwork.action.ActionContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@WebSudoRequired
public class ConfigureFieldScreen extends JiraWebActionSupport
{
    private final FieldManager fieldManager;
    private final FieldScreenManager fieldScreenManager;
    private Long id;
    private FieldScreen fieldScreen;
    private List addableFields;
    private Collection destinationTabs;
    private Collection hlFields;

    private int tabPosition;
    // Set as string as it can be left blank
    private String fieldPosition;
    private String[] fieldId;
    private String tabName;
    private String newTabName;
    private String confirm;

    private static final String NEW_FIELD_POSITION_PREFIX = "newFieldPosition_";
    private static final String NEW_TAB_POSITION_PREFIX = "newTabPosition_";
    private static final String REMOVE_FILED_PREFIX = "removeField_";
    private Boolean tabsAllowed;
    private AddFieldToScreenUtil addFieldToScreenUtil;
    private final ProjectFieldScreenHelper helper;
    private List<Project> projects;

    public ConfigureFieldScreen(final FieldManager fieldManager, final FieldScreenManager fieldScreenManager,
            AddFieldToScreenUtil addFieldToScreenUtil, final ProjectFieldScreenHelper helper)
    {
        this.fieldManager = fieldManager;
        this.fieldScreenManager = fieldScreenManager;
        this.addFieldToScreenUtil = addFieldToScreenUtil;
        this.helper = helper;
        hlFields = new LinkedList();
        tabPosition = -1;
    }

    public String doDefault() throws Exception
    {
        return doExecute();
    }

    protected String doExecute() throws Exception
    {
        if (id == null)
        {
            addErrorMessage(getText("admin.errors.id.cannot.be.null"));
            return getResult();
        }

        if (tabPosition < 0 && getFieldScreen() != null && !getFieldScreen().getTabs().isEmpty())
            tabPosition = 0;

        FieldScreenTab tab = getTab();
        if (tab != null)
            setTabName(tab.getName());

        return getResult();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public FieldScreen getFieldScreen()
    {
        if (fieldScreen == null && id != null)
            fieldScreen = fieldScreenManager.getFieldScreen(id);

        return fieldScreen;
    }

    public String getFieldName(Field field)
    {
        if (fieldManager.isCustomField(field))
        {
            return field.getNameKey();
        }
        else
        {
            return getText(field.getNameKey());
        }
    }

    public Collection getAddableFields()
    {
        if (addableFields == null)
        {
            addableFields = new LinkedList(fieldManager.getOrderableFields());
            // Iterate over the field screen's layout items and remove them from addableFields
            for (Iterator iterator = getFieldScreen().getTabs().iterator(); iterator.hasNext();)
            {
                FieldScreenTab fieldScreenTab = (FieldScreenTab) iterator.next();
                for (Iterator iterator1 = fieldScreenTab.getFieldScreenLayoutItems().iterator(); iterator1.hasNext();)
                {
                    FieldScreenLayoutItem fieldScreenLayoutItem = (FieldScreenLayoutItem) iterator1.next();
                    addableFields.remove(fieldScreenLayoutItem.getOrderableField());
                }
            }

            // a nasty hack to not allow Unscreenable fields to be placed onto a screen (an example is
            // CommentSystemField)
            for (Iterator iterator = addableFields.iterator(); iterator.hasNext();)
            {
                OrderableField orderableField = (OrderableField) iterator.next();
                if(fieldManager.isUnscreenableField(orderableField))
                {
                    iterator.remove();
                }
            }

            Collections.sort(addableFields);
        }

        return addableFields;
    }

    public int getControlRowSize()
    {
        if (getTab().getFieldScreenLayoutItems().isEmpty())
        {
            return 2;
        }
        else
        {
            int rowSize = 3;
            if (getTab().getFieldScreenLayoutItems().size() > 1)
                rowSize += 2;
            if (getFieldScreen().getTabs().size() > 1)
                rowSize++;

            return rowSize;
        }
    }

    public int getButtonRowSize()
    {
        int rowSize = 2;
        if (getTab().getFieldScreenLayoutItems().size() > 1)
            rowSize++;

        return rowSize;
    }

    @RequiresXsrfCheck
    public String doAddField()
    {
        addFieldToScreenUtil.setFieldScreenId(getFieldScreen().getId());
        addFieldToScreenUtil.setTabPosition(getTabPosition());
        addFieldToScreenUtil.setFieldId(getFieldId());
        addFieldToScreenUtil.setFieldPosition(getFieldPosition());
        addErrorCollection(addFieldToScreenUtil.validate());
        
        if (!invalidInput())
        {
            addFieldToScreenUtil.execute();
            hlFields.addAll(addFieldToScreenUtil.getHlFields());
            return redirectToView();
        }

        return getResult();
    }

    public int getTabPosition()
    {
        return tabPosition;
    }

    public FieldScreenTab getTab()
    {
        if (getTabPosition() > -1)
            return getFieldScreen().getTab(getTabPosition());
        else
            return null;
    }

    public void setTabPosition(int tabPosition)
    {
        this.tabPosition = tabPosition;
    }

    public String[] getFieldId()
    {
        return fieldId;
    }

    public void setFieldId(String[] fieldId)
    {
        this.fieldId = fieldId;
    }

    public String getFieldPosition()
    {
        return fieldPosition;
    }

    public void setFieldPosition(String fieldPosition)
    {
        this.fieldPosition = fieldPosition;
    }

    public String doMoveLayoutItemFirst()
    {
        int fieldPosition = Integer.parseInt(getFieldPosition());
        populateHlField(fieldPosition);
        FieldScreenTab fieldScreenTab = getFieldScreen().getTab(getTabPosition());
        fieldScreenTab.moveFieldScreenLayoutItemFirst(fieldPosition);
        fieldScreenTab.store();
        return redirectToView();
    }

    public String doMoveLayoutItemUp()
    {
        int fieldPosition = Integer.parseInt(getFieldPosition());
        populateHlField(fieldPosition);
        FieldScreenTab fieldScreenTab = getFieldScreen().getTab(getTabPosition());
        fieldScreenTab.moveFieldScreenLayoutItemUp(fieldPosition);
        fieldScreenTab.store();
        return redirectToView();
    }

    public String doMoveLayoutItemDown()
    {
        int fieldPosition = Integer.parseInt(getFieldPosition());
        populateHlField(fieldPosition);
        FieldScreenTab fieldScreenTab = getFieldScreen().getTab(getTabPosition());
        fieldScreenTab.moveFieldScreenLayoutItemDown(fieldPosition);
        fieldScreenTab.store();
        return redirectToView();
    }

    public String doMoveLayoutItemLast()
    {
        int fieldPosition = Integer.parseInt(getFieldPosition());
        populateHlField(fieldPosition);
        FieldScreenTab fieldScreenTab = getFieldScreen().getTab(getTabPosition());
        fieldScreenTab.moveFieldScreenLayoutItemLast(fieldPosition);
        fieldScreenTab.store();
        return redirectToView();
    }

    private void populateHlField(int fieldPosition)
    {
        FieldScreenLayoutItem fieldScreenLayoutItem = getFieldScreen().getTab(getTabPosition()).getFieldScreenLayoutItems().get(fieldPosition);
        hlFields.add(fieldScreenLayoutItem.getFieldId());
    }

    public Collection getDestinationTabs()
    {
        if (destinationTabs == null)
        {
            destinationTabs = new LinkedList(getFieldScreen().getTabs());
            destinationTabs.remove(getTab());
        }

        return destinationTabs;
    }

    public String getDestinationTabBoxName(int index)
    {
        return NEW_TAB_POSITION_PREFIX + index;
    }

    public String getRemoveFieldBoxName(int index)
    {
        return REMOVE_FILED_PREFIX + index;
    }

    @RequiresXsrfCheck
    public String doAddTab()
    {
        if (!TextUtils.stringSet(getNewTabName()))
        {
            addError("newTabName", getText("admin.common.errors.validname"));
        }
        else
        {
            for (Iterator iterator = getFieldScreen().getTabs().iterator(); iterator.hasNext();)
            {
                FieldScreenTab fieldScreenTab = (FieldScreenTab) iterator.next();
                if (getNewTabName().equals(fieldScreenTab.getName()))
                {
                    addError("newTabName", getText("admin.errors.field.tab.already.exists"));
                    break;
                }
            }
        }

        if (!invalidInput())
        {
            FieldScreenTab fieldScreenTab = getFieldScreen().addTab(getNewTabName());
            setTabPosition(fieldScreenTab.getPosition());
            return redirectToView();
        }

        return getResult();
    }

    private String redirectToView()
    {
        StringBuilder redirectUrl = new StringBuilder("ConfigureFieldScreen!default.jspa?id=").append(getId());
        if (getTabPosition() > -1)
            redirectUrl.append("&tabPosition=").append(getTabPosition());

        for (Iterator iterator = hlFields.iterator(); iterator.hasNext();)
        {
            redirectUrl.append("&currentFields=").append((String) iterator.next());
        }

        return getRedirect(redirectUrl.toString());
    }

    @RequiresXsrfCheck
    public String doRenameTab()
    {
        if (getTabPosition() < 0 || getTabPosition() >= getFieldScreen().getTabs().size())
        {
            addErrorMessage("Invalid tab position '" + getTabPosition() + "'");
        }

        if (!invalidInput())
        {
            if (TextUtils.stringSet(getTabName()) && !getTab().getName().equals(getTabName()))
            {
                // Rename the tab
                return renameTab();
            }

            return redirectToView();
        }

        return getResult();
    }

    public String doViewDeleteTab()
    {
        validateTabPosition();

        if (invalidInput())
        {
            return getResult();
        }

        return "confirm";
    }

    @RequiresXsrfCheck
    public String doDeleteTab()
    {
        validateTabPosition();

        if (invalidInput())
        {
            return getResult();
        }

        getFieldScreen().removeTab(getTabPosition());
        if (!getFieldScreen().getTabs().isEmpty())
            setTabPosition(0);
        else
            setTabPosition(-1);

        return redirectToView();
    }

    private void validateTabPosition()
    {
        if (getTabPosition() < 0 || getTabPosition() >= getFieldScreen().getTabs().size())
        {
            addErrorMessage("Invalid tab position '" + getTabPosition() + "'");
        }
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    public String getNewPositionTextBoxName(int index)
    {
        FieldScreenLayoutItem fieldScreenLayoutItem = getTab().getFieldScreenLayoutItem(index);
        return NEW_FIELD_POSITION_PREFIX + fieldScreenLayoutItem.getFieldId();
    }

    public String getNewPositionValue(int index)
    {
        return getTextValueFromParams(getNewPositionTextBoxName(index));
    }

    private String getTextValueFromParams(String newPositionFieldName)
    {
        String[] newFieldPositionArray = (String[]) ActionContext.getParameters().get(newPositionFieldName);

        if (newFieldPositionArray != null && newFieldPositionArray.length > 0)
            return newFieldPositionArray[0];
        else
            return "";
    }

    @RequiresXsrfCheck
    public String doConfigureTab()
    {
        Map parameters = ActionContext.getParameters();
        if (parameters.containsKey("moveFieldsToPosition"))
        {
            // Move the fields to a different position on their tab
            return changeFieldPositions(parameters);
        }
        else if (parameters.containsKey("moveFieldsToTab"))
        {
            // Move fields to a different tab
            return changeFieldTabs(parameters);
        }
        else if (parameters.containsKey("deleteFieldsFromTab"))
        {
            // Remove the fields from their tab
            return removeFieldsFromTab(parameters);
        }

        throw new IllegalStateException("Unknown operation.");
    }

    private String renameTab()
    {
        // Validate that the tab with this name already exists
        for (Iterator iterator = getFieldScreen().getTabs().iterator(); iterator.hasNext();)
        {
            FieldScreenTab fieldScreenTab = (FieldScreenTab) iterator.next();
            if ((fieldScreenTab.getPosition() != getTabPosition()) && (getTabName().equals(fieldScreenTab.getName())))
            {
                addError("tabName", getText("admin.errors.tab.already.exists"));
            }
        }

        if (!invalidInput())
        {
            FieldScreenTab fieldScreenTab = getFieldScreen().getTab(getTabPosition());
            fieldScreenTab.setName(getTabName());
            fieldScreenTab.store();
            return redirectToView();
        }

        return getResult();
    }

    private String removeFieldsFromTab(Map parameters)
    {
        Set fieldPositions = new TreeSet(new ReverseComparator());

        // Loop through the submitted parameters and find out which fields to remove from the tab
        for (Iterator iterator = parameters.keySet().iterator(); iterator.hasNext();)
        {
            String paramName = (String) iterator.next();
            if (paramName.startsWith(REMOVE_FILED_PREFIX) && TextUtils.stringSet(getTextValueFromParams(paramName)))
            {
                fieldPositions.add(Integer.valueOf(paramName.substring(REMOVE_FILED_PREFIX.length())));
            }
        }

        if (!invalidInput())
        {
            for (Iterator iterator = fieldPositions.iterator(); iterator.hasNext();)
            {
                Integer fieldPosition = (Integer) iterator.next();
                getTab().removeFieldScreenLayoutItem(fieldPosition.intValue());
            }

            return redirectToView();
        }

        return getResult();
    }

    private String changeFieldTabs(Map parameters)
    {
        Map destinationTabs = new TreeMap(new ReverseComparator());

        // Loop through the submitted parameters and find out which fields to move
        for (Iterator iterator = parameters.keySet().iterator(); iterator.hasNext();)
        {
            String paramName = (String) iterator.next();
            if (paramName.startsWith(NEW_TAB_POSITION_PREFIX) && TextUtils.stringSet(getTextValueFromParams(paramName)))
            {
                Integer fieldPosition = Integer.valueOf(paramName.substring(NEW_TAB_POSITION_PREFIX.length()));
                Integer newTabPosition;
                try
                {
                    newTabPosition = Integer.valueOf(getTextValueFromParams(paramName));
                    if (newTabPosition.intValue() < 0 || newTabPosition.intValue() >= getFieldScreen().getTabs().size())
                    {
                        addError(paramName, getText("admin.errors.invalid.position"));
                    }
                    else
                    {
                        destinationTabs.put(fieldPosition, newTabPosition);
                    }

                }
                catch (NumberFormatException e)
                {
                    addError(paramName, getText("admin.errors.invalid.position"));
                }
            }
        }

        if (!invalidInput())
        {
            for (Iterator iterator = destinationTabs.keySet().iterator(); iterator.hasNext();)
            {
                Integer fieldPosition = (Integer) iterator.next();
                Integer newTabPosition = (Integer) destinationTabs.get(fieldPosition);
                FieldScreenLayoutItem fieldScreenLayoutItem = getTab().removeFieldScreenLayoutItem(fieldPosition.intValue());
                getFieldScreen().getTab(newTabPosition.intValue()).addFieldScreenLayoutItem(fieldScreenLayoutItem.getFieldId());
            }

            return redirectToView();
        }

        return getResult();
    }

    private String changeFieldPositions(Map parameters)
    {
        Map fieldPositions = new TreeMap();

        // Loop through the submitted parameters and find out which fields to move
        for (Iterator iterator = parameters.keySet().iterator(); iterator.hasNext();)
        {
            String paramName = (String) iterator.next();
            if (paramName.startsWith(NEW_FIELD_POSITION_PREFIX) && TextUtils.stringSet(getTextValueFromParams(paramName)))
            {
                String fieldId = paramName.substring(NEW_FIELD_POSITION_PREFIX.length());
                Integer newFieldPosition;
                try
                {
                    newFieldPosition = Integer.valueOf(getTextValueFromParams(paramName));
                    Integer newIndex = new Integer(newFieldPosition.intValue() - 1);
                    if (newFieldPosition.intValue() <= 0 || newFieldPosition.intValue() > getTab().getFieldScreenLayoutItems().size())
                    {
                        //addError(paramName, "Invalid position.");
                        addError(paramName, getText("admin.errors.invalid.position"));
                    }
                    else if (fieldPositions.containsKey(newIndex))
                    {
                        //addError(paramName, "Invalid position.");
                        addError(paramName, getText("admin.errors.invalid.position"));
                    }
                    else
                    {
                        fieldPositions.put(newIndex, getTab().getFieldScreenLayoutItem(fieldId));
                    }
                }
                catch (NumberFormatException e)
                {
                    //addError(paramName, "Invalid position.");
                    addError(paramName, getText("admin.errors.invalid.position"));
                }
            }
        }

        if (!invalidInput())
        {
            getTab().moveFieldScreenLayoutItemToPosition(fieldPositions);
            // Mark fields for highlighting
            for (Iterator iterator = fieldPositions.values().iterator(); iterator.hasNext();)
            {
                populateHlField(((FieldScreenLayoutItem) iterator.next()).getPosition());
            }

            return redirectToView();
        }

        return getResult();
    }

    public String getTabName()
    {
        return tabName;
    }

    public void setTabName(String tabName)
    {
        this.tabName = tabName;
    }

    public String doMoveTabLeft()
    {
        getFieldScreen().moveFieldScreenTabLeft(getTabPosition());
        getFieldScreen().store();
        // Ensure we return to the right tab - its position has now changed, so adjust it
        tabPosition--;
        return redirectToView();
    }

    public String doMoveTabRight()
    {
        getFieldScreen().moveFieldScreenTabRight(getTabPosition());
        getFieldScreen().store();
        // Ensure we return to the right tab - its position has now changed, so adjust it
        tabPosition++;
        return redirectToView();
    }

    public String getNewTabName()
    {
        return newTabName;
    }

    public void setNewTabName(String newTabName)
    {
        this.newTabName = newTabName;
    }

    public Collection getHlFields()
    {
        return hlFields;
    }

    public void setCurrentFields(String[] currentFields)
    {
        this.hlFields = Arrays.asList(currentFields);
    }

    public boolean isTabsAllowed()
    {
        if (tabsAllowed == null)
        {
            tabsAllowed = Boolean.TRUE;
        }

        return tabsAllowed;
    }

    public List<Project> getUsedIn()
    {
        if (projects == null)
        {
            projects = helper.getProjectsForFieldScreen(getFieldScreen());
        }
        return projects;
    }
}