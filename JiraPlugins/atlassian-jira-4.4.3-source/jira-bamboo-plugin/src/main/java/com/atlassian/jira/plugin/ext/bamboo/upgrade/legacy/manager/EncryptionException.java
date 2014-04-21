package com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.manager;

public  class EncryptionException extends RuntimeException
{
    public EncryptionException(String buildName, Throwable throwable)
    {
        super(buildName, throwable);
    }
}
