package com.atlassian.streams.api;


public interface FeedContentSanitizer
{
    /**
     * Takes the input string, which may be tainted with malicious HTML tags, and sanitizes it, returning the safe
     * version of the original string.
     * 
     * @param taintedInput The suspicious input string from an external system that needs to be sanitized.
     * @return the sanitized version of the string
     */
    String sanitize(String taintedInput);

}