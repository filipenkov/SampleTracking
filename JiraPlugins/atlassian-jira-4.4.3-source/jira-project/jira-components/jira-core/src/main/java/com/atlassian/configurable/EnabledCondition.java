package com.atlassian.configurable;

import com.atlassian.core.util.ClassLoaderUtils;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;

/**
 * An encapsulation of the conditional logic to decide whether to display and configure a given
 * {@link ObjectConfigurationProperty}. Implementers may declare the specific class to use via the
 * <code>enabled-config</code> on ObjectConfigurationProperty entities to make the display of the property
 * dependent on the result returned by the {#isEnabled} method.
 * <p/>
 * EnabledCondition objects are constructed by reflection and MUST have a default no-arg constructor.
 *
 * @since 28 August 2007 for JIRA v3.11
 */
public interface EnabledCondition
{
    /**
     * A condition that means <strong>always enabled</strong>.
     */
    static final EnabledCondition TRUE = new EnabledCondition()
    {
        public boolean isEnabled()
        {
            return true;
        }
    };

    /**
     * Whether or not to display and use an ObjectConfigurationProperty for the given {@link User}.
     *
     * @return true only if the property is enabled in the current context.
     */
    boolean isEnabled();

    /**
     * Factory for constructing EnabledCondition instances.
     */
    static class Factory
    {
        // TODO Note this is for Java 1.4 - when we move to Java5 we should setup the logger with getCanonicalName() to remove the $ in the logger name
        private static final Logger log = Logger.getLogger(EnabledCondition.Factory.class);

        /**
         * Attempts to instantiate the given class name as an instance of EnabledCondition and returns it.
         * @param enabledConditionClass the name of an implementing class with a no-arg constructor, if null will reutrn null.
         * @return the constructed instance or null if the class can't be found or instantiated.
         */
        static EnabledCondition create(String enabledConditionClass)
        {
            return create(enabledConditionClass, Factory.class.getClassLoader());
        }
        
        static EnabledCondition create(String enabledConditionClass, ClassLoader classLoader)
        {
            if (enabledConditionClass == null)
            {
                log.debug("Cannot instantiate null EnabledCondition");
                return null;
            }
            try
            {
                return (EnabledCondition) ClassLoaderUtils.loadClass(enabledConditionClass, EnabledCondition.Factory.class).newInstance();
            }
            catch (InstantiationException e)
            {
                log.warn("Cannot instantiate Enabled Condition: '" + enabledConditionClass + "'", e);
            }
            catch (IllegalAccessException e)
            {
                log.warn("Cannot instantiate Enabled Condition: '" + enabledConditionClass + "'", e);
            }
            catch (ClassNotFoundException e)
            {
                log.warn("Cannot find Enabled Condition: '" + enabledConditionClass + "'", e);
            }
            catch (ClassCastException e)
            {
                log.warn("Enabled Condition : '" + enabledConditionClass + "' must implement " + EnabledCondition.class.getName(), e);
            }
            return null;
        }
    }
}
