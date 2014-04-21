package com.atlassian.jira.bc.dataimport;

import org.apache.commons.lang.StringUtils;

/**
 * Provides all the parameters required to perform a data import in JIRA.  Specifically this includes:
 * <strong>Required</strong>
 * <ul>
 *     <li>filename - Path of the file to be imported</li>
 * </ul>
 * <p/>
 * <strong>Optional</strong>
 * <ul>
 *     <li>licenseString - A license string to override the license from the XML data file</li>
 *     <li>quickImport - If this is set to true the plugins system will *not* be restarted. (false by default)</li>
 *     <li>useDefaultPaths - Whether default paths in JIRA home should be used for indexing & attachments (true by default)</li>
 *     <li>isSetup - true if this import is being performed during JIRA's setup (false by default)</li>
 * </ul>
 * <p/>
 * Clients should use the provided {@link DataImportParams.Builder} to construct an instance of this class.
 *
 * @since v4.4
 */
public final class DataImportParams
{
    private final String filename;
    private final String licenseString;
    private final boolean quickImport;
    private final boolean useDefaultPaths;
    private final boolean isSetup;

    private DataImportParams(String filename, String licenseString, boolean quickImport, boolean useDefaultPaths, boolean isSetup)
    {
        this.filename = filename;
        this.licenseString = licenseString;
        this.quickImport = quickImport;
        this.useDefaultPaths = useDefaultPaths;
        this.isSetup = isSetup;
    }

    public String getFilename()
    {
        return filename;
    }

    public String getLicenseString()
    {
        return licenseString;
    }

    public boolean isQuickImport()
    {
        return quickImport;
    }

    public boolean isUseDefaultPaths()
    {
        return useDefaultPaths;
    }

    public boolean isSetup()
    {
        return isSetup;
    }

    public static class Builder
    {
        private final String filename;
        private String licenseString;
        private boolean quickImport = false;
        private boolean useDefaultPaths = true;
        private boolean isSetup = false;

        public Builder(String filename)
        {
            this.filename = StringUtils.stripToEmpty(filename);
        }

        public Builder setupImport()
        {
            isSetup = true;
            return this;
        }

        public Builder setLicenseString(String licenseString)
        {
            this.licenseString = StringUtils.stripToNull(licenseString);
            return this;
        }

        public Builder setQuickImport(boolean quickImport)
        {
            this.quickImport = quickImport;
            return this;
        }

        public Builder setUseDefaultPaths(boolean useDefaultPaths)
        {
            this.useDefaultPaths = useDefaultPaths;
            return this;
        }

        public DataImportParams build()
        {
            return new DataImportParams(this.filename, this.licenseString, this.quickImport, this.useDefaultPaths, this.isSetup);
        }
    }
}
