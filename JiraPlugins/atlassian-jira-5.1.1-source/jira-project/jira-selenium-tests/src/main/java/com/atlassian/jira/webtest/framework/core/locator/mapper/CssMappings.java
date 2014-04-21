package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.webtest.framework.core.locator.Locators.*;

/**
 * Mappings with CSS locator type as a parent.
 *
 * @since v4.2
 */
final class CssMappings
{
    static final List<LocatorMapping> ALL = createAll();

    private static List<LocatorMapping> createAll()
    {
        return Collections.unmodifiableList(Arrays.asList(
                css(), cssToId(), cssToClass(), cssToJQuery()
        ));
    }

    private CssMappings()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static LocatorMapping css()
    {
        return Css.INSTANCE;
    }

    public static LocatorMapping cssToId()
    {
        return CssToId.INSTANCE;
    }

    public static LocatorMapping cssToClass()
    {
        return CssToClass.INSTANCE;
    }

    public static LocatorMapping cssToJQuery()
    {
        return CssToJQuery.INSTANCE;
    }

    private static class Css extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new Css();

        private Css()
        {
            super(CSS, CSS);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, parentValue + " " + childValue);
        }
    }

    private static class CssToId extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new CssToId();

        private CssToId()
        {
            super(CSS, ID);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, parentValue + " #" + childValue);
        }
    }

    private static class CssToClass extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new CssToClass();

        private CssToClass()
        {
            super(CSS, CLASS);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, parentValue + " ." + childValue);
        }
    }

    private static class CssToJQuery extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new CssToJQuery();

        private CssToJQuery()
        {
            super(CSS, JQUERY);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(JQUERY, parentValue + " " + childValue);
        }
    }
}
