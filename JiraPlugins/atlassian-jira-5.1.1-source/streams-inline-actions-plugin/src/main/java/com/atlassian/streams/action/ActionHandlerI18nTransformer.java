package com.atlassian.streams.action;

import java.util.Map;

import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.AbstractStringTransformedDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.spi.StreamsLocaleProvider;

import org.dom4j.Element;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link WebResourceTransformer} to dynamically insert i18n key/values into the loaded action handler javascript.
 *
 * Proper usage of this transformer in your atlassian-plugin.xml is as follows:
 *
 * <pre>
 *   &lt;streams-action-handlers key=&quot;actionHandlers&quot;&gt;
 *       ...
 *       &lt;transformation extension=&quot;i18n.js&quot;&gt;
 *           &lt;transformer key=&quot;action-i18n-transformer&quot; /&gt;
 *       &lt;/transformation&gt;
 *
 *       &lt;resource type=&quot;download&quot; name=&quot;&lt;i18n-prefix&gt;.i18n.js&quot; location=&quot;&lt;i18n-prefix&gt;.i18n.js&quot;/&gt;
 *       ...
 *   &lt;/streams-action-handlers&gt;
 * </pre>
 *
 * where <i18n-prefix> is the prefix of your i18n properties, for example "streams.jira.action". Make sure your i18n resource
 * is defined before any additional javascript resources; otherwise, the ActivityStreams.i18n.get() javascript function
 * will not be able to find any i18n values upon page load.
 */
public class ActionHandlerI18nTransformer implements WebResourceTransformer
{
    private static final String SUFFIX = ".i18n.js";
    private final I18nResolver i18nResolver;
    private final StreamsLocaleProvider localeProvider;

    public ActionHandlerI18nTransformer(I18nResolver i18nResolver, StreamsLocaleProvider localeProvider)
    {
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        this.localeProvider = checkNotNull(localeProvider, "localeProvider");
    }

    public DownloadableResource transform(Element configElement, final ResourceLocation location, String filePath, DownloadableResource nextResource)
    {
         return new AbstractStringTransformedDownloadableResource(nextResource)
        {
            protected String transform(String originalContent)
            {
                int suffixIndex = location.getName().indexOf(SUFFIX);
                if (suffixIndex == -1)
                {
                    //the transformer wasn't registered for the "i18n.js" file extension, so let's just return the original content.
                    return originalContent;
                }
                else
                {
                    return getI18nContents(location.getName().substring(0, suffixIndex)) + originalContent;
                }
            }
        };
    }

    private String getI18nContents(String prefix)
    {
        Map<String, String> translations = i18nResolver.getAllTranslationsForPrefix(prefix, localeProvider.getUserLocale());

        StringBuilder contents = new StringBuilder();
        for (String key : translations.keySet())
        {
            contents.append("ActivityStreams.i18n.put('")
                .append(key)
                .append("', '")
                .append(translations.get(key))
                .append("');\n");
        }

        return contents.toString();
    }
}
