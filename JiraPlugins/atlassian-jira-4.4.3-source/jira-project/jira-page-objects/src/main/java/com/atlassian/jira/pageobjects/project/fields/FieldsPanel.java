package com.atlassian.jira.pageobjects.project.fields;

import com.atlassian.jira.pageobjects.components.DropDown;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.admin.ChangeFieldSchemePage;
import com.atlassian.jira.pageobjects.pages.admin.EditDefaultFieldConfigPage;
import com.atlassian.jira.pageobjects.pages.admin.EditFieldConfigPage;
import com.atlassian.jira.pageobjects.pages.admin.EditFieldSchemePage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Page object representing the Project Configuration Fields tab
 *
 * @since v4.4
 */
public class FieldsPanel extends AbstractJiraPage
{

    private static final String URI = "/plugins/servlet/project-config/%s/fields";
    private final String projectKey;
    private static final String SCHEME_NAME = "project-config-fields-scheme-name";
    private static final String EDIT_LINK_ID = "project-config-fields-scheme-edit";
    private static final String CHANGE_LINK_ID = "project-config-fields-scheme-change";

    @ElementBy(id = "project-config-panel-fields")
    private PageElement fieldsContainer;

    @ElementBy(name = "projectId")
    private PageElement projectId;

    private PageElement schemeName;
    private PageElement schemeEditLink;
    private PageElement schemeChangeLink;
    private DropDown dropDown;

    public FieldsPanel(final String projectKey)
    {
        this.projectKey = projectKey;
    }

    @Init
    public void initialise()
    {
        dropDown = pageBinder.bind(DropDown.class, By.id("project-config-tab-actions"), By.id("project-config-tab-actions-list"));
        schemeName = elementFinder.find(By.id(SCHEME_NAME));
        schemeEditLink = elementFinder.find(By.id(EDIT_LINK_ID));
        schemeChangeLink = elementFinder.find(By.id(CHANGE_LINK_ID));
    }

    @Override
    public TimedCondition isAt()
    {
        return fieldsContainer.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return String.format(URI, projectKey);
    }

    public boolean isSchemeLinked()
    {
        return dropDown.hasItemById(EDIT_LINK_ID);
    }

    public boolean isSchemeChangeAvailable()
    {
        return dropDown.hasItemById(CHANGE_LINK_ID);
    }

    public String getSchemeName()
    {
        return schemeName.getText();
    }

    public long getProjectId()
    {
        return Long.parseLong(projectId.getValue());
    }

    public List<FieldConfiguration> getFieldConfigurations()
    {
        final List<FieldConfiguration> fieldConfigurations = Lists.newArrayList();
        final List<PageElement> fieldConfigurationElements = fieldsContainer.findAll(By.className("project-config-webpanel"));
        for (final PageElement fieldConfigurationElement : fieldConfigurationElements)
        {
            final FieldConfiguration fieldConfiguration = pageBinder.bind(FieldConfigurationImpl.class, fieldConfigurationElement.getAttribute("id"));
            fieldConfigurations.add(fieldConfiguration);
        }
        return fieldConfigurations;
    }

    public EditFieldSchemePage gotoEditConfigScheme()
    {
        return dropDown.openAndClick(By.id(EDIT_LINK_ID), EditFieldSchemePage.class, 0L);
    }

    public EditDefaultFieldConfigPage gotoEditDefaultFieldConfigScheme()
    {
        return dropDown.openAndClick(By.id(EDIT_LINK_ID), EditDefaultFieldConfigPage.class);
    }

    public ChangeFieldSchemePage gotoChangeConfigScheme()
    {
        final String projectId = schemeChangeLink.getAttribute("data-id");
        return dropDown.openAndClick(By.id(CHANGE_LINK_ID), ChangeFieldSchemePage.class, Long.valueOf(projectId));
    }

    /**
     * @since v4.4
     */
    public static class FieldConfigurationImpl implements FieldConfiguration
    {
        private final String id;

        private PageElement fieldConfigContainer;
        private PageElement fieldConfigName;
        private PageElement isDefault;
        private PageElement fieldDefinitionContainer;
        private PageElement sharedBy;
        private PageElement sharedByText;
        private PageElement editFieldConfig;

        @Inject
        private PageBinder pageBinder;
        @Inject
        private PageElementFinder elementFinder;

