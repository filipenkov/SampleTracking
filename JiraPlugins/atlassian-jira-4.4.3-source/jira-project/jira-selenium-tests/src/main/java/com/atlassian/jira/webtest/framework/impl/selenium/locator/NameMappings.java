package com.atlassian.jira.webtest.framework.impl.selenium.locator;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import com.atlassian.jira.webtest.framework.core.locator.mapper.AbstractMapping;
import com.atlassian.jira.webtest.framework.core.locator.mapper.LocatorDataBean;
import com.atlassian.jira.webtest.framework.core.locator.mapper.LocatorMapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.webtest.framework.core.locator.Locators.CLASS;
import static com.atlassian.jira.webtest.framework.core.locator.Locators.CSS;
import static com.atlassian.jira.webtest.framework.core.locator.Locators.ID;
import static com.atlassian.jira.webtest.framework.core.locator.Locators.JQUERY;
import static com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators.NAME;

/**
 * Locator mappings for Selenium 'name' locator type.
 *
 * @since v4.3
 */
final class NameMappings
{
    private static final String NAME_CSS_TEMPLATE = "[name='%s']";

    private static String cssFor(String nameValue)
    {
        return String.format(NAME_CSS_TEMPLATE, nameValue);
    }

    static final List<LocatorMapping> ALL = createAll();

    private static List<LocatorMapping> createAll()
    {
        return Collections.unmodifiableList(Arrays.asList(
                name(),
                idtoName(), nameToId(),
                classToName(), nameToClass(),
                cssToName(), nameToCss(),
                jQueryToName(), nameToJQuery()
        ));
    }

    private NameMappings()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static LocatorMapping name()
    {
        return Name.INSTANCE;
    }

    public static LocatorMapping nameToId()
    {
        return NameToId.INSTANCE;
    }

    public static LocatorMapping idtoName()
    {
        return IdToName.INSTANCE;
    }

    public static LocatorMapping nameToClass()
    {
        return NameToClass.INSTANCE;
    }

    public static LocatorMapping classToName()
    {
        return ClassToName.INSTANCE;
    }

    public static LocatorMapping nameToCss()
    {
        return NameToCss.INSTANCE;
    }

    public static LocatorMapping cssToName()
    {
        return CssToName.INSTANCE;
    }

    public static LocatorMapping nameToJQuery()
    {
        return NameToJQuery.INSTANCE;
    }

    public static LocatorMapping jQueryToName()
    {
        return JQueryToName.INSTANCE;
    }

    private static class Name extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new Name();

        private Name()
        {
            super(NAME, NAME);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, cssFor(parentValue) + " " + cssFor(childValue));
        }
    }

    private static class NameToId extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new NameToId();

        private NameToId()
        {
            super(NAME, ID);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, cssFor(parentValue) + " #" + childValue);
        }
    }

    private static class IdToName extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new JQueryToName();

        private IdToName()
        {
            super(ID, NAME);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, "#" + parentValue + " " + cssFor(childValue));
        }
    }

    private static class NameToClass extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new NameToClass();

        private NameToClass()
        {
            super(NAME, CLASS);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, cssFor(parentValue) + " ." + childValue);
        }
    }

    private static class ClassToName extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new JQueryToName();

        private ClassToName()
        {
            super(CLASS, NAME);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, "." + parentValue + " " + cssFor(childValue));
        }
    }

    private static class NameToCss extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new NameToJQuery();

        private NameToCss()
        {
            super(NAME, CSS);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, cssFor(parentValue) + " " + childValue);
        }
    }

    private static class CssToName extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new JQueryToName();

        private CssToName()
        {
            super(CSS, NAME);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(CSS, parentValue + " " + cssFor(childValue));
        }
    }

    private static class NameToJQuery extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new NameToJQuery();

        private NameToJQuery()
        {
            super(NAME, JQUERY);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(JQUERY, cssFor(parentValue) + " " + childValue);
        }
    }

    private static class JQueryToName extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new JQueryToName();

        private JQueryToName()
        {
            super(JQUERY, NAME);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(JQUERY, parentValue + " " + cssFor(childValue));
        }
    }
}
