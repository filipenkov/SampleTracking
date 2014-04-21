package com.atlassian.jira.plugin.ext.bamboo.model;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class PlanKeys
{
    private static final Logger log = Logger.getLogger(PlanKeys.class);
    // ------------------------------------------------------------------------------------------------------- Constants

    public static final char SEP = '-';

    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors

    private PlanKeys()
    {
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods

    /**
     * Creates a {@link PlanKey} from its string form 'BAM-BOO' or from the result key form 'BAM-BOO-123'
     *
     * @param planKey
     * @throws IllegalArgumentException if key could not be parsed
     * @return key
     */
    public static PlanKey getPlanKey(String planKey)
    {
        String[] parts = StringUtils.split(planKey, SEP);

        if (parts == null || parts.length > 4 || parts.length < 2)
        {
            throw new IllegalArgumentException("Could not parse key '" + planKey + "'");
        }
        else if (parts.length >= 3)
        {
            //check part 3 it could be part of the plan or it could be a number
            try
            {
                Integer.parseInt(parts[2]);
            }
            catch(NumberFormatException e)
            {
                // yep looks like we have a three part key
                return new PlanKey(parts[0] + SEP + parts[1] + SEP + parts[2]);
            }
        }
        return new PlanKey(parts[0] + SEP + parts[1]);
    }

    /**
     * Creates a {@link PlanResultKey} from its string form 'BAM-BOO-123'
     * @param planResultKey
     * @throws IllegalArgumentException if key could not be parsed
     * @return key
     */
    public static PlanResultKey getPlanResultKey(String planResultKey)
    {
        return parse(getPlanKey(planResultKey), planResultKey);
    }

    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    private static PlanResultKey parse(PlanKey planKey, String key)
    {
        String[] parts = StringUtils.split(key, SEP);
        String numberPart;
        if (parts.length == 3)
        {
            numberPart = parts[2];
        }
        else if (parts.length == 4)
        {
            numberPart = parts[3];
        }
        else
        {
            throw new IllegalArgumentException("Could not parse key '" + key + "'");
        }

        Integer buildNumber;

        try
        {
            buildNumber = Integer.parseInt(numberPart);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Could not parse build number part of key '" + key + "'");
        }

        return new PlanResultKey(planKey, buildNumber);
    }
}
