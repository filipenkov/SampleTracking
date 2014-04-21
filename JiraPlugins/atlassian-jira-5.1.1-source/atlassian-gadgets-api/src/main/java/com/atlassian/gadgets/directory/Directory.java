package com.atlassian.gadgets.directory;

import java.net.URI;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.gadgets.GadgetRequestContext;

public interface Directory
{
    public static final String DIRECTORY_FEATURE_NAME = "gadget-directory";
    public static final String DIRECTORY_FEATURE_CATEGORIES_NAME = "categories";

    /**
     * Returns all the entries available, localized by the {@code locale}.
     *
     * @param gadgetRequestContext the context of this request
     * @return all the entries available
     */
    Iterable<Entry> getEntries(GadgetRequestContext gadgetRequestContext);

    /**
     * @param gadgetSpecUri uri of the gadget spec to check if it's in the directory
     * @return true if the gadget spec at the location specificied by the uri is in the directory, false otherwise
     */
    boolean contains(URI gadgetSpecUri);

    /**
     * A single entry in the directory.
     */
    public static interface Entry
    {
        /**
         * Returns the unique URI of the directory entry.
         *
         * @return the {@code URI} that represents this directory entry, or {@code null} if the entry is not directly
         *         addressable.
         */
        @Nullable URI getSelf();

        /**
         * Can this Gadget Spec File be removed from the directory?
         *
         * @return whether or not the Gadget Spec File be removed from the directory.
         */
        boolean isDeletable();

        /**
         * Returns the URI of the gadget spec file for this directory entry.
         *
         * @return the URI of the gadget spec file for this entry.  Must not be {@code null}.
         */
        URI getGadgetSpecUri();

        /**
         * Returns the title of the directory entry.  This should be human-readable and localized.  It will not have any
         * dynamic substitution or other post-processing performed on it.
         *
         * @return the human-readable title of this entry.  Must not be {@code null}, but may be an empty string.
         */
        String getTitle();

        /**
         * Returns a URI of the gadget author's choosing that is displayed as a link in the directory, with the title as
         * the link text.  In most cases this should be the gadget's web site.  This method may return {@code null} to
         * suppress the link.
         *
         * @return a URI to link to from the title in the directory entry.  May be {@code null}.
         */
        @Nullable URI getTitleUri();

        /**
         * Returns the URI of an image to display as a thumbnail or icon in the directory for this entry.  This method
         * may return {@code null} if no thumbnail or icon is available.
         *
         * @return the URI of an image to display in the directory.  May be {@code null}.
         */
        @Nullable URI getThumbnailUri();

        /**
         * Returns the name of the author of the gadget for this directory entry.
         *
         * @return the name of the gadget author for this entry.  Must not be {@code null}, but may be an empty string.
         */
        String getAuthorName();

        /**
         * Returns the email address of the author of the gadget for this directory entry.  This must be a well-formed
         * and valid address, or an empty string if no email address should be displayed.
         *
         * @return the valid email address of the gadget author for this entry.  Must not be {@code null}, but may be an
         *         empty string.
         */
        String getAuthorEmail();

        /**
         * Returns a textual description of the gadget for this directory entry.
         *
         * @return a description of this entry.  Must not be {@code null}, but may be an empty string.
         */
        String getDescription();

        /**
         * Returns the directory categories of this entry.  If the gadget does not specify any valid category, this
         * should return a set containing only {@link Category#OTHER}.
         *
         * @return a set of the directory categories of this entry or a set containing {@link Category#OTHER}.  Must not
         *         be {@code null}.
         */
        Set<Category> getCategories();
    }
}
