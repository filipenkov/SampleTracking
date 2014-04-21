package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.IssueConstantOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.util.JiraArrayUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ConfigureOptionScheme extends AbstractManageIssueTypeOptionsAction implements ExecutableAction
{
    // ------------------------------------------------------------------------------------------------------- Constants
    private String name;
    private String description;
    private String defaultOption;
    private String[] selectedOptions;

    // For adding a constant on the fly
    private String constantName;
    private String constantDescription;
    private String style;
    private String iconurl;

    // For associating on the fly
    private Long projectId;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    protected final ConstantsManager constantsManager;
    private static final String[] NO_OPTIONS = new String[0];

    // ---------------------------------------------------------------------------------------------------- Constructors
    public ConfigureOptionScheme(final FieldConfigSchemeManager configSchemeManager, final IssueTypeSchemeManager issueTypeSchemeManager, final FieldManager fieldManager, final OptionSetManager optionSetManager, final IssueTypeManageableOption manageableOptionType, final BulkMoveOperation bulkMoveOperation, final SearchProvider searchProvider, final ConstantsManager constantsManager, final IssueManager issueManager)
    {
        super(configSchemeManager, issueTypeSchemeManager, fieldManager, optionSetManager, manageableOptionType, bulkMoveOperation, searchProvider,
            issueManager);
        this.constantsManager = constantsManager;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    @Override
    public String doDefault() throws Exception
    {
        final FieldConfigScheme configScheme = getConfigScheme();
        if (configScheme != null)
        {
            setName(configScheme.getName());
            setDescription(configScheme.getDescription());

            // Set the default
            final IssueType defaultValue = issueTypeSchemeManager.getDefaultValue(configScheme.getOneAndOnlyConfig());
            if (defaultValue != null)
            {
                setDefaultOption(defaultValue.getId());
            }
        }

        return INPUT;
    }

    public String doCopy() throws Exception
    {
        final FieldConfigScheme configScheme = getConfigScheme();
        setName(getComponentManager().getJiraAuthenticationContext().getI18nHelper().getText("common.words.copyof", configScheme.getName()));
        setDescription(configScheme.getDescription());

        // Set the default
        final IssueType defaultValue = issueTypeSchemeManager.getDefaultValue(configScheme.getOneAndOnlyConfig());
        if (defaultValue != null)
        {
            setDefaultOption(defaultValue.getId());
        }

        final Collection originalOptions = getOriginalOptions();
        final String[] optionIds = new String[originalOptions.size()];
        int i = 0;
        for (final Iterator iterator = originalOptions.iterator(); iterator.hasNext();)
        {
            final Option option = (Option) iterator.next();
            optionIds[i] = option.getId();
            i++;
        }
        setSelectedOptions(optionIds);

        // Clear the schemes
        setSchemeId(null);
        setConfigScheme(null);

        return INPUT;
    }

    @Override
    protected void doValidation()
    {
        if (StringUtils.isBlank(name))
        {
            addError("name", getText("admin.common.errors.validname"));
        }

        if ((selectedOptions == null) || (selectedOptions.length == 0))
        {
            addErrorMessage(getText("admin.errors.issuetypes.must.select.option"));
        }
        else
        {
            final String fieldId = getManageableOption().getFieldId();
            for (final String selectedOption : selectedOptions)
            {
                final IssueConstant constant = constantsManager.getConstantObject(fieldId, selectedOption);
                if (constant == null)
                {
                    addErrorMessage(getText("admin.errors.issuetypes.invalid.option.id", selectedOption));
                }
            }
        }

        if (StringUtils.isNotBlank(getDefaultOption()) && !ArrayUtils.contains(getSelectedOptions(), getDefaultOption()))
        {
            addError("defaultOption", getText("admin.errors.issuetypes.default.option.must.be.in.selected"));
        }
    }

    @Override
    protected String doExecute() throws Exception
    {
        final FieldConfigScheme configScheme = executeUpdate();

        if (getProjectId() == null)
        {
            return getRedirect(configScheme);
        }
        else
        {
            // Associate, the returnUrl will be added in forceRedirect.
            final String redirectUrl = "SelectIssueTypeSchemeForProject.jspa?" + "&schemeId=" + configScheme.getId() + "&projectId=" + getProjectId() + "&atl_token=" + getXsrfToken();
            return forceRedirect(redirectUrl);
        }
    }

    public String doAddConstant() throws Exception
    {
        getConstantsManager().validateCreateIssueType(getConstantName(), getStyle(), getConstantDescription(), getIconurl(), this, "constantName");
        if (hasAnyErrors())
        {
            return ERROR;
        }
        final GenericValue createdIssueType = getConstantsManager().createIssueType(getConstantName(), null, getStyle(), getConstantDescription(),
            getIconurl());

        // Add to default scheme
        final String createdId = createdIssueType.getString("id");
        issueTypeSchemeManager.addOptionToDefault(createdId);

        // Add to the selected list
        setSelectedOptions(JiraArrayUtils.add(getSelectedOptions(), createdId));

        return INPUT;
    }

    public void run()
    {
        executeUpdate();
    }

    private FieldConfigScheme executeUpdate()
    {
        final Set<String> optionIds = new LinkedHashSet<String>(Arrays.asList(selectedOptions));

        FieldConfigScheme configScheme = getConfigScheme();
        if (configScheme.getId() == null)
        {
            // Create
            configScheme = issueTypeSchemeManager.create(name, description, new ArrayList<String>(optionIds));
            issueTypeSchemeManager.setDefaultValue(configScheme.getOneAndOnlyConfig(), getDefaultOption());

            log.info("Config scheme '" + configScheme.getName() + "' created successfully. ");
        }
        else
        {
            // Update
            configScheme = issueTypeSchemeManager.update(
                new FieldConfigScheme.Builder(configScheme).setName(name).setDescription(description).toFieldConfigScheme(), optionIds);
            issueTypeSchemeManager.setDefaultValue(configScheme.getOneAndOnlyConfig(), getDefaultOption());
        }

        return configScheme;
    }

    // --------------------------------------------------------------------------------------------- View Helper Methods
    public Collection getOptionsForScheme()
    {
        final String[] selectedOptions = getSelectedOptions();
        if (selectedOptions != null)
        {
            return getNewOptions();
        }
        else
        {
            return getOriginalOptions();
        }
    }

    public boolean isAllowEditOptions()
    {
        return true;
    }

    public Collection getAvailableOptions()
    {
        return CollectionUtils.subtract(getAllOptions(), getOptionsForScheme());
    }

    public Collection getAllOptions()
    {
        final Collection constantObjects = constantsManager.getConstantObjects(getManageableOption().getFieldId());
        final Collection options = new ArrayList(constantObjects);
        CollectionUtils.transform(options, new Transformer()
        {
            public Object transform(final Object input)
            {
                return new IssueConstantOption((IssueConstant) input);
            }
        });
        return options;
    }

    public long getMaxHeight()
    {
        final Collection constantObjects = constantsManager.getConstantObjects(getManageableOption().getFieldId());
        if ((constantObjects != null) && !constantObjects.isEmpty())
        {
            return 23L * constantObjects.size();
        }
        else
        {
            return 23;
        }
    }

    @Override
    public FieldConfigScheme getConfigScheme()
    {
        if (configScheme == null)
        {
            if (schemeId != null)
            {
                configScheme = configSchemeManager.getFieldConfigScheme(schemeId);
            }
            else
            {
                configScheme = new FieldConfigScheme.Builder().setName(name).setDescription(description).setFieldId(fieldId).toFieldConfigScheme();
            }
        }

        return configScheme;
    }

    public GenericValue getProject()
    {
        return ManagerFactory.getProjectManager().getProject(getProjectId());
    }

    // -------------------------------------------------------------------------------------------------- Protected Methods
    public Collection getTargetOptions()
    {
        final List optionIds = new ArrayList(Arrays.asList(getSelectedOptions()));
        return CollectionUtils.collect(optionIds, new Transformer()
        {
            String fieldId = getManageableOption().getFieldId();

            public Object transform(final Object input)
            {
                final String id = (String) input;

                return new IssueConstantOption(constantsManager.getConstantObject(fieldId, id));
            }
        });
    }

    protected Collection getNewOptions()
    {
        final String[] selectedOptions = getSelectedOptions();
        final List selectedOptionsList = new ArrayList();
        for (final String selectedOption : selectedOptions)
        {
            final IssueConstant constantObject = constantsManager.getConstantObject(getManageableOption().getFieldId(), selectedOption);
            selectedOptionsList.add(new IssueConstantOption(constantObject));
        }
        return selectedOptionsList;
    }

    protected Collection getOriginalOptions()
    {
        final FieldConfigScheme configScheme = getConfigScheme();
        final FieldConfig config = configScheme.getOneAndOnlyConfig();
        if (config != null)
        {
            return optionSetManager.getOptionsForConfig(config).getOptions();
        }

        return Collections.EMPTY_LIST;
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public String getDefaultOption()
    {
        return defaultOption;
    }

    public void setDefaultOption(final String defaultOption)
    {
        this.defaultOption = defaultOption;
    }

    public String getConstantName()
    {
        return constantName;
    }

    public void setConstantName(final String constantName)
    {
        this.constantName = constantName;
    }

    public String getConstantDescription()
    {
        return constantDescription;
    }

    public void setConstantDescription(final String constantDescription)
    {
        this.constantDescription = constantDescription;
    }

    public String getIconurl()
    {
        return iconurl;
    }

    public void setIconurl(final String iconurl)
    {
        this.iconurl = iconurl;
    }

    public String getStyle()
    {
        return style;
    }

    public void setStyle(final String style)
    {
        this.style = style;
    }

    public String[] getSelectedOptions()
    {
        return selectedOptions;
    }

    public void setSelectedOptions(final String[] selectedOptions)
    {
        // Iff the one and only select is null
        if (JiraArrayUtils.isContainsOneBlank(selectedOptions))
        {
            this.selectedOptions = NO_OPTIONS;
        }
        else
        {
            this.selectedOptions = selectedOptions;
        }
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(final Long projectId)
    {
        this.projectId = projectId;
    }
}
