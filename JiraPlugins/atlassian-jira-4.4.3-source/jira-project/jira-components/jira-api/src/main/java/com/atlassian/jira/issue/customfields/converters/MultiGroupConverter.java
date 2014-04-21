package com.atlassian.jira.issue.customfields.converters;

import java.util.Collection;

public interface MultiGroupConverter extends GroupConverter
{
    public Collection<String> extractGroupStringsFromString(String value);
}
