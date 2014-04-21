package com.atlassian.jira.functest.framework.navigator;

import com.meterware.httpunit.WebForm;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the "Text Search" condition in Navigator.
 *
 * @since v3.13
 */
public class QuerySearchCondition implements NavigatorCondition
{
    private static final String QUERY_ELEMENT = "query";
    
    private String queryString = "";
    private Set <QueryField> fields = new HashSet<QueryField>();

    public QuerySearchCondition()
    {
        this("");
    }

    public QuerySearchCondition(String queryString)
    {
        this(queryString, Collections.<QueryField>emptySet());
    }

    public QuerySearchCondition(String queryString, Collection<? extends QueryField> fields)
    {
        setQueryString(queryString);
        setFields(fields);
    }

    public QuerySearchCondition(QuerySearchCondition condition)
    {
        this(condition.queryString, condition.fields);
    }

    public void setQueryString(String queryString)
    {
        this.queryString = StringUtils.isBlank(queryString) ? "" : queryString;
    }

    public String getQueryString()
    {
        return queryString;
    }

    public void setFields(Collection <? extends QueryField> fields)
    {
        this.fields = (fields == null) ? new HashSet<QueryField>() : new HashSet<QueryField>(fields);
    }

    public boolean addField(QueryField field)
    {
        return field != null && fields.add(field);
    }

    public boolean removeField(QueryField field)
    {
        return field != null && fields.remove(field);
    }

    public void clearFields()
    {
        fields.clear();
    }

    public void setForm(WebTester tester)
    {
        if (!StringUtils.isBlank(queryString) && !fields.isEmpty())
        {
            tester.setFormElement(QUERY_ELEMENT, queryString);

            for (QueryField queryField : QueryField.DEFAULT_FIELDS)
            {
                tester.uncheckCheckbox(queryField.getField());
            }

            for (QueryField queryField : fields)
            {
                //We can't call 'tester.checkCheckbox(queryField.getField());' because it does not work. 
                tester.setFormElement(queryField.getField(), "true");
            }
        }
    }

    public void parseCondition(WebTester tester)
    {
        WebForm form = tester.getDialog().getForm();

        setQueryString(form.getParameterValue(QUERY_ELEMENT));

        clearFields();
        for (final QueryField field : QueryField.DEFAULT_FIELDS)
        {
            final String parameterValue = form.getParameterValue(field.getField());

            if (StringUtils.isNotBlank(parameterValue) && Boolean.valueOf(parameterValue))
            {
                addField(field);
            }
        }
    }

    public void assertSettings(WebTester tester)
    {
        tester.assertFormElementEquals(QUERY_ELEMENT, queryString);
        for (QueryField field : fields)
        {
            tester.assertFormElementPresent(field.getField());
            Assert.assertTrue("Query for field '" + field.getField() + "' should be set.", parseBoolean(tester.getDialog().getFormParameterValue(field.getField())));
        }

        Set<QueryField> disabledFields = new HashSet<QueryField>(QueryField.DEFAULT_FIELDS);
        disabledFields.removeAll(fields);

        for (QueryField field : disabledFields)
        {
            tester.assertFormElementPresent(field.getField());
            Assert.assertFalse("Query for field '" + field.getField() + "' should not be set.", parseBoolean(tester.getDialog().getFormParameterValue(field.getField())));
        }
    }

    public NavigatorCondition copyCondition()
    {
        return new QuerySearchCondition(this);
    }

    public NavigatorCondition copyConditionForParse()
    {
        return new QuerySearchCondition();
    }

    public String toString()
    {
        return "Query Search: '" + queryString + "' on fields: " + fields;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        QuerySearchCondition that = (QuerySearchCondition) o;

        if (fields != null ? !fields.equals(that.fields) : that.fields != null)
        {
            return false;
        }
        if (queryString != null ? !queryString.equals(that.queryString) : that.queryString != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (queryString != null ? queryString.hashCode() : 0);
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        return result;
    }

    private boolean parseBoolean(final String bool)
    {
        return !StringUtils.isBlank(bool) && Boolean.valueOf(bool);
    }

    public static class QueryField
    {
        public static final QueryField SUMMARY = new QueryField("summary");
        public static final QueryField DESCRIPTION = new QueryField("description");
        public static final QueryField COMMENTS = new QueryField("body");
        public static final QueryField ENVIRONMENT = new QueryField("environment");

        public static final Set<QueryField> DEFAULT_FIELDS;

        static
        {
            Set<QueryField> fields = new HashSet<QueryField>();

            fields.add(QueryField.COMMENTS);
            fields.add(QueryField.DESCRIPTION);
            fields.add(QueryField.ENVIRONMENT);
            fields.add(QueryField.SUMMARY);

            DEFAULT_FIELDS = Collections.unmodifiableSet(fields);
        }

        private final String field;

        private QueryField(String field)
        {
            this.field = field;
        }

        public String getField()
        {
            return field;
        }

        public String toString()
        {
            return getField();
        }

        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            QueryField that = (QueryField) o;

            if (!field.equals(that.field))
            {
                return false;
            }

            return true;
        }

        public int hashCode()
        {
            return field.hashCode();
        }
    }
}
