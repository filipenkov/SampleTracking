/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 28, 2004
 * Time: 8:29:01 PM
 */
package com.atlassian.renderer.v2;

/**
 * Describes which operations are permitted at any point in the rendering of a page from wiki
 * to HTML.
 *
 * <p>Essentially, this is a big collection of bitwise flags. If we need more than 64 flags we'll
 * have to switch to using the java.util.BitSet, but for now, clients get cleaner-looking code
 * just using the flag constants.
 */
public class RenderMode
{
    // FLAGS
    /** Render everything */
    public static final long F_ALL = 0x7FFFFFFFEFFFFFFFL;
    /** Render nothing */
    public static final long F_NONE = 0;

    /** Render paragraph (you might also need {@link #F_FIRST_PARA} */
    public static final long F_PARAGRAPHS =         0x0000000000000001;
    /** Render newlines and forced linebreaks */
    public static final long F_LINEBREAKS =         0x0000000000000002;
    /** Render macros */
    public static final long F_MACROS =             0x0000000000000004;
    /** Render [links], free URLs and (if enabled) CamelCaseLinks */
    public static final long F_LINKS =              0x0000000000000008;
    /** Render phrase markup - bold, italic etc. as well as typographical markup and emoticons */
    public static final long F_PHRASES =            0x0000000000000010;
    /** Render explicit images */
    public static final long F_IMAGES =             0x0000000000000020;
    /** Render tables */
    public static final long F_TABLES =             0x0000000000000040;
    /** Escape characters that might be confused with HTML */
    public static final long F_HTMLESCAPE =         0x0000000000000080;
    /** Render a paragraph even if it is the first line of a block of content:
     *  that is, having no linebreak before to trigger an automatic p tag (requires {@link #F_PARAGRAPHS}) */
    public static final long F_FIRST_PARA =         0x0000000000000100;
    /** Render lists */
    public static final long F_LISTS =              0x0000000000000200;
    /** Resolve tokens */
    public static final long F_RESOLVE_TOKENS =     0x0000000000000400;
    /** Preserve HTML entities (i.e.&nbsp;don't escape the & in &#1234;) -- only meaningful when F_HTMLESCAPE is set*/
    public static final long F_PRESERVE_ENTITIES =  0x0000000000000800;
    /** Escape special characters preceded by a backslash */
    public static final long F_BACKSLASH_ESCAPE =   0x0000000000001000;
    /** Template variables */
    public static final long F_TEMPLATE =           0x0000000000002000;
    /** Render an error message when a macro is not found */
    public static final long F_MACROS_ERR_MSG   =   0x0000000000004000;



    /** Render everything */
    public static final RenderMode ALL = RenderMode.allow(F_ALL);
    /** Render everything, but assume HTML escaping has already occurred */
    public static final RenderMode NO_ESCAPE = RenderMode.suppress(F_HTMLESCAPE);
    /** Render only links (usually for link extraction) */
    public static final RenderMode LINKS_ONLY = RenderMode.allow(F_LINKS | F_HTMLESCAPE | F_BACKSLASH_ESCAPE | F_PRESERVE_ENTITIES);
    /** Render things you'd normally find inside a paragraph */
    public static final RenderMode INLINE = RenderMode.allow(F_HTMLESCAPE | F_BACKSLASH_ESCAPE | F_PRESERVE_ENTITIES |F_PHRASES | F_IMAGES | F_LINKS | F_LINEBREAKS | F_TEMPLATE);
    /** Render phrases and images */
    public static final RenderMode PHRASES_IMAGES = RenderMode.allow(F_HTMLESCAPE | F_BACKSLASH_ESCAPE | F_PRESERVE_ENTITIES | F_PHRASES | F_IMAGES| F_TEMPLATE);
    /** Render phrases and links */
    public static final RenderMode PHRASES_LINKS = RenderMode.allow(F_HTMLESCAPE | F_BACKSLASH_ESCAPE | F_PRESERVE_ENTITIES | F_PHRASES | F_LINKS| F_TEMPLATE);
    /** Render text made up only of paragraphs, without images or links */
    public static final RenderMode SIMPLE_TEXT = RenderMode.allow(F_HTMLESCAPE | F_BACKSLASH_ESCAPE | F_PRESERVE_ENTITIES | F_PHRASES | F_LINEBREAKS | F_PARAGRAPHS | F_FIRST_PARA| F_TEMPLATE);
    /** Render mode for contents of a list item */
    public static final RenderMode LIST_ITEM = RenderMode.suppress(F_LISTS | F_FIRST_PARA);
    /** Render mode for contents of a table cell (links in a table are rendered before cell division to avoid pipe conflicts...) */
    public static final RenderMode TABLE_CELL = RenderMode.suppress(F_TABLES | F_FIRST_PARA | F_LINKS);
    /** Render mode for suppressing the warning messages for unfound macros */
    public static final RenderMode ALL_WITH_NO_MACRO_ERRORS = RenderMode.suppress(F_MACROS_ERR_MSG);
    /** Render mode for macros only */
    public static final RenderMode MACROS_ONLY = RenderMode.allow(F_MACROS);

