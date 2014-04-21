package com.atlassian.renderer;

import com.atlassian.renderer.embedded.EmbeddedImage;
import com.atlassian.renderer.embedded.EmbeddedResource;
import com.atlassian.renderer.embedded.EmbeddedResourceRenderer;
import com.atlassian.renderer.links.LinkResolver;
import com.atlassian.renderer.util.UrlUtil;
import com.atlassian.renderer.v2.*;
import com.atlassian.renderer.v2.components.*;
import com.atlassian.renderer.v2.components.block.*;
import com.atlassian.renderer.v2.components.list.ListBlockRenderer;
import com.atlassian.renderer.v2.components.phrase.*;
import com.atlassian.renderer.v2.components.table.TableBlockRenderer;
import com.atlassian.renderer.v2.macro.DefaultMacroManager;
import com.atlassian.renderer.wysiwyg.DefaultWysiwygConverter;
import com.mockobjects.dynamic.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is just a utility class that will generate a V2RendererFacade who's object graph
 * has been filled out so that the entire renderer will work.
 */
public class RendererFactory
{
    private static DefaultMacroManager macroManager;
    public static DefaultWysiwygConverter getDefaultWysiwygConverter()
    {
        V2Renderer renderer = new V2Renderer();
        V2SubRenderer subRenderer = new V2SubRenderer();
        DefaultIconManager iconManager = new DefaultIconManager();
        subRenderer.setRenderer(renderer);

        V2RendererFacade rendererFacade = getV2RendererFacade(renderer, subRenderer, iconManager);
        DefaultWysiwygConverter converter = new DefaultWysiwygConverter();
        converter.setIconManager(iconManager);
        if (macroManager == null)
        {
            macroManager = createDummyMacroManager(subRenderer);
        }
        converter.setMacroManager(macroManager);
        converter.setWikiStyleRenderer(rendererFacade);
        return converter;
    }

    public static RenderContext getRenderContext()
    {
        RenderContext context = new RenderContext();
        context.setImagePath("http://localhost:8080/images");
        context.setSiteRoot("http://localhost:8080");
        context.setAttachmentsPath("http://localhost:8080/download/attachments/0");
        return context;
    }

    public static V2RendererFacade getV2RendererFacade()
    {
        V2Renderer renderer = new V2Renderer();
        V2SubRenderer subRenderer = new V2SubRenderer();
        DefaultIconManager defaultIconManager = new DefaultIconManager();
        return getV2RendererFacade(renderer, subRenderer, defaultIconManager);
    }

    private static V2RendererFacade getV2RendererFacade(V2Renderer renderer, V2SubRenderer subRenderer, DefaultIconManager defaultIconManager)
    {
        Mock mockRendererConfiguration = new Mock(RendererConfiguration.class);
        mockRendererConfiguration.matchAndReturn("getWebAppContextPath", "http://test.example.com:8081");
        mockRendererConfiguration.matchAndReturn("getCharacterEncoding", "UTF-8");
        mockRendererConfiguration.matchAndReturn("isAllowCamelCase", false);
        mockRendererConfiguration.matchAndReturn("isNofollowExternalLinks", false);

        V2RendererFacade rendererFacade = new V2RendererFacade();
        subRenderer.setRenderer(renderer);

        V2LinkRenderer linkRenderer = new V2LinkRenderer(subRenderer, defaultIconManager, (RendererConfiguration) mockRendererConfiguration.proxy());

        LinkResolver linkResolver = getLinkResolver();

        rendererFacade.setDefaultLinkRenderer(linkRenderer);
        rendererFacade.setRendererConfiguration((RendererConfiguration) mockRendererConfiguration.proxy());
        rendererFacade.setRenderer(renderer);
        rendererFacade.setDefaultEmbeddedRenderer(getTestEmbeddedResourceRenderer());

        setupComponents(renderer, subRenderer, linkResolver, (RendererConfiguration) mockRendererConfiguration.proxy(), defaultIconManager);

        return rendererFacade;
    }

    private static LinkResolver getLinkResolver()
    {
        return new SimpleLinkResolver();
    }

