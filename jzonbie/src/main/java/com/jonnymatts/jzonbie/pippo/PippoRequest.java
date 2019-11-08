package com.jonnymatts.jzonbie.pippo;

import com.jonnymatts.jzonbie.Request;
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
        this(request.getScheme(),
                request.getUrl(),
                request.getPort(),
                request.getPath(),
                request.getMethod(),
                createHeaders(request),
                request.getBody(),
                createQueryMap(request),
                getPrimingFileContentFromRequest(request));
    }
    public PippoRequest(String protocol, String url, int port, String path, String method, Map<String, String> headers, String body, Map<String, List<String>> queryMap, String primingFileContent) {
        this.protocol = protocol;
        this.url = url;
        this.port = port;
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.body = body;
        this.queryMap = queryMap;
        this.primingFileContent = primingFileContent;
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

    private static Map<String, String> createHeaders(ro.pippo.core.Request request) {
        return list(request.getHttpServletRequest().getHeaderNames())
                .stream()
                .collect(
                        toMap(
                                identity(),
                                request::getHeader
                        )
                );
    }

    private static Map<String, List<String>> createQueryMap(ro.pippo.core.Request request) {
        return request.getQueryParameters().entrySet()
                .stream()
                .collect(
                        toMap(
                                Entry::getKey,
                                e -> e.getValue().toList()
                        )
                );
    }

    private static String getPrimingFileContentFromRequest(ro.pippo.core.Request request) {
        final String contentType = request.getContentType();
        if (contentType == null || !contentType.startsWith(FILE_CONTENT_TYPE) || request.getFiles().isEmpty())
            return null;
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