package com.atlassian.jira.issue.statistics.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Searchable;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;

public class FieldHitCollector extends DocumentHitCollector
{
    private List<String> values = new ArrayList<String>();
    private final String fieldName;

    public FieldHitCollector(Searchable searcher, final String fieldName)
    {
        super(searcher);
        this.fieldName = notBlank("fieldName", fieldName);
    }

    public void collect(Document d)
    {
        values.add(d.get(getFieldName()));
    }

    public List<String> getValues()
    {
        return values;
    }

    private String getFieldName()
    {
        return fieldName;
    }
}
