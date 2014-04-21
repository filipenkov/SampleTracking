package com.atlassian.jira.plugins.importer.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import org.junit.Ignore;

import javax.annotation.Nonnull;

public class SampleDataBackdoorControl extends JimBackdoorControl<SampleDataBackdoorControl> {

    public SampleDataBackdoorControl(JIRAEnvironmentData environmentData) {
        super(environmentData);
    }

    public void importSampleData(@Nonnull String json) {
        post(createResource().path("sampleData").path("create"), json, String.class);
    }

}
