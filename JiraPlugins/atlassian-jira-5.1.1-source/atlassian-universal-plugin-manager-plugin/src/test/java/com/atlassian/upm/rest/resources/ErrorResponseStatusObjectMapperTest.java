package com.atlassian.upm.rest.resources;

import com.atlassian.plugins.rest.common.Status;
import com.atlassian.upm.rest.representations.ErrorRepresentation;
import com.atlassian.upm.rest.representations.ErrorResponseStatusObjectMapper;
import com.atlassian.upm.rest.representations.RepresentationFactory;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ErrorResponseStatusObjectMapperTest
{
    @Mock RepresentationFactory representationFactory;
    private ObjectMapper mapper;

    private static final String ERROR_I18N_KEY = "upm.plugin.error.unexpected.error";
    private static final String ERROR_MSG = "This is the error message";

    @Before
    public void setUp()
    {
        when(representationFactory.createI18nErrorRepresentation(ERROR_I18N_KEY))
            .thenReturn(new ErrorRepresentation(null, ERROR_I18N_KEY));
        when(representationFactory.createErrorRepresentation(ERROR_MSG, ERROR_I18N_KEY))
            .thenReturn(new ErrorRepresentation(ERROR_MSG, ERROR_I18N_KEY));

        this.mapper = new ErrorResponseStatusObjectMapper(representationFactory);
    }


    @Test
    public void verifyObjectMapperCanSerializeRestStatus()
    {
        assertTrue(mapper.canSerialize(Status.class));
    }

    @Test
    public void verifyThatRestStatusIsSerializedProperlyToErrorRepresentation() throws Exception
    {
        String serializedStatusRepresentation = mapper.writeValueAsString(Status.error().message(ERROR_MSG).build());
        String serializedErrorRepresentation = mapper.writeValueAsString(new ErrorRepresentation(ERROR_MSG, ERROR_I18N_KEY));

        assertThat(serializedStatusRepresentation, is(equalTo(serializedErrorRepresentation)));
    }

    @Test
    public void verifyThatRestStatusWithoutMessageIsSerializedProperlyToErrorRepresentation() throws Exception
    {
        String serializedStatusRepresentation = mapper.writeValueAsString(Status.error().build());
        String serializedErrorRepresentation = mapper.writeValueAsString(new ErrorRepresentation(null, ERROR_I18N_KEY));

        assertThat(serializedStatusRepresentation, is(equalTo(serializedErrorRepresentation)));
    }
}
