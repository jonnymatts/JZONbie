package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.requests.AppRequest.post;
import static com.jonnymatts.jzonbie.responses.AppResponse.notFound;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse.staticDefault;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PrimedMappingUploaderTest {

    private static final AppRequest APP_REQUEST_1 = get("/").build();
    private static final AppRequest APP_REQUEST_2 = post("/").build();

    private static final AppResponse APP_RESPONSE_1 = ok().build();
    private static final AppResponse APP_RESPONSE_2 = notFound().build();

    @Mock private PrimingContext primingContext;

    private PrimedMappingUploader uploader;

    @BeforeEach
    void setUp() throws Exception {
         uploader = new PrimedMappingUploader(primingContext);
    }

    @Test
    void uploadAddsPrimedMappingsToPrimingContext() throws Exception {
        final List<PrimedMapping> primedMappings = asList(
                new PrimedMapping(APP_REQUEST_1, new DefaultingQueue() {{
                    add(APP_RESPONSE_1);
                    setDefault(staticDefault(APP_RESPONSE_2));
                }}),
                new PrimedMapping(APP_REQUEST_2, new DefaultingQueue() {{
                    add(APP_RESPONSE_2);
                    setDefault(staticDefault(APP_RESPONSE_1));
                }})
        );

        uploader.upload(primedMappings);

        verify(primingContext).add(APP_REQUEST_1, APP_RESPONSE_1);
        verify(primingContext).addDefault(APP_REQUEST_1, staticDefault(APP_RESPONSE_2));
        verify(primingContext).add(APP_REQUEST_2, APP_RESPONSE_2);
        verify(primingContext).addDefault(APP_REQUEST_2, staticDefault(APP_RESPONSE_1));
    }
}