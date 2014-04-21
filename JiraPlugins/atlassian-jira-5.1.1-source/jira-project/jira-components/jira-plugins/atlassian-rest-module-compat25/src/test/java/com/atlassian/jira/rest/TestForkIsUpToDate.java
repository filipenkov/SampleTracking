package com.atlassian.jira.rest;

import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestForkIsUpToDate
{
    /**
     * when updating this value you MUST verify that the maven-bundle-plugin instructions in this project's pom.xml
     * are identical to those in atlassian-rest-module except for the version that is exported for
     * org.codehaus.jackson*.
     *
     * this fork is to be removed in JIRA 6.x
     */
    static final String MODIFY_REST_VERSION_TO_FIX_TEST = "2.6.5.1";

    @Test
    public void forkShouldBeReviewedEveryTimeRestIsUpgradedInJira() throws IOException
    {
        Properties props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("rest-compat25.properties"));

        assertThat(format("You have upgraded JIRA to atlassian-rest-%s. You now need to check that the maven-bundle-plugin <instructions> are correct for the new version before fixing this test.", props.getProperty("rest.version")), props.getProperty("rest.version"), equalTo(MODIFY_REST_VERSION_TO_FIX_TEST));
    }
}
