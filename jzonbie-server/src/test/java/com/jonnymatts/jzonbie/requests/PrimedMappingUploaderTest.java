package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.DefaultingQueue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.requests.AppRequest.post;
import static com.jonnymatts.jzonbie.responses.AppResponse.notFound;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PrimedMappingUploaderTest {

    private static final AppRequest APP_REQUEST_1 = get("/").build();
    private static final AppRequest APP_REQUEST_2 = post("/").build();

    private static final AppResponse APP_RESPONSE_1 = ok().build();
    private static final AppResponse APP_RESPONSE_2 = notFound().build();

    @Mock private PrimingContext primingContext;

    private PrimedMappingUploader uploader;

    @Before
    public void setUp() throws Exception {
         uploader = new PrimedMappingUploader(primingContext);
    }

    @Test
    public void uploadAddsPrimedMappingsToPrimingContext() throws Exception {
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