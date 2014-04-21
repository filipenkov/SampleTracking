package com.atlassian.plugins.rest.doclet.generators;

import com.atlassian.plugins.rest.doclet.generators.resourcedoc.AtlassianWadlGeneratorResourceDocSupport;
import com.sun.jersey.api.wadl.config.WadlGeneratorConfig;
import com.sun.jersey.api.wadl.config.WadlGeneratorDescription;
import com.sun.jersey.server.wadl.generators.WadlGeneratorApplicationDoc;
import com.sun.jersey.server.wadl.generators.WadlGeneratorGrammarsSupport;

import java.util.List;

/**
 * @since 2.5.1
 */
public class AtlassianWadlGeneratorConfig extends WadlGeneratorConfig
{
    public static final String APPLICATION_XML = "application-doc.xml";
    public static final String GRAMMARS_XML = "application-grammars.xml";
    public static final String RESOURCE_XML = "resourcedoc.xml";

    @Override
    public List<WadlGeneratorDescription> configure()
    {
        return generator( WadlGeneratorApplicationDoc.class )
            .prop( "applicationDocsStream", APPLICATION_XML)
        .generator( WadlGeneratorGrammarsSupport.class )
            .prop( "grammarsStream", GRAMMARS_XML)
        .generator( AtlassianWadlGeneratorResourceDocSupport.class )
            .prop( "resourceDocStream", RESOURCE_XML)
        .descriptions();
    }
}
