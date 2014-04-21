package com.atlassian.jira.collector.plugin.transformer;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.collector.plugin.components.Collector;
import com.atlassian.jira.collector.plugin.components.CollectorService;
import com.atlassian.jira.collector.plugin.components.Trigger;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.AbstractStringTransformedDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A webresource transformer that looks up collector details and substitutes them into the
 * js resource.
 *
 * @since 1.0
 */
public class CollectorTransformer implements WebResourceTransformer
{

    private final CollectorService collectorService;

    public CollectorTransformer(final CollectorService collectorService)
    {
        this.collectorService = collectorService;
    }

    @Override
    public DownloadableResource transform(final Element element, final ResourceLocation resourceLocation, final String s, final DownloadableResource downloadableResource)
    {
        final String collectorId = ExecutingHttpRequest.get().getParameter("collectorId");
        ServiceOutcome<Collector> result = collectorService.getCollector(collectorId);
        return new JavascriptSubstitutionDownloadableResource(result.getReturnedValue(), downloadableResource);
    }

    static class JavascriptSubstitutionDownloadableResource extends AbstractStringTransformedDownloadableResource
    {
        private static final Pattern VARIABLE_PATTERN = Pattern.compile("@([a-zA-Z][a-zA-Z0-9_]*)");
        private final Map<String, String> params = new HashMap<String, String>();

        public JavascriptSubstitutionDownloadableResource(final Collector collector, DownloadableResource originalResource)
        {
            super(originalResource);
            String shouldCollectFeedback = "false";
            String collectorId = "";
            String triggerText = "";
            String triggerPosition = "\"\"";
            if (collector != null)
            {
                shouldCollectFeedback = String.valueOf(collector.isRecordWebInfo());
                collectorId = collector.getId();
                triggerText = StringEscapeUtils.escapeJavaScript(collector.getTrigger().getText());
                if (collector.getTrigger().getPosition().equals(Trigger.Position.CUSTOM))
                {
                    triggerPosition = collector.getTrigger().getCustomFunction();
                }
                else
                {
                    triggerPosition = "\"" + collector.getTrigger().getPosition().toString() + "\"";
                }
            }

            params.put("shouldCollectFeedback", shouldCollectFeedback);
            params.put("collectorId", collectorId);
            params.put("triggerText", triggerText);
            params.put("triggerPosition", triggerPosition);
        }

        @Override
        protected String transform(final String input)
        {
            final Matcher matcher = VARIABLE_PATTERN.matcher(input);
            int start = 0;
            StringBuilder out = null;
            while (matcher.find())
            {
                if (out == null)
                {
                    out = new StringBuilder();
                }

                out.append(input.subSequence(start, matcher.start()));
                String token = matcher.group(1);
                String subst = params.get(token);
                if (subst != null)
                {
                    out.append(subst);
                }
                else
                {
                    out.append(matcher.group());
                }
                start = matcher.end();
            }
            if (out == null)
            {
                return input;
            }
            else
            {
                out.append(input.subSequence(start, input.length()));
                return out.toString();
            }
        }
    }
}
