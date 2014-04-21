package com.atlassian.streams.jira;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.streams.api.ActivityObjectType;
import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.jira.builder.ActivityObjectBuilder;
import com.atlassian.streams.jira.builder.JiraEntryBuilderFactory;
import com.atlassian.streams.spi.UserProfileAccessor;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JiraHelperTest
{
    private static final String STATUS = "status";
    private static final String OLD_VALUE = "1";
    private static final String OLD_ENGLISH_TRANSLATION = "In Progress";
    private static final String OLD_FRENCH_TRANSLATION = "En Cours";
    private static final String NEW_VALUE = "2";
    private static final String NEW_ENGLISH_TRANSLATION = "Under Review";
    private static final String NEW_FRENCH_TRANSLATION = "En cours d'examen";

    @Mock ConstantsManager constantsManager;
    @Mock JiraEntryBuilderFactory entryBuilderFactory;
    @Mock UriProvider uriProvider;
    @Mock ActivityObjectBuilder activityObjectBuilder;
    @Mock UserProfileAccessor userProfileAccessor;
    @Mock AttachmentManager attachmentManager;
    @Mock RemoteIssueLinkManager remoteIssueLinkManager;
    @Mock RendererManager rendererManager;
    @Mock FieldLayoutManager fieldLayoutManager;

    @Mock GenericValue changeItem;
    @Mock Status inProgress;
    @Mock Status underReview;

    private JiraHelper helper;

    @Before
    public void setup()
    {
        when(inProgress.getNameTranslation()).thenReturn(OLD_FRENCH_TRANSLATION);
        when(underReview.getNameTranslation()).thenReturn(NEW_FRENCH_TRANSLATION);

        when(changeItem.getString("field")).thenReturn(STATUS);
        when(changeItem.getString("oldstring")).thenReturn(OLD_ENGLISH_TRANSLATION);
        when(changeItem.getString("oldvalue")).thenReturn(OLD_VALUE);
        when(changeItem.getString("newstring")).thenReturn(NEW_ENGLISH_TRANSLATION);
        when(changeItem.getString("newvalue")).thenReturn(NEW_VALUE);

        when(constantsManager.getConstantObject(STATUS, OLD_VALUE)).thenReturn(inProgress);
        when(constantsManager.getConstantObject(STATUS, NEW_VALUE)).thenReturn(underReview);

        helper = new JiraHelper(entryBuilderFactory, uriProvider, activityObjectBuilder,
                userProfileAccessor, attachmentManager, remoteIssueLinkManager, rendererManager,
                fieldLayoutManager, constantsManager);
    }

    @Test
    public void assertThatNewNameTranslationTranslatesWhenNewValueAndObjectExist()
    {
        assertThat(helper.getNewChangeItemNameTranslation(changeItem).get(), is(equalTo(NEW_FRENCH_TRANSLATION)));
    }

    @Test
    public void assertThatNewNameTranslationUsesNewstringValueWhenObjectDoesNotExist()
    {
        //this scenario could happen if the object was deleted after the change item was persisted
        when(constantsManager.getConstantObject(STATUS, NEW_VALUE)).thenReturn(null);
        assertThat(helper.getNewChangeItemNameTranslation(changeItem).get(), is(equalTo(NEW_ENGLISH_TRANSLATION)));
    }

    @Test
    public void assertThatNewNameTranslationUsesNewstringValueWhenNewvalueValueDoesNotExist()
    {
        //this scenario could happen as not all change items store an "newvalue"
        when(changeItem.getString("newvalue")).thenReturn(null);
        assertThat(helper.getNewChangeItemNameTranslation(changeItem).get(), is(equalTo(NEW_ENGLISH_TRANSLATION)));
    }

    @Test
    public void assertThatNewNameTranslationReturnsNoneWhenNewvalueAndNewstringDoNotExist()
    {
        when(changeItem.getString("newstring")).thenReturn(null);
        when(changeItem.getString("newvalue")).thenReturn(null);
        assertThat(helper.getNewChangeItemNameTranslation(changeItem).isDefined(), is(equalTo(false)));
    }

    @Test
    public void assertThatOldNameTranslationTranslatesWhenOldValueAndObjectExist()
    {
        assertThat(helper.getOldChangeItemNameTranslation(changeItem).get(), is(equalTo(OLD_FRENCH_TRANSLATION)));
    }

    @Test
    public void assertThatOldNameTranslationUsesOldstringValueWhenObjectDoesNotExist()
    {
        //this scenario could happen if the object was deleted after the change item was persisted
        when(constantsManager.getConstantObject(STATUS, OLD_VALUE)).thenReturn(null);
        assertThat(helper.getOldChangeItemNameTranslation(changeItem).get(), is(equalTo(OLD_ENGLISH_TRANSLATION)));
    }

    @Test
    public void assertThatOldNameTranslationUsesOldstringValueWhenOldvalueValueDoesNotExist()
    {
        //this scenario could happen as not all change items store an "oldvalue"
        when(changeItem.getString("oldvalue")).thenReturn(null);
        assertThat(helper.getOldChangeItemNameTranslation(changeItem).get(), is(equalTo(OLD_ENGLISH_TRANSLATION)));
    }

    @Test
    public void assertThatOldNameTranslationReturnsNoneWhenOldvalueAndOldstringDoNotExist()
    {
        when(changeItem.getString("oldstring")).thenReturn(null);
        when(changeItem.getString("oldvalue")).thenReturn(null);
        assertThat(helper.getOldChangeItemNameTranslation(changeItem).isDefined(), is(equalTo(false)));
    }

    @Test
    public void assertThatGetTransitionVerbHandlesNullChangeItemValues()
    {
        when(changeItem.getString("fieldtype")).thenReturn("jira");
        when(changeItem.getString("oldstring")).thenReturn(null);
        when(changeItem.getString("oldvalue")).thenReturn(null);

        ChangeHistory changeHistory = mock(ChangeHistory.class);
        when(changeHistory.getChangeItems()).thenReturn(Lists.newArrayList(changeItem));

        Option<Pair<ActivityObjectType, ActivityVerb>> pairOption = helper.jiraActivity(changeHistory);

        assertThat(pairOption.get().second(), is(equalTo(JiraActivityVerbs.transition())));
    }

    @Test
    public void assertThatGetTransitionVerbHandlesEmptyChangeItemValues()
    {
        when(changeItem.getString("fieldtype")).thenReturn("jira");
        when(changeItem.getString("oldstring")).thenReturn("");
        when(changeItem.getString("oldvalue")).thenReturn("");

        ChangeHistory changeHistory = mock(ChangeHistory.class);
        when(changeHistory.getChangeItems()).thenReturn(Lists.newArrayList(changeItem));

        Option<Pair<ActivityObjectType, ActivityVerb>> pairOption = helper.jiraActivity(changeHistory);

        assertThat(pairOption.get().second(), is(equalTo(JiraActivityVerbs.transition())));
    }
}
