package com.atlassian.jira.plugins.importer.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

import javax.annotation.Nonnull;

public class JimBackdoor {

    private final CreateProjectHandlerBackdoorControl createProjectHandlerBackdoorControl;
    private final SampleDataBackdoorControl sampleDataBackdoorControl;
    private final JimProjectControl jimProjectControl;

    public JimBackdoor(@Nonnull JIRAEnvironmentData environmentData) {
        this.createProjectHandlerBackdoorControl = new CreateProjectHandlerBackdoorControl(environmentData);
        this.sampleDataBackdoorControl = new SampleDataBackdoorControl(environmentData);
        this.jimProjectControl = new JimProjectControl(environmentData);
    }

    @Nonnull
    public CreateProjectHandlerBackdoorControl createProjectHandler() {
        return createProjectHandlerBackdoorControl;
    }

    @Nonnull
    public SampleDataBackdoorControl sampleData() {
        return sampleDataBackdoorControl;
    }

    @Nonnull
    public JimProjectControl project() {
        return jimProjectControl;
    }
}
