package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.Request;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.metadata.MetaDataContext;

public interface RequestHandler {

    Response handle(Request request, MetaDataContext metaDataContext);
}
