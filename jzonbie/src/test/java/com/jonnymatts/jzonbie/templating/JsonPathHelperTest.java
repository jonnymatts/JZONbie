package com.jonnymatts.jzonbie.templating;

import com.github.jknack.handlebars.Options;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.InvalidPathException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonPathHelperTest {

    private static final String JSON = "{\n" +
            "  \"field\": \"value\",\n" +
            "  \"list\": [\"value1\", \"value2\", \"value3\"],\n" +
            "  \"object\": {\n" +
            "    \"innerField\": \"innerValue\"\n" +
            "  }\n" +
            "}";

    @Mock private Options options;

    private JsonPathHelper underTest;

    @BeforeEach
    void setUp() throws Exception {
        when(options.param(0)).thenReturn("$.field");

        underTest = new JsonPathHelper();
    }

    @Test
    void canExtractSuccessfully() throws IOException {
        final Object got = underTest.apply(JSON, options);

        assertThat(got).isEqualTo("value");
    }

    @Test
    void canExtractFromList() throws IOException {
        when(options.param(0)).thenReturn("$.list[0]");

        final Object got = underTest.apply(JSON, options);

        assertThat(got).isEqualTo("value1");
    }

    @Test
    void canExtractFromObject() throws IOException {
        when(options.param(0)).thenReturn("$.object.innerField");

        final Object got = underTest.apply(JSON, options);

        assertThat(got).isEqualTo("innerValue");
    }

    @Test
    void applyThrowsExceptionIfJsonPathDoesNotCompile() throws IOException {
        when(options.param(0)).thenReturn("$field");

        assertThatThrownBy(() -> underTest.apply(JSON, options))
                .isExactlyInstanceOf(TransformResponseException.class)
                .hasCauseInstanceOf(InvalidPathException.class);
    }

    @Test
    void applyThrowsExceptionIfJsonDoesNotContainRequestedValue() throws IOException {
        when(options.param(0)).thenReturn("$.notFound");

        assertThatThrownBy(() -> underTest.apply(JSON, options))
                .isExactlyInstanceOf(TransformResponseException.class)
                .hasCauseInstanceOf(InvalidPathException.class);
    }

    @Test
    void applyThrowsExceptionIfJsonIsNotValid() throws IOException {
        when(options.param(0)).thenReturn("$.field");

        assertThatThrownBy(() -> underTest.apply("[", options))
                .isExactlyInstanceOf(TransformResponseException.class)
                .hasCauseInstanceOf(InvalidJsonException.class);
    }
}