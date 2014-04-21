package com.atlassian.crowd.plugin.descriptors;

import com.atlassian.crowd.password.encoder.PasswordEncoder;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.plugin.StateAware;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Module descriptor that handles creating PasswordEncoder plugins
 * NOTE: This class has to be constructor injected since it's the only way moduleFactory can be set at its parent.
 */
public class PasswordEncoderModuleDescriptor<T extends PasswordEncoder> extends AbstractModuleDescriptor<T> implements StateAware
{
    private static final Logger log = Logger.getLogger(PasswordEncoderModuleDescriptor.class);

    private final PasswordEncoderFactory passwordEncoderFactory;


    public PasswordEncoderModuleDescriptor(PasswordEncoderFactory passwordEncoderFactory, ModuleFactory moduleFactory)
    {
        super(moduleFactory);

        Validate.notNull(passwordEncoderFactory);
        this.passwordEncoderFactory = passwordEncoderFactory;
    }

    public T getModule()
    {
        return moduleFactory.createModule(moduleClassName, this);
    }

    public void enabled()
    {
        super.enabled();

        PasswordEncoder passwordEncoder = getModule();

        if (passwordEncoder instanceof StateAware)
        {
            StateAware stateAware = (StateAware) passwordEncoder;
            stateAware.enabled();
        }

        passwordEncoderFactory.addEncoder(passwordEncoder);
    }

    public void disabled()
    {
        passwordEncoderFactory.removeEncoder(getModule());

        super.disabled();
    }
}
