package com.atlassian.gadgets.directory.internal.jaxb;

import java.util.Collection;
import java.util.EnumSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.directory.Category;
import com.atlassian.gadgets.directory.Directory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Models the contents of the directory.
 * TODO: AG-428 We should add LINK elements to this representation, as the REST Guidleines recommend.
 */
@XmlRootElement
public class JAXBDirectoryContents
{
    @SuppressWarnings({"UnusedDeclaration", "unused"})
    @XmlElement
    private final Collection<JAXBCategory> categories;
    @SuppressWarnings({"UnusedDeclaration", "unused"})
    @XmlElement(name = "gadgets")
    private final Collection<JAXBDirectoryEntry> entries;

    // Provided for JAXB.
    @SuppressWarnings("UnusedDeclaration")
    private JAXBDirectoryContents()
    {
        categories = null;
        entries = null;
    }

    private JAXBDirectoryContents(Iterable<JAXBCategory> categories, Iterable<JAXBDirectoryEntry> entries)
    {
        this.categories = ImmutableList.copyOf(categories);
        this.entries = ImmutableList.copyOf(entries);
    }

    public static JAXBDirectoryContents getDirectoryContents(Directory directory, GadgetRequestContext gadgetRequestContext)
    {
        final Iterable<JAXBCategory> categories = Iterables.transform(
                EnumSet.allOf(Category.class),
                CategoryToJAXBCategory.FUNCTION);

        final Iterable<JAXBDirectoryEntry> entries = Iterables.transform(
                directory.getEntries(gadgetRequestContext),
                DirectoryEntryToJAXBDirectoryEntry.FUNCTION);

        return new JAXBDirectoryContents(categories, entries);
    }

    private static enum CategoryToJAXBCategory implements Function<Category, JAXBCategory>
    {
        FUNCTION;

        public JAXBCategory apply(Category category)
        {
            return new JAXBCategory(category);
        }
    }

    private static enum DirectoryEntryToJAXBDirectoryEntry implements Function<Directory.Entry, JAXBDirectoryEntry>
    {
        FUNCTION;

        public JAXBDirectoryEntry apply(Directory.Entry entry)
        {
            return new JAXBDirectoryEntry(entry);
        }
    }
}
