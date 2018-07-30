package com.jonnymatts.jzonbie.templating;

import com.github.jknack.handlebars.Template;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Theories.class)
public class JzonbieHandlebarsTest {

    private TransformationContext transformationContext;

    private JzonbieHandlebars underTest;

    @Before
    public void setUp() throws Exception {
        transformationContext = new TransformationContext("http", "http://hostname:8080/a/long/path", 8080, "/a/long/path", singletonMap("param1", singletonList("paramValue")), singletonMap("header1", "headerValue"), "METHOD", "body");

        underTest = new JzonbieHandlebars();
    }

    @DataPoints("extractions")
    public static Data[] extractions = new Data[]{
            new Data("path", "{{ request.url }}", "http://hostname:8080/a/long/path"),
            new Data("baseUrl", "{{ request.baseUrl }}", "http://hostname:8080"),
            new Data("port", "{{ request.port }}", "8080"),
            new Data("host", "{{ request.host }}", "hostname"),
            new Data("path", "{{ request.path }}", "/a/long/path"),
            new Data("pathSegment", "{{ request.pathSegment.[1] }}", "long"),
            new Data("method", "{{ request.method }}", "METHOD"),
            new Data("header", "{{ request.header.header1 }}", "headerValue"),
            new Data("queryParam", "{{ request.queryParam.param1.[0] }}", "paramValue"),
            new Data("body", "{{ request.body }}", "body"),
    };

    @Theory
    public void canExtractValuesCorrectly(@FromDataPoints("extractions") Data data) throws IOException {
        System.out.println("Running extraction: " + data.testName);
        final Template template = underTest.compileInline(data.pattern);

        final String got = template.apply(transformationContext);

        assertThat(got).isEqualTo(data.expectedValue);
    }

    private static class Data {
        private String testName;
        private String pattern;
        private String expectedValue;

        public Data(String testName, String pattern, String expectedValue) {
            this.testName = testName;
            this.pattern = pattern;
            this.expectedValue = expectedValue;
        }
    }
}