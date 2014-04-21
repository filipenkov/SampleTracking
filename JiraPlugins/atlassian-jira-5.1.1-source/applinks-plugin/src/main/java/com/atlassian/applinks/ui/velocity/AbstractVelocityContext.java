package com.atlassian.applinks.ui.velocity;

import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.velocity.htmlsafe.HtmlSafe;

import java.net.URI;

/**
 * This Object holds a references to all objects that are used in the velocity templates which list the application links
 * and entity links. Rather than adding a reference to each object in the velocity context, we add one reference to this object to the
 * velocity context. The reason for this is that the confluence specific webwork actions used to render the list application links and entity links
 * screens can't add objects to the velocity context, but the velocity context can access the action which then returns a reference to this object.
 *
 * @since 3.0
 */
public abstract class AbstractVelocityContext
{
    protected final String contextPath;
    protected final InternalHostApplication internalHostApplication;
    protected final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory;
    protected final InternalTypeAccessor typeAccessor;
    protected final DocumentationLinker documentationLinker;

    protected AbstractVelocityContext(
            final String contextPath,
            final InternalHostApplication internalHostApplication,
            final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
            final InternalTypeAccessor typeAccessor,
            final DocumentationLinker documentationLinker)
    {
        this.contextPath = contextPath;
        this.internalHostApplication = internalHostApplication;
        this.batchedJSONi18NBuilderFactory = batchedJSONi18NBuilderFactory;
        this.typeAccessor = typeAccessor;
        this.documentationLinker = documentationLinker;
    }

    public DocumentationLinker getDocLinker()
    {
        return documentationLinker;
    }

    public String getContextPath()
    {
        return contextPath;
    }

    public String getBaseUrl()
    {
        return internalHostApplication.getBaseUrl().toString();
    }

    public String getApplicationName()
    {
        return internalHostApplication.getName();
    }

    @HtmlSafe
    public String getApplinksI18n()
    {
        return batchedJSONi18NBuilderFactory.builder()
        .withProperties("applinks")
        .withPluggableApplinksModules().build();
    }

    @HtmlSafe
    public String getApplinksDocs()
    {
        return batchedJSONi18NBuilderFactory.builder()
        .withProperties("applinks.docs")
        .with("applinks.docs.root", internalHostApplication.getDocumentationBaseUrl().toASCIIString())
        .build();
    }

    @HtmlSafe
    public String getAppLinksI18n()
    {
        return batchedJSONi18NBuilderFactory.builder()
        .withProperties("applinks")
        .withPluggableApplinksModules().build();
    }
}
