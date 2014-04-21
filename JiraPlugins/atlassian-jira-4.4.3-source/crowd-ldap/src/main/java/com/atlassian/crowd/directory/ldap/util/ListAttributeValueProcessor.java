package com.atlassian.crowd.directory.ldap.util;


import java.util.ArrayList;
import java.util.List;

/**
 * Code copied from: http://jira.springframework.org/browse/LDAP-176
 * To fix: http://jira.atlassian.com/browse/CWD-1445
 *
 * @author Marius Scurtescu
 */
public class ListAttributeValueProcessor implements AttributeValueProcessor
{
    private List<String> _values = new ArrayList<String>();

    public void process(Object value)
    {
        _values.add((String) value);
    }

    public List<String> getValues()
    {
        return _values;
    }
}
