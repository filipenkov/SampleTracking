package com.atlassian.jira.timezone;

import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentMap;

/**
 * This class is used to generate cannocial timezone IDs as described here
 * http://joda-time.sourceforge.net/timezones.html
 *
 * @since v4.4
 */
public class TimeZoneIds
{
    static final long cNow = System.currentTimeMillis();
    private static final Logger log = Logger.getLogger(TimeZoneIds.class);

    static final ConcurrentMap<TimeZone, DateTimeZone> dateTimeZoneMap = new MapMaker()
       .makeComputingMap(
               new Function<TimeZone, DateTimeZone>()
               {
                   public DateTimeZone apply(TimeZone timeZone)
                   {
                       return DateTimeZone.forTimeZone(timeZone);
                   }
               });

    public static Set<String> getCanonicalIds()
    {
        Set<String> canonicalIds = new HashSet<String>();
        Set idSet = DateTimeZone.getAvailableIDs();
        ZoneData[] zones = new ZoneData[idSet.size()];

        {
            Iterator it = idSet.iterator();
            int i = 0;
            while (it.hasNext())
            {
                String id = (String) it.next();
                zones[i++] = new ZoneData(id, DateTimeZone.forID(id));
            }
            Arrays.sort(zones);
        }
        for (int i = 0; i < zones.length; i++)
        {
            ZoneData zone = zones[i];

            if (zone.isCanonical())
            {
                canonicalIds.add(zone.getCanonicalID());
            }
        }
        return canonicalIds;
    }

    public static TimeZone ensureCanonicalTimeZone(TimeZone timeZone)
    {
        try
        {
            DateTimeZone dateTimeZone = dateTimeZoneMap.get(timeZone);
            return dateTimeZone.toTimeZone();
        }
        catch (ComputationException ex)
        {
            log.debug("Unrecognised time zone with id '" + timeZone.getID() + "'");
            return timeZone;
        }
    }

    private static class ZoneData implements Comparable
    {
        private final String iID;
        private final DateTimeZone iZone;

        ZoneData(String id, DateTimeZone zone)
        {
            iID = id;
            iZone = zone;
        }

        public String getID()
        {
            return iID;
        }

        public String getCanonicalID()
        {
            return iZone.getID();
        }

        public boolean isCanonical()
        {
            return getID().equals(getCanonicalID());
        }

        public int compareTo(Object obj)
        {
            ZoneData other = (ZoneData) obj;

            int offsetA = iZone.getStandardOffset(cNow);
            int offsetB = other.iZone.getStandardOffset(cNow);

            if (offsetA < offsetB)
            {
                return -1;
            }
            if (offsetA > offsetB)
            {
                return 1;
            }

            int result = getCanonicalID().compareTo(other.getCanonicalID());

            if (result != 0)
            {
                return result;
            }

            if (isCanonical())
            {
                if (!other.isCanonical())
                {
                    return -1;
                }
            }
            else if (other.isCanonical())
            {
                return 1;
            }

            return getID().compareTo(other.getID());
        }
    }

}
