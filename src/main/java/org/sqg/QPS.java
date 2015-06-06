package org.sqg;

public final class QPS {
    private double _M_min;
    private double _M_max;
    private double _M_total;

    public QPS() {
        _M_min = Double.NaN;
        _M_max = Double.NaN;
        _M_total = 0.0;
    }

    public void update(final double singleWorkerCurrentQPS) {
        _M_min = Double.isNaN(_M_min) ? singleWorkerCurrentQPS : Math.min(
                singleWorkerCurrentQPS, _M_min);
        _M_max = Double.isNaN(_M_max) ? singleWorkerCurrentQPS : Math.max(
                singleWorkerCurrentQPS, _M_max);
        _M_total += Double.isNaN(singleWorkerCurrentQPS) ? 0
                : singleWorkerCurrentQPS;
    }

    public void assign(QPS other) {
        _M_min = other._M_min;
        _M_max = other._M_max;
        _M_total = other._M_total;
    }

    public double getMin() {
        return _M_min;
    }

    public double getMax() {
        return _M_max;
    }

    public double getTotal() {
        return _M_total;
    }

    @Override
    public String toString() {
        return "{min: " + _M_min + ", max: " + _M_max + ", total: "
                + _M_total + "}";
    }
}
