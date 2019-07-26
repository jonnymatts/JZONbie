package com.jonnymatts.jzonbie.body;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.jonnymatts.jzonbie.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class ArrayBodyContentTest {

    private ArrayBodyContent underTest;

    @BeforeEach
    void setUp() throws Exception {
        underTest = arrayBody(asList(1, 2, 3));
    }

    @Test
    void getContentReturnsContent() {
        assertThat(underTest.getContent()).isEqualTo(asList(1, 2, 3));
    }

    @Test
    void matchesReturnsTrueIfContentsMatch() {
        assertThat(underTest.matches(arrayBody(asList(1, 2, 3)))).isTrue();
    }

    @Test
    void matchesReturnsFalseIfContentsDoNotMatch() {
        assertThat(underTest.matches(arrayBody(asList(1, 2)))).isFalse();
    }

    @Test
    void matchesReturnsFalseIfContentIsNotTheSameType() {
        final List<BodyContent<?>> bodyContents = asList(
                literalBody(""),
                stringBody(""),
                objectBody(emptyMap())
        );

        bodyContents.forEach(bodyContent -> assertThat(underTest.matches(bodyContent)).isFalse());
    }

    @Test
    void copyReturnsNewInstanceWithSameContent() {
        final ArrayBodyContent copy = underTest.copy();

        assertThat(copy).isNotSameAs(underTest);
        assertThat(copy).isEqualTo(underTest);
    }
}