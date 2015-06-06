package org.sqg;

public final class Stats {

    private long _M_total;
    private long _M_success;
    private long _M_failure;
    private long _M_timestamp;

    public Stats() {
        this(0L, 0L, 0L, System.nanoTime());
    }

    public Stats(final long total, final long success, final long failure) {
        this(total, success, failure, System.nanoTime());
    }

    public Stats(final long total, final long success, final long failure,
            final long timestamp) {
        _M_total = total;
        _M_success = success;
        _M_failure = failure;
        _M_timestamp = timestamp;
    }

    public long getSuccess() {
        return _M_success;
    }

    public void setSuccess(long value) {
        this._M_success = value;
    }

    public long getTotal() {
        return _M_total;
    }

    public void setTotal(long value) {
        this._M_total = value;
    }

    public long getFailure() {
        return _M_failure;
    }

    public void setFailure(long value) {
        this._M_failure = value;
    }

    public long getTimestamp() {
        return _M_timestamp;
    }

    public void setTimestamp(long value) {
        this._M_timestamp = value;
    }

    @Override
    public String toString() {
        return "{total = " + _M_total + ", success = " + _M_success
                + ", failure = " + _M_failure + "}";
    }
}