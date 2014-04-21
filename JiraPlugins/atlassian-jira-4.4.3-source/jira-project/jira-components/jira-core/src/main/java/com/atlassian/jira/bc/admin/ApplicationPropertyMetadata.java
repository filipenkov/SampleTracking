package com.atlassian.jira.bc.admin;

import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.validation.ApplicationPropertyEnumerator;
import com.atlassian.validation.BooleanValidator;
import com.atlassian.validation.IntegerValidator;
import com.atlassian.validation.NonValidator;
import com.atlassian.validation.Validator;
import com.atlassian.validation.ValidatorFactory;
import com.google.common.base.Supplier;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an individual property setting as defined in the jpm.xml file. This implementation models the various
 * metadata about a single JIRA application property and also provides access to some type and validation logic.
 *
 * @since v4.4
 */
public class ApplicationPropertyMetadata
{
    private static final Map<String, Validator> DEFAULT_VALIDATORS_BY_TYPE = new HashMap<String, Validator>();

    static
    {
        DEFAULT_VALIDATORS_BY_TYPE.put("string", new NonValidator());
        DEFAULT_VALIDATORS_BY_TYPE.put("uint", new IntegerValidator(0, Integer.MAX_VALUE));
        DEFAULT_VALIDATORS_BY_TYPE.put("int", new IntegerValidator());
        DEFAULT_VALIDATORS_BY_TYPE.put("boolean", new BooleanValidator());
    }

    private String key;
    private String type;
    private String defaultValue;
    private Supplier<? extends Validator> validator;
    private boolean userEditable;
    private boolean requiresRestart;
    private String name;
    private String desc;
    private ApplicationPropertyEnumerator enumerator;

    private static Supplier<Validator> validatorResolver(final String type)
    {
        return new LazyReference<Validator>()
        {
            @Override
            protected Validator create() throws Exception
            {
                Validator validator = DEFAULT_VALIDATORS_BY_TYPE.get(type);
                if (validator == null)
                {
                    validator = new NonValidator();
                }
                return validator;
            }
        };

    }

    public ApplicationPropertyMetadata(String key, String type, String defaultValue, Supplier<? extends Validator> validatorSupplier, boolean userEditable, boolean requiresRestart)
    {
        this(key, type, defaultValue, validatorSupplier, userEditable, requiresRestart, null, null, null);
    }

    public ApplicationPropertyMetadata(final String key, final String type, final String defaultValue, final String validatorName,
            boolean userEditable, boolean requiresRestart, final String name, final String desc, final ApplicationPropertyEnumerator enumerator)
    {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
        this.userEditable = userEditable;
        this.requiresRestart = requiresRestart;
        this.name = name;
        this.desc = desc;
        this.enumerator = enumerator;

        if (validatorName == null)
        {
            this.validator = validatorResolver(type);
        }
        else
        {
            this.validator = new ValidatorFactory().getInstanceLazyReference(validatorName);
        }
    }

    public ApplicationPropertyMetadata(final String key, final String type, final String defaultValue, final Supplier<? extends Validator> validatorSupplier,
            boolean userEditable, boolean requiresRestart, final String name, final String desc, final ApplicationPropertyEnumerator enumerator)
    {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
        this.userEditable = userEditable;
        this.requiresRestart = requiresRestart;
        this.name = name;
        this.desc = desc;
        this.validator = validatorSupplier;
        this.enumerator = enumerator;
    }

    public String getType()
    {
        return type;
    }

    public String getKey()
    {
        return key;
    }

    public boolean isUserEditable()
    {
        return userEditable;
    }

    /**
     * Whether or not changing the property value requires a restart in order to take effect.
     *
     * @return true only if the property requires a restart.
     */
    public boolean isRequiresRestart()
    {
        return requiresRestart;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return desc;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public Validator getValidator()
    {
        return validator.get();
    }

    public ApplicationPropertyEnumerator getEnumerator()
    {
        if(!"enum".equals(type))
        {
            throw new IllegalStateException("Tried to get enumerator for a non-enum type");
        }
        return enumerator;
    }

    public Validator.Result validate(String value)
    {
        return validator.get().validate(value);
    }
}

