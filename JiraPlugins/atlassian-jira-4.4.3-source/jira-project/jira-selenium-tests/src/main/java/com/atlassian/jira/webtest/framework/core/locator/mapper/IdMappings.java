package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.webtest.framework.core.locator.Locators.*;

/**
 * Mappings with ID locator type as a parent.
 *
 * @since v4.2
 */
final class IdMappings
{
    static final List<LocatorMapping> ALL = createAll();

    private static List<LocatorMapping> createAll()
    {
        return Collections.unmodifiableList(Arrays.asList(
                id(), idToClass(), idToCss(), idToJQuery(), idToXpath()
        ));
    }

    private IdMappings()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static LocatorMapping id()
    {
        return Id.INSTANCE;
    }

    public static LocatorMapping idToClass()
    {
        return IdToClass.INSTANCE;
    }

    public static LocatorMapping idToCss()
    {
        return IdToCss.INSTANCE;
    }

    public static LocatorMapping idToJQuery()
    {
        return IdToJQuery.INSTANCE;
    }

    public static LocatorMapping idToXpath()
    {
        return IdToXPath.INSTANCE;
    }

    private static class Id extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new Id();

        private Id()
        {
            super(ID, ID);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, "#" + parentValue + " #" + childValue);
        }
    }

    private static class IdToClass extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new IdToClass();

        private IdToClass()
        {
            super(ID, CLASS);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, "#" + parentValue + " ." + childValue);
        }
    }

    private static class IdToCss extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new IdToCss();

        private IdToCss()
        {
            super(ID, CSS);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, "#" + parentValue + " " + childValue);
        }
    }

    private static class IdToJQuery extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new IdToJQuery();

        private IdToJQuery()
        {
            super(ID, JQUERY);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(JQUERY, "#" + parentValue + " " + childValue);
        }
    }

    private static class IdToXPath extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new IdToXPath();

        private IdToXPath()
        {
            super(ID, XPATH);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(XPATH, XPathMappings.combine("//*[@id='" + parentValue + "']", childValue));
        }
    }
}
