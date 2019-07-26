package com.jonnymatts.jzonbie.body;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.jonnymatts.jzonbie.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;

class ObjectBodyContentTest {
    private ObjectBodyContent underTest;

    @BeforeEach
    void setUp() throws Exception {
        underTest = objectBody(singletonMap("key", "val"));
    }

    @Test
    void getContentReturnsContent() {
        assertThat(underTest.getContent()).isEqualTo(singletonMap("key", "val"));
    }

    @Test
    void matchesReturnsTrueIfContentsMatch() {
        assertThat(underTest.matches(objectBody(singletonMap("key", "val")))).isTrue();
    }

    @Test
    void matchesReturnsFalseIfContentsDoNotMatch() {
        assertThat(underTest.matches(objectBody(emptyMap()))).isFalse();
    }

    @Test
    void matchesReturnsFalseIfContentIsNotTheSameType() {
        final List<BodyContent<?>> bodyContents = asList(
                arrayBody(emptyList()),
                literalBody(""),
                stringBody("")
        );

        bodyContents.forEach(bodyContent -> assertThat(underTest.matches(bodyContent)).isFalse());
    }

    @Test
    void copyReturnsNewInstanceWithSameContent() {
        final ObjectBodyContent copy = underTest.copy();

        assertThat(copy).isNotSameAs(underTest);
        assertThat(copy).isEqualTo(underTest);
    }
}