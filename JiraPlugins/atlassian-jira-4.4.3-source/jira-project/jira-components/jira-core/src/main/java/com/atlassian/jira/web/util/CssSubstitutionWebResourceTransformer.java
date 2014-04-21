package com.atlassian.jira.web.util;

import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.AbstractStringTransformedDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A resource transform that makes variable substitutions based on L&F settings.
 *
 * Variables are prefixed with @. Variables will be:
 * <ul>
 * <li> Of the form <code>@[a-zA-Z][a-zA-Z0-9_]*</code> (e.g. alphanumeric and underscores and starts with an alpha).
 * <li> Any @FOO token found that doesn't correspond to a known variable will be left as-is.
 * <li> @TextHeadingColour, @MenuBackgroundColour, etc.
 * <li> Copies of all the above with any leading hash # removed e.g. @TextHeadingColourNoHash, @MenuBackgroundColourNoHash
 * <li> @ContextPath. This will either be the empty string, or a string that starts with a slash and never ends in a slash.
 * </ul>
 *
 * @since v4.3
 */
public class CssSubstitutionWebResourceTransformer implements WebResourceTransformer
{
    private static final Logger log = Logger.getLogger(CssSubstitutionWebResourceTransformer.class);

    public static class VariableMap
    {

        private final LookAndFeelBean lAndF;

        public VariableMap(LookAndFeelBean lAndF)
        {
            this.lAndF = lAndF;
        }

        public Map<String, String> getVariableMap(boolean addLegacyVars)
        {
            final Map<String, String> result = new LinkedHashMap<String, String>();
            final Map<String, Object> beanProperties = getLookAndFeelProperties();
            for (Map.Entry<String, Object> entry : beanProperties.entrySet())
            {
                final String name = entry.getKey();
                final Object value = entry.getValue();
                if (value instanceof String) {
                    String stringValue = (String) value;
                    result.put(name, stringValue);
                    if (addLegacyVars) {
                        result.put(name + "NoHash", StringUtils.strip(stringValue, "#"));
                    }
                }
            }

            addFieldLabelWidthVariables(result, addLegacyVars);

            addGadgetColours(result);

            result.put("contextPath", ExecutingHttpRequest.get().getContextPath());

            return result;

        }

        private void addGadgetColours(Map<String, String> result)
        {
            for (Color color : Color.values())
            {
                //color8 is chromeless and therefore special.
                if(color.equals(Color.color8))
                {
                    continue;
                }
                result.put("gadget" + color.name(), lAndF.getGadgetChromeColor(color.name()));
            }
        }

        /**
         * These fieldLabelWidth variables are necessary but not desirable, and they should
         * be replaced with in-CSS, LESS-style expressions when possible
         */
        @Deprecated
        private void addFieldLabelWidthVariables(Map<String, String> result, boolean addLegacyVars)
        {
            final String labelWidthStr = lAndF.getDefaultBackedString(APKeys.JIRA_LF_FIELD_LABEL_WIDTH, "9");
            int labelWidth = 9;
            if (StringUtils.isNumeric(labelWidthStr))
            {
                labelWidth = Integer.parseInt(labelWidthStr);
            }

            result.put("fieldLabelWidth", Integer.toString(labelWidth) + "em");

            if (addLegacyVars) {
                result.put("fieldLabelWidth05", Double.toString(labelWidth + 0.5) + "em");
                result.put("fieldLabelWidth10", Integer.toString(labelWidth + 1) + "em");
                result.put("fieldLabelWidth15", Double.toString(labelWidth + 1.5) + "em");
                result.put("fieldLabelWidth20", Integer.toString(labelWidth + 2) + "em");
            }
        }

        private Map<String, Object> getLookAndFeelProperties()
        {
            try
            {
                return (Map<String, Object>) PropertyUtils.describe(lAndF);
            }
            catch (Exception e)
            {
                log.warn("Could not read LookAndFeelBean", e);
                return Collections.emptyMap();
            }
        }

    }

    static class CssSubstitutionDownloadableResource extends AbstractStringTransformedDownloadableResource
    {
        private final VariableMap variableMap;

        public CssSubstitutionDownloadableResource(DownloadableResource originalResource, LookAndFeelBean lAndF)
        {
            super(originalResource);
            this.variableMap = new VariableMap(lAndF);
        }

        @Override
        protected String transform(String input)
        {
            final Map<String, String> variables = variableMap.getVariableMap(true);
            final Matcher matcher = VARIABLE_PATTERN.matcher(input);
            int start = 0;
            StringBuilder out = null;
            while (matcher.find()) {
                if (out == null) {
                    out = new StringBuilder();
                }

                out.append(input.subSequence(start, matcher.start()));
                String token = matcher.group(1);
                String subst = variables.get(token);
                if (subst != null) {
                    out.append(subst);
                } else {
                    out.append(matcher.group());
                }
                start = matcher.end();
            }
            if (out == null) {
                return input;
            }
            else
            {
                out.append(input.subSequence(start, input.length()));
                return out.toString();
            }
        }
    }

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("@([a-zA-Z][a-zA-Z0-9_]*)");

    private final ApplicationProperties applicationProperties;

    public CssSubstitutionWebResourceTransformer(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public DownloadableResource transform(Element configElement, ResourceLocation location, String filePath, DownloadableResource nextResource)
    {
        return new CssSubstitutionDownloadableResource(nextResource, LookAndFeelBean.getInstance(applicationProperties));
    }
}
