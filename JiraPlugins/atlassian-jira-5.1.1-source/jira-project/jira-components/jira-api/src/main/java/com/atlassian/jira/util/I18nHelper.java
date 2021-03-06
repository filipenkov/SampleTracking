/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 12:35:33 PM
 */
package com.atlassian.jira.util;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * I18nHelper is the main interface for finding internationalised text in JIRA.
 * <p/>
 * You should only use the i18nHelper in your code which generally you get from {@link
 * com.atlassian.jira.security.JiraAuthenticationContext#getI18nHelper()} or {@link I18nHelper.BeanFactory}.
 * Both of these are available in PICO to be dependency injected or available from {@link com.atlassian.jira.component.ComponentAccessor#getI18nHelperFactory()}
 * or {@link com.atlassian.jira.component.ComponentAccessor#getJiraAuthenticationContext()}.
 * <p/>
 * You MUST never directly instantiate the underlying I18nBean.  You will get it wrong and miss out on cool stuff like
 * flyweight caching.
 *
 * @see com.atlassian.jira.security.JiraAuthenticationContext#getI18nHelper()
 * @see I18nHelper.BeanFactory
 */
@PublicApi
public interface I18nHelper
{
    /**
     * @return the {@link Locale} that is associated with this I18nHelper
     */
    Locale getLocale();

    /**
     * @return the default {@link ResourceBundle} within JIRA
     */
    ResourceBundle getDefaultResourceBundle();

    /**
     * Called to return the un-formatted text of the specified i18n key or the key itself if no message can be found
     *
     * @param key the key of the i18n message
     * @return the un-formatted, translated, text or the key itself if no i18n message can be found
     */
    String getUnescapedText(String key);

    /**
     * Same as {@link #getUnescapedText(String)} but does not apply any {@link com.atlassian.jira.plugin.language.TranslationTransform}s.
     * 
     * @param key the key of the i18n message
     * @return the un-formatted text or the key itself if no i18n message can be found
     */
    String getUntransformedRawText(String key);

    /**
     * <p/>
     * For the given <tt>key</tt>, checks whether such a key is defined in the locale context represented by this
     * helper instance. Note that the actual translation may be in the 'fallback' default locale rather than the current
     * locale.
     *
     * <p/>
     * The contract of this is method is correlated with {@link #getUntransformedRawText(String)}, whereby if this method
     * returns <code>false</code>, {@link #getUntransformedRawText(String)} will return the key provided as an argument,
     * and if this method returns <code>true</code>, {@link #getUntransformedRawText(String)} will generally return
     * the translation, which in some very unlikely cases may be equal to the <tt>key</tt>.
     *
     * @param key translation key
     * @return <code>true</code>, if any translation for such key exists, <code>false</code> otherwise
     */
    boolean isKeyDefined(String key);

    /**
     * Called to return the formatted text of the specified i18n key or the key itself if no message can be found
     *
     * @param key the key of the i18n message
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(String key);

    /**
     * Called to return the formatted text of the specified i18n key or the key itself if no message can be found
     *
     * @param key the key of the i18n message
     * @param value1 a value to be substituted into the message
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(String key, String value1);

    /**
     * Called to return the formatted text of the specified i18n key or the key itself if no message can be found
     *
     * @param key the key of the i18n message
     * @param value1 a value to be substituted into the message
     * @param value2 a value to be substituted into the message
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(String key, String value1, String value2);

    /**
     * Called to return the formatted text of the specified i18n key or the key itself if no message can be found
     *
     * @param key the key of the i18n message
     * @param value1 a value to be substituted into the message
     * @param value2 a value to be substituted into the message
     * @param value3 a value to be substituted into the message
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(String key, String value1, String value2, String value3);

    /**
     * Called to return the formatted text of the specified i18n key or the key itself if no message can be found
     *
     * @param key the key of the i18n message
     * @param value1 a value to be substituted into the message
     * @param value2 a value to be substituted into the message
     * @param value3 a value to be substituted into the message
     * @param value4 a value to be substituted into the message
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(String key, String value1, String value2, String value3, String value4);

    /**
     * Called to return the formatted text of the specified i18n key or the key itself if no message can be found
     *
     * @param key the key of the i18n message
     * @param value1 a value to be substituted into the message
     * @param value2 a value to be substituted into the message
     * @param value3 a value to be substituted into the message
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(final String key, final Object value1, final Object value2, final Object value3);

    /**
     * Called to return the formatted text of the specified i18n key or the key itself if no message can be found
     *
     * @param key the key of the i18n message
     * @param value1 a value to be substituted into the message
     * @param value2 a value to be substituted into the message
     * @param value3 a value to be substituted into the message
     * @param value4 a value to be substituted into the message
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4);

    /**
     * Called to return the formatted text of the specified i18n key or the key itself if no message can be found
     *
     * @param key the key of the i18n message
     * @param value1 a value to be substituted into the message
     * @param value2 a value to be substituted into the message
     * @param value3 a value to be substituted into the message
     * @param value4 a value to be substituted into the message
     * @param value5 a value to be substituted into the message
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5);

    /**
     * Called to return the formatted text of the specified i18n key or the key itself if no message can be found
     *
     * @param key the key of the i18n message
     * @param value1 a value to be substituted into the message
     * @param value2 a value to be substituted into the message
     * @param value3 a value to be substituted into the message
     * @param value4 a value to be substituted into the message
     * @param value5 a value to be substituted into the message
     * @param value6 a value to be substituted into the message
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6);

    /**
     * Called to return the formatted text of the specified i18n key or the key itself if no message can be found
     *
     * @param key the key of the i18n message
     * @param value1 a value to be substituted into the message
     * @param value2 a value to be substituted into the message
     * @param value3 a value to be substituted into the message
     * @param value4 a value to be substituted into the message
     * @param value5 a value to be substituted into the message
     * @param value6 a value to be substituted into the message
     * @param value7 a value to be substituted into the message
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6, final Object value7);

    /**
     * Called to return the formatted text of the specified i18n key
     *
     * @param key the key of the i18n message
     * @param value1 a value to be substituted into the message
     * @param value2 a value to be substituted into the message
     * @param value3 a value to be substituted into the message
     * @param value4 a value to be substituted into the message
     * @param value5 a value to be substituted into the message
     * @param value6 a value to be substituted into the message
     * @param value7 a value to be substituted into the message
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(String key, String value1, String value2, String value3, String value4, String value5, String value6, String value7);

    /**
     * Called to return the formatted text of the specified i18n key or the key itself if no message can be found
     *
     * @param key the key of the i18n message
     * @param value1 a value to be substituted into the message
     * @param value2 a value to be substituted into the message
     * @param value3 a value to be substituted into the message
     * @param value4 a value to be substituted into the message
     * @param value5 a value to be substituted into the message
     * @param value6 a value to be substituted into the message
     * @param value7 a value to be substituted into the message
     * @param value8 a value to be substituted into the message
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6, final Object value7, final Object value8);

    /**
     * Called to return the formatted text of the specified i18n key or the key itself if no message can be found
     *
     * @param key the key of the i18n message
     * @param value1 a value to be substituted into the message
     * @param value2 a value to be substituted into the message
     * @param value3 a value to be substituted into the message
     * @param value4 a value to be substituted into the message
     * @param value5 a value to be substituted into the message
     * @param value6 a value to be substituted into the message
     * @param value7 a value to be substituted into the message
     * @param value8 a value to be substituted into the message
     * @param value9 a value to be substituted into the message
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(String key, String value1, String value2, String value3, String value4, String value5, String value6, String value7, String value8, String value9);


    /**
     * Called to return the formatted text of the specified i18n key or the key itself if no message can be found.
     * <p/>
     * The object passed in can be an array, a {@link java.util.List}} or a single parameter object.  It will be then
     * used as substitution parameters within the message.
     *
     * @param key the key of the i18n message
     * @param parameters This can be an Array, a {@link java.util.List} and Array or a single object parameter
     * @return the formatted text or the key itself if no i18n message can be found
     */
    String getText(String key, Object parameters);

    /**
     * Given a prefix for an i18n key, this method will return all keys that start with the prefix specified.
     *
     * @param prefix The prefix for i18n keys. May not be null
     * @return An immutable set of translation keys that start with the prefix specified.
     * @since 4.0.1
     */
    Set<String> getKeysForPrefix(String prefix);

    /**
     * Ths BeanFactory is used to instantiate {@link I18nHelper} instances
     */
    public interface BeanFactory
    {
        /**
         * Returns an {@link I18nHelper} instance associated with the provided {@link Locale}
         *
         * @param locale the locale in play
         * @return an {@link I18nHelper} instance associated with the provided {@link Locale}
         */
        I18nHelper getInstance(Locale locale);

        /**
         * Returns an {@link I18nHelper} instance associated with the provided {@link User}'s locale preference
         *
         * @param user the user in play
         * @return an {@link I18nHelper} instance associated with the provided {@link User}'s locale preference
         */
        I18nHelper getInstance(User user);
    }
}
