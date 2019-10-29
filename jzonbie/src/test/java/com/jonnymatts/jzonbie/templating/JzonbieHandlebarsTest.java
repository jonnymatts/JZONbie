package com.jonnymatts.jzonbie.templating;

import com.github.jknack.handlebars.Template;
import com.jonnymatts.jzonbie.metadata.MetaDataContext;
import com.jonnymatts.jzonbie.metadata.MetaDataTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class JzonbieHandlebarsTest {

    private MetaDataContext metaDataContext;

    private JzonbieHandlebars underTest;

    @BeforeEach
    void setUp() throws Exception {
        metaDataContext = new MetaDataContext("http", "http://hostname:8080/a/long/path", 8080, "/a/long/path", singletonMap("param1", singletonList("paramValue")), singletonMap("header1", "headerValue"), "METHOD", "{\"field\": \"value\"}");
        metaDataContext.withMetaData(MetaDataTag.ENDPOINT_REQUEST_COUNT, "123123");
        underTest = new JzonbieHandlebars();
    }

    static Stream<Data> extractions() {
        return Stream.of(
                new Data("path", "{{ request.url }}", "http://hostname:8080/a/long/path"),
                new Data("baseUrl", "{{ request.baseUrl }}", "http://hostname:8080"),
                new Data("port", "{{ request.port }}", "8080"),
                new Data("host", "{{ request.host }}", "hostname"),
                new Data("path", "{{ request.path }}", "/a/long/path"),
                new Data("pathSegment", "{{ request.pathSegment.[1] }}", "long"),
                new Data("method", "{{ request.method }}", "METHOD"),
                new Data("header", "{{ request.header.header1 }}", "headerValue"),
                new Data("queryParam", "{{ request.queryParam.param1.[0] }}", "paramValue"),
                new Data("body", "{{{ request.body }}}", "{\"field\": \"value\"}"),
                new Data("jsonPath", "{{jsonPath request.body '$.field'}}", "value"),
                new Data("endpointRequestCount", "{{ ENDPOINT_REQUEST_COUNT }}", "123123")
        );
    }

    @ParameterizedTest
    @MethodSource("extractions")
    void canExtractValuesCorrectly(Data data) throws IOException {
        System.out.println("Running extraction: " + data.testName);
        final Template template = underTest.compileInline(data.pattern);

        final String got = template.apply(metaDataContext.getContext());

        assertThat(got).isEqualTo(data.expectedValue);
    }

    private static class Data {
        private String testName;
        private String pattern;
        private String expectedValue;

        Data(String testName, String pattern, String expectedValue) {
            this.testName = testName;
            this.pattern = pattern;
            this.expectedValue = expectedValue;
        }
    }
}