package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.util.LuceneUtils;

import java.util.Comparator;
import java.util.Date;

public abstract class AbstractDateStatisticsMapper implements StatisticsMapper
{
    protected String documentConstant;

    public AbstractDateStatisticsMapper(String documentConstant)
    {
        this.documentConstant = documentConstant;
    }

    public boolean isValidValue(Object value)
    {
        return true;
    }

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

    public abstract SearchRequest getSearchUrlSuffix(Object value, SearchRequest searchRequest);

    public String getDocumentConstant()
    {
        return documentConstant;
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        return LuceneUtils.stringToDate(documentValue);
    }

    public Comparator getComparator()
    {
        return new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((Date)o1).compareTo((Date) o2);
            }
        };
    }
}
