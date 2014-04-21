package com.atlassian.soy.impl;

import com.atlassian.soy.renderer.SoyException;
import com.google.inject.Module;
import com.google.template.soy.SoyFileSet;

import java.util.Map;

public interface SoyManager
{
    /**
     * Make a SoyFileSet builder, optionally loading soy functions from the provided modules. Modules may be
     * soy-resources modules (loading functions directly) or web-resources modules (loading any functions referenced
     * by one of the modules dependencies)
     *
     * @param functionModuleKeys the plugin keys of any module that can provide soy functions
     * @return a new Soy builder instance for that module
     */
    public SoyFileSet.Builder makeBuilder(String... functionModuleKeys);

    /**
     * @param additionalModules additional modules to load in the guice container
     * @return a new Soy builder instance
     */
    SoyFileSet.Builder makeBuilder(Module... additionalModules);

    /**
     *
     * @param appendable the appendable to write to
     * @param completeModuleKey - a complete plugin module key
     * @param templateName - a namespaced Soy template name
     * @param data - a map of data to render the template with
     * @param injectedData - a map of injected data to render the template with
     *
     * @throws SoyException when an error occurs in rendering the template
     *
     */
    void render(Appendable appendable, String completeModuleKey, String templateName,
                Map<String, Object> data, Map<String, Object> injectedData) throws SoyException;
}
