package com.jonnymatts.jzonbie;

public class PrimingKey {
    private final String path;
    private final PrimedRequest primedRequest;

    public PrimingKey(String path, PrimedRequest primedRequest) {
        this.path = path;
        this.primedRequest = primedRequest;
    }

    public String getPath() {
        return path;
    }

    public PrimedRequest getPrimedRequest() {
        return primedRequest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimingKey that = (PrimingKey) o;

        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        return primedRequest != null ? primedRequest.equals(that.primedRequest) : that.primedRequest == null;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (primedRequest != null ? primedRequest.hashCode() : 0);
        return result;
    }
}
