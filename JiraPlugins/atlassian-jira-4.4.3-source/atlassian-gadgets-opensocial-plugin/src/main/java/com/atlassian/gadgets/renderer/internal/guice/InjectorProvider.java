package com.atlassian.gadgets.renderer.internal.guice;

import com.atlassian.gadgets.opensocial.internal.guice.OpenSocialModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Stage;

/**
 * A {@link Provider} that creates and returns the Guice {@link Injector}
 */
public class InjectorProvider implements Provider<Injector>
{
    private final Injector injector;
    
    public InjectorProvider(SalModule salModule, ShindigModule shindigModule, AuthModule authModule, OpenSocialModule socialModule)
    {
        injector = Guice.createInjector(Stage.PRODUCTION,
            salModule,
            shindigModule,
            authModule,
            socialModule
        );
    }

    public Injector get()
    {
        return injector;
    }
}
