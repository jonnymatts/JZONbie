package com.jonnymatts.jzonbie.templating;

import com.github.jknack.handlebars.Options;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.PathNotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JsonPathHelperTest {

    private static final String JSON = "{\n" +
            "  \"field\": \"value\",\n" +
            "  \"list\": [\"value1\", \"value2\", \"value3\"],\n" +
            "  \"object\": {\n" +
            "    \"innerField\": \"innerValue\"\n" +
            "  }\n" +
            "}";

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private Options options;

    private JsonPathHelper underTest;

    @Before
    public void setUp() throws Exception {
        when(options.param(0)).thenReturn("$.field");

        underTest = new JsonPathHelper();
    }

    @Test
    public void canExtractSuccessfully() throws IOException {
        final Object got = underTest.apply(JSON, options);

        assertThat(got).isEqualTo("value");
    }

    @Test
    public void canExtractFromList() throws IOException {
        when(options.param(0)).thenReturn("$.list[0]");

        final Object got = underTest.apply(JSON, options);

        assertThat(got).isEqualTo("value1");
    }

    @Test
    public void canExtractFromObject() throws IOException {
        when(options.param(0)).thenReturn("$.object.innerField");

        final Object got = underTest.apply(JSON, options);

        assertThat(got).isEqualTo("innerValue");
    }

    @Test
    public void applyThrowsExceptionIfJsonPathDoesNotCompile() throws IOException {
        when(options.param(0)).thenReturn("$field");

        expectedException.expect(TransformResponseException.class);
        expectedException.expectCause(isA(InvalidPathException.class));

        underTest.apply(JSON, options);
    }

    @Test
    public void applyThrowsExceptionIfJsonDoesNotContainRequestedValue() throws IOException {
        when(options.param(0)).thenReturn("$.notFound");

        expectedException.expect(TransformResponseException.class);
        expectedException.expectCause(isA(PathNotFoundException.class));

        underTest.apply(JSON, options);
    }

    @Test
    public void applyThrowsExceptionIfJsonIsNotValid() throws IOException {
        when(options.param(0)).thenReturn("$.field");

        expectedException.expect(TransformResponseException.class);
        expectedException.expectCause(isA(InvalidJsonException.class));

        underTest.apply("[", options);
    }
}