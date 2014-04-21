package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.webtest.framework.core.locator.Locators.*;

/**
 * Mappings with jQuery locator type as a parent.
 *
 * @since v4.2
 */
final class JQueryMappings
{
    static final List<LocatorMapping> ALL = createAll();

    private static List<LocatorMapping> createAll()
    {
        return Collections.unmodifiableList(Arrays.asList(
                jQuery(), jQueryToId(), jQueryToClass(), jQueryToCss()
        ));
    }

    private JQueryMappings()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static LocatorMapping jQuery()
    {
        return JQuery.INSTANCE;
    }

    public static LocatorMapping jQueryToId()
    {
        return JQueryToId.INSTANCE;
    }

    public static LocatorMapping jQueryToClass()
    {
        return JQueryToClass.INSTANCE;
    }

    public static LocatorMapping jQueryToCss()
    {
        return JQueryToCss.INSTANCE;
    }

    private static class JQuery extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new JQuery();

        private JQuery()
        {
            super(JQUERY, JQUERY);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(JQUERY, parentValue + " " + childValue);
        }
    }

    private static class JQueryToId extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new JQueryToId();

        private JQueryToId()
        {
            super(JQUERY, ID);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(JQUERY, parentValue + " #" + childValue);
        }
    }

    private static class JQueryToClass extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new JQueryToClass();

        private JQueryToClass()
        {
            super(JQUERY, CLASS);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(JQUERY, parentValue + " ." + childValue);
        }
    }

    private static class JQueryToCss extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new JQueryToCss();

        private JQueryToCss()
        {
            super(JQUERY, CSS);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(JQUERY, parentValue + " " + childValue);
        }
    }
}
