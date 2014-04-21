package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.web.action.admin.constants.AbstractViewConstants;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

@WebSudoRequired
public class ViewIssueTypes extends AbstractViewConstants
{
    public static final String NEW_ISSUE_TYPE_DEFAULT_ICON = "/images/icons/genericissue.gif";

    private String style;

    private final FieldManager fieldManager;
    private final FieldConfigSchemeManager configSchemeManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final IssueTypeManageableOption issueTypeManageableOption;

    public ViewIssueTypes(final FieldManager fieldManager, final FieldConfigSchemeManager configSchemeManager,
            final IssueTypeSchemeManager issueTypeSchemeManager, final TranslationManager translationManager,
            final IssueTypeManageableOption issueTypeManageableOption)
    {
        super(translationManager);
        this.fieldManager = fieldManager;
        this.configSchemeManager = configSchemeManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.issueTypeManageableOption = issueTypeManageableOption;
        setIconurl(NEW_ISSUE_TYPE_DEFAULT_ICON);
    }

    protected String getConstantEntityName()
    {
        return "IssueType";
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.issuetype.lowercase");
    }

    protected Collection getConstants()
    {
        return getConstantsManager().getAllIssueTypes();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshIssueTypes();
        fieldManager.refresh();
    }

    protected String getIssueConstantField()
    {
        return "type";
    }

    protected GenericValue getConstant(String id)
    {
        return getConstantsManager().getIssueType(id);
    }

    protected String getRedirectPage()
    {
        return "ViewIssues.jspa";
    }

    public String doAddIssueType() throws Exception
    {
        getConstantsManager().validateCreateIssueType(getName(), getStyle(), getDescription(), getIconurl(), this, "name");
        if (hasAnyErrors())
        {
            return ERROR;
        }
        GenericValue createdIssueType = getConstantsManager().createIssueType(getName(), null, getStyle(), getDescription(), getIconurl());

        // Add to default scheme
        issueTypeSchemeManager.addOptionToDefault(createdIssueType.getString("id"));

        return redirectToView();
    }

    protected String redirectToView()
    {
        return getRedirect("ViewIssueTypes.jspa");
    }

    protected String getDefaultPropertyName()
    {
        return APKeys.JIRA_CONSTANT_DEFAULT_ISSUE_TYPE;
    }

    public ManageableOptionType getManageableOption()
    {
        return issueTypeManageableOption;
    }

    public String getActionType()
    {
        return "view";
    }

    public Collection getAllRelatedSchemes(String id)
    {
        return issueTypeSchemeManager.getAllRelatedSchemes(id);
    }

    public List getSchemes()
    {
        return configSchemeManager.getConfigSchemesForField(fieldManager.getIssueTypeField());
    }

    public FieldConfigScheme getDefaultScheme()
    {
        return issueTypeSchemeManager.getDefaultIssueTypeScheme();
    }

    public String getStyle()
    {
        return style;
    }

    public void setStyle(String style)
    {
        this.style = style;
    }
}
