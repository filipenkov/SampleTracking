package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;
import com.atlassian.jira.web.action.util.CalendarResourceIncluder;
import com.atlassian.query.Query;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import webwork.action.Action;

import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Render for date base searchers in Jira.
 *
 * @since v4.0
 */
public abstract class AbstractDateSearchRenderer extends AbstractSearchRenderer implements SearchRenderer
{
    private static final Logger log = Logger.getLogger(AbstractDateSearchRenderer.class);

    private final CalendarLanguageUtil calendarUtils;
    private final DateSearcherConfig config;
    private final TranslationsHelper translationHelper;
    private final SimpleFieldSearchConstants constants;

    public AbstractDateSearchRenderer(SimpleFieldSearchConstants constants, DateSearcherConfig config, TranslationsHelper translationHelper,
            VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties,
            VelocityManager velocityManager, String searcherNameKey, CalendarLanguageUtil calendarUtils
    )
    {
        super(velocityRequestContextFactory, applicationProperties, velocityManager, constants, searcherNameKey);
        this.constants = notNull("constants", constants);
        this.config = notNull("config", config);
        this.calendarUtils = notNull("calendarUtils", calendarUtils);
        this.translationHelper = notNull("translationHelper", translationHelper);
    }

    abstract public boolean isShown(final User searcher, final SearchContext searchContext);

    public String getEditHtml(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        return renderEditTemplate("date-searcher-edit.vm", addEditParameters(searcher, velocityParams));
    }

    private Map<String, Object> addEditParameters(final User searcher, final Map<String, Object> velocityParams)
    {
        final I18nHelper i18n = getI18n(searcher);
        final String language = i18n.getLocale().getLanguage();
        velocityParams.put("hasCalendarTranslation", calendarUtils.hasTranslationForLanguage(language));
        velocityParams.put("calendarIncluder", new CalendarResourceIncluder());

        return addCommonParameters(searcher, velocityParams);
    }

    public String getViewHtml(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        return renderViewTemplate("date-searcher-view.vm", addViewParameters(searcher, velocityParams, fieldValuesHolder));
    }

    private Map<String, Object> addViewParameters(final User searcher, final Map<String, Object> velocityParams, final FieldValuesHolder fieldValuesHolder)
    {
        // insert "pretty names" for duration values
        final Long previousOffSet = getPeriodOffset(fieldValuesHolder, config.getPreviousField());
        if (previousOffSet != null)
        {
            velocityParams.put("previousFieldView", prettyPrintPeriodOffset(getI18n(searcher), previousOffSet));
        }

        final Long nextOffSet = getPeriodOffset(fieldValuesHolder, config.getNextField());
        if (nextOffSet != null)
        {
            velocityParams.put("nextFieldView", prettyPrintPeriodOffset(getI18n(searcher), nextOffSet));
        }

        return addCommonParameters(searcher, velocityParams);
    }

    private Map<String, Object> addCommonParameters(final User searcher, final Map<String, Object> velocityParams)
    {
        //Insert navigator form names.
        velocityParams.put("afterField", config.getAfterField());
        velocityParams.put("beforeField", config.getBeforeField());
        velocityParams.put("previousField", config.getPreviousField());
        velocityParams.put("nextField", config.getNextField());
        velocityParams.put("id", config.getId());

        final I18nHelper i18n = getI18n(searcher);

        //Insert the labels used to render the editor.
        velocityParams.put("afterFieldLabel", translationHelper.getAfterLabel(i18n));
        velocityParams.put("beforeFieldLabel", translationHelper.getBeforeLabel(i18n));
        velocityParams.put("periodLabel", translationHelper.getPeriodLabel(i18n));
        velocityParams.put("description", translationHelper.getDescription(i18n));

        return velocityParams;
    }

    public boolean isRelevantForQuery(final User searcher, final Query query)
    {
        return isRelevantForQuery(constants.getJqlClauseNames(), query);
    }

    private String prettyPrintPeriodOffset(final I18nHelper i18n, final long periodOffSet)
    {
        final String msg = DateUtils.getDurationPretty((long) (Math.abs(periodOffSet) / 1000.0), i18n.getDefaultResourceBundle());
        if (periodOffSet > 0)
        {
            return i18n.getText("navigator.hidden.search.request.summary.date.ago", msg);
        }
        else if (periodOffSet < 0)
        {
            return i18n.getText("navigator.hidden.search.request.summary.date.from.now", msg);
        }
        else
        {
            return i18n.getText("navigator.hidden.search.request.summary.date.now");
        }
    }

    private Long getPeriodOffset(final FieldValuesHolder fieldValuesHolder, final String paramField)
    {
        if (fieldValuesHolder.containsKey(paramField))
        {
            final String periodStr = StringUtils.trimToNull(ParameterUtils.getStringParam(fieldValuesHolder, paramField));

            if (periodStr != null)
            {
                try
                {
                    return -DateUtils.getDurationWithNegative(periodStr) * DateUtils.SECOND_MILLIS;
                }
                catch (final InvalidDurationException e)
                {
                    log.debug("Could not get duration for: " + periodStr, e);
                }
                catch (final NumberFormatException e)
                {
                    log.debug("Could not get duration for: " + periodStr, e);
                }
            }
        }

        return null;
    }

    /**
     * Interface used by the renderer to get the translations needed to render a date searcher.
     *
     * @since 4.0
     */
    public interface TranslationsHelper
    {
        /**
         * Get the label associated with the before field.
         *
         * @param helper the i18n helper that can return translations.
         * @return the label associated with the before field.
         */
        String getBeforeLabel(I18nHelper helper);

        /**
         * Get the label associated with the after field.
         *
         * @param helper the i18n helper that can return translations.
         * @return the label associated with the after field.
         */
        String getAfterLabel(I18nHelper helper);

        /**
         * Get the label associated with the period fields.
         *
         * @param helper the i18n helper that can return translations.
         * @return the label associated with the period fields.
         */
        String getPeriodLabel(I18nHelper helper);

        /**
         * Get the description associated with the period fields.
         *
         * @param helper the i18n helper that can return translations.
         * @return the description associated with the searcher.
         */
        String getDescription(I18nHelper helper);
    }
}
