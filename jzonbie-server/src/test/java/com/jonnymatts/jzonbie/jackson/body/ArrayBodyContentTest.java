package com.jonnymatts.jzonbie.jackson.body;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.jonnymatts.jzonbie.jackson.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.jackson.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.jackson.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.jackson.body.StringBodyContent.stringBody;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

public class ArrayBodyContentTest {

    private ArrayBodyContent underTest;

    @Before
    public void setUp() throws Exception {
        underTest = arrayBody(asList(1, 2, 3));
    }

    @Test
    public void getContentReturnsContent() {
        assertThat(underTest.getContent()).isEqualTo(asList(1, 2, 3));
    }

    @Test
    public void matchesReturnsTrueIfContentsMatch() {
        assertThat(underTest.matches(arrayBody(asList(1, 2, 3)))).isTrue();
    }

    @Test
    public void matchesReturnsFalseIfContentsDoNotMatch() {
        assertThat(underTest.matches(arrayBody(asList(1, 2)))).isFalse();
    }

    @Test
    public void matchesReturnsFalseIfContentIsNotTheSameType() {
        final List<BodyContent<?>> bodyContents = asList(
                literalBody(""),
                stringBody(""),
                objectBody(emptyMap())
        );

        bodyContents.forEach(bodyContent -> assertThat(underTest.matches(bodyContent)).isFalse());
    }

    @Test
    public void copyReturnsNewInstanceWithSameContent() {
        final ArrayBodyContent copy = underTest.copy();

        assertThat(copy).isNotSameAs(underTest);
        assertThat(copy).isEqualTo(underTest);
    }
}