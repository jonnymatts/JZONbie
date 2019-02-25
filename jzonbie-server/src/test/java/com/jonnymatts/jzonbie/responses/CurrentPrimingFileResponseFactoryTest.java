package com.jonnymatts.jzonbie.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.responses.CurrentPrimingFileResponseFactory.FileResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CurrentPrimingFileResponseFactoryTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();
    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Fixture private String seriliazedBody;

    @Mock private List<PrimedMapping> primedMappings;
    @Mock private ObjectMapper objectMapper;

    @Test
    public void createReturnsAFileResponse() throws Exception {
        final CurrentPrimingFileResponseFactory factory = new CurrentPrimingFileResponseFactory(objectMapper);

        when(objectMapper.writeValueAsString(primedMappings)).thenReturn(seriliazedBody);

        final FileResponse got = factory.create(primedMappings);

        final LocalDateTime localDateTime = LocalDateTime.ofInstant(now(), ZoneId.systemDefault());
        final String expectedFileName = "jzonbie-current-priming-" + localDateTime.toLocalDate();

        assertThat(got.getFileName()).startsWith(expectedFileName);
        assertThat(got.getContents()).isEqualTo(seriliazedBody);
    }

    @Test
    public void createThrowsExceptionIfObjectMapperThrowsException() throws Exception {
        final CurrentPrimingFileResponseFactory factory = new CurrentPrimingFileResponseFactory(objectMapper);

        when(objectMapper.writeValueAsString(primedMappings)).thenThrow(new RuntimeException("blah"));

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("blah");

        factory.create(primedMappings);
    }
}