
package com.atlassian.jira.configurator.config;

/**
 * Cleans or validates a string input value for a field.
 *
 * @since v5.1
 */
public abstract class Validator<T>
{
    public abstract T apply(String label, String input) throws ValidationException;

    static Boolean parseBoolean(String label, String input) throws ValidationException
    {
        input = trim(input);
        if (input == null)
        {
            return null;
        }
        input = input.toLowerCase();
        switch (input.charAt(0))
        {
            case '1': case 'y': case 't': return Boolean.TRUE;
            case '0': case 'n': case 'f': return Boolean.FALSE;
        }
        throw new ValidationException(label, "Please use a boolean value like 'true' or 'false'");
    }

    static Integer parseInteger(String label, String input) throws ValidationException
    {
        return parseInteger(label, input, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    static Integer parseInteger(String label, String input, int minValue, int maxValue) throws ValidationException
    {
        final Long value = parseLong(label, input);
        if (value == null)
        {
            return null;
        }
        if (value < minValue || value > maxValue)
        {
            throw new ValidationException(label, "The value " + value + " is outside of the accepted range [" + minValue + ',' + maxValue + ']');
        }
        return value.intValue();
    }

    static Long parseLong(String label, String input) throws ValidationException
    {
        input = trim(input);
        if (input == null)
        {
            return null;
        }
        try
        {
            return Long.valueOf(input);
        }
        catch (NumberFormatException nfe)
        {
            throw new ValidationException(label, "An integer value is required");
        }
    }

    static String trim(String input)
    {
        if (input != null)
        {
            input = input.trim();
            if (input.length() == 0)
            {
                return null;
            }
        }
        return input;
    }

    public static final Validator<Integer> INTEGER = new Validator<Integer>()
    {
        @Override
        public Integer apply(String label, String input) throws ValidationException
        {
            return parseInteger(label, input);
        }
    };

    public static final Validator<Integer> INTEGER_POSITIVE = new Validator<Integer>()
    {
        @Override
        public Integer apply(String label, String input) throws ValidationException
        {
            final Integer value = parseInteger(label, input);
            if (value != null && value <= 0)
            {
                throw new ValidationException(label, "Only positive values are allowed");
            }
            return value;
        }
    };

    public static final Validator<Integer> INTEGER_POSITIVE_OR_ZERO = new Validator<Integer>()
    {
        @Override
        public Integer apply(String label, String input) throws ValidationException
        {
            final Integer value = parseInteger(label, input);
            if (value != null && value < 0)
            {
                throw new ValidationException(label, "Negative values are not allowed");
            }
            return value;
        }
    };

    public static final Validator<Integer> INTEGER_ALLOW_MINUS_1 = new Validator<Integer>()
    {
        @Override
        public Integer apply(String label, String input) throws ValidationException
        {
            final Integer value = parseInteger(label, input);
            if (value != null && value < -1)
            {
                throw new ValidationException(label, "Negative one (-1) is the only negative value that is allowed");
            }
            return value;
        }
    };

    public static final Validator<Long> LONG = new Validator<Long>()
    {
        @Override
        public Long apply(String label, String input) throws ValidationException
        {
            return parseLong(label, input);
        }
    };

    public static final Validator<Long> LONG_POSITIVE = new Validator<Long>()
    {
        @Override
        public Long apply(String label, String input) throws ValidationException
        {
            final Long value = parseLong(label, input);
            if (value != null && value <= 0L)
            {
                throw new ValidationException(label, "Only positive values are allowed");
            }
            return value;
        }
    };

    public static final Validator<Long> LONG_POSITIVE_OR_ZERO = new Validator<Long>()
    {
        @Override
        public Long apply(String label, String input) throws ValidationException
        {
            final Long value = parseLong(label, input);
            if (value != null && value < 0L)
            {
                throw new ValidationException(label, "Negative values are not allowed");
            }
            return value;
        }
    };

    public static final Validator<Long> LONG_ALLOW_MINUS_1 = new Validator<Long>()
    {
        @Override
        public Long apply(String label, String input) throws ValidationException
        {
            final Long value = parseLong(label, input);
            if (value != null && value < -1L)
            {
                throw new ValidationException(label, "Negative one (-1) is the only negative value that is allowed");
            }
            return value;
        }
    };

    public static final Validator<Boolean> BOOLEAN = new Validator<Boolean>()
    {
        @Override
        public Boolean apply(String label, String input) throws ValidationException
        {
            return parseBoolean(label, input);
        }
    };

    public static final Validator<String> TRIMMED_STRING = new Validator<String>()
    {
        @Override
        public String apply(String label, String input)
        {
            return trim(input);
        }
    };
}

