package com.atlassian.jira.webtest.framework.model.admin;

import static com.atlassian.jira.util.dbc.NumberAssertions.greaterThan;

/**
 * Represents a screen in the JIRA administration UI. Screens are used to configure data that users will see on different
 * issue views (view issue, edit issue, workflow transitions etc.) for particular context (workflow, user, project etc.).
 *
 * @see com.atlassian.jira.webtest.framework.page.admin.ViewScreens
 * @since v4.3
 */
public final class Screen
{
    public static Screen DEFAULT = new Screen(1, "Default Screen");
    public static Screen WORKFLOW = new Screen(2, "Workflow Screen");
    public static Screen RESOLVE_ISSUE = new Screen(3, "Resolve Issue Screen");

    /**
     * Create custom instance of Screen.
     *
     * @param id ID of the screen
     * @param name name of the screen
     * @return ne screen instance
     */
    public static Screen custom(long id, String name)
    {
        return new Screen(id, name);
    }

    /**
     * Create custom instance of Screen without specifying a name.
     *
     * @param id ID of the screen
     * @return ne screen instance
     */
    public static Screen custom(long id)
    {
        return new Screen(id, null);
    }

    private final String name;
    private final long id;


    private Screen(long id, String name)
    {
        this.id = greaterThan("id", id, 0);
        this.name = name;
    }


    /**
     * Checks if this screen instance has name. Equivalent to {@link #name()} != <code>null</code>. 
     *
     * @return <code>true</code>, if this screen has a name (i.e. name is not <code>null</code>)
     */
    public boolean hasName()
    {
        return name != null;
    }

    /**
     * Name as seen on the {@link com.atlassian.jira.webtest.framework.page.admin.ViewScreens} page.
     *
     * @return name of the screen, may be <code>null</code>
     */
    public String name()
    {
        return name;
    }

    /**
     * Id of the screen.
     *
     * @return screen id
     */
    public long id()
    {
        return id;
    }
}
