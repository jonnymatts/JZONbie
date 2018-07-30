package com.jonnymatts.jzonbie.pippo;

import com.jonnymatts.jzonbie.requests.Request;
import ro.pippo.core.util.IoUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Collections.list;
import static java.util.Optional.of;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class PippoRequest implements Request {

    private static final String FILE_CONTENT_TYPE = "multipart/form-data";

    private final String protocol;
    private final String url;
    private final int port;
    private final String path;
    private final String method;
    private final Map<String, String> headers;
    private final String body;
    private final Map<String, List<String>> queryMap;
    private final String primingFileContent;

    public PippoRequest(ro.pippo.core.Request request) {
        protocol = request.getScheme();
        url = request.getUrl();
        port = request.getPort();
        path = request.getPath();
        method = request.getMethod();
        headers = createHeaders(request);
        body = request.getBody();
        queryMap = createQueryMap(request);
        primingFileContent = getPrimingFileContentFromRequest(request);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public Map<String, List<String>> getQueryParams() {
        return queryMap;
    }

    @Override
    public String getPrimingFileContent() {
        return primingFileContent;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUrl() {
        return url;
    }

    public int getPort() {
        return port;
    }

    private Map<String,String> createHeaders(ro.pippo.core.Request request) {
        return list(request.getHttpServletRequest().getHeaderNames())
                .stream()
                .collect(
                        toMap(
                                identity(),
                                request::getHeader
                        )
                );
    }

    private Map<String,List<String>> createQueryMap(ro.pippo.core.Request request) {
        return request.getQueryParameters().entrySet()
                .stream()
                .collect(
                        toMap(
                                Entry::getKey,
                                e -> e.getValue().toList()
                        )
                );
    }

    private String getPrimingFileContentFromRequest(ro.pippo.core.Request request) {
        final String contentType = request.getContentType();
        if(contentType == null || !contentType.startsWith(FILE_CONTENT_TYPE) || request.getFiles().isEmpty()) return null;
        return of(request.getFile("priming"))
                .map(fileItem -> {
                    try {
                        return IoUtils.toString(fileItem.getInputStream());
                    } catch (IOException e) {
                        return null;
                    }
                })
                .orElse(null);
    }
}