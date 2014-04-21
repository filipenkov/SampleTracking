package com.atlassian.gadgets.opensocial.internal.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import org.apache.shindig.common.servlet.ParameterFetcher;
import org.apache.shindig.social.core.util.BeanJsonConverter;
import org.apache.shindig.social.core.util.BeanXStreamAtomConverter;
import org.apache.shindig.social.core.util.BeanXStreamConverter;
import org.apache.shindig.social.core.util.ContainerConf;
import org.apache.shindig.social.core.util.JsonContainerConf;
import org.apache.shindig.social.opensocial.service.ActivityHandler;
import org.apache.shindig.social.opensocial.service.AppDataHandler;
import org.apache.shindig.social.opensocial.service.BeanConverter;
import org.apache.shindig.social.opensocial.service.DataServiceServletFetcher;
import org.apache.shindig.social.opensocial.service.PersonHandler;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.PersonService;

/**
 * Guice module configuration file
 */
public class OpenSocialModule extends AbstractModule
{
    private final PersonService shindigPersonService;
    private final ActivityService shindigActivityService;
    private final AppDataService shindigAppDataService;

    public OpenSocialModule(PersonService shindigPersonService,
            ActivityService shindigActivityService, 
            AppDataService shindigAppDataService)
    {
        this.shindigPersonService = shindigPersonService;
        this.shindigActivityService = shindigActivityService;
        this.shindigAppDataService = shindigAppDataService;
    }
    @Override
    protected void configure()
    {
        bind(PersonService.class).toInstance(shindigPersonService);
        bind(ActivityService.class).toInstance(shindigActivityService);
        bind(AppDataService.class).toInstance(shindigAppDataService);
        bind(ParameterFetcher.class).annotatedWith(Names.named("DataServiceServlet")).to(
                                        DataServiceServletFetcher.class);
        bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.xml")).to(
                                        BeanXStreamConverter.class);
        bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.json")).to(
                                        BeanJsonConverter.class);
        bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.atom")).to(
                                        BeanXStreamAtomConverter.class);
        bind(PersonHandler.class);
        bind(ActivityHandler.class);
        bind(AppDataHandler.class);
        bind(ContainerConf.class).to(JsonContainerConf.class);
    }

}