        @Init
        public void initialize()
        {
            this.fieldConfigContainer = elementFinder.find(By.id(id));
            this.fieldConfigName = fieldConfigContainer.find(By.className("project-config-fields-name"));
            this.fieldDefinitionContainer = elementFinder.find(By.className("project-config-fields-definition"));
            this.isDefault = fieldConfigContainer.find(By.className("status-default"));
            this.sharedBy = fieldConfigContainer.find(By.className("shared-by"));
            this.sharedByText = fieldConfigContainer.find(By.className("shared-item-trigger"));
            this.editFieldConfig = fieldConfigContainer.find(By.className("project-config-icon-edit"));
        }

        public FieldConfigurationImpl(final String id)
        {
            this.id = id;
        }

        @Override
        public String getName()
        {
            return fieldConfigName.getText();
        }

        @Override
        public List<Field> getFields()
        {
            final boolean fieldsVisible = fieldDefinitionContainer.isVisible();
            if(!fieldsVisible)
            {
                fieldConfigName.click();
                assertTrue("Tried to unhide fields but was unsuccessful", fieldDefinitionContainer.isVisible());
            }

            final List<Field> fields = Lists.newArrayList();
            final List<PageElement> fieldRows = fieldDefinitionContainer.findAll(By.cssSelector(".project-config-fieldconfig-row"));
            for (final PageElement fieldRow : fieldRows)
            {
                fields.add(pageBinder.bind(FieldImpl.class, fieldRow));
            }
            return fields;
        }

        @Override
        public boolean isDefault()
        {
            return isDefault.isPresent();
        }

        @Override
        public SharedProjectsDialog openSharedProjects()
        {
            final PageElement trigger = fieldConfigContainer.find(By.className("shared-item-trigger"));
            final String triggerTarget = trigger.getAttribute("href").substring(1);
            return pageBinder.bind(SharedProjectsDialog.class, trigger, triggerTarget).open();
        }

        @Override
        public EditFieldConfigPage gotoEditFieldConfigPage()
        {
            editFieldConfig.click();
            return pageBinder.bind(EditFieldConfigPage.class);
        }

        @Override
        public boolean hasSharedProjects()
        {
            return sharedBy.isPresent();
        }

        @Override
        public String getSharedProjectsText()
        {
            return sharedByText.isPresent() ? sharedByText.getText() : null;
        }

        @Override
        public boolean hasEditLink()
        {
            return editFieldConfig.isPresent();
        }

        @Override
        public List<IssueType> getIssueTypes()
        {
            final List<IssueType> issueTypes = Lists.newArrayList();

            final List<PageElement> issueTypeElements = fieldConfigContainer.find(By.className("project-config-fields-issuetypes"))
                    .findAll(By.className("project-config-list-label"));
            for (final PageElement issueTypeElement : issueTypeElements)
            {
                issueTypes.add(pageBinder.bind(IssueTypeImpl.class, issueTypeElement));
            }
            return issueTypes;
        }

        @Override
        public boolean equals(Object o)
        {
            if(o == null || !(o instanceof  FieldConfiguration))
            {
                return false;
            }
            FieldConfiguration rhs = (FieldConfiguration)o;
            return new EqualsBuilder()
                    .append(getName(), rhs.getName())
                    .append(getFields(), rhs.getFields())
                    .append(isDefault(), rhs.isDefault())
                    .append(getIssueTypes(), rhs.getIssueTypes())
                    .append(hasSharedProjects(), rhs.hasSharedProjects())
                    .append(getSharedProjectsText(), rhs.getSharedProjectsText())
                    .append(hasEditLink(), rhs.hasEditLink())
                    .isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder()
                    .append(getName())
                    .append(getFields())
                    .append(isDefault())
                    .append(getIssueTypes())
                    .append(hasSharedProjects())
                    .append(getSharedProjectsText())
                    .append(hasEditLink())
                    .toHashCode();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                    append("name", getName()).
                    append("fields", getFields()).
                    append("isDefault", isDefault()).
                    append("issueTypes", getIssueTypes()).
                    append("sharedProjects", hasSharedProjects()).
                    append("sharedProjectsText", getSharedProjectsText()).
                    append("editLink", hasEditLink()).
                    toString();
        }

