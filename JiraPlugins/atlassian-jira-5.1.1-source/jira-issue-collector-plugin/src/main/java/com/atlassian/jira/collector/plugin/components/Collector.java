package com.atlassian.jira.collector.plugin.components;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents an issue collector and all its relevant configuration. This includes in what project and issue type
 * issues will be created, what user will be used for the reporter as well as a form template to use to get
 * user input and a trigger type.
 * </p>
 * To construct a new collector one should use the provided {@link Builder}.
 *
 * @since v1.0
 */
public final class Collector
{    
    public static class Builder
    {
        private String id = null;
        private String name;
        private Long projectId;
        private Long issueTypeId;
        private String creator;
        private String reporter;
        private String description = null;
        private Template template = null;
        private boolean enabled = false;
        private boolean recordWebInfo = false;
        private boolean useCredentials = false;
        private Trigger trigger = null;
        private String customMessage;
        private List<String> customTemplateFields = new ArrayList<String>();
        private String customTemplateTitle;
        private String customTemplateLabels;

        public Builder id(final String id)
        {
            this.id = id;
            return this;
        }

        public Builder collector(final Collector collector)
        {
            this.id = collector.getId();
            this.name = collector.getName();
            this.projectId = collector.getProjectId();
            this.issueTypeId = collector.getIssueTypeId();
            this.creator = collector.getCreator();
            this.reporter = collector.getReporter();
            this.description = collector.getDescription();
            this.enabled = collector.isEnabled();
            this.recordWebInfo = collector.isRecordWebInfo();
            this.template = collector.getTemplate();
            this.useCredentials = collector.isUseCredentials();
            this.trigger = collector.getTrigger();
            this.customMessage = collector.getCustomMessage();
            this.customTemplateFields = new ArrayList<String>(collector.getCustomTemplateFields());
            this.customTemplateTitle = collector.getCustomTemplateTitle();
            this.customTemplateLabels = collector.getCustomTemplateLabels();
            return this;
        }

        public Builder name(final String name)
        {
            this.name = name;
            return this;
        }

        public Builder projectId(final Long projectId)
        {
            this.projectId = projectId;
            return this;
        }

        public Builder issueTypeId(final Long issueTypeId)
        {
            this.issueTypeId = issueTypeId;
            return this;
        }

        public Builder creator(final String creator)
        {
            this.creator = creator;
            return this;
        }
        
        public Builder reporter(final String reporter)
        {
            this.reporter = reporter;
            return this;
        }

        public Builder description(final String description)
        {
            this.description = description;
            return this;
        }

        public Builder template(final Template template)
        {
            this.template = template;
            return this;
        }

        public Builder enabled(final boolean enabled)
        {
            this.enabled = enabled;
            return this;
        }

        public Builder recoredWebInfo(final boolean enabled)
        {
            this.recordWebInfo = enabled;
            return this;
        }

        public Builder useCredentials(final boolean useCredentials)
        {
            this.useCredentials = useCredentials;
            return this;
        }

        public Builder customMessage(final String customMessage) 
        {
            this.customMessage = customMessage;
            return this;
        }
        
        public Builder customTemplateFields(final List<String> customTemplateFields)
        {
            this.customTemplateFields.clear();
            this.customTemplateFields.addAll(customTemplateFields);
            return this;
        }

        public Builder trigger(final Trigger trigger)
        {
            this.trigger = trigger;
            return this;
        }

        public Collector build()
        {
            return new Collector(id, name, projectId, issueTypeId, creator, reporter, description, template, enabled,
                    recordWebInfo, useCredentials, trigger, customMessage, customTemplateFields, customTemplateTitle, customTemplateLabels);
        }

        public Builder customTemplateTitle(final String customTemplateTitle)
        {
            this.customTemplateTitle  = customTemplateTitle;
            return this;
        }

        public Builder customTemplateLabels(final String customTemplateLabels)
        {
            this.customTemplateLabels = customTemplateLabels;
            return this;
        }
    }

    private final String id;
    private final String name;
    private final Long projectId;
    private final Long issueTypeId;
    private final String creator;
    private final String reporter;
    private final String description;
    private final Template template;
    private final boolean enabled;
    private final boolean recordWebInfo;
    private final String customMessage;
    private final String customTemplateTitle;
    private final boolean useCredentials;
    private final Trigger trigger;
    private final List<String> customTemplateFields = new ArrayList<String>();
    private String customTemplateLabels;



    private Collector(final String id, final String name, final Long projectId, final Long issueTypeId,
            final String creator, final String reporter, final String description, final Template template,
            final boolean enabled, final boolean recordWebInfo, final boolean useCredentials, final Trigger trigger, final String customMessage,
            final List<String> customTemplateFields, final String customTemplateTitle, final String customTemplateLabels)
    {
        this.id = id;
        this.recordWebInfo = recordWebInfo;
        this.customMessage = customMessage;
        this.customTemplateLabels = customTemplateLabels;
        this.customTemplateTitle = customTemplateTitle;
        this.name = notNull("name", name);
        this.projectId = notNull("projectId", projectId);
        this.issueTypeId = notNull("issueTypeId", issueTypeId);
        this.creator = creator;
        this.reporter = notNull("reporter", reporter);
        this.template = notNull("template", template);
        this.description = description;
        this.enabled = enabled;
        this.useCredentials = useCredentials;
        this.trigger = notNull("trigger", trigger);
        //go via a set here to ensure we strip out dupes
        this.customTemplateFields.addAll(new LinkedHashSet<String>(customTemplateFields));
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public Long getIssueTypeId()
    {
        return issueTypeId;
    }

    public String getReporter()
    {
        return reporter;
    }

    public String getDescription()
    {
        return description;
    }

    public Template getTemplate()
    {
        return template;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public boolean isRecordWebInfo()
    {
        return recordWebInfo;
    }

    public boolean isUseCredentials()
    {
        return useCredentials;
    }

    public Trigger getTrigger()
    {
        return trigger;
    }

    public String getCustomMessage()
    {
        return customMessage;
    }

    public List<String> getCustomTemplateFields()
    {
        return customTemplateFields;
    }

    public String getCustomTemplateTitle()
    {
        return customTemplateTitle;
    }

    public String getCustomTemplateLabels()
    {
        return customTemplateLabels == null ? "" : customTemplateLabels;
    }
    
    public String getCreator()
    {
        return creator;
    }


    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final Collector collector = (Collector) o;

        if (id != null ? !id.equals(collector.id) : collector.id != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }
}
