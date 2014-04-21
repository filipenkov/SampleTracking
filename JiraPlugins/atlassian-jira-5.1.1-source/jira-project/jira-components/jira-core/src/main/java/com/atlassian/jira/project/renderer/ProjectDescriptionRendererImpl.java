package com.atlassian.jira.project.renderer;

import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.project.Project;
import com.atlassian.util.concurrent.LazyReference;

import javax.annotation.Nonnull;

/**
 * Renders the project description either as wiki markup or returns the plain value (in case HTML is allowed).
 *
 * @since 5.0.5
 */
public class ProjectDescriptionRendererImpl implements ProjectDescriptionRenderer
{
    private final FeatureManager featureManager;
    private final RendererManager rendererManager;

    private final LazyReference<ProjectDescriptionRenderer> renderer = new LazyReference<ProjectDescriptionRenderer>()
    {
        @Override
        protected ProjectDescriptionRenderer create() throws Exception
        {
            return isUseWikiMarkup() ? new WikiMarkupProjectDescriptionRenderer(rendererManager) : new FullHtmlProjectDescriptionRenderer();
        }
    };

    public ProjectDescriptionRendererImpl(@Nonnull final FeatureManager featureManager, @Nonnull final RendererManager rendererManager)
    {
        this.featureManager = featureManager;
        this.rendererManager = rendererManager;
    }

    @Override
    @Nonnull
    public String getViewHtml(@Nonnull final Project project)
    {
        return renderer().getViewHtml(project);
    }

    @Nonnull
    @Override
    public String getEditHtml(@Nonnull final Project project)
    {
        return renderer().getEditHtml(project);
    }

    @Override
    @Nonnull
    public String getDescriptionI18nKey()
    {
        return renderer().getDescriptionI18nKey();
    }

    @Nonnull
    private ProjectDescriptionRenderer renderer()
    {
        return renderer.get();
    }

    /**
     * @return true if we should use wiki markup in the project description field
     */
    private boolean isUseWikiMarkup()
    {
        return featureManager.isEnabled(CoreFeatures.ON_DEMAND);
    }

}
