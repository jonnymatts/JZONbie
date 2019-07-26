package com.jonnymatts.jzonbie.body;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.jonnymatts.jzonbie.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class StringBodyContentTest {
    private StringBodyContent underTest;

    @BeforeEach
    void setUp() throws Exception {
        underTest = stringBody("string");
    }

    @Test
    void getContentReturnsContent() {
        assertThat(underTest.getContent()).isEqualTo("string");
    }

    @Test
    void matchesReturnsTrueIfContentsMatch() {
        assertThat(underTest.matches(stringBody("string"))).isTrue();
    }

    @Test
    void matchesReturnsFalseIfContentsDoNotMatch() {
        assertThat(underTest.matches(stringBody("different"))).isFalse();
    }

    @Test
    void matchesReturnsFalseIfContentIsNotTheSameType() {
        final List<BodyContent<?>> bodyContents = asList(
                arrayBody(emptyList()),
                literalBody(""),
                objectBody(emptyMap())
        );

        bodyContents.forEach(bodyContent -> assertThat(underTest.matches(bodyContent)).isFalse());
    }

    @Test
    void copyReturnsNewInstanceWithSameContent() {
        final StringBodyContent copy = underTest.copy();

        assertThat(copy).isNotSameAs(underTest);
        assertThat(copy).isEqualTo(underTest);
    }
}