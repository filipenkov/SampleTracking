package com.atlassian.jira.config;

import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.CharSequenceDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import org.dom4j.Element;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringEscapeUtils.escapeJavaScript;

/**
 * This web resource transformer inserts the enabled features into a JavaScript file, so that JS code can query the
 * enabled features.
 *
 * @since v5.0
 */
public class DarkFeaturesResourceTransformer implements WebResourceTransformer
{
    private static final String FEATURES_SUBSTITUTION = "ENABLED_DARK_FEATURES_SUBSTITUTION";
    private static final Pattern PATTERN_REGEX = Pattern.compile(FEATURES_SUBSTITUTION, Pattern.LITERAL);
    private final FeatureManager featureManager;

    public DarkFeaturesResourceTransformer(FeatureManager featureManager)
    {
        this.featureManager = featureManager;
    }

    @Override
    public DownloadableResource transform(Element configElement, ResourceLocation location, String filePath, DownloadableResource nextResource)
    {
        return new DarkFeatureInterpolatedResource(nextResource);
    }

    /**
     * Returns a string that is a Javascript array literal, e.g. <code>[ 'feat1', 'feat2' ]</code>. This text is
     * inserted into Javscript resources that use this transformer.
     *
     * @return a String containing a Javscript array literal
     */
    protected String getEnabledFeatureKeysAsJS()
    {
        Set<String> keys = featureManager.getDarkFeatures().getAllEnabledFeatures();

        boolean isFirst = true;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (String key : keys)
        {
            if (!isFirst) { sb.append(","); }
            sb.append("'").append(escapeJavaScript(key)).append("'");
            isFirst = false;
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * Replaces "{@value DarkFeaturesResourceTransformer#FEATURES_SUBSTITUTION}" with the actual list of enabled features.
     */
    class DarkFeatureInterpolatedResource extends CharSequenceDownloadableResource
    {
        public DarkFeatureInterpolatedResource(DownloadableResource originalResource)
        {
            super(originalResource);
        }

        @Override
        protected CharSequence transform(CharSequence original)
        {
            if (original == null)
            {
                return null;
            }

            Matcher matcher = PATTERN_REGEX.matcher(original);
            return matcher.replaceAll(getEnabledFeatureKeysAsJS());
        }
    }
}
