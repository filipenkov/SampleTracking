package com.atlassian.jira.webtest.framework.core.locator;

/**
 * Enumeration of default locators.
 *
 */
public enum Locators implements LocatorType
{
    /**
     * Locates page elements by their ID.
     *
     */
    ID("id"),

    /**
     * Locates page elements by their class.
     *
     */
    CLASS("class"),

    /**
     * Uses CSS selector syntax to locate page elements.
     *
     */
    CSS("css"),

    /**
     * Uses jQuery selector syntax to locate page elements.
     *
     */
    JQUERY("jquery"),

    /**
     * Uses XPATH syntax to locate page elements.
     *
     */
    XPATH("xpath");


    private final String id;

    private Locators(final String id) 
    {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     *
     * 
     */
    public String id()
    {
        return id;
    }


    /**
     * For given locator <tt>value</tt>, creates a generic locator object. This locator is not useful for any test
     * context-sensitive operations (checking for presence, nesting), but may prove useful as an argument to
     * other locators from test context, e.g. to created nested locators or check for their support for other
     * locator types.
     *
     * @param value value of the locator
     * @return new generic locator with given <tt>value</tt>
     */
    public Locator create(String value)
    {
        return new GenericLocator(this, value);
    }


    private static class GenericLocator implements Locator
    {
        private final LocatorType type;
        private final String value;

        public GenericLocator(final LocatorType type, final String value)
        {
            this.type = type;
            this.value = value;
        }

        public LocatorType type()
        {
            return type;
        }

        public String value()
        {
            return value;
        }

        public Element element()
        {
            throw new UnsupportedOperationException("GenericLocator");
        }

        public boolean supports(final Locator other)
        {
            throw new UnsupportedOperationException("GenericLocator");
        }

        public Locator combine(final Locator toNest)
        {
            throw new UnsupportedOperationException("GenericLocator");
        }
    }
}
