package com.atlassian.renderer.v2.functional;

import com.atlassian.renderer.RendererFactory;
import com.atlassian.renderer.WikiStyleRenderer;

public class FunctionalTestSetup
{
    protected WikiStyleRenderer wikiStyleRenderer;
    public static final String DEFAULT_SPACE_KEY = "SPC";

    public WikiStyleRenderer getWikiStyleRenderer()
    {
        return wikiStyleRenderer;
    }

    protected void setUp() throws Exception
    {
        wikiStyleRenderer = RendererFactory.getV2RendererFacade();//setupRenderer((PageManager) mockPageManager.proxy());
    }

    public String convertWikiToHtml(String wikiText, String spaceKey, String pageTitle)
    {
        final String wikiResult = RendererFactory.getV2RendererFacade().convertWikiToXHtml(RendererFactory.getRenderContext(), wikiText);
        return wikiResult;
    }
}