    /** Don't render anything */
    public static final RenderMode NO_RENDER = RenderMode.allow(F_NONE);

    private final long flags;
    public static final RenderMode COMPATIBILITY_MODE = suppress(F_FIRST_PARA);

    /**
     * Create a new render mode by starting from the state where everything is permitted,
     * and then switching off those flags passed into the method. So, for example, if you
     * want a mode where everything is allowed EXCEPT macros and links, you would use:
     * <code>RenderMode.suppress(F_MACROS | F_LINKS};</code>.
     *
     * @param flags a bitmask of the flags that are NOT set in this render mode
     * @return the appropriate render mode with 'flags' turned off
     * @see RenderMode#allow(long)
     */
    public static RenderMode suppress(long flags)
    {
        return new RenderMode(F_ALL & ~flags);
    }


    /**
     * Create a new render mode by starting from the state where nothing is permitted,
     * then turning on those flags passed into the method. So, for example, if you want
     * a mode where ONLY paragraphs and linebreaks are rendered, you would use:
     * <code>RenderMode.allow(F_PARAGRAPHS | F_LINEBREAKS);</code>.
     *
     * @param flags a bitmask of the flags that are set in this render mode.
     * @return the appropriate render mode with 'flags' turned on
     * @see RenderMode#suppress(long)
     */
    public static RenderMode allow(long flags)
    {
        return new RenderMode(flags);
    }

    /**
     * Create a new render mode that is the logical AND of this and another render mode.
     * This is useful for a case in which you need to render with certain
     * flags set UNLESS they're already forbidden by a previous mode.
     */
    public RenderMode and(RenderMode otherMode)
    {
        return new RenderMode(flags & otherMode.flags);
    }

    /**
     * Create a new render mode that is the logical OR of this and another render mode.
     * This is useful for a case in which you need to render with certain flags set
     * IN ADDITION TO a previous mode.
     */
    public RenderMode or(RenderMode otherMode)
    {
        return new RenderMode(flags | otherMode.flags);
    }

    /**
     * Private constructor.
     * Use {@link RenderMode#allow(long)} or {@link RenderMode#suppress(long)} instead.
     *
     * @see RenderMode#allow(long)
     * @see RenderMode#suppress(long)
     * @param flags the raw value of the 'flags' variable for this render mode.
     */
    private RenderMode(long flags)
    {
        this.flags = flags;
    }

    public boolean renderLinebreaks()
    {
        return flagSet(F_LINEBREAKS);
    }

    public boolean renderLinks()
    {
        return flagSet(F_LINKS);
    }

    public boolean renderMacros()
    {
        return flagSet(F_MACROS);
    }

    public boolean renderParagraphs()
    {
        return flagSet(F_PARAGRAPHS);
    }

    public boolean renderPhrases()
    {
        return flagSet(F_PHRASES);
    }

    public boolean renderImages()
    {
        return flagSet(F_IMAGES);
    }

    public boolean renderTables()
    {
        return flagSet(F_TABLES);
    }

    public boolean renderNothing()
    {
        return flags == 0;
    }

    public boolean htmlEscape()
    {
        return flagSet(F_HTMLESCAPE);
    }

    public boolean backslashEscape()
    {
        return flagSet(F_BACKSLASH_ESCAPE);
    }

    public boolean renderFirstParagraph()
    {
        return flagSet(F_FIRST_PARA);
    }

    public boolean renderTemplate()
    {
        return flagSet(F_TEMPLATE);
    }

    public boolean renderLists()
    {
        return flagSet(F_LISTS);
    }

    public boolean resolveTokens()
    {
        return flagSet(F_RESOLVE_TOKENS);
    }

    public boolean renderMacroErrorMessages()
    {
        return flagSet(F_MACROS_ERR_MSG);
    }

    public boolean preserveEntities()
    {
        return flagSet(F_PRESERVE_ENTITIES);
    }
    /**
     * Whether or not this current render mode will create tokens that need to be detokenized.
     */
    public boolean tokenizes()
    {
        return renderLinks() || renderImages() || renderMacros();
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof RenderMode)) return false;

        return flags == ((RenderMode) o).flags;
    }

    // Same hashcode calc as java.lang.Long
    public int hashCode()
    {
        return (int)(flags ^ (flags >>> 32));
    }

    private boolean flagSet(long flag)
    {
        return (flags & flag) == flag;
    }
}