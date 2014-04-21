package com.atlassian.renderer.links;

/**
 * A link to a resource that exists, but that the current user is not permitted to access. Generally,
 * these should be treated identically to unresolved links (so users don't get an idea about what
 * might exist but they can't see), but internal link-tracking systems will obviously need to know
 * the difference.
 */
public class UnpermittedLink extends Link
{
    private Link wrappedLink;

    public UnpermittedLink(Link wrappedLink)
    {
        super(wrappedLink.getOriginalLinkText());
        this.wrappedLink = wrappedLink;
    }

    public Link getWrappedLink()
    {
        return wrappedLink;
    }

    public String getLinkBody()
    {
        return wrappedLink.getUnpermittedLinkBody();
    }

    public boolean isRelativeUrl()
    {
        return false;
    }

    public String getTitle()
    {
        // don't return the wrapped title, that may contain information the user isn't allowed to see
        return wrappedLink.getUnpermittedLinkBody();
    }

    public String getUrl()
    {
        return "";
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof UnpermittedLink)) return false;

        final UnpermittedLink unpermittedLink = (UnpermittedLink) o;

        if (wrappedLink != null ? !wrappedLink.equals(unpermittedLink.wrappedLink) : unpermittedLink.wrappedLink != null) return false;

        return true;
    }

    public int hashCode()
    {
        return (wrappedLink != null ? wrappedLink.hashCode() : 0);
    }
}
