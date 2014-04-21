package com.atlassian.gadgets.directory.internal.impl;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.gadgets.directory.Category;
import com.atlassian.gadgets.directory.Directory;
import com.atlassian.gadgets.spec.Feature;
import com.atlassian.gadgets.spec.GadgetSpec;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.defaultString;

class GadgetSpecDirectoryEntry implements Directory.Entry
{
    private final static String LINEBREAK_PATTERN_STRING = "\r\n|\r|\n";

    private final GadgetSpec spec;
    private final boolean isDeletable;
    private final URI selfUri;

    public GadgetSpecDirectoryEntry(GadgetSpec spec, boolean isDeleteable, URI selfUri)
    {
        this.spec = checkNotNull(spec, "spec");
        this.isDeletable = isDeleteable;
        this.selfUri = selfUri;
    }

    public URI getSelf()
    {
        return selfUri;
    }

    public boolean isDeletable()
    {
        return isDeletable;
    }

    public URI getGadgetSpecUri()
    {
        return checkNotNull(spec.getUrl(), "spec url");
    }

    public String getTitle()
    {
        return defaultString(spec.getDirectoryTitle(),
            defaultString(spec.getTitle()));
    }

    public URI getTitleUri()
    {
        return spec.getTitleUrl();
    }

    public URI getThumbnailUri()
    {
        return spec.getThumbnail();
    }

    public String getAuthorName()
    {
        return defaultString(spec.getAuthor());
    }

    public String getAuthorEmail()
    {
        return defaultString(spec.getAuthorEmail());
    }

    public String getDescription()
    {
        return defaultString(spec.getDescription());
    }

    public Set<Category> getCategories()
    {
        Set<Category> categorySetForGadget = new HashSet<Category>();
        Feature gadgetDirectoryFeature = spec.getFeatures().get(Directory.DIRECTORY_FEATURE_NAME);
        if (gadgetDirectoryFeature != null)
        {
            String categoriesString = gadgetDirectoryFeature.getParameterValue(
                Directory.DIRECTORY_FEATURE_CATEGORIES_NAME);
            if (categoriesString != null)
            {
                // split the big string into categories -- one per line
                String[] categories = categoriesString.split(LINEBREAK_PATTERN_STRING);

                for (String categoryString : categories)
                {
                    // if this is an actual named category, add it to the set for this gadget
                    Category category = Category.named(categoryString.trim());
                    if (!category.equals(Category.OTHER))
                    {
                        categorySetForGadget.add(category);
                    }
                }
            }
        }

        if (categorySetForGadget.isEmpty())
        {
            categorySetForGadget.add(Category.OTHER);
        }

        return categorySetForGadget;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        GadgetSpecDirectoryEntry that = (GadgetSpecDirectoryEntry) o;

        return isDeletable == that.isDeletable &&
            !(selfUri != null ? !selfUri.equals(that.selfUri) : that.selfUri != null) &&
            spec.equals(that.spec);

    }

    @Override
    public int hashCode()
    {
        int result = spec.hashCode();
        result = 31 * result + (isDeletable ? 1 : 0);
        result = 31 * result + (selfUri != null ? selfUri.hashCode() : 0);
        return result;
    }

    @Override public String toString()
    {
        return "GadgetSpecDirectoryEntry{" +
            "spec=" + spec +
            ", isDeletable=" + isDeletable +
            ", selfUri=" + selfUri +
            '}';
    }
}
