package com.jonnymatts.jzonbie.templating;

import com.jonnymatts.jzonbie.pippo.PippoRequest;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public class TransformationContext {

    public TransformationContext(PippoRequest request) {
        this(request.getProtocol(), request.getUrl(), request.getPort(),request.getPath(), request.getQueryParams(), request.getHeaders(), request.getMethod(), request.getBody());
    }

    public TransformationContext(String protocol, String url, int port, String path, Map<String, List<String>> queryParams, Map<String, String> headers, String method, String body) {
        this.request = new RequestContext(protocol, url, port, path, queryParams, headers, method, body);
    }

    public RequestContext request;

    public RequestContext getRequest() {
        return request;
    }

    public static class RequestContext {
        private final String protocol;
        private final String url;
        private final String baseUrl;
        private final String host;
        private final int port;
        private final String path;
        private final List<String> pathSegment;
        private final Map<String, List<String>> queryParam;
        private final Map<String, String> header;
        private final String method;
        private final String body;

        public RequestContext(String protocol, String url, int port, String path, Map<String, List<String>> queryParams, Map<String, String> headers, String method, String body) {
            this.protocol = protocol;
            this.url = url;
            this.host = url.split("/")[2].split(":")[0];
            this.port = port;
            this.baseUrl = format("%s://%s%s", protocol, host, port == 0 ? "" : ":" + port);
            this.path = path;
            this.pathSegment = asList(path.substring(1).split("/"));
            this.queryParam = queryParams;
            this.header = headers;
            this.method = method;
            this.body = body;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getUrl() {
            return url;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getPath() {
            return path;
        }

        public List<String> getPathSegment() {
            return pathSegment;
        }

        public Map<String, List<String>> getQueryParam() {
            return queryParam;
        }

        public Map<String, String> getHeader() {
            return header;
        }

        public String getMethod() {
            return method;
        }

        public String getBody() {
            return body;
        }
    }
}