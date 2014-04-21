package com.atlassian.renderer.links;

import com.atlassian.renderer.RenderContext;

import java.util.List;

/**
 * Converts some arbitrary text (i.e. "My Issue|CONF-1234@JIRA") into a Link object that can then
 * be rendered.
 */
public interface LinkResolver
{
    /**
     * Given some link text, convert it into a link object that can then be rendered into
     * the page. The link text supplied should <i>not</i> contain any surrounding delimiters,
     * so for example if you write "[Foo|Bar]" in the page, you should just be passing
     * "Foo|Bar" to this method
     *
     * @param linkText The text to turn into a link
     * @return the corresponding link. This method should never return null, in the event of
     *         the link not being parseable, an UnparseableLink should be returned instead.
     */
    Link createLink(RenderContext context, String linkText);

    /**
     * @param pageContent
     * @return a list of links in the content passed in string format
     */
    List extractLinkTextList(String pageContent);

    /**
     * @param context
     * @param text
     * @return a list of {@link Link} objects extracted from the text provided
     */
    List extractLinks(RenderContext context, String text);

    String removeLinkBrackets(String linkText);
}
