package com.jonnymatts.jzonbie.priming;

import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.priming.content.ArrayBodyContent;
import com.jonnymatts.jzonbie.priming.content.LiteralBodyContent;
import com.jonnymatts.jzonbie.priming.content.ObjectBodyContent;
import com.jonnymatts.jzonbie.priming.content.StringBodyContent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.jonnymatts.jzonbie.priming.content.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.priming.content.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.priming.content.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.priming.content.StringBodyContent.stringBody;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class BodyContentTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures(); 
    
    @Fixture private String string;
    @Fixture private Map<String, Object> map;
    @Fixture private List<String> list;
    @Fixture private Long number;

    private LiteralBodyContent literalBodyContent;
    private ObjectBodyContent objectBodyContent;
    private StringBodyContent stringBodyContent;
    private ArrayBodyContent arrayBodyContent;

    @Before
    public void setUp() throws Exception {
        literalBodyContent = literalBody(string);
        objectBodyContent = objectBody(map);
        stringBodyContent = stringBody(string);
        arrayBodyContent = arrayBody(list);
    }

    @Test
    public void matchesReturnsTrueIfLiteralBodyValuesAreEqual() throws Exception {
        assertThat(literalBodyContent.matches(literalBody(string))).isTrue();
    }

    @Test
    public void matchesReturnsTrueIfMapBodyValuesAreEqual() throws Exception {
        assertThat(objectBodyContent.matches(objectBody(map))).isTrue();
    }

    @Test
    public void matchesReturnsTrueIfJsonStringBodyValuesAreEqual() throws Exception {
        assertThat(stringBodyContent.matches(stringBody(string))).isTrue();
    }

    @Test
    public void matchesReturnsTrueIfListBodyValuesAreEqual() throws Exception {
        assertThat(arrayBodyContent.matches(arrayBody(list))).isTrue();
    }

    @Test
    public void matchesReturnsTrueIfMapBodyValuesMatchRegex() throws Exception {
        final Map<String, Object> mapBodyContentBody = objectBodyContent.getContent();
        mapBodyContentBody.clear();
        mapBodyContentBody.put("key", "val.*");

        final ObjectBodyContent matchesRegex = objectBody(singletonMap("key", "value"));

        assertThat(objectBodyContent.matches(matchesRegex)).isTrue();
    }

    @Test
    public void matchesReturnsTrueIfThisBodyContentIsEmptyMapAndThatBodyIsNullMap() throws Exception {
        final Map<String, Object> mapBodyContentBody = objectBodyContent.getContent();
        mapBodyContentBody.clear();

        final ObjectBodyContent nullObjectBodyContent = objectBody(null);

        assertThat(this.objectBodyContent.matches(nullObjectBodyContent)).isTrue();
    }

    @Test
    public void matchesReturnsTrueIfBothMapBodiesContainAValueOfTheSameNumberButOfDifferentTypes() throws Exception {
        final Map<String, Object> mapBodyContentBody = objectBodyContent.getContent();
        mapBodyContentBody.clear();
        mapBodyContentBody.put("key", 10L);

        final ObjectBodyContent numbersMatch = objectBody(singletonMap("key", 10));

        assertThat(objectBodyContent.matches(numbersMatch)).isTrue();
    }

    @Test
    public void matchesReturnsFalseIfMapBodiesDoNotMatch() throws Exception {
        final ObjectBodyContent empty = objectBody(emptyMap());

        assertThat(objectBodyContent.matches(empty)).isFalse();
    }

    @Test
    public void matchesReturnsFalseIfMatchingMapBodyWithABodyContentNotOfThatType() throws Exception {
        assertThat(objectBodyContent.matches(literalBodyContent)).isFalse();
    }

    @Test
    public void matchesReturnsFalseIfMatchingLiteralBodyWithABodyContentNotOfThatType() throws Exception {
        assertThat(literalBodyContent.matches(objectBodyContent)).isFalse();
    }

    @Test
    public void matchesReturnsFalseIfMatchingJsonStringBodyWithABodyContentNotOfThatType() throws Exception {
        assertThat(stringBodyContent.matches(objectBodyContent)).isFalse();
    }

    @Test
    public void matchesReturnsFalseIfMatchingListBodyWithABodyContentNotOfThatType() throws Exception {
        assertThat(stringBodyContent.matches(objectBodyContent)).isFalse();
    }
}