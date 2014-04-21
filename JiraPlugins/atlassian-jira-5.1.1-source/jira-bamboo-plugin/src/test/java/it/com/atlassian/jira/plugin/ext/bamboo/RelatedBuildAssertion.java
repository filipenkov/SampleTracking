package it.com.atlassian.jira.plugin.ext.bamboo;

/**
 * Contains information about a build result visible on the Builds tab.
 */
public class RelatedBuildAssertion
{
    private final String projectName;
    private final String projectKey;
    private final String planName;
    private final String planKey;
    private final int buildNumber;
    private final Iterable<String> relatedIssues;
    private final String reason;
    private final int totalTests;
    private final int testsFailed;
    private final boolean success;

    private RelatedBuildAssertion(Builder builder)
    {
        this.projectName = builder.projectName;
        this.projectKey = builder.projectKey;
        this.planName = builder.planName;
        this.planKey = builder.planKey;
        this.buildNumber = builder.buildNumber;
        this.relatedIssues = builder.relatedIssues;
        this.reason = builder.reason;
        this.totalTests = builder.totalTests;
        this.testsFailed = builder.testsFailed;
        this.success = builder.success;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public String getPlanName()
    {
        return planName;
    }

    public String getPlanKey()
    {
        return planKey;
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public Iterable<String> getRelatedIssues()
    {
        return relatedIssues;
    }

    public String getReason()
    {
        return reason;
    }

    public int getTotalTests()
    {
        return totalTests;
    }

    public int getTestsFailed()
    {
        return testsFailed;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String projectName;
        private String projectKey;
        private String planName;
        private String planKey;
        private int buildNumber;
        private Iterable<String> relatedIssues;
        private String reason;
        private int totalTests;
        private int testsFailed;
        private boolean success;

        public RelatedBuildAssertion build()
        {
            return new RelatedBuildAssertion(this);
        }

        public Builder projectName(String projectName)
        {
            this.projectName = projectName;
            return this;
        }

        public Builder projectKey(String projectKey)
        {
            this.projectKey = projectKey;
            return this;
        }

        public Builder planName(String planName)
        {
            this.planName = planName;
            return this;
        }

        public Builder planKey(String planKey)
        {
            this.planKey = planKey;
            return this;
        }

        public Builder buildNumber(int buildNumber)
        {
            this.buildNumber = buildNumber;
            return this;
        }

        public Builder relatedIssues(Iterable<String> relatedIssues)
        {
            this.relatedIssues = relatedIssues;
            return this;
        }

        public Builder reason(String reason)
        {
            this.reason = reason;
            return this;
        }

        public Builder totalTests(int totalTests)
        {
            this.totalTests = totalTests;
            return this;
        }

        public Builder testsFailed(int testsFailed)
        {
            this.testsFailed = testsFailed;
            return this;
        }

        public Builder success(boolean success)
        {
            this.success = success;
            return this;
        }
    }
}
