package com.atlassian.renderer.embedded;

import java.util.Properties;

/**
 * Link to an embedded mime resource.
 */
public class EmbeddedResource
{
    /**
     * Embedded resource parser, available for accessing the original embedded text.
     */
    protected final EmbeddedResourceParser parser;

    /**
     * The filename of this resource.
     */
    protected final String filename;

    /**
     * The mime type of this resource.
     */
    protected final String type;

    /**
     * The confluence page to which the resource is attached. This applies for relative/internal resources
     * only.
     */
    protected final String page;

    /**
     * The confluence space to which the resource is attached. This applied for relative/internal resources
     * only.
     */
    protected final String space;

    /**
     * The unparsed reference to the embedded resource. ie: if external, this is a URL, if internal it is
     * space:page^filename.ext
     */
    protected final String url;

    /**
     * The original (unparsed) embedded link text.
     */
    protected final String originalText;

    /**
     * The resource properties
     */
    protected Properties properties;

    /**
     * Create a new EmbeddedResource.
     *
     * @param parser
     */
    public EmbeddedResource(EmbeddedResourceParser parser)
    {
        this.originalText = parser.getOriginalText();
        this.parser = parser;

        url = parser.getResource();
        filename = parser.getFilename();
        type = parser.getType();
        space = parser.getSpace();
        page = parser.getPage();
    }

    /**
     * Create a new Embedded resource using embedded resource text.
     *
     * @param originalText
     */
    public EmbeddedResource(String originalText)
    {
        this(new EmbeddedResourceParser(originalText));
    }

    /**
     * Returns whether this class will handle the given file or not
     *
     * @param parser The parser for the file
     * @return True is there is a match, false otherwise
     */
    public static boolean matchesType(EmbeddedResourceParser parser) throws IllegalArgumentException
    {
        return (parser.getType() == null || parser.getType().equals(""));
    }

    /**
     * Returns true if the resource is external to confluence.
     * @return
     */
    public boolean isExternal()
    {
        return parser.isExternal();
    }

    /**
     * Returns true if this resource is internal to confluence.
     * @return
     */
    public boolean isInternal()
    {
        return !isExternal();
    }

    public String getUrl()
    {
        return url;
    }

    public String getFilename()
    {
        return filename;
    }

    /**
     * Retrieve the resource mime type, or null if the type is unknown.
     * @return
     */
    public String getType()
    {
        return type;
    }

    /**
     * Retrieve the original embedded resource text. This represents the text as entered in
     * the wiki markup.
     * @return
     */
    public String getOriginalLinkText()
    {
        return originalText;
    }

    /**
     * The name of the space this resource is attached to. The space name is only relevant for
     * internal resources.
     *
     * @return
     * @see #isInternal()
     */
    public String getSpace()
    {
        return space;
    }

    /**
     * The name of the page this resource is attached to. The page name is only relevant for internal resources.
     *
     * @return
     * @see #isInternal()
     */
    public String getPage()
    {
        return page;
    }

    public String toString()
    {
        return "EmbeddedResource["+originalText+"]";
    }

    public Properties getProperties() {
        return properties;
    }
}