        public static class IssueTypeImpl implements IssueType
        {

            private PageElement issueTypeElement;
            private PageElement name;
            private PageElement icon;

            @Init
            public void initialize()
            {
                this.name = issueTypeElement.find(By.className("project-config-issuetype-name"));
                this.icon = issueTypeElement.find(By.className("project-config-icon-issuetype"));
            }

            public IssueTypeImpl(final PageElement issueTypeElement)
            {
                this.issueTypeElement = issueTypeElement;
            }

            @Override
            public String getName()
            {
                return name.getText();
            }

            @Override
            public String getIconSrc()
            {
                return icon.getAttribute("src");
            }

            @Override
            public boolean equals(Object o)
            {
                if(o == null || !(o instanceof IssueType))
                {
                    return false;
                }
                final IssueType rhs = (IssueType)o;
                return new EqualsBuilder()
                        .append(getName(), rhs.getName())
                        .append(getIconSrc(), rhs.getIconSrc())
                        .isEquals();
            }

            @Override
            public int hashCode()
            {
                return new HashCodeBuilder()
                        .append(getName())
                        .append(getIconSrc())
                        .toHashCode();
            }

            @Override
            public String toString()
            {
                return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                        append("name", getName()).
                        append("iconSrc", getIconSrc()).
                        toString();
            }
        }
    }

    /**
     * @since v4.4
     */
    public static class FieldImpl implements Field
    {
        //plugins/servlet/dialog-project-config?fieldId=customfield_10000&amp;projectKey=HSP
        private static final Pattern SCREEN_DIALOG_PATTERN = Pattern.compile("dialog-project-config\\?.*?fieldId=([^&]*)");

        private PageElement fieldRow;
        private PageElement name;
        private PageElement description;
        private PageElement required;
        private PageElement renderer;

        private PageElement screens;
        @Inject
        private PageBinder pageBinder;

        @Init
        public void initialize()
        {
            this.name = fieldRow.find(By.className("project-config-fieldconfig-name"));
            this.description = fieldRow.find(By.className("project-config-fieldconfig-description"));
            this.required = fieldRow.find(By.className("project-config-fieldconfig-required"));
            this.renderer = fieldRow.find(By.className("project-config-fieldconfig-renderer"));
            this.screens = fieldRow.find(By.className("project-config-associated-screens"));
        }

        public FieldImpl(final PageElement fieldRow)
        {
            this.fieldRow = fieldRow;
        }

        @Override
        public String getName()
        {
            return name.getText();
        }

        @Override
        public String getDescription()
        {
            return description.isPresent() ? description.getText() : null;
        }

        @Override
        public boolean isRequired()
        {
            return required.isPresent();
        }

        @Override
        public String getRenderer()
        {
            return renderer.isPresent() ? renderer.getText() : null;
        }

        @Override
        public String getScreens()
        {
            return screens.getText();
        }

        @Override
        public ScreensDialog openScreensDialog()
        {
            final PageElement trigger = fieldRow.find(By.className("project-config-inlinedialog-trigger"));
            final Matcher matcher = SCREEN_DIALOG_PATTERN.matcher(trigger.getAttribute("href"));
            if (matcher.find())
            {
                final String dialogId = String.format("project-config-inlinedialog-%s", matcher.group(1));
                return pageBinder.bind(ScreensDialog.class, trigger, dialogId).open();
            }
            else
            {
                throw new IllegalStateException("Unexpected URL on the screens dialog.");
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if(o == null || !(o instanceof Field))
            {
                return false;
            }
            final Field rhs = (Field)o;
            return new EqualsBuilder()
                    .append(getName(), rhs.getName())
                    .append(getDescription(), rhs.getDescription())
                    .append(isRequired(), rhs.isRequired())
                    .append(getRenderer(), rhs.getRenderer())
                    .append(getScreens(), rhs.getScreens())
                    .isEquals();

        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder()
                    .append(getName())
                    .append(getDescription())
                    .append(isRequired())
                    .append(getRenderer())
                    .append(getScreens())
                    .toHashCode();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                    append("name", getName()).
                    append("description", getDescription()).
                    append("required", isRequired()).
                    append("renderer", getRenderer()).
                    append("screens", getScreens()).
                    toString();
        }

    }
}
