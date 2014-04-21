package com.atlassian.crowd.directory.ldap;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Information bean for building the UI configuration screen.
 *
 */
public class LdapTypeConfig
{
    private final String key;
    private final String displayName;
    private final Properties defaultValues;
    private final Set<String> hiddenFields = new HashSet<String>();

    public LdapTypeConfig(final String key, final String displayName, final Properties defaultValues)
    {
        this.key = key;
        this.displayName = displayName;
        this.defaultValues = defaultValues;
    }

    public String getKey()
    {
        return key;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setHiddenField(String fieldName)
    {
        hiddenFields.add(fieldName);
    }

    /**
     * Get a JSON String of an array of fields with default value and visibility.
     * @return
     */
    public String getLdapTypeAsJson()
    {
        // { "key": "Open LDAP",
        //   "defaults": {
        //       "ldapBaseDn" : "o=sgi,c=us",
        //       "ldapUserDn" : "o=sgi,c=xd"
        //    },
        //   "hidden":
        //      { "ldapNestedGroups", "ldapfield33",   ...............}
        //  }
        String comma = "";
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"key\": \"").append(key).append("\", ");
        sb.append("\"defaults\": {");
        for (Map.Entry<Object, Object> entry : defaultValues.entrySet())
        {
            String fieldWithDash = entry.getKey().toString().replace('.', '-');
            sb.append(comma);
            sb.append("\"").append(fieldWithDash).append("\":");
            sb.append("\"").append(entry.getValue()).append("\"");
            comma = ",";
        }
        sb.append("},");
        sb.append("\"hidden\": [");
        comma = "";
        for (String field : hiddenFields)
        {
            String fieldWithDash = field.replace('.', '-');
            sb.append(comma);
            sb.append("\"").append(fieldWithDash).append("\"");
            comma = ",";
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

}
