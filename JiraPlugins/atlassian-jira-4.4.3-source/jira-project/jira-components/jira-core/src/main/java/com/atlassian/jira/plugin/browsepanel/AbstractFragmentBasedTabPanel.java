package com.atlassian.jira.plugin.browsepanel;

import com.atlassian.jira.plugin.AbstractTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.projectpanel.fragment.MenuFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.ProjectTabPanelFragment;
import com.atlassian.jira.project.browse.BrowseContext;

import java.util.Collections;
import java.util.List;

/**
 * Abstract base for {@link TabPanel} implementers to extend.  This looks
 * after the layout of all the fragments on the page and delegates content to the
 * {@link com.atlassian.jira.plugin.projectpanel.fragment.ProjectTabPanelFragment}.
 *
 * @since v4.0
 */
public abstract class AbstractFragmentBasedTabPanel<D extends AbstractTabPanelModuleDescriptor, C extends BrowseContext, F extends ProjectTabPanelFragment> implements TabPanel<D, C>
{
    protected D moduleDescriptor;

    public void init(final D descriptor)
    {
        this.moduleDescriptor = descriptor;
    }

    public String getHtml(final C ctx)
    {
        final StringBuilder sb = new StringBuilder();
        final List<MenuFragment> menuFrags = getMenuFragments();
        if (!menuFrags.isEmpty())
        {
            sb.append("<div id=\"quicklinks\">\n");
            sb.append("<ul class=\"operations\">\n");
            for (MenuFragment menuFragment : menuFrags)
            {
                if (menuFragment.showFragment(ctx))
                {
                    sb.append("<li class=\"aui-dd-parent\">\n");
                    sb.append(menuFragment.getHtml(ctx));
                    sb.append("</li>\n");
                }
            }
            sb.append("</div>");
        }
        createContent(ctx, sb);
        return sb.toString();
    }

    private void createContent(C ctx, StringBuilder sb)
    {
        final StringBuilder leftcolSb = new StringBuilder();
        final boolean hasLeftContent = createColumnContent(ctx, leftcolSb, getLeftColumnFragments(ctx));

        final StringBuilder rightcolSb = new StringBuilder();
        final boolean hasRightContent = createColumnContent(ctx, rightcolSb, getRightColumnFragments(ctx));

        if (!hasLeftContent && !hasRightContent)
        {
            sb.append("<div id=\"").append("primary").append("\" class=\"column full\">").append("<div class=\"content\">");
            sb.append(createEmptyContent());
            sb.append("</div>").append("</div>");
        }
        else if (hasLeftContent && !hasRightContent)
        {
            sb.append("<div id=\"").append("primary").append("\" class=\"column full\">").append("<div class=\"content\">");
            sb.append(leftcolSb);
            sb.append("</div>").append("</div>");
        }
        else if (!hasLeftContent && hasRightContent)
        {
            sb.append("<div id=\"").append("primary").append("\" class=\"column full\">").append("<div class=\"content\">");
            sb.append(rightcolSb);
            sb.append("</div>").append("</div>");
        }
        else
        {
            sb.append("<div id=\"").append("primary").append("\" class=\"column\">").append("<div class=\"content\">");
            sb.append(leftcolSb);
            sb.append("</div>").append("</div>");
            sb.append("<div id=\"").append("secondary").append("\" class=\"column\">").append("<div class=\"content\">");
            sb.append(rightcolSb);
            sb.append("</div>").append("</div>");

        }

    }

    /**
     * Create content for when there ae no fragments shown.
     *
     * @return the html for the empty content.
     */
    public String createEmptyContent()
    {
        return moduleDescriptor.getI18nBean().getText("browseproject.empty.tab.generic");
    }

    private boolean createColumnContent(C ctx, StringBuilder sb, List<F> columnFragments)
    {
        boolean hasAtleastOneFrag = false;
        for (F fragment : columnFragments)
        {
            hasAtleastOneFrag = appendFragmentIfVisible(ctx, sb, fragment) || hasAtleastOneFrag;
        }
        return hasAtleastOneFrag;
    }


    /**
     * Appends the raw HTML of the fragment to an output buffer, if the fragment should be displayed.
     *
     * @param ctx      the browse context
     * @param sb       the buffer to append to
     * @param fragment the fragment to display
     * @return true if the fragment was shown, false otherwise
     */
    private boolean appendFragmentIfVisible(final C ctx, final StringBuilder sb, final F fragment)
    {
        if (fragment.showFragment(ctx))
        {
            sb.append("<div class=\"module\" id=\"frag").append(fragment.getId()).append("\">");
            sb.append(fragment.getHtml(ctx));
            sb.append("</div>");
            return true;
        }
        return false;
    }

    /**
     * @param ctx the browse context
     * @return a list of fragments to be displayed in the left column of the project tab panel
     */
    protected abstract List<F> getLeftColumnFragments(C ctx);

    /**
     * @param ctx the browse context
     * @return a list of fragments to be displayed in the right column of the project tab panel
     */
    protected abstract List<F> getRightColumnFragments(C ctx);

    /**
     * @return a list of fragments to be displayed in the menu section of the panel
     */
    protected List<MenuFragment> getMenuFragments()
    {
        return Collections.emptyList();
    }

    public boolean showPanel(final C context)
    {
        // TODO: Check is there are any components in this project as well

        return !(getLeftColumnFragments(context).isEmpty() && getRightColumnFragments(context).isEmpty());
    }
}
