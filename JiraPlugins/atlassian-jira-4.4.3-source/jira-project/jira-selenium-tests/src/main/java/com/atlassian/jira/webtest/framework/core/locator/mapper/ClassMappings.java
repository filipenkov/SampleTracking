package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.webtest.framework.core.locator.Locators.*;

/**
 * Mappings with class locator type as a parent.
 *
 * @since v4.2
 */
final class ClassMappings
{
    static final List<LocatorMapping> ALL = createAll();

    private static List<LocatorMapping> createAll()
    {
        return Collections.unmodifiableList(Arrays.asList(
                classToClass(), classToId(), classToCss(), classToJQuery(), classToXPath()
        ));
    }

    private ClassMappings()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static LocatorMapping classToClass()
    {
        return HtmlClass.INSTANCE;
    }

    public static LocatorMapping classToId()
    {
        return ClassToId.INSTANCE;
    }

    public static LocatorMapping classToCss()
    {
        return ClassToCss.INSTANCE;
    }

    public static LocatorMapping classToJQuery()
    {
        return ClassToJQuery.INSTANCE;
    }

    public static LocatorMapping classToXPath()
    {
        return ClassToXPath.INSTANCE;
    }

    private static class HtmlClass extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new HtmlClass();

        private HtmlClass()
        {
            super(CLASS, CLASS);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, "." + parentValue + " ." + childValue);
        }
    }

    private static class ClassToId extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new ClassToId();

        private ClassToId()
        {
            super(CLASS, ID);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, "." + parentValue + " #" + childValue);
        }
    }

    private static class ClassToCss extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new ClassToCss();

        private ClassToCss()
        {
            super(CLASS, CSS);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, "." + parentValue + " " + childValue);
        }
    }

    private static class ClassToJQuery extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new ClassToJQuery();

        private ClassToJQuery()
        {
            super(CLASS, JQUERY);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(JQUERY, "." + parentValue + " " + childValue);
        }
    }

    private static class ClassToXPath extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new ClassToXPath();

        private ClassToXPath()
        {
            super(CLASS, XPATH);
        }

        @Override
        protected LocatorData doCombine(final String parentValue, final String childValue)
        {
            return new LocatorDataBean(XPATH, XPathMappings.combine("//*[@class='" + parentValue + "']", childValue));
        }
    }
}
