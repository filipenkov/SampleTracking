package com.atlassian.jira.template.velocity;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.template.TemplateSource;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.velocity.JiraVelocityManager;
import com.atlassian.velocity.VelocityManager;
import com.atlassian.velocity.htmlsafe.directive.DefaultDirectiveChecker;
import com.atlassian.velocity.htmlsafe.directive.DirectiveChecker;
import com.atlassian.velocity.htmlsafe.event.referenceinsertion.EnableHtmlEscapingDirectiveHandler;
import com.google.common.collect.Maps;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.event.EventCartridge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since v5.1
 */
public class DefaultVelocityTemplatingEngine implements VelocityTemplatingEngine
{
    private final VelocityManager velocityManager;
    private final ApplicationProperties applicationProperties;
    private final DirectiveChecker directiveChecker;

    public DefaultVelocityTemplatingEngine(final VelocityManager velocityManager, final ApplicationProperties applicationProperties)
    {
        this(velocityManager, applicationProperties, null);
    }

    public DefaultVelocityTemplatingEngine(final VelocityManager velocityManager, final ApplicationProperties applicationProperties, @Nullable VelocityTemplateCache velocityTemplateCache)
    {
        this.velocityManager = velocityManager;
        this.applicationProperties = applicationProperties;
        this.directiveChecker = velocityTemplateCache == null ? new DefaultDirectiveChecker() : new CachingDirectiveChecker(velocityTemplateCache);
    }

    @Override
    public RenderRequest render(final TemplateSource source)
    {
        return new DefaultRenderRequest(source);
    }

    class DefaultRenderRequest implements RenderRequest
    {
        private final TemplateSource source;
        private VelocityContext context = createContextFrom(Collections.<String, Object>emptyMap());

        public DefaultRenderRequest(final TemplateSource source)
        {
            checkNotNull(source);
            this.source = source;
        }

        @Override
        public RenderRequest applying(final Map<String, Object> parameters)
        {
            this.context = createContextFrom(parameters);
            return this;
        }

        @Override
        public RenderRequest applying(final VelocityContext context)
        {
            this.context = context;
            return this;
        }

        @Override
        public String asPlainText()
        {
            if (source instanceof TemplateSource.File)
            {
                final TemplateSource.File template = (TemplateSource.File) source;
                return velocityManager.getEncodedBody(template.getPath(), "", getBaseUrl(), applicationProperties.getEncoding(), context);
            }
            else
            {
                if (source instanceof TemplateSource.Fragment)
                {
                    final TemplateSource.Fragment fragment = (TemplateSource.Fragment) source;
                    return ((JiraVelocityManager) velocityManager).getEncodedBodyForContent(fragment.getContent(), context);
                }
            }
            return "";
        }

        @Override
        public String asHtml()
        {
            if (source instanceof TemplateSource.File)
            {
                final TemplateSource.File template = (TemplateSource.File) source;
                context.attachEventCartridge(createDefaultCartridge());
                return velocityManager.getEncodedBody(template.getPath(), "", getBaseUrl(), applicationProperties.getEncoding(), context);
            }
            else
            {
                if (source instanceof TemplateSource.Fragment)
                {
                    final TemplateSource.Fragment fragment = (TemplateSource.Fragment) source;
                    context.attachEventCartridge(createDefaultCartridge());
                    return ((JiraVelocityManager) velocityManager).getEncodedBodyForContent(fragment.getContent(), context);
                }
            }
            return "";
        }

        private String getBaseUrl()
        {
            if (ExecutingHttpRequest.get() != null)
            {
                return ExecutingHttpRequest.get().getContextPath();
            }
            return applicationProperties.getString(APKeys.JIRA_BASEURL);
        }

        private VelocityContext createContextFrom(final Map<String, Object> suppliedParameters)
        {
            final Map<String, Object> contextParameters =
                    CompositeMap.of
                            (
                                    suppliedParameters,
                                    Collections.<String, Object>singletonMap("baseurl", getBaseUrl())
                            );

            return new VelocityContext(CompositeMap.of(Maps.<String, Object>newHashMap(), contextParameters));
        }

    }

    private EventCartridge createDefaultCartridge()
    {
        // set up EnableHtmlEscapingDirectiveHandler with caching
        EnableHtmlEscapingDirectiveHandler handler = new EnableHtmlEscapingDirectiveHandler();
        handler.setDirectiveChecker(directiveChecker);

        final EventCartridge cartridge = new EventCartridge();
        cartridge.addEventHandler(handler);

        return cartridge;
    }

    private static class CachingDirectiveChecker implements DirectiveChecker
    {
        private final VelocityTemplateCache velocityTemplateCache;

        public CachingDirectiveChecker(@Nonnull VelocityTemplateCache velocityTemplateCache)
        {
            this.velocityTemplateCache = velocityTemplateCache;
        }

        @Override
        public boolean isPresent(String directiveName, Template template)
        {
            return velocityTemplateCache.isDirectivePresent(directiveName, template);
        }
    }
}
