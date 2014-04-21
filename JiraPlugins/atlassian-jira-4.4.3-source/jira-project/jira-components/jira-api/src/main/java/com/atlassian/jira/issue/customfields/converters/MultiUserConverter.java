package com.atlassian.jira.issue.customfields.converters;

import java.util.Collection;

public interface MultiUserConverter extends UserConverter
{
    public Collection<String> extractUserStringsFromString(String value);
}
