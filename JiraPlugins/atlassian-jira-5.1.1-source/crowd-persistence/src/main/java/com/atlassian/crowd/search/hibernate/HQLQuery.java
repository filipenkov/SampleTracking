package com.atlassian.crowd.search.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HQLQuery
{
    protected final StringBuilder select = new StringBuilder("");
    protected final StringBuilder from = new StringBuilder(" FROM ");
    protected final StringBuilder where = new StringBuilder(" WHERE ");
    protected final StringBuilder orderBy = new StringBuilder(" ORDER BY ");
    protected int aliasCounter = 0;
    protected boolean distinctRequired = false;
    protected boolean whereRequired = false;
    protected boolean orderByRequired = false;
    private final List<Object> parameterValues = new ArrayList<Object>();

    public StringBuilder appendSelect(CharSequence hql)
    {
        select.append(hql);
        return this.select;
    }

    public StringBuilder appendFrom(CharSequence hql)
    {
        from.append(hql);
        return this.from;
    }

    public StringBuilder appendWhere(CharSequence hql)
    {
        whereRequired = true;
        where.append(hql);
        return this.where;
    }

    public StringBuilder appendOrderBy(CharSequence hql)
    {
        orderByRequired = true;
        orderBy.append(hql);
        return this.orderBy;
    }

    public StringBuilder getNextAlias(String baseAliasName)
    {
        aliasCounter++;
        return new StringBuilder(baseAliasName).append(aliasCounter);
    }

    public void addParameterValue(Object value)
    {
        parameterValues.add(value);
    }

    public List<Object> getParameterValues()
    {
        return Collections.unmodifiableList(parameterValues);
    }

    public void requireDistinct()
    {
        distinctRequired = true;
    }

    @Override
    public String toString()
    {
        StringBuilder hql = new StringBuilder("SELECT ");
        if (distinctRequired)
        {
            hql.append("DISTINCT ");
        }

        hql.append(select);
        hql.append(from);

        if (whereRequired)
        {
            hql.append(where);
        }
        if (orderByRequired)
        {
            hql.append(orderBy);
        }
        return hql.toString();
    }
}
