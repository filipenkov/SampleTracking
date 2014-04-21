package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.statistics.AssigneeStatisticsMapper;
import com.atlassian.jira.issue.statistics.ComponentStatisticsMapper;
import com.atlassian.jira.issue.statistics.FixForVersionStatisticsMapper;
import com.atlassian.jira.issue.statistics.IssueTypeStatisticsMapper;
import com.atlassian.jira.issue.statistics.LabelsStatisticsMapper;
import com.atlassian.jira.issue.statistics.PriorityStatisticsMapper;
import com.atlassian.jira.issue.statistics.ProjectStatisticsMapper;
import com.atlassian.jira.issue.statistics.RaisedInVersionStatisticsMapper;
import com.atlassian.jira.issue.statistics.ReporterStatisticsMapper;
import com.atlassian.jira.issue.statistics.ResolutionStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.StatusStatisticsMapper;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.opensymphony.user.UserManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * REST resource for retreiving and validating Statistic Types.
 *
 * @since v4.0
 */
@Path ("/statTypes")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class StatisticTypesResource
{
    public static final String COMPONENTS = "components";
    public static final String FIXFOR = "fixfor";
    public static final String ALLFIXFOR = "allFixfor";
    public static final String VERSION = "version";
    public static final String ALLVERSION = "allVersion";
    public static final String ASSIGNEES = "assignees";
    public static final String ISSUETYPE = "issuetype";
    public static final String PRIORITIES = "priorities";
    public static final String PROJECT = "project";
    public static final String REPORTER = "reporter";
    public static final String RESOLUTION = "resolution";
    public static final String STATUSES = "statuses";
    public static final String LABELS = "labels";

    protected static final Map<String, String> systemValues = new LinkedHashMap<String, String>();

    static
    {
        systemValues.put(ASSIGNEES, "gadget.filterstats.field.statistictype.assignees");
        systemValues.put(COMPONENTS, "gadget.filterstats.field.statistictype.components");
        systemValues.put(ISSUETYPE, "gadget.filterstats.field.statistictype.issuetype");
        systemValues.put(FIXFOR, "gadget.filterstats.field.statistictype.fixfor");
        systemValues.put(ALLFIXFOR, "gadget.filterstats.field.statistictype.allfixfor");
        systemValues.put(PRIORITIES, "gadget.filterstats.field.statistictype.priorities");
        systemValues.put(PROJECT, "gadget.filterstats.field.statistictype.project");
        systemValues.put(VERSION, "gadget.filterstats.field.statistictype.version");
        systemValues.put(ALLVERSION, "gadget.filterstats.field.statistictype.allversion");
        systemValues.put(REPORTER, "gadget.filterstats.field.statistictype.reporter");
        systemValues.put(RESOLUTION, "gadget.filterstats.field.statistictype.resolution");
        systemValues.put(STATUSES, "gadget.filterstats.field.statistictype.statuses");
        systemValues.put(LABELS, "gadget.filterstats.field.statistictype.labels");
    }

    private CustomFieldManager customFieldManager;
    private JiraAuthenticationContext authenticationContext;

    public StatisticTypesResource(final CustomFieldManager customFieldManager, final JiraAuthenticationContext authenticationContext)
    {

        this.customFieldManager = customFieldManager;
        this.authenticationContext = authenticationContext;
    }

    /**
     * Retreive all available Statistic Types in JIRA.  Both System and Custom Fields.
     *
     * @return A collection key/value pairs {@link MapEntry}
     */
    @GET
    public Response getAxies()
    {
        final Map<String, String> values = getValues();
        return Response.ok(new StatTypeCollection(values)).cacheControl(NO_CACHE).build();
    }

    private Map<String, String> getValues()
    {
        final I18nBean i18n = new I18nBean(authenticationContext.getUser());

        final Map<String, String> allValues = new LinkedHashMap<String, String>();

        for (Map.Entry entry : systemValues.entrySet())
        {
            allValues.put((String) entry.getKey(), i18n.getText((String) entry.getValue()));
        }
        final List<CustomField> customFieldObjects = customFieldManager.getCustomFieldObjects();
        for (CustomField customField : customFieldObjects)
        {
            if (customField.getCustomFieldSearcher() instanceof CustomFieldStattable)
            {
                allValues.put(customField.getId(), i18n.getText(customField.getName()));
            }
        }
        return allValues;
    }

    /**
     * Returns the display name for a field
     *
     * @param field The field to get the displayable name for
     * @return A human consumable name for the field
     */
    public String getDisplayName(String field)
    {
        final I18nBean i18n = new I18nBean(authenticationContext.getUser());

        String fieldName = "";
        if (systemValues.containsKey(field))
        {
            fieldName = i18n.getText(systemValues.get(field));
        }
        else
        {
            final CustomField customField = customFieldManager.getCustomFieldObject(field);
            if (customField != null)
            {
                fieldName = customField.getName();
            }
        }
        return fieldName;
    }

    /**
     * Get the {@link StatisticsMapper} associated with passed key
     *
     * @param statsMapperKey The key for the statsmapper.  Usually a customfield id, or one of the constants in this
     *                       class
     * @return The StatisticsMapper associated with the passed in key.
     */
    public StatisticsMapper getStatsMapper(String statsMapperKey)
    {
        StatisticsMapper systemMapper = getSystemMapper(statsMapperKey);
        if (systemMapper != null)
        {
            return systemMapper;
        }

        CustomField customField = customFieldManager.getCustomFieldObject(statsMapperKey);
        if (customField == null)
        {
            throw new RuntimeException("No custom field with id " + statsMapperKey);
        }
        if (customField.getCustomFieldSearcher() instanceof CustomFieldStattable)
        {
            final CustomFieldStattable customFieldStattable = (CustomFieldStattable) customField.getCustomFieldSearcher();
            return customFieldStattable.getStatisticsMapper(customField);
        }
        else
        {
            return null;
        }
    }

    private StatisticsMapper getSystemMapper(String statsMapperKey)
    {
        if (COMPONENTS.equals(statsMapperKey))
        {
            return new ComponentStatisticsMapper();
        }
        else if (ASSIGNEES.equals(statsMapperKey))
        {
            return new AssigneeStatisticsMapper(ManagerFactory.getUserManager(), ComponentAccessor.getJiraAuthenticationContext());
        }
        else if (ISSUETYPE.equals(statsMapperKey))
        {
            return new IssueTypeStatisticsMapper(ManagerFactory.getConstantsManager());
        }
        else if (FIXFOR.equals(statsMapperKey))
        {
            return new FixForVersionStatisticsMapper(ComponentAccessor.getVersionManager(), false);
        }
        else if (ALLFIXFOR.equals(statsMapperKey))
        {
            return new FixForVersionStatisticsMapper(ComponentAccessor.getVersionManager(), true);
        }
        else if (PRIORITIES.equals(statsMapperKey))
        {
            return new PriorityStatisticsMapper(ManagerFactory.getConstantsManager());
        }
        else if (PROJECT.equals(statsMapperKey))
        {
            return new ProjectStatisticsMapper(ManagerFactory.getProjectManager());
        }
        else if (VERSION.equals(statsMapperKey))
        {
            return new RaisedInVersionStatisticsMapper(ComponentAccessor.getVersionManager(), false);
        }
        else if (ALLVERSION.equals(statsMapperKey))
        {
            return new RaisedInVersionStatisticsMapper(ComponentAccessor.getVersionManager(), true);
        }
        else if (REPORTER.equals(statsMapperKey))
        {
            return new ReporterStatisticsMapper(ManagerFactory.getUserManager(), ComponentAccessor.getJiraAuthenticationContext());
        }
        else if (RESOLUTION.equals(statsMapperKey))
        {
            return new ResolutionStatisticsMapper(ManagerFactory.getConstantsManager());
        }
        else if (STATUSES.equals(statsMapperKey))
        {
            return new StatusStatisticsMapper(ManagerFactory.getConstantsManager());
        }
        else if (LABELS.equals(statsMapperKey))
        {
            return new LabelsStatisticsMapper(false);
        }
        

        return null; // custom field maybe?
    }

    ///CLOVER:OFF
    @XmlRootElement
    public static class StatTypeCollection
    {
        @XmlElement
        private Collection<MapEntry> stats;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private StatTypeCollection()
        { }

        public StatTypeCollection(final Map<String, String> values)
        {

            this.stats = convertToMapEntryCollection(values);
        }

        public Collection<MapEntry> getValues()
        {
            return stats;
        }

        private Collection<MapEntry> convertToMapEntryCollection(final Map<String, String> values)
        {
            final Collection<MapEntry> entries = new ArrayList<MapEntry>();
            for (Map.Entry<String, String> mapEntry : values.entrySet())
            {
                entries.add(new MapEntry(mapEntry.getKey(), mapEntry.getValue()));
            }
            return entries;
        }
    }

    @XmlRootElement
    public static class MapEntry
    {
        @XmlElement
        private final String value;

        @XmlElement
        private final String label;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private MapEntry()
        {
            value = null;
            label = null;
        }

        public MapEntry(final String value, final String label)
        {
            this.value = value;
            this.label = label;
        }

        public String getKey()
        {
            return value;
        }

        public String getValue()
        {
            return label;
        }
    }

    @XmlRootElement
    public static class StatName
    {
        @XmlElement
        private String name;
        @XmlElement
        private String description;
        @XmlElement
        private String query;
        @XmlElement
        private String img;
        @XmlElement
        private boolean html;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private StatName()
        {}

        public StatName(final String name, final String query, final boolean isHtml)
        {
            this.name = name;
            this.query = query;
        }

        public StatName(final String name, final String description, final String query, final String img, final boolean isHtml)
        {
            this.name = name;
            this.description = description;
            this.query = query;
            this.img = img;
            html = isHtml;
        }
    }
    ///CLOVER:ON
}
