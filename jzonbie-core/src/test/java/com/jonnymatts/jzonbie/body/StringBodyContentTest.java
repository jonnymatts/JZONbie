package com.jonnymatts.jzonbie.body;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.jonnymatts.jzonbie.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

public class StringBodyContentTest {
    private StringBodyContent underTest;

    @Before
    public void setUp() throws Exception {
        underTest = stringBody("string");
    }

    @Test
    public void getContentReturnsContent() {
        assertThat(underTest.getContent()).isEqualTo("string");
    }

    @Test
    public void matchesReturnsTrueIfContentsMatch() {
        assertThat(underTest.matches(stringBody("string"))).isTrue();
    }

    @Test
    public void matchesReturnsFalseIfContentsDoNotMatch() {
        assertThat(underTest.matches(stringBody("different"))).isFalse();
    }

    @Test
    public void matchesReturnsFalseIfContentIsNotTheSameType() {
        final List<BodyContent<?>> bodyContents = asList(
                arrayBody(emptyList()),
                literalBody(""),
                objectBody(emptyMap())
        );

        bodyContents.forEach(bodyContent -> assertThat(underTest.matches(bodyContent)).isFalse());
    }

    @Test
    public void copyReturnsNewInstanceWithSameContent() {
        final StringBodyContent copy = underTest.copy();

        assertThat(copy).isNotSameAs(underTest);
        assertThat(copy).isEqualTo(underTest);
    }
}