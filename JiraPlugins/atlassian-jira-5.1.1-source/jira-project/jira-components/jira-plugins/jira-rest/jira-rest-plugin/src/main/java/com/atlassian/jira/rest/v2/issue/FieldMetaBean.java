package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.rest.StandardOperation;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.rest.api.issue.JsonTypeBean;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

/**
 * Represents the meta data of a field.
 *
 * @since v4.2
 */
@XmlRootElement (name="availableField")
public class FieldMetaBean
{
    @XmlElement
    private boolean required;

    @XmlElement
    private JsonTypeBean schema;

    @XmlElement
    private String name;

    @XmlElement
    private String autoCompleteUrl;

    @XmlElement
    private Collection<String> operations;

    @XmlElement
    private Collection<?> allowedValues;

    FieldMetaBean() {}

    public static FieldMetaBean newBean()
    {
        return new FieldMetaBean();
    }

    public FieldMetaBean(boolean required, JsonType schema, String name, String autoCompleteUrl, Collection<String> operations, Collection<?> allowedValues)
    {
        this.required = required;
        this.schema = schema == null ? null : new JsonTypeBean(schema.getType(), schema.getItems(), schema.getSystem(), schema.getCustom(), schema.getCustomId());
        this.name = name;
        this.autoCompleteUrl = autoCompleteUrl;
        this.operations = operations;
        this.allowedValues = allowedValues;
    }

    static public final FieldMetaBean DOC_EXAMPLE = new FieldMetaBean(
            false, JsonTypeBuilder.customArray(JsonType.OPTION_TYPE, "com.atlassian.jira.plugin.system.customfieldtypes:multiselect", 10001L),
            "My Multi Select",
            null, Lists.newArrayList(StandardOperation.SET.getName(), StandardOperation.ADD.getName()), Lists.newArrayList("red", "blue"));
}
