package com.atlassian.crowd.embedded.admin.support;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Builds a support information output string from a bunch of fields and headings.
 */
public final class SupportInformationBuilder
{
    private final StringBuilder builder = new StringBuilder(1000);

    public void addHeading(String heading)
    {
        builder.append("=== ").append(heading).append(" ===\n");
    }

    public void addField(String name, Object value)
    {
        builder.append(name).append(": ").append(value).append("\n");
    }

    public void newLine()
    {
        builder.append("\n");
    }

    public void addAttributes(String name, Map<String, String> attributes)
    {
        builder.append(name).append(": ").append("\n");
        List<String> keys = Lists.newArrayList(attributes.keySet());
        Collections.sort(keys);
        for (String key : keys)
        {
            String value = (key.contains("password") || key.contains("credential")) ?
                "(not shown)" : "\"" + attributes.get(key) + "\"";
            builder.append("    \"").append(key).append("\": ").append(value).append("\n");
        }
    }

    public String build()
    {
        return builder.toString();
    }
}
