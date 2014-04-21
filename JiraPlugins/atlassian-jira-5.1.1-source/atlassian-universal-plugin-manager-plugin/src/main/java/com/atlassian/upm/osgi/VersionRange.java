package com.atlassian.upm.osgi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.upm.Interval;
import com.atlassian.upm.osgi.impl.Versions;

import static com.atlassian.upm.Interval.Bound.Type.INCLUSIVE;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A version range as specified in OSGi R4, 3.2.6.
 */
public final class VersionRange extends Interval<Version>
{
    private static final Pattern rangePattern =
        Pattern.compile("([\\[\\(])([^,]++),([^\\]\\)]++)([\\]\\)])");

    public VersionRange(Floor<Version> floor, Ceiling<Version> ceiling)
    {
        super(checkNotNull(floor, "floor"), ceiling);
    }

    /**
     * Parse a version range string
     *
     * @param range string to parse as a version range
     * @return a new object representing the parsed version range
     */
    public static VersionRange fromString(String range)
    {
        Matcher rangeMatcher = rangePattern.matcher(range);
        return rangeMatcher.matches() ?
            new VersionRange(
                new Floor<Version>(
                    Versions.fromString(rangeMatcher.group(2).trim()),
                    Floor.getType(rangeMatcher.group(1).charAt(0))),
                new Ceiling<Version>(
                    Versions.fromString(rangeMatcher.group(3).trim()),
                    Ceiling.getType(rangeMatcher.group(4).charAt(0)))) :
            atLeast(Versions.fromString(range));
    }

    /**
     * Construct a version range containing all versions greater than
     * or equal to the given version
     *
     * @param version the floor of the version range
     * @return a new version range
     */
    public static VersionRange atLeast(Version version)
    {
        return new VersionRange(new Floor<Version>(version, INCLUSIVE), null);
    }

    /**
     * Construct a version range containing only the given version
     *
     * @param version the only version the returned version range should contain
     * @return a new version range
     */
    public static VersionRange exactly(Version version)
    {
        return new VersionRange(new Floor<Version>(version, INCLUSIVE), new Ceiling<Version>(version, INCLUSIVE));
    }

    public String toString()
    {
        Floor<Version> floor = getFloor();
        Ceiling<Version> ceiling = getCeiling();
        return ceiling == null ?
            floor.getValue().toString() :
            String.format("%s,%s", floor, ceiling);
    }
}
