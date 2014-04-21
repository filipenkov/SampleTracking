package com.atlassian.gadgets.directory.internal;

import javax.annotation.Nullable;

import com.atlassian.gadgets.directory.Category;
import com.atlassian.gadgets.directory.Directory;

import com.google.common.base.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates predicates that can be used to filter a group of directory entries so that only entries matching the predicate can be returned
 */
final class DirectoryPredicates
{
    private DirectoryPredicates() {}
    
    static Predicate<Directory.Entry> inCategory(final Category categoryToMatch)
    {
        return new InCategoryPredicate(checkNotNull(categoryToMatch));
    }

    /**
     * Predicate that returns true when applied to a directory entry in the specified categoryToMatch
     */
    private static final class InCategoryPredicate implements Predicate<Directory.Entry>
    {
        private final Category categoryToMatch;

        public InCategoryPredicate(Category categoryToMatch)
        {
            this.categoryToMatch = categoryToMatch;
        }

        public boolean apply(@Nullable Directory.Entry entry)
        {
            return entry != null && entry.getCategories().contains(categoryToMatch);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }

            if ( !(obj instanceof InCategoryPredicate) )
            {
                return false;
            }

            InCategoryPredicate predicateObj = (InCategoryPredicate) obj;
            
            if(categoryToMatch != null)
            {
                return categoryToMatch.equals(predicateObj.categoryToMatch);
            }

            // this.categoryToMatch is null
            return predicateObj.categoryToMatch == null;
        }

        @Override
        public int hashCode()
        {
            return categoryToMatch.hashCode();
        }

        @Override
        public String toString()
        {
            return "inCategory(" + categoryToMatch + ")";
        }
    }
}
