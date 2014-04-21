package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


/**
 * Condition that an element specified by given locator must contain a particular CSS class.
 *
 * @since v4.3
 *
 * @see com.atlassian.jira.webtest.framework.core.condition.TimedCondition
 */
public class HasClassCondition extends AbstractLocatorBasedTimedCondition implements TimedCondition
{
    private static final Logger log = Logger.getLogger(HasClassCondition.class);

    private static final String CLASS_ATTR_SUFFIX = "@class";

    public static final class Builder extends AbstractLocatorBasedTimedConditionBuilder<Builder, HasClassCondition>
    {
        private String cssClass;

        public Builder(SeleniumContext context)
        {
            super(context, Builder.class);
        }

        /**
         * The CSS class that the target element should contain.
         *
         * @param cssClass CSS class
         * @return this builder instance
         */
        public Builder cssClass(String cssClass)
        {
            this.cssClass = cssClass;
            return this;
        }

        @Override
        public HasClassCondition build()
        {
            return new HasClassCondition(this);
        }
    }

    public static Builder forContext(SeleniumContext ctx)
    {
        return new Builder(ctx);
    }

    private final String cssClass;

    private HasClassCondition(Builder builder)
    {
        super(builder);
        this.cssClass = notNull("cssClass", builder.cssClass);
    }
    
    public boolean now()
    {
        // TODO this crap throws exception when an element does not have any CSS class (attribute is not present)
        // TODO thanks again Selenium for being such a shit and we have to come up with a workaround
        String classes = client.getAttribute(locator + CLASS_ATTR_SUFFIX);
        log.debug("classes=<" + classes + ">");
        if (StringUtils.isBlank(classes))
        {
            return false;
        }
        if (!classes.contains(cssClass))
        {
            return false;
        }
        for (String singleClass : classes.split("\\s"))
        {
            if (cssClass.equals(singleClass))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        return super.toString() + "[cssClass=" + cssClass + "]";
    }
}
