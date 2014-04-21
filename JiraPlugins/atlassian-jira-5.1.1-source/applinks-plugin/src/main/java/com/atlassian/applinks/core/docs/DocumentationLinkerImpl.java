package com.atlassian.applinks.core.docs;

import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.sal.api.message.I18nResolver;
import org.apache.commons.lang.StringUtils;

import java.net.URI;

public class DocumentationLinkerImpl implements DocumentationLinker
{        
    private final InternalHostApplication internalHostApplication;
    private final I18nResolver i18nResolver;

    public DocumentationLinkerImpl(final InternalHostApplication internalHostApplication,
                                   final I18nResolver i18nResolver)
    {
        this.internalHostApplication = internalHostApplication;
        this.i18nResolver = i18nResolver;
    }

    public URI getLink(final String pageKey)
    {
        return getLink(pageKey, null);
    }

    public URI getLink(final String pageKey, final String sectionKey)
    {
        String pageName = i18nResolver.getText(pageKey);
        if (!StringUtils.isEmpty(sectionKey)) {
            final String sectionPrefix = StringUtils.remove(pageName, "+");
            pageName += "#" + sectionPrefix + "-" + i18nResolver.getText(sectionKey);
        }
        return URIUtil.uncheckedConcatenate(internalHostApplication.getDocumentationBaseUrl(), "/" + pageName);
    }
}
