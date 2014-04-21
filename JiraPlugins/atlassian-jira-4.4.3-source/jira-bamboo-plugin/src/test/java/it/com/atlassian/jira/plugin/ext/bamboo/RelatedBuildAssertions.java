package it.com.atlassian.jira.plugin.ext.bamboo;

import com.google.common.collect.ImmutableList;

public abstract class RelatedBuildAssertions
{
    public static final RelatedBuildAssertion AA_SLAP_3 = RelatedBuildAssertion.builder()
                           .projectName("Atlassian Anarchy")
                           .projectKey("AA")
                           .planName("Sounds like a plan")
                           .planKey("SLAP")
                           .buildNumber(3)
                           .relatedIssues(ImmutableList.of("ONE-1"))
                           .reason("Updated by testadmin")
                           .totalTests(0)
                           .testsFailed(0)
                           .success(false)
                           .build();

    public static final RelatedBuildAssertion AA_SLAP_4 = RelatedBuildAssertion.builder()
                           .projectName("Atlassian Anarchy")
                           .projectKey("AA")
                           .planName("Sounds like a plan")
                           .planKey("SLAP")
                           .buildNumber(4)
                           .relatedIssues(ImmutableList.of("ONE-1"))
                           .reason("Updated by testadmin")
                           .totalTests(0)
                           .testsFailed(0)
                           .success(true)
                           .build();

    public static final RelatedBuildAssertion AA_SLAP_57 = RelatedBuildAssertion.builder()
                           .projectName("Atlassian Anarchy")
                           .projectKey("AA")
                           .planName("Sounds like a plan")
                           .planKey("SLAP")
                           .buildNumber(57)
                           .relatedIssues(ImmutableList.of("THREE-26"))
                           .reason("Updated by testadmin")
                           .totalTests(0)
                           .testsFailed(0)
                           .success(true)
                           .build();
}
