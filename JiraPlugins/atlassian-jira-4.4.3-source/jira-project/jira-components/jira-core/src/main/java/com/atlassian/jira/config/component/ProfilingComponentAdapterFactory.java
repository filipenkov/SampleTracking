package com.atlassian.jira.config.component;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.defaults.AssignabilityRegistrationException;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapter;
import org.picocontainer.defaults.NotConcreteRegistrationException;
import org.picocontainer.defaults.SynchronizedComponentAdapter;

public class ProfilingComponentAdapterFactory implements ComponentAdapterFactory
{
    public ComponentAdapter createComponentAdapter(Object componentKey, Class componentImplementation, Parameter[] parameters) throws PicoIntrospectionException, AssignabilityRegistrationException, NotConcreteRegistrationException
    {
        //this *must* be synchronised - pico by default is not Threadsafe.  See http://jira.codehaus.org/secure/ViewIssue.jspa?key=PICO-46
        return new ProfilingComponentAdapter(new SynchronizedComponentAdapter(new ConstructorInjectionComponentAdapter(componentKey, componentImplementation, parameters)));
    }
}