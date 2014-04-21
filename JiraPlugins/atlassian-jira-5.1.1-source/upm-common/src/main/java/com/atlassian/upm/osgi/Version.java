package com.atlassian.upm.osgi;

public interface Version extends Comparable<Version>
{
    /**
     * Fetch the major component of the version
     *
     * @return the version major component
     */
    int getMajor();

    /**
     * Fetch the minor component of the version
     *
     * @return the version minor component
     */
    int getMinor();

    /**
     * Fetch the micro component of the version
     *
     * @return the version micro component
     */
    int getMicro();

    /**
     * Fetch the qualifier of the version
     *
     * @return the version qualifier
     */
    String getQualifier();
}
