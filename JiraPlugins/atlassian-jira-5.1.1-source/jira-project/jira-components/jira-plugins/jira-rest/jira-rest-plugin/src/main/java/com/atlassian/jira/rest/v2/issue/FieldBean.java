package com.atlassian.jira.rest.v2.issue;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.rest.api.issue.JsonTypeBean;
import com.google.common.base.Function;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Locale;

import static com.google.common.collect.Collections2.transform;

/**
* @since v5.0
*/
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name="field")
public class FieldBean
{

    /**
     * A version bean instance used for auto-generated documentation.
     */
    static final FieldBean DOC_EXAMPLE;
    static final FieldBean DOC_EXAMPLE_2;
    static final Collection<FieldBean> DOC_EXAMPLE_LIST;

    static
    {
        JsonType schema = JsonTypeBuilder.system(JsonType.STRING_TYPE, IssueFieldConstants.DESCRIPTION);
        DOC_EXAMPLE = new FieldBean();
        DOC_EXAMPLE.id = "description";
        DOC_EXAMPLE.name = "Description";
        DOC_EXAMPLE.custom = false;
        DOC_EXAMPLE.orderable = true;
        DOC_EXAMPLE.navigable = true;
        DOC_EXAMPLE.searchable = true;
        DOC_EXAMPLE.schema = new JsonTypeBean(schema.getType(), schema.getItems(), schema.getSystem(), schema.getCustom(), schema.getCustomId());

        schema = JsonTypeBuilder.system(JsonType.STRING_TYPE, IssueFieldConstants.SUMMARY);
        DOC_EXAMPLE_2 = new FieldBean();
        DOC_EXAMPLE_2.id = "summary";
        DOC_EXAMPLE_2.name = "Summary";
        DOC_EXAMPLE_2.custom = false;
        DOC_EXAMPLE.orderable = true;
        DOC_EXAMPLE.navigable = true;
        DOC_EXAMPLE.searchable = true;
        DOC_EXAMPLE_2.schema = new JsonTypeBean(schema.getType(), schema.getItems(), schema.getSystem(), schema.getCustom(), schema.getCustomId());

        DOC_EXAMPLE_LIST = EasyList.build(DOC_EXAMPLE, DOC_EXAMPLE_2);
    }

    @XmlElement
    private String id;

    @XmlElement
    private String name;

    @XmlElement
    private Boolean custom;

    @XmlElement
    private Boolean orderable;

    @XmlElement
    private Boolean navigable;

    @XmlElement
    private Boolean searchable;

    @XmlElement
    private JsonTypeBean schema;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Boolean getCustom()
    {
        return custom;
    }

    public void setCustom(Boolean custom)
    {
        this.custom = custom;
    }

    public Boolean getOrderable()
    {
        return orderable;
    }

    public void setOrderable(Boolean orderable)
    {
        this.orderable = orderable;
    }

    public Boolean getNavigable()
    {
        return navigable;
    }

    public void setNavigable(Boolean navigable)
    {
        this.navigable = navigable;
    }

    public Boolean getSearchable()
    {
        return searchable;
    }

    public void setSearchable(Boolean searchable)
    {
        this.searchable = searchable;
    }

    public JsonTypeBean getSchema()
    {
        return schema;
    }

    public void setSchema(JsonTypeBean schema)
    {
        this.schema = schema;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    //Needed so that JAXB works.
    public FieldBean() {}

    public static Collection<FieldBean> shortBeans(final Collection<Field> fields, final FieldManager fieldManager)
    {
        if (fields == null)
        {
            return null;
        }
        return transform(fields, new Function<Field, FieldBean>()
        {
            @Override
            public FieldBean apply(Field from)
            {
                return shortBean(from, fieldManager);
            }
        });
    }

    /**
     *
     * @return null if the input is null
     */
    public static FieldBean shortBean(final Field field, FieldManager fieldManager)
    {
        if (field == null)
        {
            return null;
        }
        final FieldBean bean = new FieldBean();
        bean.id = field.getId();
        bean.name = field.getName();
        bean.custom = field instanceof CustomField;
        bean.orderable = fieldManager.isOrderableField(field);
        bean.navigable = fieldManager.isNavigableField(field);
        bean.searchable = field instanceof SearchableField;

        if (field instanceof RestAwareField)
        {
            JsonType type = ((RestAwareField) field).getJsonSchema();
            bean.schema = new JsonTypeBean(type.getType(), type.getItems(), type.getSystem(), type.getCustom(), type.getCustomId());
        }
        else
        {
            bean.schema = null;
        }
        return bean;
    }

}
