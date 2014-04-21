package com.atlassian.jira.local;

import com.atlassian.jira.local.runner.ListeningRunner;
import org.junit.runner.RunWith;

/**
 * A base class that can plugin into our listener framework so we can get information about test runs
 *
 * @since v4.3
 */
@RunWith (ListeningRunner.class)
public abstract class ListeningTestCase
{
}