    private static EmbeddedResourceRenderer getTestEmbeddedResourceRenderer()
    {
        return new EmbeddedResourceRenderer() {
            public String renderResource(EmbeddedResource resource, RenderContext context)
            {
                // At the moment, this resource renderer only supports the EmbeddedImage type.
                String token;
                if (!(resource instanceof EmbeddedImage))
                {
                    token = context.addRenderedContent(RenderUtils.error(context, "Unsupported embedded type '"+resource.getType()+"'", originalLink(resource), false));
                    return token;
                }

                EmbeddedImage image = (EmbeddedImage) resource;

                Map imageParams = new HashMap();
                imageParams.putAll(image.getProperties());
                if (context.isRenderingForWysiwyg())
                {
                    imageParams.put("imagetext", resource.getOriginalLinkText().replaceAll("\"",""));
                }
                String imageUrl = "";

                if (image.isExternal())
                    imageUrl = UrlUtil.escapeSpecialCharacters(image.getUrl());
                else
                {
                    String attachmentsPath = context.getAttachmentsPath();
                    if (attachmentsPath != null)
                        imageUrl = UrlUtil.escapeSpecialCharacters(attachmentsPath) + "/";

                    imageUrl += UrlUtil.escapeSpecialCharacters(image.getFilename());
                }

                boolean centered = extractCenteredParam(imageParams);

                token = context.addRenderedContent(writeImage("<img src=\"" + imageUrl + "\" " + outputParameters(imageParams) + "/>", centered));

                return token;
            }

            private String originalLink(EmbeddedResource resource)
            {
                return "!" + resource.getOriginalLinkText() + "!";
            }


            private boolean extractCenteredParam(Map imageParams)
            {
                boolean centered = "center".equalsIgnoreCase((String) imageParams.get("align")) || "centre".equalsIgnoreCase((String) imageParams.get("align"));
                if (centered)
                    imageParams.remove("align");
                return centered;
            }

            private String outputParameters(Map params)
            {
                StringBuffer buff = new StringBuffer(20);

                for (Iterator iterator = params.keySet().iterator(); iterator.hasNext();)
                {
                    String key = (String) iterator.next();
                    buff.append(key + "=\"" + params.get(key) + "\" ");
                }

                return buff.toString();
            }

            private String writeImage(String imageTag, boolean centered)
            {
                String result = "";

                if (centered)
                    result += "<div align=\"center\">";

                result += imageTag;

                if (centered)
                    result += "</div>";

                return result;
            }
        };
    }

    private static void setupComponents(V2Renderer renderer, V2SubRenderer subRenderer, LinkResolver linkResolver, RendererConfiguration rendererConfiguration, DefaultIconManager defaultIconManager)
    {
        ArrayList components = new ArrayList();
        components.add(new MacroRendererComponent(createDummyMacroManager(subRenderer), subRenderer));

        ArrayList blockRenderers = new ArrayList();
        blockRenderers.add(new BlankLineBlockRenderer());
        blockRenderers.add(new HeadingBlockRenderer());
        blockRenderers.add(new BlockquoteBlockRenderer());
        blockRenderers.add(new HorizontalRuleBlockRenderer());
        blockRenderers.add(new ListBlockRenderer());
        blockRenderers.add(new TableBlockRenderer());
        BlockRendererComponent blockRenderer = new BlockRendererComponent(subRenderer, blockRenderers);
        components.add(blockRenderer);
        components.add(new LinkRendererComponent(linkResolver));
        components.add(new UrlRendererComponent(linkResolver));
        components.add(new CamelCaseLinkRendererComponent(linkResolver, rendererConfiguration));
        components.add(new BackslashEscapeRendererComponent());
        components.add(new HtmlEscapeRendererComponent());
        components.add(new TemplateParamRenderComponent());
        components.add(new DashRendererComponent());
        components.add(new EmoticonRendererComponent(defaultIconManager));
        components.add(new EmbeddedRendererComponent());
        components.add(PhraseRendererComponent.getDefaultRenderer("strong"));
        components.add(PhraseRendererComponent.getDefaultRenderer("superscript"));
        components.add(PhraseRendererComponent.getDefaultRenderer("subscript"));
        components.add(PhraseRendererComponent.getDefaultRenderer("emphasis"));
        components.add(PhraseRendererComponent.getDefaultRenderer("deleted"));
        components.add(PhraseRendererComponent.getDefaultRenderer("citation"));
        components.add(PhraseRendererComponent.getDefaultRenderer("inserted"));
        components.add(PhraseRendererComponent.getDefaultRenderer("monospaced"));
        components.add(new ForceNewLineRendererComponent());
        components.add(new NewLineRendererComponent());
        components.add(new TokenRendererComponent(subRenderer));

        renderer.setComponents(components);
    }

    private static DefaultMacroManager createDummyMacroManager(V2SubRenderer subRenderer)
    {
        if (macroManager == null)
        {
            macroManager = new DefaultMacroManager(subRenderer);
        }
        
        // add simple test macro
        macroManager.registerMacro("simple", new SimpleMacro());

        return macroManager;
    }

    public static DefaultMacroManager getMacroManager()
    {
        return macroManager;
    }
}
