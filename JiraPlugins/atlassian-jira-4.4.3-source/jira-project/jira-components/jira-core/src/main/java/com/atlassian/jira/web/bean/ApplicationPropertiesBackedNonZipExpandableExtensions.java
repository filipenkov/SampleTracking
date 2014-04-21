package com.atlassian.jira.web.bean;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation of {@link NonZipExpandableExtensions} that is backed by a jira application property.
 *
 * By default, it includes the MS Office OpenXml extensions and OpenOffice OpenDocument extensions.
 *
 * @since v4.2
 */
public class ApplicationPropertiesBackedNonZipExpandableExtensions implements NonZipExpandableExtensions
{
    private static final String DEFAULT_JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST = "docx, docm, dotx, dotm," 
            + " xlsx, xlsm, xltx, xltm, xlsb, xlam, pptx, pptm, potx, potm, ppam, ppsx, ppsm, sldx, sldm, thmx,odt,"
            + " odp, ods, odg, odb, odf, ott, otp, ots, otg, odm, sxw, stw, sxc, stc, sxi, sti, sxd, std, sxg";

    private final ApplicationProperties applicationProperties;

    public ApplicationProperties getApplicationProperties()
    {
        return applicationProperties;
    }

    public ApplicationPropertiesBackedNonZipExpandableExtensions(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    private String getNonExpandableExtensionsList()
    {
        if (getApplicationProperties().getDefaultBackedString(APKeys.JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST) == null)
        {
            return DEFAULT_JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST;
        }
        else
        {
            return getApplicationProperties().getDefaultBackedString(APKeys.JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST);
        }
    }

    public boolean contains(String extension)
    {
        notNull("extension", extension);

        if (StringUtils.isBlank(extension)) { return false; }

        String nonExpandableExtensionsList = getNonExpandableExtensionsList();

        for (final StrTokenizer csvTokenizer = StrTokenizer.getCSVInstance(nonExpandableExtensionsList); csvTokenizer.hasNext();)
        {
            String nonExpandableExtension = csvTokenizer.nextToken();
            if (nonExpandableExtension.equalsIgnoreCase(extension)) { return true; }
        }
        return false;
    }
}