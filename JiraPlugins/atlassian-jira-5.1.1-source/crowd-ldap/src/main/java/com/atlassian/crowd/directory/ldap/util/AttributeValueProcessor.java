package com.atlassian.crowd.directory.ldap.util;

/**
 * Code copied from: http://jira.springframework.org/browse/LDAP-176
 * To fix: http://jira.atlassian.com/browse/CWD-1445
 * 
 * @author Marius Scurtescu
 */
public interface AttributeValueProcessor
{
    public void process(Object value);
}
