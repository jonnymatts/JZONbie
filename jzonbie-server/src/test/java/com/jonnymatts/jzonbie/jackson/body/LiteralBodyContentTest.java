package com.jonnymatts.jzonbie.jackson.body;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.jonnymatts.jzonbie.jackson.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.jackson.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.jackson.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.jackson.body.StringBodyContent.stringBody;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

public class LiteralBodyContentTest {

    private LiteralBodyContent underTest;

    @Before
    public void setUp() throws Exception {
        underTest = literalBody("literal");
    }

    @Test
    public void getContentReturnsContent() {
        assertThat(underTest.getContent()).isEqualTo("literal");
    }

    @Test
    public void matchesReturnsTrueIfContentsMatch() {
        assertThat(underTest.matches(literalBody("literal"))).isTrue();
    }

    @Test
    public void matchesReturnsFalseIfContentsDoNotMatch() {
        assertThat(underTest.matches(literalBody("different"))).isFalse();
    }

    @Test
    public void matchesReturnsFalseIfContentIsNotTheSameType() {
        final List<BodyContent<?>> bodyContents = asList(
                arrayBody(emptyList()),
                stringBody(""),
                objectBody(emptyMap())
        );

        bodyContents.forEach(bodyContent -> assertThat(underTest.matches(bodyContent)).isFalse());
    }

    @Test
    public void copyReturnsNewInstanceWithSameContent() {
        final LiteralBodyContent copy = underTest.copy();

        assertThat(copy).isNotSameAs(underTest);
        assertThat(copy).isEqualTo(underTest);
    }
}