package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.HTMLUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.IssueTypeSystemField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.ProjectSystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.plugin.webfragment.contextproviders.BaseUrlContextProvider;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class IssueCreationHelperBeanImpl implements IssueCreationHelperBean
{
    private final UserUtil userUtil;
    private final FieldManager fieldManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final JiraLicenseService jiraLicenseService;
    private final JiraContactHelper jiraContactHelper;

    public IssueCreationHelperBeanImpl(UserUtil userUtil,
            FieldManager fieldManager, FieldScreenRendererFactory fieldScreenRendererFactory, final JiraLicenseService jiraLicenseService, JiraContactHelper jiraContactHelper)
    {
        this.userUtil = userUtil;
        this.fieldManager = fieldManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.jiraContactHelper = jiraContactHelper;
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
    }

    public void validateCreateIssueFields(final JiraServiceContext jiraServiceContext, final Collection<String> providedFields, final Issue issueObject, final FieldScreenRenderer fieldScreenRenderer,
                                          final OperationContext operationContext, final Map<String, String[]> actionParams, final I18nHelper i18n)
    {
        User remoteUser = jiraServiceContext.getUser();
        ErrorCollection errors = jiraServiceContext.getErrorCollection();
        FieldLayout fieldLayout = fieldScreenRenderer.getFieldLayout();
        List visibleLayoutItems = fieldLayout.getVisibleLayoutItems(remoteUser, issueObject.getProjectObject(), EasyList.build(issueObject.getIssueTypeObject().getId()));
        for (Iterator iterator = visibleLayoutItems.iterator(); iterator.hasNext();)
        {
            FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) iterator.next();
            OrderableField orderableField = fieldLayoutItem.getOrderableField();

            // A hack to get arround issue type not being shown  - issue type is always shown as it is always required.
            if (!IssueFieldConstants.ISSUE_TYPE.equals(orderableField.getId()))
            {
                FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = fieldScreenRenderer.getFieldScreenRenderLayoutItem(orderableField);

                // Test if the field has been shown to the user (i.e. test that it appears on the field screen and was rendered for the user) - otherwise there is no need to validate it
                if (fieldScreenRenderLayoutItem != null && fieldScreenRenderLayoutItem.isShow(issueObject) && providedFields.contains(orderableField.getId()))
                {
                    orderableField.populateFromParams(operationContext.getFieldValuesHolder(), actionParams);
                    try
                    {

                        orderableField.validateParams(operationContext, errors, i18n, issueObject, fieldScreenRenderLayoutItem);
                    }
                    catch (FieldValidationException e)
                    {
                        errors.addError(orderableField.getId(), e.getMessage());
                    }
                }
                else
                {
                    // The default resolution should not be set on issue creation
                    if (!IssueFieldConstants.RESOLUTION.equals(orderableField.getId()))
                    {
                        // If the field has not been shown then let it populate the params with 'default' values
                        orderableField.populateDefaults(operationContext.getFieldValuesHolder(), issueObject);
                        ErrorCollection errorCollection = new SimpleErrorCollection();
                        // Validate the parameter. In theory as the field places a default value itself the value should be valid, however, a check for
                        // 'requireability' still has to be made.
                        try
                        {
                            orderableField.validateParams(operationContext, errorCollection, i18n, issueObject, fieldScreenRenderLayoutItem);
                        }
                        catch (FieldValidationException e)
                        {
                            errorCollection.addError(orderableField.getId(), e.getMessage());
                        }
                        if (errorCollection.getErrors() != null && !errorCollection.getErrors().isEmpty())
                        {
                            // The field has reported errors but is not rendered on the screen - report errors as error messages
                            for (Iterator iterator1 = errorCollection.getErrors().values().iterator(); iterator1.hasNext();)
                            {
                                String result;
                                if (orderableField instanceof CustomField)
                                {
                                    result = orderableField.getName();
                                }
                                else
                                {
                                    result = i18n.getText(orderableField.getNameKey());
                                }
                                errors.addErrorMessage(result + ": " + iterator1.next());
                            }
                        }
                        errors.addErrorMessages(errorCollection.getErrorMessages());
                    }
                }
            }
        }
    }

    public void validateLicense(final ErrorCollection errors,
                                final I18nHelper i18n)
    {
        String contactLink = jiraContactHelper.getAdministratorContactMessage(i18n);
        final LicenseDetails licenseDetails = getLicenseDetails();
        if (!licenseDetails.isLicenseSet())
        {
            errors.addErrorMessage(i18n.getText("createissue.error.invalid.license", contactLink));
        }
        else if (licenseDetails.isExpired())
        {
            errors.addErrorMessage(i18n.getText("createissue.error.license.expired", contactLink));
        }
        else if(userUtil.hasExceededUserLimit())
        {
            errors.addErrorMessage(i18n.getText("createissue.error.license.user.limit.exceeded", contactLink));
        }
    }

    @Override
    public void updateIssueFromFieldValuesHolder(FieldScreenRenderer fieldScreenRenderer, com.opensymphony.user.User remoteUser, MutableIssue issueObject, Map fieldValuesHolder)
    {
        updateIssueFromFieldValuesHolder(fieldScreenRenderer, (User) remoteUser, issueObject, fieldValuesHolder);
    }

    //used for testing
    LicenseDetails getLicenseDetails()
    {
        return jiraLicenseService.getLicense();
    }

    public void updateIssueFromFieldValuesHolder(final FieldScreenRenderer fieldScreenRenderer, final User remoteUser, final MutableIssue issueObject, final Map fieldValuesHolder)
    {
        FieldLayout fieldLayout = fieldScreenRenderer.getFieldLayout();
        List visibleLayoutItems = fieldLayout.getVisibleLayoutItems(remoteUser, issueObject.getProjectObject(), EasyList.build(issueObject.getIssueTypeObject().getId()));
        for (Iterator iterator = visibleLayoutItems.iterator(); iterator.hasNext();)
        {
            FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) iterator.next();
            OrderableField orderableField = fieldLayoutItem.getOrderableField();

            // A hack to get arround issue type not being shown  - issue type is always shown as it is always required.
            if (!IssueFieldConstants.ISSUE_TYPE.equals(orderableField.getId()))
            {
                // Update the issue with needed values
                orderableField.updateIssue(fieldLayoutItem, issueObject, fieldValuesHolder);
            }
        }
    }

    @Override
    public FieldScreenRenderer createFieldScreenRenderer(com.opensymphony.user.User remoteUser, Issue issueObject)
    {
        return createFieldScreenRenderer((User) remoteUser, issueObject);
    }

    /**
     * Create a field screen renderer
     *
     * @param remoteUser
     * @param issueObject - with issue type and project
     */
    public FieldScreenRenderer createFieldScreenRenderer(final User remoteUser, final Issue issueObject)
    {
        return fieldScreenRendererFactory.getFieldScreenRenderer(remoteUser, issueObject, IssueOperations.CREATE_ISSUE_OPERATION, false);
    }

    @Override
    public List<String> getProvidedFieldNames(com.opensymphony.user.User remoteUser, Issue issueObject)
    {
        return getProvidedFieldNames((User) remoteUser, issueObject);
    }

    public List<String> getProvidedFieldNames(final User remoteUser, final Issue issueObject)
    {
        List providedFieldNames = new ArrayList();
        FieldScreenRenderer fieldScreenRenderer = createFieldScreenRenderer(remoteUser, issueObject);
        List visibleLayoutItems = fieldScreenRenderer.getFieldLayout().getVisibleLayoutItems(remoteUser, issueObject.getProjectObject(), EasyList.build(issueObject.getIssueTypeObject().getId()));
        for (Iterator iterator = visibleLayoutItems.iterator(); iterator.hasNext();)
        {
            FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) iterator.next();
            String fieldId = fieldLayoutItem.getOrderableField().getId();
            providedFieldNames.add(fieldId);
        }
        return providedFieldNames;
    }

    public List<OrderableField> getFieldsForCreate(User user, Issue issueObject)
    {
        final List<OrderableField> fields = new ArrayList<OrderableField>();

        FieldScreenRenderer fieldScreenRenderer = createFieldScreenRenderer(user, issueObject);

        for (FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderer.getFieldScreenRenderTabs())
        {
            for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
            {
                if (fieldScreenRenderLayoutItem.isShow(issueObject))
                {
                    fields.add(fieldScreenRenderLayoutItem.getOrderableField());
                }
            }
        }
        return fields;
    }

    public void validateProject(Issue issue, OperationContext operationContext, Map actionParams, final ErrorCollection errors,
                                final I18nHelper i18n)
    {
        // Check that the project selected is a valid one
        ProjectSystemField projectField = (ProjectSystemField) getField(IssueFieldConstants.PROJECT);
        projectField.populateFromParams(operationContext.getFieldValuesHolder(), actionParams);
        projectField.validateParams(operationContext, errors, i18n, issue, null);
    }

    public void validateIssueType(Issue issue, OperationContext operationContext, Map actionParams, final ErrorCollection errors,
                                  final I18nHelper i18n)
    {
        IssueTypeSystemField issueTypeField = (IssueTypeSystemField) getField(IssueFieldConstants.ISSUE_TYPE);
        issueTypeField.populateFromParams(operationContext.getFieldValuesHolder(), actionParams);
        issueTypeField.validateParams(operationContext, errors, i18n, issue, null);
    }

    public Field getField(String id)
    {
        return fieldManager.getField(id);
    }
}
