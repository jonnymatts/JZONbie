package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.PrimingContext;
import com.jonnymatts.jzonbie.response.DefaultingQueue;
import com.jonnymatts.jzonbie.util.AppRequestBuilderUtil;
import com.jonnymatts.jzonbie.util.AppResponseBuilderUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static com.jonnymatts.jzonbie.response.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PrimedMappingUploaderTest {

    private static final AppRequest APP_REQUEST_1 = AppRequestBuilderUtil.getFixturedAppRequest();
    private static final AppRequest APP_REQUEST_2 = AppRequestBuilderUtil.getFixturedAppRequest();

    private static final AppResponse APP_RESPONSE_1 = AppResponseBuilderUtil.getFixturedAppResponse();
    private static final AppResponse APP_RESPONSE_2 = AppResponseBuilderUtil.getFixturedAppResponse();

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