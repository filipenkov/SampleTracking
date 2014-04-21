package com.atlassian.aui.javascript;

import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.CharSequenceDownloadableResource;

/**
 * A transforming downloadable resources that performs the given search-and-replace.
 *
 * @since 3.5
 */
public class SearchAndReplaceDownloadableResource extends CharSequenceDownloadableResource {

    private final SearchAndReplacer grep;

    public SearchAndReplaceDownloadableResource(DownloadableResource originalResource, SearchAndReplacer grep) {
        super(originalResource);
        this.grep = grep;
    }

    @Override
    public CharSequence transform(CharSequence originalContent) {
        return grep.replaceAll(originalContent);
    }
}
