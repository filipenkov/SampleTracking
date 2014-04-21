package com.atlassian.jira.issue;

import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.comparator.VersionComparator;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.LabelsSystemField;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Issue implementation which caches read data, and can persist its data to the database (via an Ofbiz {@link GenericValue}.
 *
 * @since 3.2
 */
public class IssueImpl extends AbstractIssue implements MutableIssue
{
    private final ProjectManager projectManager;
    private final VersionManager versionManager;
    private final LabelManager labelManager;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final SubTaskManager subTaskManager;
    private final ProjectComponentManager projectComponentManager;
    private final UserManager userManager;

    private GenericValue genericValue;

    // Issue fields
    private Long projectId;
    private String key;
    private String issueTypeId;
    private String summary;
    private String description;
    private String environment;
    private String assigneeId;
    private String reporterId;
    private Timestamp created;
    private Timestamp updated;
    private Timestamp dueDate;
    private Timestamp resolutionDate;
    private Long securityLevelId;
    private String priorityId;
    private String resolutionId;
    private String statusId;
    private Long votes;
    private Long watches;
    private Long originalEstimate;
    private Long estimate;
    private Long timespent;
    private Long workflowId;
    private Set<Label> labels;

    // Related entities
    private GenericValue project;
    private GenericValue issueType;
    private User assignee;
    private User reporter;
    private Collection<GenericValue> components;
    private Collection<Version> affectedVersions;
    private Collection<Version> fixVersions;
    private GenericValue securityLevel;
    private GenericValue priority;
    private GenericValue resolution;
    private GenericValue status;

    private final Map<CustomField, Object> customFieldValues;
    private final Map<String, ModifiedValue> modifiedFields;

    /** Used to keep pass parameters for external fields such as attachments */
    private final Map<String, Object> externalFields;

    private Long parentId;
    boolean hasNoParentId = false;
    private Issue parentIssue;

    /**
     * Load an issue backed by the database.
     *
     * @param genericValue              generic value of the issue
     * @param issueManager              issue manager
     * @param projectManager            project manager
     * @param versionManager            version manager
     * @param issueSecurityLevelManager issue security level manager
     * @param constantsManager          constant manager
     * @param subTaskManager            sub-task manager
     * @param attachmentManager         attachment manager
     * @param labelManager              label manager
     * @param projectComponentManager   project component manager
     * @param userManager               user manager
     */
    public IssueImpl(GenericValue genericValue, IssueManager issueManager, ProjectManager projectManager,
            VersionManager versionManager, IssueSecurityLevelManager issueSecurityLevelManager,
            ConstantsManager constantsManager, SubTaskManager subTaskManager, AttachmentManager attachmentManager,
            final LabelManager labelManager, final ProjectComponentManager projectComponentManager,
            final UserManager userManager)
    {
        super(constantsManager, issueManager, attachmentManager);
        this.genericValue = genericValue;
        this.projectManager = projectManager;
        this.versionManager = versionManager;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.subTaskManager = subTaskManager;
        this.labelManager = labelManager;
        this.projectComponentManager = projectComponentManager;
        this.userManager = userManager;
        this.customFieldValues = new HashMap<CustomField, Object>();
        this.modifiedFields = new HashMap<String, ModifiedValue>();
        this.externalFields = new HashMap<String, Object>();
        init(genericValue);
    }

    /**
     * Create an issue, cloning another issue's data.
     *
     * @param issue                     issue
     * @param issueManager              issue manager
     * @param projectManager            project manager
     * @param versionManager            version manager
     * @param issueSecurityLevelManager issue security level manager
     * @param constantsManager          constant manager
     * @param subTaskManager            sub-task manager
     * @param attachmentManager         attachment manager
     * @param labelManager              label manager
     * @param projectComponentManager   project component manager
     * @param userManager               user manager
     */
    public IssueImpl(Issue issue, IssueManager issueManager, ProjectManager projectManager,
            VersionManager versionManager, IssueSecurityLevelManager issueSecurityLevelManager,
            ConstantsManager constantsManager, SubTaskManager subTaskManager, AttachmentManager attachmentManager,
            final LabelManager labelManager, final ProjectComponentManager projectComponentManager, UserManager userManager)
    {
        // We have to pass through null here since we DO NOT want this issue created with a GV
        // If an issue has a Generic Value associated with it it's considered to already exist in JIRA, a clone does not.
        this((GenericValue) null, issueManager, projectManager, versionManager, issueSecurityLevelManager, constantsManager, subTaskManager, attachmentManager, labelManager, projectComponentManager, userManager);

        // Copy all the related properties
        setAffectedVersions(issue.getAffectedVersions());
        setFixVersions(issue.getFixVersions());
        setComponents(issue.getComponents());
        setLabels(issue.getLabels());
        // Now call the init to setup all the attributes for an issue
        init(issue.getGenericValue());
        setParentId(issue.getParentId());
    }

    /**
     * This is a static way of instantiating an Issue Object.
     *
     * @param genericValue generic value of the issue
     * @return new instance of MutableIssue
     * @deprecated You should construct this yourself if at all possible,
     *             or use {@link IssueFactory#getIssue(org.ofbiz.core.entity.GenericValue)} instead.
     */
    public static MutableIssue getIssueObject(GenericValue genericValue)
    {
        return new IssueImpl(genericValue,
                ComponentAccessor.getIssueManager(),
                ComponentAccessor.getProjectManager(),
                ComponentAccessor.getVersionManager(),
                ComponentAccessor.getIssueSecurityLevelManager(),
                ComponentAccessor.getConstantsManager(),
                ComponentManager.getInstance().getSubTaskManager(),
                ComponentAccessor.getAttachmentManager(),
                ComponentManager.getComponentInstanceOfType(LabelManager.class),
                ComponentManager.getComponentInstanceOfType(ProjectComponentManager.class),
                ComponentManager.getComponentInstanceOfType(UserManager.class));
    }

    private void init(GenericValue genericValue)
    {
        if (genericValue != null)
        {
            projectId = genericValue.getLong(IssueFieldConstants.PROJECT);
            key = genericValue.getString("key");
            issueTypeId = genericValue.getString("type");
            summary = genericValue.getString(IssueFieldConstants.SUMMARY);
            description = genericValue.getString(IssueFieldConstants.DESCRIPTION);
            environment = genericValue.getString(IssueFieldConstants.ENVIRONMENT);
            assigneeId = genericValue.getString(IssueFieldConstants.ASSIGNEE);
            reporterId = genericValue.getString(IssueFieldConstants.REPORTER);
            dueDate = genericValue.getTimestamp(IssueFieldConstants.DUE_DATE);
            securityLevelId = genericValue.getLong(IssueFieldConstants.SECURITY);
            priorityId = genericValue.getString(IssueFieldConstants.PRIORITY);
            statusId = genericValue.getString(IssueFieldConstants.STATUS);
            resolutionId = genericValue.getString(IssueFieldConstants.RESOLUTION);
            created = genericValue.getTimestamp(IssueFieldConstants.CREATED);
            updated = genericValue.getTimestamp(IssueFieldConstants.UPDATED);
            resolutionDate = genericValue.getTimestamp(IssueFieldConstants.RESOLUTION_DATE);
            originalEstimate = genericValue.getLong(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE);
            estimate = genericValue.getLong(IssueFieldConstants.TIME_ESTIMATE);
            timespent = genericValue.getLong(IssueFieldConstants.TIME_SPENT);
            votes = genericValue.getLong(IssueFieldConstants.VOTES);
            watches = genericValue.getLong(IssueFieldConstants.WATCHES);

            //older issues don't have any votes stored.
            if (votes == null)
            {
                votes = new Long(0);
            }
            workflowId = genericValue.getLong("workflowId");
        }
    }

    public Long getId()
    {
        return genericValue == null ? null : genericValue.getLong("id");
    }

    public GenericValue getProject()
    {
        //TODO: Why do we lazy load the project? The projectManager already caches this for us.
        if (project == null && projectId != null)
        {
            project = projectManager.getProject(projectId);
        }

        return project;
    }

    public Project getProjectObject()
    {
        Project project = null;
        if (projectId != null)
        {
            project = projectManager.getProjectObj(projectId);
        }

        return project;
    }

    public void setProjectId(Long projectId)
    {
        // This method will call through to setProject(GenericValue project) to do the actual work to ensure compatibility.
        if (projectId == null)
        {
            // Assume the developer knows what they are doing
            setProject(null);
        }
        else
        {
            // Attempt to find the project
            GenericValue gvProject = projectManager.getProject(projectId);
            if (gvProject == null)
            {
                throw new IllegalArgumentException("Invalid Project ID '" + projectId + "'.");
            }
            // Call through to the original setProject() method
            setProject(gvProject);
        }
    }

    public void setProject(GenericValue project)
    {
        // TODO: this method should be deprecated in favour of setProjectId(). We need to investigate if/when the ModifiedValue is used as it currently relies on having a GenericValue.
        ModifiedValue modifiedValue = new ModifiedValue(getProject(), project);

        this.project = project;
        modifiedFields.put(IssueFieldConstants.PROJECT, modifiedValue);

        if (project != null)
        {
            projectId = project.getLong("id");
        }
        else
        {
            projectId = null;
        }

        if (genericValue != null)
        {
            genericValue.set(IssueFieldConstants.PROJECT, projectId);
        }
    }

    @Override
    public void setProjectObject(Project project)
    {
        if (project == null)
        {
            setProject(null);
        }
        else
        {
            setProject(project.getGenericValue());
        }
    }

    public GenericValue getIssueType()
    {
        if (issueType == null && issueTypeId != null)
        {
            issueType = constantsManager.getIssueType(issueTypeId);
        }

        return issueType;
    }

    public void setIssueType(GenericValue issueType)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getIssueType(), issueType);

        this.issueType = issueType;
        modifiedFields.put(IssueFieldConstants.ISSUE_TYPE, modifiedValue);

        if (issueType != null)
        {
            issueTypeId = issueType.getString("id");
        }
        else
        {
            issueTypeId = null;
        }

        updateGV("type", issueTypeId);
    }

    @Override
    public void setIssueTypeObject(IssueType issueType)
    {
        if (issueType == null)
        {
            setIssueType(null);
        }
        else
        {
            setIssueType(issueType.getGenericValue());
        }
    }

    /** Set the issue type, by type id. */
    public void setIssueTypeId(String issueTypeId)
    {
        if (issueTypeId != null)
        {
            setIssueType(constantsManager.getIssueType(issueTypeId));
        }
        else
        {
            setIssueType(null);
        }
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getSummary(), summary);

        this.summary = summary;
        updateGV(IssueFieldConstants.SUMMARY, summary);
        modifiedFields.put(IssueFieldConstants.SUMMARY, modifiedValue);
    }

    @Override
    public User getReporterUser()
    {
        return getReporter();
    }

    @Override
    public User getReporter()
    {
        if (reporter == null && reporterId != null)
        {
            reporter = getUser(reporterId);
        }

        return reporter;
    }

    @Override
    public String getReporterId()
    {
        return reporterId;
    }

    @Override
    public void setReporter(User reporter)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getReporter(), reporter);

        this.reporter = reporter;
        modifiedFields.put(IssueFieldConstants.REPORTER, modifiedValue);

        if (reporter != null)
        {
            reporterId = reporter.getName();
        }
        else
        {
            reporterId = null;
        }

        updateGV(IssueFieldConstants.REPORTER, reporterId);
    }

    @Override
    public void setReporterId(String reporterId)
    {
        setReporter(getUser(reporterId));
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getDescription(), description);

        this.description = description;
        modifiedFields.put(IssueFieldConstants.DESCRIPTION, modifiedValue);
        updateGV(IssueFieldConstants.DESCRIPTION, description);
    }

    public String getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(String environment)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getEnvironment(), environment);

        this.environment = environment;
        modifiedFields.put(IssueFieldConstants.ENVIRONMENT, modifiedValue);
        updateGV(IssueFieldConstants.ENVIRONMENT, environment);
    }

    public User getAssigneeUser()
    {
        return getAssignee();
    }

    public User getAssignee()
    {
        if (assignee == null && assigneeId != null)
        {
            assignee = getUser(assigneeId);
        }

        return assignee;
    }

    public String getAssigneeId()
    {
        return this.assigneeId;
    }

    public void setAssignee(User assignee)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getAssignee(), assignee);

        this.assignee = assignee;
        modifiedFields.put(IssueFieldConstants.ASSIGNEE, modifiedValue);

        if (assignee != null)
        {
            assigneeId = assignee.getName();
        }
        else
        {
            assigneeId = null;
        }

        updateGV(IssueFieldConstants.ASSIGNEE, assigneeId);
    }

    public void setAssigneeId(String assigneeName)
    {
        setAssignee(getUser(assigneeName));
    }

    public Collection<ProjectComponent> getComponentObjects()
    {
        return projectComponentManager.findComponentsByIssue(this);
    }

    public Collection<GenericValue> getComponents()
    {
        if (components == null)
        {
            components = projectComponentManager.findComponentsByIssueGV(this);
        }

        // Clone the list so that if modifications are made to it then the issue is not automatically updated.
        // To update the components for an issue the setter has to be called
        return new ArrayList<GenericValue>(components);
    }

    public void setComponents(Collection<GenericValue> components)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getComponents(), components);

        this.components = components;
        modifiedFields.put(IssueFieldConstants.COMPONENTS, modifiedValue);
    }

    @Override
    public void setComponentObjects(Collection<ProjectComponent> components)
    {
        if (components == null)
        {
            components = Collections.emptyList();
        }
        Collection<GenericValue> gvComponents = new ArrayList<GenericValue>(components.size());
        for (ProjectComponent component : components)
        {
            gvComponents.add(component.getGenericValue());
        }
        setComponents(gvComponents);
    }

    public Collection<Version> getFixVersions()
    {
        if (fixVersions == null)
        {
            if (genericValue != null)
            {
                final List<Version> newValue = new ArrayList<Version>(versionManager.getFixVersionsFor(this));
                Collections.sort(newValue, VersionComparator.COMPARATOR);
                fixVersions = newValue;
            }
            else
            {
                fixVersions = Collections.emptyList();
            }
        }

        // Clone the list so that if modifications are made to it then the issue is not automatically updated.
        // To update the fix versions for an issue the setter has to be called
        return new ArrayList<Version>(fixVersions);
    }

    public void setFixVersions(Collection<Version> fixVersions)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getFixVersions(), fixVersions);

        this.fixVersions = fixVersions;
        modifiedFields.put(IssueFieldConstants.FIX_FOR_VERSIONS, modifiedValue);
    }

    public Timestamp getDueDate()
    {
        return dueDate;
    }

    public void setDueDate(Timestamp dueDate)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getDueDate(), dueDate);

        this.dueDate = dueDate;
        modifiedFields.put(IssueFieldConstants.DUE_DATE, modifiedValue);
        updateGV(IssueFieldConstants.DUE_DATE, dueDate);
    }

    public GenericValue getSecurityLevel()
    {
        if (securityLevel == null && securityLevelId != null)
        {
            try
            {
                securityLevel = issueSecurityLevelManager.getIssueSecurityLevel(securityLevelId);
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException("Cannot retrieve security level with id '" + securityLevelId + "'.", e);
            }
        }

        return securityLevel;
    }

    public Long getSecurityLevelId()
    {
        return this.securityLevelId;
    }

    public void setSecurityLevelId(Long securityLevelId)
    {
        // This method will call through to setSecurityLevel(GenericValue securityLevel) to do the actual work to ensure compatibility.
        if (securityLevelId == null)
        {
            // Assume the developer knows what they are doing
            setSecurityLevel(null);
        }
        else
        {
            // Attempt to find the level
            GenericValue securityLevelGV = null;
            try
            {
                securityLevelGV = issueSecurityLevelManager.getIssueSecurityLevel(securityLevelId);
            }
            catch (GenericEntityException e)
            {
                throw new IllegalArgumentException("Invalid SecurityLevel ID '" + securityLevelId + "'.");
            }
            if (securityLevelGV == null)
            {
                throw new IllegalArgumentException("Invalid SecurityLevel ID '" + securityLevelId + "'.");
            }
            // Call through to the original setSecurityLevel() method
            setSecurityLevel(securityLevelGV);
        }
    }

    public void setSecurityLevel(GenericValue securityLevel)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getSecurityLevel(), securityLevel);

        this.securityLevel = securityLevel;
        modifiedFields.put(IssueFieldConstants.SECURITY, modifiedValue);

        if (securityLevel != null)
        {
            securityLevelId = securityLevel.getLong("id");
        }
        else
        {
            securityLevelId = null;
        }

        updateGV(IssueFieldConstants.SECURITY, securityLevelId);
    }

    public GenericValue getPriority()
    {
        if (priority == null && priorityId != null)
        {
            Priority priorityObject = constantsManager.getPriorityObject(priorityId);
            priority = (priorityObject != null) ? priorityObject.getGenericValue() : null;
        }

        return priority;
    }

    public void setPriority(GenericValue priority)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getPriority(), priority);

        this.priority = priority;
        modifiedFields.put(IssueFieldConstants.PRIORITY, modifiedValue);

        if (priority != null)
        {
            priorityId = priority.getString("id");
        }
        else
        {
            priorityId = null;
        }

        updateGV(IssueFieldConstants.PRIORITY, priorityId);
    }

    @Override
    public void setPriorityObject(Priority priority)
    {
        if (priority == null)
        {
            setPriority(null);
        }
        else
        {
            setPriority(priority.getGenericValue());
        }
    }

    public void setPriorityId(String priorityId)
    {
        if (priorityId != null)
        {
            Priority priorityObject = constantsManager.getPriorityObject(priorityId);
            setPriority((priorityObject != null) ? priorityObject.getGenericValue() : null);
        }
        else
        {
            setPriority(null);
        }
    }

    @Override
    public String getResolutionId()
    {
        return resolutionId;
    }

    public GenericValue getResolution()
    {
        if (resolution == null && resolutionId != null)
        {
            resolution = constantsManager.getResolution(resolutionId);
        }

        return resolution;
    }

    public void setResolutionId(String resolutionId)
    {
        if (resolutionId != null)
        {
            setResolution(constantsManager.getResolution(resolutionId));
        }
        else
        {
            setResolution(null);
        }
    }

    public void setResolution(GenericValue resolution)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getResolution(), resolution);

        this.resolution = resolution;
        modifiedFields.put(IssueFieldConstants.RESOLUTION, modifiedValue);

        if (resolution != null && resolution.getString("id") != null)
        {
            String oldResolutionId = resolutionId;
            resolutionId = resolution.getString("id");
            //changing the resolution, also needs to update the resolution date.
            if (!resolutionId.equals(oldResolutionId) || resolutionDate == null)
            {
                setResolutionDate(new Timestamp(System.currentTimeMillis()));
            }
        }
        else
        {
            resolutionId = null;
            //if we are changing back to an unresolved state, also clear the resolution date.
            setResolutionDate(null);
        }

        updateGV(IssueFieldConstants.RESOLUTION, resolutionId);
    }

    @Override
    public void setResolutionObject(Resolution resolution)
    {
        if (resolution == null)
        {
            setResolution(null);
        }
        else
        {
            setResolution(resolution.getGenericValue());
        }
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
        updateGV("key", key);
    }

    public Long getVotes()
    {
        return votes;
    }

    public void setVotes(Long votes)
    {
        this.votes = votes;
        updateGV(IssueFieldConstants.VOTES, votes);
    }

    public Long getWatches()
    {
        return watches;
    }

    public void setWatches(Long watches)
    {
        this.watches = watches;
        updateGV(IssueFieldConstants.WATCHES, watches);
    }

    public Collection<Version> getAffectedVersions()
    {
        if (affectedVersions == null)
        {
            if (genericValue != null)
            {
                final List<Version> newValue = new ArrayList<Version>(versionManager.getAffectedVersionsFor(this));
                Collections.sort(newValue, VersionComparator.COMPARATOR);
                affectedVersions = newValue;
            }
            else
            {
                affectedVersions = Collections.emptyList();
            }
        }

        // Clone the list so that if modifications are made to it then the issue is not automatically updated.
        // To update the affected versions for an issue the setter has to be called
        return new ArrayList<Version>(affectedVersions);
    }

    public void setAffectedVersions(Collection<Version> affectedVersions)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getAffectedVersions(), affectedVersions);

        this.affectedVersions = affectedVersions;
        modifiedFields.put(IssueFieldConstants.AFFECTED_VERSIONS, modifiedValue);
    }

    public String getString(String name)
    {
        if (genericValue != null)
        {
            return genericValue.getString(name);
        }
        else
        {
            throw new IllegalStateException("Cannot retrieve '" + name + "' as the issue has not been saved yet.");
        }
    }

    public Timestamp getTimestamp(String name)
    {
        if (genericValue != null)
        {
            return genericValue.getTimestamp(name);
        }
        else
        {
            throw new IllegalArgumentException("Cannot retrieve '" + name + "' as the issue has not been saved yet.");
        }
    }

    public Long getLong(String name)
    {
        if (genericValue != null)
        {
            return genericValue.getLong(name);
        }
        else
        {
            throw new IllegalArgumentException("Cannot retrieve '" + name + "' as the issue has not been saved yet.");
        }
    }

    public GenericValue getGenericValue()
    {
        return genericValue;
    }

    public Timestamp getCreated()
    {
        return created;
    }

    public void setCreated(Timestamp created)
    {
        this.created = created;
        updateGV(IssueFieldConstants.CREATED, created);
    }

    public Timestamp getUpdated()
    {
        return updated;
    }

    public void setUpdated(Timestamp updated)
    {
        this.updated = updated;
        updateGV(IssueFieldConstants.UPDATED, updated);
    }

    public Timestamp getResolutionDate()
    {
        return resolutionDate;
    }

    public void setResolutionDate(Timestamp resolutionDate)
    {
        this.resolutionDate = resolutionDate;
        updateGV(IssueFieldConstants.RESOLUTION_DATE, resolutionDate);
    }

    public Long getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId)
    {
        this.workflowId = workflowId;
    }

    public Object getCustomFieldValue(CustomField customField)
    {
        if (!customFieldValues.containsKey(customField))
        {
            if (genericValue != null)
            {
                customFieldValues.put(customField, customField.getValue(this));
            }
            else
            {
                customFieldValues.put(customField, customField.getDefaultValue(this));
            }
        }

        return customFieldValues.get(customField);
    }

    public void setCustomFieldValue(CustomField customField, Object value)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getCustomFieldValue(customField), value);

        customFieldValues.put(customField, value);

        modifiedFields.put(customField.getId(), modifiedValue);
    }

    public Object getExternalFieldValue(String fieldId)
    {
        return externalFields.get(fieldId);
    }

    public void setExternalFieldValue(String fieldId, Object newValue)
    {
        setExternalFieldValue(fieldId,null,newValue);
    }

    public void setExternalFieldValue(final String fieldId, final Object oldValue, final Object newValue)
    {
        ModifiedValue modifiedValue = new ModifiedValue(oldValue, newValue);
        externalFields.put(fieldId, newValue);
        modifiedFields.put(fieldId, modifiedValue);
    }

    public boolean isSubTask()
    {
        return getParentId() != null;
    }

    public Long getParentId()
    {
        // Parent ID is not stored in the issue table. It is just a special kind of Issue Link.
        if (parentId == null && getGenericValue() != null && !hasNoParentId)
        {
            // Parent ID is null, we attempt to load one from the SubTaskManager
            parentId = subTaskManager.getParentIssueId(getGenericValue());
            if (parentId == null)
            {
                // parent ID is supposed to be null - we are a top level issue. Set the flag so we don't look up again.
                hasNoParentId = true;
            }
        }

        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
        // Remove any explicit link to a parent issue object.
        this.parentIssue = null;
        // TODO: this should probably set hasNoParentId to true, so we can actually set ParentId to null explicitly.
    }

    public void setParentObject(Issue parentIssue) throws IllegalArgumentException
    {
        // JRA-13937 - we allow operations that change the parent and subtask in a single transaction to link the
        // subtask to the NEW Issue object.
        // Particularly useful for when subtask fields get their value from the parent's value - eg Security Level.
        if (parentIssue == null)
        {
            parentId = null;
        }
        else
        {
            if (parentIssue.getId() == null)
            {
                // This is considered an Illegal State - the parent must already be saved to the DB and have an ID.
                // Maybe in the future we may want to allow this; eg in order to have a new issue and new subtask in
                // memory that haven't been saved to DB yet. IF this is ever the case, make sure we reconsider all the
                // getParent()/setParent() and isSubtask() methods.
                throw new IllegalArgumentException("Parent issue cannot have a null ID.");
            }
            parentId = parentIssue.getId();
        }
        this.parentIssue = parentIssue;
    }

    public Issue getParentObject()
    {
        if (parentIssue == null)
        {
            // no explicit parent Object set - let the AbstractIssue look up the object via parentId
            return super.getParentObject();
        }
        else
        {
            // An explicit object was set - we will return this actual object.
            return parentIssue;
        }
    }

    /**
     * @deprecated Use {@link #getParentObject()} instead.
     */
    public GenericValue getParent()
    {
        if (parentIssue == null)
        {
            // no explicit parent Object set - let the AbstractIssue look up the object via parentId
            return super.getParent();
        }
        else
        {
            // An explicit object was set - we will return this actual object.
            return parentIssue.getGenericValue();
        }
    }

    public Collection<GenericValue> getSubTasks()
    {
        if (isCreated())
        {
            return subTaskManager.getSubTasks(getGenericValue());
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public Collection<Issue> getSubTaskObjects()
    {
        if (isCreated())
        {
            return subTaskManager.getSubTaskObjects(this);
        }
        else
        {
            return Collections.emptyList();
        }

    }

    public boolean isCreated()
    {
        return getGenericValue() != null;
    }

    public GenericValue getStatus()
    {
        if (status == null && statusId != null)
        {
            status = constantsManager.getStatus(statusId);
        }

        return status;
    }

    public void setStatusId(String statusId)
    {
        this.statusId = statusId;
        this.status = constantsManager.getStatus(statusId);
        updateGV(IssueFieldConstants.STATUS, statusId);
    }

    public void setLabels(final Set<Label> labels)
    {
        final String oldLabels = StringUtils.join(getLabels(), LabelsSystemField.SEPARATOR_CHAR);
        final String newLabels = StringUtils.join(labels, LabelsSystemField.SEPARATOR_CHAR);
        ModifiedValue modifiedValue = new ModifiedValue(oldLabels, newLabels);

        this.labels = labels;
        modifiedFields.put(IssueFieldConstants.LABELS, modifiedValue);
    }

    public Set<Label> getLabels()
    {
        if (labels == null)
        {
            if (genericValue != null)
            {
                labels = labelManager.getLabels(getId());
            }
            else
            {
                labels = Collections.emptySet();
            }
        }

        return labels;
    }

    public void setStatus(GenericValue status)
    {
        this.status = status;

        // TODO: If status is null, then we should set statusId = null, or throw error.
        if (status != null)
        {
            statusId = status.getString("id");
        }

        updateGV(IssueFieldConstants.STATUS, statusId);
    }

    @Override
    public void setStatusObject(Status status)
    {
        if (status == null)
        {
            setStatus(null);
        }
        else
        {
            setStatus(status.getGenericValue());
        }
    }

    public Long getOriginalEstimate()
    {
        return originalEstimate;
    }

    public void setOriginalEstimate(Long originalEstimate)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getOriginalEstimate(), originalEstimate);

        this.originalEstimate = originalEstimate;
        modifiedFields.put(IssueFieldConstants.TIMETRACKING, modifiedValue);
        updateGV(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE, originalEstimate);
    }

    public Long getEstimate()
    {
        return estimate;
    }

    public void setEstimate(Long estimate)
    {
        ModifiedValue modifiedValue = new ModifiedValue(getEstimate(), estimate);

        this.estimate = estimate;
        modifiedFields.put(IssueFieldConstants.TIMETRACKING, modifiedValue);
        updateGV(IssueFieldConstants.TIME_ESTIMATE, estimate);
    }

    public Long getTimeSpent()
    {
        return timespent;
    }

    public void setTimeSpent(Long timespent)
    {
        this.timespent = timespent;
        updateGV(IssueFieldConstants.TIME_SPENT, timespent);
    }

    public IssueRenderContext getIssueRenderContext()
    {
        return new IssueRenderContext(this);
    }

    public void store()
    {
        try
        {
            if (!isCreated())
            {
                // If we do not have a generic value we need to create a new record
                FieldMap fields = new FieldMap();
                fields.put(IssueFieldConstants.PROJECT, projectId);
                fields.put("type", issueTypeId);
                fields.put(IssueFieldConstants.CREATED, created);
                fields.put(IssueFieldConstants.UPDATED, updated);
                fields.put(IssueFieldConstants.DUE_DATE, dueDate);
                fields.put(IssueFieldConstants.RESOLUTION_DATE, resolutionDate);
                fields.put(IssueFieldConstants.ASSIGNEE, assigneeId);
                fields.put(IssueFieldConstants.REPORTER, reporterId);
                fields.put(IssueFieldConstants.SUMMARY, summary);
                fields.put(IssueFieldConstants.DESCRIPTION, description);
                fields.put(IssueFieldConstants.ENVIRONMENT, environment);
                fields.put(IssueFieldConstants.PRIORITY, priorityId);
                fields.put(IssueFieldConstants.STATUS, statusId);
                fields.put("key", key);
                fields.put(IssueFieldConstants.VOTES, votes);
                fields.put(IssueFieldConstants.WATCHES, watches);
                fields.put(IssueFieldConstants.SECURITY, securityLevelId);
                fields.put(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE, originalEstimate);
                fields.put(IssueFieldConstants.TIME_ESTIMATE, estimate);
                fields.put(IssueFieldConstants.TIME_SPENT, timespent);
                fields.put("workflowId", workflowId);

                genericValue = EntityUtils.createValue("Issue", fields);
                init(genericValue);

                // TODO Create sub-task link
//                if (parentId != null)
//                {
//                    subTaskManager.createSubTaskIssueLink(null, null, null);
//                }
            }
            else
            {
                // Otherwise, just update the record of the issue
                genericValue.store();
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while storing issue.", e);
        }
    }

    public Map<String, ModifiedValue> getModifiedFields()
    {
        return modifiedFields;
    }

    public void resetModifiedFields()
    {
        modifiedFields.clear();
        externalFields.clear();
    }

    private void updateGV(String fieldName, Object fieldValue)
    {
        if (genericValue != null)
        {
            genericValue.set(fieldName, fieldValue);
        }
    }

    /**
     * Get the given User.
     *
     * @param username username
     * @return User with the given username.
     */
    private User getUser(String username)
    {
        if (username != null)
        {
            return userManager.getUserEvenWhenUnknown(username);
        }
        else
        {
            return null;
        }
    }

    public String toString()
    {
        return key == null ? summary : key;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Issue))
        {
            return false;
        }

        final Issue issue = (Issue) o;

        return !(key != null ? !key.equals(issue.getKey()) : issue.getKey() != null);

    }

    public int hashCode()
    {
        return (key != null ? key.hashCode() : 0);
    }
}
