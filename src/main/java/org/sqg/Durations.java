package org.sqg;

final class Durations {
    private double _M_min;
    private double _M_max;

    public Durations() {
        _M_min = Double.NaN;
        _M_max = Double.NaN;
    }

    public void update(final double duration) {
        _M_min = Double.isNaN(_M_min) ? duration : Math.min(duration,
                _M_min);
        _M_max = Double.isNaN(_M_max) ? duration : Math.max(duration,
                _M_max);
    }

    public void update(Durations other) {
        _M_min = Double.isNaN(_M_min) ? Double.isNaN(other._M_min) ? Double.NaN
                : other._M_min
                : Math.min(_M_min, other._M_min);
        _M_max = Double.isNaN(_M_max) ? Double.isNaN(other._M_max) ? Double.NaN
                : other._M_max
                : Math.max(_M_max, other._M_max);
    }

    public double getMax() {
        return _M_max;
    }

    public double getMin() {
        return _M_min;
    }

    @Override
    public String toString() {
        return "{min: " + _M_min * 1e-6 + " ms, max: " + _M_max * 1e-6
                + " ms}";
    }
}