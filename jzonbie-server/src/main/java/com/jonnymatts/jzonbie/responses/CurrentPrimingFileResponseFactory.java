package com.jonnymatts.jzonbie.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.Body;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.priming.PrimedMapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class CurrentPrimingFileResponseFactory {

    private final ObjectMapper objectMapper;

    public CurrentPrimingFileResponseFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public FileResponse create(List<PrimedMapping> primedMappings) {
        try {
            final String fileName = createFileName();
            final String fileContents = objectMapper.writeValueAsString(primedMappings);
            return new FileResponse(fileName, fileContents);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize current priming", e);
        }
    }

    private String createFileName() {
        final Instant now = Instant.now();
        return "jzonbie-current-priming-" + LocalDateTime.ofInstant(now, ZoneId.systemDefault());
    }

    public static class FileResponse implements Response {
        private String fileName;
        private String contents;

        public FileResponse(String fileName, String contents) {
            this.fileName = fileName;
            this.contents = contents;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContents() {
            return contents;
        }

        @Override
        public int getStatusCode() {
            return 0;
        }

        @Override
        public Map<String, String> getHeaders() {
            return null;
        }

        @Override
        public Body<?> getBody() {
            return null;
        }

        @Override
        public boolean isTemplated() {
            return false;
        }

    }
}