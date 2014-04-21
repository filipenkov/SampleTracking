package com.atlassian.crowd.model.application;

/**
 * Constants for attributes of an application.
 *
 * @since v2.1
 */
public class ApplicationAttributeConstants
{
    /**
     * Key of attribute indicating whether the application password has been encoded with the Atlassian SHA1 algorithm.
     */
    public static final String ATTRIBUTE_KEY_ATLASSIAN_SHA1_APPLIED = "atlassian_sha1_applied";

    /**
     * Key of attribute storing the value of the Application URL.
     */
    public static final String ATTRIBUTE_KEY_APPLICATION_URL = "applicationURL";

    /**
     * Key of attribute indicating whether the user and group names returned should be in lowercase.
     */
    public static final String ATTRIBUTE_KEY_LOWER_CASE_OUTPUT = "lowerCaseOutput";

    /**
     * Key of attribute indicating whether aliasing is enabled for the application.
     */
    public static final String ATTRIBUTE_KEY_ALIASING_ENABLED = "aliasingEnabled";

    private ApplicationAttributeConstants() {} // prevent instantiation
}
