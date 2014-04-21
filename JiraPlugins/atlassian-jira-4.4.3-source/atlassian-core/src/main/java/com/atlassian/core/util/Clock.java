package com.atlassian.core.util;

import java.util.Date;

/**
 * Convenient way to allow test classes to tell another class to use a different
 * idea of what the time is - allows much easier testing of time-based functions.
 */
public interface Clock
{
    Date getCurrentDate();
}
