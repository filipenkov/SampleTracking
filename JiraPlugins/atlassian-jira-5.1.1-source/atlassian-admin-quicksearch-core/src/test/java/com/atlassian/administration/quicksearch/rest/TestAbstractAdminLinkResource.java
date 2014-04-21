package com.atlassian.administration.quicksearch.rest;

import com.atlassian.administration.quicksearch.impl.spi.AdminLinkBean;
import com.atlassian.administration.quicksearch.impl.spi.AdminLinkSectionBean;
import com.atlassian.administration.quicksearch.spi.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.administration.quicksearch.impl.spi.SectionKeys.fullSectionKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Test case for {@link com.atlassian.administration.quicksearch.rest.AbstractAdminLinkResource}
 *
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAbstractAdminLinkResource
{

    @Mock private AdminLinkManager mockLinkManager;
    @Mock private AdminLinkAliasProvider mockAliasProvider;
    @Mock private UserContext mockUserContext;

    private TestedResource tested;

    @Before
    public void initTested()
    {
        tested = new TestedResource(mockLinkManager, mockAliasProvider);
    }

    @Test
    public void shouldConvertSingleAdminSection()
    {
        when(mockLinkManager.getSection("test-location", mockUserContext)).thenReturn(createTestTree("test-location"));
        final LocationBean result = tested.getLinksFor("test-location", mockUserContext);
        assertEquals("test-location", result.key());
        assertEquals(3, result.links().size());
        assertLink("link-test-location-1", "link-label-test-location-1", "link-url-test-location-1", result.links().get(0));
        assertLink("link-test-location-2", "link-label-test-location-2", "link-url-test-location-2", result.links().get(1));
        assertLink("link-test-location-3", "link-label-test-location-3", "link-url-test-location-3", result.links().get(2));
        assertEquals(3, result.sections().size());
        {
            final SectionBean section1 = result.sections().get(0);
            assertSection("test-location-1", "label-test-location-1", "test-location", section1);
            assertEquals(0, section1.sections().size());
            assertEquals(3, section1.links().size());
            assertLink("link-test-location/test-location-1-1", "link-label-test-location/test-location-1-1",
                    "link-url-test-location/test-location-1-1", section1.links().get(0));
            assertLink("link-test-location/test-location-1-2", "link-label-test-location/test-location-1-2",
                    "link-url-test-location/test-location-1-2", section1.links().get(1));
            assertLink("link-test-location/test-location-1-3", "link-label-test-location/test-location-1-3",
                    "link-url-test-location/test-location-1-3", section1.links().get(2));
        }
        {
            final SectionBean section2 = result.sections().get(1);
            assertSection("test-location-2", "label-test-location-2", "test-location", section2);
            assertEquals(0, section2.sections().size());
            assertEquals(3, section2.links().size());
            assertLink("link-test-location/test-location-2-1", "link-label-test-location/test-location-2-1",
                    "link-url-test-location/test-location-2-1", section2.links().get(0));
            assertLink("link-test-location/test-location-2-2", "link-label-test-location/test-location-2-2",
                    "link-url-test-location/test-location-2-2", section2.links().get(1));
            assertLink("link-test-location/test-location-2-3", "link-label-test-location/test-location-2-3",
                    "link-url-test-location/test-location-2-3", section2.links().get(2));
        }
        {
            final SectionBean section3 = result.sections().get(2);
            assertSection("test-location-3", "label-test-location-3", "test-location", section3);
            assertEquals(0, section3.sections().size());
            assertEquals(3, section3.links().size());
            assertLink("link-test-location/test-location-3-1", "link-label-test-location/test-location-3-1",
                    "link-url-test-location/test-location-3-1", section3.links().get(0));
            assertLink("link-test-location/test-location-3-2", "link-label-test-location/test-location-3-2",
                    "link-url-test-location/test-location-3-2", section3.links().get(1));
            assertLink("link-test-location/test-location-3-3", "link-label-test-location/test-location-3-3",
                    "link-url-test-location/test-location-3-3", section3.links().get(2));
        }
    }

    @Test
    public void shouldRepresentMultipleRootSectionsAsOne()
    {
        when(mockLinkManager.getSection("test-location", mockUserContext)).thenReturn(createTestTree("test-location"));
        when(mockLinkManager.getSection("another-location", mockUserContext)).thenReturn(createTestTree("another-location"));
        final LocationBean result = tested.getLinksFor(ImmutableList.<String>of("test-location", "another-location"), mockUserContext);
        assertNull(result.key());
        assertEquals(6, result.links().size());
        assertLink("link-test-location-1", "link-label-test-location-1", "link-url-test-location-1", result.links().get(0));
        assertLink("link-test-location-2", "link-label-test-location-2", "link-url-test-location-2", result.links().get(1));
        assertLink("link-test-location-3", "link-label-test-location-3", "link-url-test-location-3", result.links().get(2));
        assertLink("link-another-location-1", "link-label-another-location-1", "link-url-another-location-1", result.links().get(3));
        assertLink("link-another-location-2", "link-label-another-location-2", "link-url-another-location-2", result.links().get(4));
        assertLink("link-another-location-3", "link-label-another-location-3", "link-url-another-location-3", result.links().get(5));
        assertEquals(6, result.sections().size());
        {
            final SectionBean section1 = result.sections().get(0);
            assertSection("test-location-1", "label-test-location-1", "test-location", section1);
            assertEquals(0, section1.sections().size());
            assertEquals(3, section1.links().size());
        }
        {
            final SectionBean section4 = result.sections().get(3);
            assertSection("another-location-1", "label-another-location-1", "another-location", section4);
            assertEquals(0, section4.sections().size());
            assertEquals(3, section4.links().size());
        }
    }

    @Test
    public void shouldProvideAliasesUsingAliasProvider()
    {
        when(mockLinkManager.getSection("test-location", mockUserContext)).thenReturn(createTestTree("test-location"));
        when(mockAliasProvider.getAliases(argThat(hasId("link-test-location-1")), anyListOf(AdminLinkSection.class), eq(mockUserContext)))
                .thenReturn(ImmutableSet.<String>of("one", "two", "three"));
        when(mockAliasProvider.getAliases(argThat(hasId("link-test-location/test-location-1-1")), anyListOf(AdminLinkSection.class), eq(mockUserContext)))
                .thenReturn(ImmutableSet.<String>of("child1-one", "child1-two", "child1-three"));
        when(mockAliasProvider.getAliases(argThat(hasId("link-test-location/test-location-2-2")), anyListOf(AdminLinkSection.class), eq(mockUserContext)))
                .thenReturn(ImmutableSet.<String>of("child2-one", "child2-two", "child2-three"));
        when(mockAliasProvider.getAliases(argThat(hasId("link-test-location/test-location-3-3")), anyListOf(AdminLinkSection.class), eq(mockUserContext)))
                .thenReturn(ImmutableSet.<String>of("child3-one", "child3-two", "child3-three"));
        final LocationBean result = tested.getLinksFor(ImmutableList.<String>of("test-location"), mockUserContext);
        assertEquals(ImmutableSet.of("one", "two", "three"), result.links().get(0).aliases());
        assertEquals(Collections.<String>emptySet(), result.links().get(1).aliases());
        assertEquals(Collections.<String>emptySet(), result.links().get(2).aliases());
        {
            final SectionBean section1 = result.sections().get(0);
            assertEquals(ImmutableSet.of("child1-one", "child1-two", "child1-three"), section1.links().get(0).aliases());
            assertEquals(Collections.<String>emptySet(), section1.links().get(1).aliases());
            assertEquals(Collections.<String>emptySet(), section1.links().get(2).aliases());
        }
        {
            final SectionBean section2 = result.sections().get(1);
            assertEquals(Collections.<String>emptySet(), section2.links().get(0).aliases());
            assertEquals(ImmutableSet.of("child2-one", "child2-two", "child2-three"), section2.links().get(1).aliases());
            assertEquals(Collections.<String>emptySet(), section2.links().get(2).aliases());
        }
        {
            final SectionBean section3 = result.sections().get(2);
            assertEquals(Collections.<String>emptySet(), section3.links().get(0).aliases());
            assertEquals(Collections.<String>emptySet(), section3.links().get(1).aliases());
            assertEquals(ImmutableSet.of("child3-one", "child3-two", "child3-three"), section3.links().get(2).aliases());
        }
    }

    @Test
    public void shouldMergeAliasesForSameUrls()
    {
        when(mockLinkManager.getSection("test-location", mockUserContext)).thenReturn(createTestTreeWithRepeatedUrls("test-location"));
        when(mockAliasProvider.getAliases(argThat(hasId("link-test-location-1")), anyListOf(AdminLinkSection.class), eq(mockUserContext)))
                .thenReturn(ImmutableSet.<String>of("one", "two", "three"));
        when(mockAliasProvider.getAliases(argThat(hasId("repeated-link-test-location")), anyListOf(AdminLinkSection.class), eq(mockUserContext)))
                .thenReturn(ImmutableSet.<String>of("four", "five", "six"));
        when(mockAliasProvider.getAliases(argThat(hasId("link-test-location/test-location-1-1")), anyListOf(AdminLinkSection.class), eq(mockUserContext)))
                .thenReturn(ImmutableSet.<String>of("child1-one", "child1-two", "child1-three"));
        when(mockAliasProvider.getAliases(argThat(hasId("repeated-link-test-location-1")), anyListOf(AdminLinkSection.class), eq(mockUserContext)))
                .thenReturn(ImmutableSet.<String>of("child1-four", "child1-five", "child1-six"));
        when(mockAliasProvider.getAliases(argThat(hasId("link-test-location/test-location-2-2")), anyListOf(AdminLinkSection.class), eq(mockUserContext)))
                .thenReturn(ImmutableSet.<String>of("child2-one", "child2-two", "child2-three"));
        when(mockAliasProvider.getAliases(argThat(hasId("repeated-link-test-location-2")), anyListOf(AdminLinkSection.class), eq(mockUserContext)))
                .thenReturn(ImmutableSet.<String>of("child2-four", "child2-five", "child2-six"));
        when(mockAliasProvider.getAliases(argThat(hasId("link-test-location/test-location-3-3")), anyListOf(AdminLinkSection.class), eq(mockUserContext)))
                .thenReturn(ImmutableSet.<String>of("child3-one", "child3-two", "child3-three"));
        when(mockAliasProvider.getAliases(argThat(hasId("repeated-link-test-location-3")), anyListOf(AdminLinkSection.class), eq(mockUserContext)))
                .thenReturn(ImmutableSet.<String>of("child3-four", "child3-five", "child3-six"));

        final LocationBean result = tested.getLinksFor(ImmutableList.<String>of("test-location"), mockUserContext);
        assertEquals(Collections.<LinkBean>emptyList(), result.sections().get(3).links());
        // merged aliases will always be sorted alphabetically
        assertEquals(ImmutableSet.of("five", "four", "one", "label-repeated-link-test-location", "six", "three", "two"), result.links().get(0).aliases());
        {
            final SectionBean section1 = result.sections().get(0);
            assertEquals(ImmutableSet.of("child1-five", "child1-four", "child1-one", "label-repeated-link-test-location-1", "child1-six", "child1-three", "child1-two"),
                    section1.links().get(0).aliases());
        }
        {
            final SectionBean section2 = result.sections().get(1);
            assertEquals(ImmutableSet.of("child2-five", "child2-four", "child2-one", "label-repeated-link-test-location-2", "child2-six", "child2-three", "child2-two"),
                    section2.links().get(1).aliases());
        }
        {
            final SectionBean section3 = result.sections().get(2);
            assertEquals(ImmutableSet.of("child3-five", "child3-four", "child3-one", "label-repeated-link-test-location-3", "child3-six", "child3-three", "child3-two"),
                    section3.links().get(2).aliases());
        }
    }

    @Test
    public void shouldConvertSingleSectionWithoutStripping()
    {
        when(mockLinkManager.getSection("test-location", mockUserContext)).thenReturn(createTestTree("test-location"));
        final LocationBean rootResult = tested.getLinksFor(ImmutableList.of("test-location"), mockUserContext, false);
        assertNull(rootResult.key());
        assertEquals(0, rootResult.links().size());
        assertEquals(1, rootResult.sections().size());
        // rest is similar to the default, stripping case
        final SectionBean result = rootResult.sections().get(0);
        assertEquals("test-location", result.key());
        assertEquals(3, result.links().size());
        assertLink("link-test-location-1", "link-label-test-location-1", "link-url-test-location-1", result.links().get(0));
        assertLink("link-test-location-2", "link-label-test-location-2", "link-url-test-location-2", result.links().get(1));
        assertLink("link-test-location-3", "link-label-test-location-3", "link-url-test-location-3", result.links().get(2));
        assertEquals(3, result.sections().size());
        {
            final SectionBean section1 = result.sections().get(0);
            assertSection("test-location-1", "label-test-location-1", "test-location", section1);
            assertEquals(0, section1.sections().size());
            assertEquals(3, section1.links().size());
            assertLink("link-test-location/test-location-1-1", "link-label-test-location/test-location-1-1",
                    "link-url-test-location/test-location-1-1", section1.links().get(0));
            assertLink("link-test-location/test-location-1-2", "link-label-test-location/test-location-1-2",
                    "link-url-test-location/test-location-1-2", section1.links().get(1));
            assertLink("link-test-location/test-location-1-3", "link-label-test-location/test-location-1-3",
                    "link-url-test-location/test-location-1-3", section1.links().get(2));
        }
        {
            final SectionBean section2 = result.sections().get(1);
            assertSection("test-location-2", "label-test-location-2", "test-location", section2);
            assertEquals(0, section2.sections().size());
            assertEquals(3, section2.links().size());
            assertLink("link-test-location/test-location-2-1", "link-label-test-location/test-location-2-1",
                    "link-url-test-location/test-location-2-1", section2.links().get(0));
            assertLink("link-test-location/test-location-2-2", "link-label-test-location/test-location-2-2",
                    "link-url-test-location/test-location-2-2", section2.links().get(1));
            assertLink("link-test-location/test-location-2-3", "link-label-test-location/test-location-2-3",
                    "link-url-test-location/test-location-2-3", section2.links().get(2));
        }
        {
            final SectionBean section3 = result.sections().get(2);
            assertSection("test-location-3", "label-test-location-3", "test-location", section3);
            assertEquals(0, section3.sections().size());
            assertEquals(3, section3.links().size());
            assertLink("link-test-location/test-location-3-1", "link-label-test-location/test-location-3-1",
                    "link-url-test-location/test-location-3-1", section3.links().get(0));
            assertLink("link-test-location/test-location-3-2", "link-label-test-location/test-location-3-2",
                    "link-url-test-location/test-location-3-2", section3.links().get(1));
            assertLink("link-test-location/test-location-3-3", "link-label-test-location/test-location-3-3",
                    "link-url-test-location/test-location-3-3", section3.links().get(2));
        }
    }

    private AdminLinkSection createTestTree(String location)
    {
        return createRootSection(location, createChildLinks(location), createChildSections(location));
    }

    private AdminLinkSection createTestTreeWithRepeatedUrls(String location)
    {
        final Iterable<AdminLinkSection> allSections = ImmutableList.<AdminLinkSection>builder()
                .addAll(createChildSections(location))
                .add(createSectionWithRepeatedUrls(location))
                .build();
        return createRootSection(location, createChildLinks(location), allSections);
    }

    private AdminLinkSection createSectionWithRepeatedUrls(String location)
    {
        final ImmutableList.Builder<AdminLink> repeatedLinksBuilder = ImmutableList.builder();
        repeatedLinksBuilder.add(new AdminLinkBean("repeated-link-" + location, "label-repeated-link-" + location,
                Collections.<String, String>emptyMap(), "link-url-" + location + "-1"));
        for (int i=1; i<=3; i++)
        {
            repeatedLinksBuilder.add(new AdminLinkBean("repeated-link-" + location + "-" + i,
                    "label-repeated-link-" + location + "-" + i, Collections.<String, String>emptyMap(),
                    "link-url-" + fullSectionKey(location, location + "-" + i) + "-" + i));
        }

        return createTestSection("repeated-urls", "repated-urls-label", location, repeatedLinksBuilder.build(),
                Collections.<AdminLinkSection>emptyList());
    }


    private List<AdminLinkSection> createChildSections(String location) {
        final List<AdminLinkSection> childSections = Lists.newArrayList();
        for (int i=1; i<=3; i++)
        {
            final String sectionId = location + "-" + i;
            childSections.add(createTestSection(sectionId, "label-" + sectionId, location,
                    createChildLinks(fullSectionKey(location, sectionId)),
                    Collections.<AdminLinkSection>emptyList()));
        }
        return childSections;
    }

    private Iterable<AdminLink> createChildLinks(String section)
    {
        final ImmutableList.Builder<AdminLink> builder = ImmutableList.builder();
        for (int i=1; i<=3; i++)
        {
            builder.add(new AdminLinkBean("link-" + section + "-" + i, "link-label-" + section + "-" + i,
                    Collections.<String, String>emptyMap(), "link-url-" + section + "-" + i));
        }
        return builder.build();
    }

    private AdminLinkSection createRootSection(String location, Iterable<AdminLink> links, Iterable<AdminLinkSection> sections)
    {
        return createTestSection(location, null, null, links, sections);
    }

    private AdminLinkSection createChildSection(AdminLinkSection parent, String id, String label,
                                                Iterable<AdminLink> links, Iterable<AdminLinkSection> sections)
    {
        return createTestSection(id, label, parent.getId(), links, sections);
    }

    private AdminLinkSection createTestSection(String id, String label, String location,
                                               Iterable<AdminLink> links, Iterable<AdminLinkSection> sections)
    {
        return createTestSection(id, label, location, null, links, sections);
    }

    private AdminLinkSection createTestSection(String id, String label, String location, Map<String,String> params,
                                               Iterable<AdminLink> links, Iterable<AdminLinkSection> sections)
    {
        return new AdminLinkSectionBean(id, label, params, location, sections, links);
    }

    private void assertLink(String expectedId, String expectedLabel, String expectedUrl, LinkBean link)
    {
        assertEquals(expectedId, link.key());
        assertEquals(expectedLabel, link.label());
        assertEquals(expectedUrl, link.linkUrl());
    }

    private void assertSection(String expectedId, String expectedLabel, String expectedLocation, SectionBean section) {
        assertEquals(expectedId, section.key());
        assertEquals(expectedLabel, section.label());
        assertEquals(expectedLocation, section.location());
    }

    private Matcher<AdminLink> hasId(final String id)
    {
        return new BaseMatcher<AdminLink>() {
            @Override
            public boolean matches(Object linkObject) {
                final AdminLink link = (AdminLink) linkObject;
                return link != null && id.equals(link.getId());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Link with ID: ").appendValue(id);
            }
        };
    }

    private static class TestedResource extends AbstractAdminLinkResource
    {

        TestedResource(AdminLinkManager linkManager, AdminLinkAliasProvider aliasProvider)
        {
            super(linkManager, aliasProvider);
        }
    }

}
