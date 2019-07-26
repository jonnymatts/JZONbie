package com.jonnymatts.jzonbie.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.JFixture;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.responses.CurrentPrimingFileResponseFactory.FileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentPrimingFileResponseFactoryTest {

    private static final JFixture FIXTURE = new JFixture();

    private String serializedBody = FIXTURE.create(String.class);

    @Mock private List<PrimedMapping> primedMappings;
    @Mock private ObjectMapper objectMapper;

    @Test
    void createReturnsAFileResponse() throws Exception {
        final CurrentPrimingFileResponseFactory factory = new CurrentPrimingFileResponseFactory(objectMapper);

        when(objectMapper.writeValueAsString(primedMappings)).thenReturn(serializedBody);

        final FileResponse got = factory.create(primedMappings);

        final LocalDateTime localDateTime = LocalDateTime.ofInstant(now(), ZoneId.systemDefault());
        final String expectedFileName = "jzonbie-current-priming-" + localDateTime.toLocalDate();

        assertThat(got.getFileName()).startsWith(expectedFileName);
        assertThat(got.getContents()).isEqualTo(serializedBody);
    }

    @Test
    void createThrowsExceptionIfObjectMapperThrowsException() throws Exception {
        final CurrentPrimingFileResponseFactory factory = new CurrentPrimingFileResponseFactory(objectMapper);

        when(objectMapper.writeValueAsString(primedMappings)).thenThrow(new RuntimeException("blah"));

        assertThatThrownBy(() -> factory.create(primedMappings))
                .hasMessageContaining("Failed")
                .hasCauseInstanceOf(RuntimeException.class);
    }
}