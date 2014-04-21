package com.atlassian.applinks.core.util;

import com.atlassian.velocity.htmlsafe.HtmlSafe;

/**
 * Used to include strings that must not be escaped into templates.
 *
 * @since   3.0
 */
public interface HtmlSafeContent
{
    @HtmlSafe
    CharSequence get();
}
