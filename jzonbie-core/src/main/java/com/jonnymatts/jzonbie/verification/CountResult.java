package com.jonnymatts.jzonbie.verification;

public class CountResult {

    private int count;

    public CountResult() {}

    public CountResult(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CountResult that = (CountResult) o;

        return count == that.count;
    }

    @Override
    public int hashCode() {
        return count;
    }
}