package com.atlassian.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that program elements that this annotation is applied to is considered usable by external developers but
 * its contracts have not stabilized.
 * <p/>
 * Experimental APIs may be changed at any time before being marked {@link Internal} or {@link PublicApi}
 */
@Documented
@Retention(RetentionPolicy.CLASS)
public @interface ExperimentalApi
{
}
