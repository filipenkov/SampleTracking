package com.atlassian.jira.issue.fields.renderer;

import com.atlassian.jira.issue.fields.AffectedVersionsSystemField;
import com.atlassian.jira.issue.fields.ComponentsSystemField;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FixVersionsSystemField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.issue.fields.renderer.HackyRendererType.FROTHER_CONTROL;
import static com.atlassian.jira.issue.fields.renderer.HackyRendererType.SELECT_LIST;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultHackyFieldRendererRegistry implements HackyFieldRendererRegistry
{

    private static final Map<Class<? extends OrderableField>, Set<HackyRendererType>> systemFieldRenderers = new HashMap<Class<? extends OrderableField>, Set<HackyRendererType>>();
    private static final Map<String, Set<HackyRendererType>> customFieldRenderers = new HashMap<String, Set<HackyRendererType>>();

    private static final String CUSTOM_FIELD_KEY_PREFIX = "com.atlassian.jira.plugin.system.customfieldtypes:";

    static
    {
        //hardcoding the frother control renderer to system version & components fields as well as the multiversion custom field.
        final Set<HackyRendererType> renderers = CollectionBuilder.newBuilder(SELECT_LIST, FROTHER_CONTROL).asImmutableListOrderedSet();
        systemFieldRenderers.put(FixVersionsSystemField.class, renderers);
        systemFieldRenderers.put(AffectedVersionsSystemField.class, renderers);
        systemFieldRenderers.put(ComponentsSystemField.class, renderers);

        customFieldRenderers.put(CUSTOM_FIELD_KEY_PREFIX + "multiversion", renderers);
    }

    public boolean shouldOverrideDefaultRenderers(final OrderableField field)
    {
        notNull("field", field);

        if(field instanceof CustomField)
        {
            final String type = ((CustomField) field).getCustomFieldType().getKey();
            return customFieldRenderers.containsKey(type);
        }
        else
        {
            return systemFieldRenderers.containsKey(field.getClass());
        }
    }

    public Set<HackyRendererType> getRendererTypes(final OrderableField field)
    {
        notNull("field", field);

        if(!shouldOverrideDefaultRenderers(field))
        {
            return Collections.emptySet();
        }

        if(field instanceof CustomField)
        {
            final String type = ((CustomField) field).getCustomFieldType().getKey();
            return customFieldRenderers.get(type);
        }
        else
        {
            return systemFieldRenderers.get(field.getClass());
        }
    }

    public HackyRendererType getDefaultRendererType(final OrderableField field)
    {
        notNull("field", field);

        if(shouldOverrideDefaultRenderers(field))
        {
            return FROTHER_CONTROL;
        }
        return null;
    }
}
