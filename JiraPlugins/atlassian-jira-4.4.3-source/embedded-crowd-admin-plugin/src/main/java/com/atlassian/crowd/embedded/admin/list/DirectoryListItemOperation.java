package com.atlassian.crowd.embedded.admin.list;

/**
 * Value bean to hold information about the state of an opertaion against a
 * spericif directory.
 */
public class DirectoryListItemOperation
{
    private final String url;
    private final String classAttribute;

    public DirectoryListItemOperation(String url, String classAttribute) {
        this.url = url;
        this.classAttribute = classAttribute;
    }

    public String getUrl() {
        return url;
    }

    public String getClassAttribute() {
        return classAttribute;
    }
}
