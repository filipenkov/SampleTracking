package com.atlassian.jira.dev.backdoor;

/**
 * Bean used to hold POST requests for the {@link DataImportBackdoor}.
 *
 * @since v5.0
 */
public class DataImportBean
{
    public String filePath;
    public String licenseString;
    public boolean quickImport;
    public boolean useDefaultPaths;
    public boolean isSetup;
    public String baseUrl;
}
