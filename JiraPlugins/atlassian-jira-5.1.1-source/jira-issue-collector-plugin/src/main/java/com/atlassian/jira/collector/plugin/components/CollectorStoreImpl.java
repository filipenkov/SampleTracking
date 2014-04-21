package com.atlassian.jira.collector.plugin.components;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.security.random.DefaultSecureTokenGenerator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CollectorStoreImpl implements CollectorStore
{
    private static final Logger log = Logger.getLogger(CollectorStoreImpl.class);
    private static final String ARCHIVED_COLLECTORS = "ARCHIVED_COLLECTORS";

    static final class Fields
    {
        static final String COLLECTOR_ID = "collectorId";
        static final String COLLECTOR_NAME = "name";
        static final String PROJECT_ID = "projectId";
        static final String ISSUE_TYPE_ID = "issueTypeId";
        static final String CREATOR = "creator";
        static final String REPORTER = "reporter";
        static final String DESCRIPTION = "description";
        static final String ENABLED = "enabled";
        static final String RECORD_WEB_INFO = "recordWebInfo";
        static final String TEMPLATE_ID = "templateId";
        static final String USE_CREDENTIALS = "useCredentials";
        static final String TRIGGER_TEXT = "triggerText";
        static final String TRIGGER_POSITION = "triggerPosition";
        static final String TRIGGER_CUSTOM_FUNC = "triggerCustomFunc";
        static final String CUSTOM_MESSAGE = "customMessage";
        static final String CUSTOM_TEMPLATE_FIELDS = "customTemplateFields";
        static final String CUSTOM_TEMPLATE_TITLE = "customTemplateTitle";
        static final String CUSTOM_TEMPLATE_LABELS = "customTemplateLabels";

        private Fields() {}
    }


    private final PluginSettingsFactory pluginSettingsFactory;
    private final TemplateStore templateStore;
	private final IssueCollectorEventDispatcher eventDispatcher;

    public CollectorStoreImpl(final PluginSettingsFactory pluginSettingsFactory,
			final TemplateStore templateStore, IssueCollectorEventDispatcher eventDispatcher)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.templateStore = templateStore;
		this.eventDispatcher = eventDispatcher;
	}

    @Override
    public List<Collector> getCollectors(final Long projectId)
    {
        final PluginSettings globalSettings = pluginSettingsFactory.createGlobalSettings();
        final List<String> ids = getAllCollectorIds(projectId, globalSettings);

        final List<Collector> collectors = new ArrayList<Collector>();
        for (String id : ids)
        {
            final Collector collector = getCollector(id);
            if (collector != null)
            {
                collectors.add(collector);
            }
        }

        return collectors;
    }

    @Override
    public Collector getCollector(final String collectorId)
    {
        final PluginSettings settings = pluginSettingsFactory.createSettingsForKey(collectorId);
        Object id = settings.get(Fields.COLLECTOR_ID);
        if (id == null)
        {
            return null;
        }
        else
        {
            Trigger.Position position = null;
            try
            {
                position = Trigger.Position.valueOf((String) settings.get(Fields.TRIGGER_POSITION));
            }
            catch (IllegalArgumentException e)
            {
                position = Trigger.Position.INVALID;
            }
            final Trigger trigger = new Trigger(
                    (String) settings.get(Fields.TRIGGER_TEXT),
                    position,
                    (String) settings.get(Fields.TRIGGER_CUSTOM_FUNC));
            return new Collector.Builder().
                    id((String) settings.get(Fields.COLLECTOR_ID)).
                    name((String) settings.get(Fields.COLLECTOR_NAME)).
                    projectId(getLongValue(settings, Fields.PROJECT_ID, collectorId)).
                    issueTypeId(getLongValue(settings, Fields.ISSUE_TYPE_ID, collectorId)).
                    creator((String) settings.get(Fields.CREATOR)).
                    reporter((String) settings.get(Fields.REPORTER)).
                    description((String) settings.get(Fields.DESCRIPTION)).
                    template(templateStore.getTemplate((String) settings.get(Fields.TEMPLATE_ID))).
                    enabled(getBooleanValue(settings.get(Fields.ENABLED))).
                    recoredWebInfo(getBooleanValue(settings.get(Fields.RECORD_WEB_INFO))).
                    useCredentials(getBooleanValue(settings.get(Fields.USE_CREDENTIALS))).
                    trigger(trigger).
                    customMessage((String) settings.get(Fields.CUSTOM_MESSAGE)).
                    customTemplateFields(getList((String) settings.get(Fields.CUSTOM_TEMPLATE_FIELDS))).
                    customTemplateTitle((String) settings.get(Fields.CUSTOM_TEMPLATE_TITLE)).
                    customTemplateLabels((String) settings.get(Fields.CUSTOM_TEMPLATE_LABELS)).
                    build();
        }
    }

    private boolean getBooleanValue(final Object booleanObject)
    {
        if (booleanObject == null)
        {
            return false;
        }
        return Boolean.valueOf((String) booleanObject);
    }

    @Override
    public boolean enableCollector(final String collectorId)
    {
        final Collector collector = getCollector(collectorId);
        if (collector != null)
        {
			final Collector enabledCollector = new Collector.Builder().collector(collector).enabled(true).build();
			storeCollector(enabledCollector);
			eventDispatcher.collectorEnabled(enabledCollector);

			return true;
        }
        return false;
    }

    @Override
    public boolean disableCollector(final String collectorId)
    {
        final Collector collector = getCollector(collectorId);
        if (collector != null)
        {
			final Collector disabledCollector = new Collector.Builder().collector(collector).enabled(false).build();
			storeCollector(disabledCollector);
			eventDispatcher.collectorDisabled(disabledCollector);

            return true;
        }
        return false;
    }

    @Override
    public void deleteCollector(final Long projectId, final String collectorId)
    {
        PluginSettings globalSettings = pluginSettingsFactory.createGlobalSettings();
        List<String> allCollectorIds = getAllCollectorIds(projectId, globalSettings);
        if (allCollectorIds.contains(collectorId))
        {
			final Collector deletedCollector = getCollector(collectorId);
			if (deletedCollector != null) {
				eventDispatcher.collectorDeleted(deletedCollector);
			}
            final List<String> collectorIds = new ArrayList<String>(allCollectorIds);
            collectorIds.remove(collectorId);
            globalSettings.put(getProjectSettingsKey(projectId), collectorIds);
        }

        PluginSettings settings = pluginSettingsFactory.createSettingsForKey(collectorId);
        settings.remove(Fields.COLLECTOR_ID);
        settings.remove(Fields.COLLECTOR_NAME);
        settings.remove(Fields.PROJECT_ID);
        settings.remove(Fields.ISSUE_TYPE_ID);
        settings.remove(Fields.DESCRIPTION);
        settings.remove(Fields.CREATOR);
        settings.remove(Fields.REPORTER);
        settings.remove(Fields.ENABLED);
        settings.remove(Fields.RECORD_WEB_INFO);
        settings.remove(Fields.TEMPLATE_ID);
        settings.remove(Fields.USE_CREDENTIALS);
        settings.remove(Fields.TRIGGER_TEXT);
        settings.remove(Fields.TRIGGER_POSITION);
        settings.remove(Fields.TRIGGER_CUSTOM_FUNC);
        settings.remove(Fields.CUSTOM_MESSAGE);
        settings.remove(Fields.CUSTOM_TEMPLATE_FIELDS);
        settings.remove(Fields.CUSTOM_TEMPLATE_TITLE);
        settings.remove(Fields.CUSTOM_TEMPLATE_LABELS);

        final Map<String, String> archive = new HashMap<String, String>(getArchivedCollectorIds(globalSettings));
        archive.put(collectorId, Long.toString(projectId));
        globalSettings.put(ARCHIVED_COLLECTORS, archive);
    }
    
    public Long getArchivedProjectId(final String collectorId)
    {        
        final Map<String, String> archivedCollectorIds = getArchivedCollectorIds(pluginSettingsFactory.createGlobalSettings());
        if(archivedCollectorIds.containsKey(collectorId))
        {
            return Long.valueOf(archivedCollectorIds.get(collectorId));
        }
        return null;
    }

    @Override
    public Collector addCollector(final Collector input)
    {
        String collectorId = generateCollectorId();
        //just in case the collector is already taken!
        while (getCollector(collectorId) != null)
        {
            collectorId = generateCollectorId();
        }

		final Collector collector = new Collector.Builder().collector(input).id(collectorId).build();
		storeCollector(collector);
		eventDispatcher.collectorCreated(collector);

        PluginSettings globalSettings = pluginSettingsFactory.createGlobalSettings();
        final List<String> allCollectorIds = new ArrayList<String>();
        allCollectorIds.addAll(getAllCollectorIds(input.getProjectId(), globalSettings));
        allCollectorIds.add(collectorId);
        globalSettings.put(getProjectSettingsKey(input.getProjectId()), allCollectorIds);

        return getCollector(collectorId);
    }

    private void storeCollector(final Collector input)
    {
        final PluginSettings settings = pluginSettingsFactory.createSettingsForKey(input.getId());
        settings.put(Fields.COLLECTOR_ID, input.getId());
        settings.put(Fields.COLLECTOR_NAME, input.getName());
        settings.put(Fields.PROJECT_ID, input.getProjectId().toString());
        settings.put(Fields.ISSUE_TYPE_ID, input.getIssueTypeId().toString());
        settings.put(Fields.CREATOR, input.getCreator());
        settings.put(Fields.REPORTER, input.getReporter());
        settings.put(Fields.DESCRIPTION, input.getDescription());
        settings.put(Fields.TEMPLATE_ID, input.getTemplate().getId());
        settings.put(Fields.ENABLED, Boolean.valueOf(input.isEnabled()).toString());
        settings.put(Fields.RECORD_WEB_INFO, Boolean.valueOf(input.isRecordWebInfo()).toString());
        settings.put(Fields.USE_CREDENTIALS, Boolean.valueOf(input.isUseCredentials()).toString());
        settings.put(Fields.TRIGGER_TEXT, input.getTrigger().getText());
        settings.put(Fields.TRIGGER_POSITION, input.getTrigger().getPosition().toString());
        settings.put(Fields.TRIGGER_CUSTOM_FUNC, input.getTrigger().getCustomFunction());
        settings.put(Fields.CUSTOM_MESSAGE, input.getCustomMessage());
        settings.put(Fields.CUSTOM_TEMPLATE_FIELDS, formatList(input.getCustomTemplateFields()));
        settings.put(Fields.CUSTOM_TEMPLATE_TITLE, input.getCustomTemplateTitle());
        settings.put(Fields.CUSTOM_TEMPLATE_LABELS, input.getCustomTemplateLabels());
    }

    private List<String> getList(String value)
    {
        if(StringUtils.isNotBlank(value))
        {
            return Arrays.asList(value.split(","));
        }
        return Collections.emptyList();
    }

    private String formatList(List<String> templateFields)
    {
        final StringBuilder ret = new StringBuilder();
        final Iterator<String> iterator = templateFields.iterator();
        while (iterator.hasNext())
        {
            String next =  iterator.next();
            ret.append(next);
            if(iterator.hasNext())
            {
                ret.append(",");
            }

        }
        return ret.toString();
    }

    private List<String> getAllCollectorIds(final Long projectId, final PluginSettings globalSettings)
    {
        @SuppressWarnings ("unchecked")
        final List<String> ids = (List<String>) globalSettings.get(getProjectSettingsKey(projectId));
        if (ids == null)
        {
            return Collections.emptyList();
        }
        return ids;
    }

    private Map<String, String> getArchivedCollectorIds(final PluginSettings globalSettings)
    {
        @SuppressWarnings("unchecked")
        final Map<String, String> archive = (Map<String, String>) globalSettings.get(ARCHIVED_COLLECTORS);
        if(archive == null)
        {
            return Collections.emptyMap();
        }
        return archive;
    }
    
    private String getProjectSettingsKey(final Long projectId)
    {
        return "COLLECTOR_IDS." + projectId;
    }

    private Long getLongValue(final PluginSettings settings, final String field, final String id)
    {
        final String valueString = (String) settings.get(field);
        try
        {
            return Long.parseLong(valueString);
        }
        catch (NumberFormatException e)
        {
            log.error("Invalid value for collector '" + id + "' field '" + field + "': '" + valueString + "'");
            return null;
        }
    }

    private String generateCollectorId()
    {
        return DefaultSecureTokenGenerator.getInstance().generateToken().substring(0, 8);
    }
}
