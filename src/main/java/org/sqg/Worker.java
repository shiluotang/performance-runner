package org.sqg;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Worker implements AutoCloseable, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);

    private volatile boolean _M_receivedStopSignal;

    private long _M_startTimestamp;
    private long _M_stopTimestamp;
    private long _M_success;
    private long _M_failure;
    private long _M_total;

    private final RingBuffer<Stats> _M_ringBuffer;
    private double _M_currentTotalQPS;
    private double _M_currentSuccessQPS;
    private double _M_currentFailureQPS;

    private Durations _M_totalDurations;
    private Durations _M_successDurations;
    private Durations _M_failureDurations;

    /**
     * Single address service client.
     */
    private Tester _M_tester;

    public Worker(Tester tester) {
        _M_tester = tester;
        _M_startTimestamp = _M_stopTimestamp = 0L;
        _M_success = _M_failure = _M_total = 0L;
        _M_ringBuffer = new RingBuffer<>(10);

        _M_totalDurations = new Durations();
        _M_successDurations = new Durations();
        _M_failureDurations = new Durations();
    }

    public void stop() {
        _M_receivedStopSignal = true;
        LOGGER.info("send stop signal to {}", this);
        if (_M_tester != null) {
            LOGGER.info("close tester");
            _M_tester.close();
            _M_tester = null;
        }
    }

    @Override
    public void close() {
        stop();
    }

    @Override
    public void run() {
        _M_receivedStopSignal = false;
        _M_startTimestamp = System.nanoTime();
        long total = 0, success = 0, failure = 0;
        double timestamp, duration = Double.NaN;
        boolean result;
        LOGGER.info("{} starts in thread {}.", this, Thread.currentThread());
        while (!_M_receivedStopSignal) {
            try {
                ++total;
                timestamp = System.nanoTime();
                result = _M_tester.test();
                duration = System.nanoTime() - timestamp;
                if (result) {
                    _M_successDurations.update(duration);
                    ++success;
                } else {
                    _M_failureDurations.update(duration);
                    ++failure;
                }
                _M_totalDurations.update(duration);
            } catch (Exception e) {
                duration = System.nanoTime();
                if (_M_receivedStopSignal)
                    break;
                LOGGER.warn(e.getMessage(), e);
                _M_failureDurations.update(duration);
                _M_totalDurations.update(duration);
                ++failure;
            }
            _M_total = total;
            _M_success = success;
            _M_failure = failure;
        }
        _M_stopTimestamp = System.nanoTime();
        LOGGER.info("{} stopped in thread {}, uptime is {} s.", this,
                Thread.currentThread(),
                (_M_stopTimestamp - _M_startTimestamp) * 1e-9);
        _M_receivedStopSignal = false;
    }

    public long getTotal() {
        return _M_total;
    }

    public long getSuccess() {
        return _M_success;
    }

    public long getFailure() {
        return _M_failure;
    }

    public long getStartTimetstamp() {
        return _M_startTimestamp;
    }

    public long getStopTimestamp() {
        return _M_stopTimestamp;
    }

    public TimeUnit getTimestampResolution() {
        return TimeUnit.NANOSECONDS;
    }

    public Stats takeSample() {
        Stats stats = new Stats(_M_total, _M_success, _M_failure);
        _M_ringBuffer.write(stats);
        return stats;
    }

    public void updateQPS() {
        if (_M_ringBuffer.size() < 2) {
            _M_currentTotalQPS = Double.NaN;
            _M_currentSuccessQPS = Double.NaN;
            _M_currentFailureQPS = Double.NaN;
            return;
        }
        Stats[] snapshot = _M_ringBuffer
                .toArray(new Stats[_M_ringBuffer.size()]);
        if (snapshot.length < 2) {
            _M_currentTotalQPS = Double.NaN;
            _M_currentSuccessQPS = Double.NaN;
            _M_currentFailureQPS = Double.NaN;
            return;
        }
        Stats first = snapshot[0];
        Stats last = snapshot[snapshot.length - 1];
        _M_currentTotalQPS = totalQPS(first, last);
        _M_currentSuccessQPS = successQPS(first, last);
        _M_currentFailureQPS = failureQPS(first, last);
    }

    private double totalQPS(Stats before, Stats after) {
        return (double) (after.getTotal() - before.getTotal())
                / (after.getTimestamp() - before.getTimestamp()) * 1e9;
    }

    private double successQPS(Stats before, Stats after) {
        return (double) (after.getSuccess() - before.getSuccess())
                / (after.getTimestamp() - before.getTimestamp()) * 1e9;
    }

    private double failureQPS(Stats before, Stats after) {
        return (double) (after.getFailure() - before.getFailure())
                / (after.getTimestamp() - before.getTimestamp()) * 1e9;
    }

    public double getCurrentTotalQPS() {
        return _M_currentTotalQPS;
    }

    public double getCurrentSuccessQPS() {
        return _M_currentSuccessQPS;
    }

    public double getCurrentFailureQPS() {
        return _M_currentFailureQPS;
    }

    public Durations getTotalDurations() {
        return _M_totalDurations;
    }

    public Durations getSuccessDurations() {
        return _M_successDurations;
    }

    public Durations getFailureDurations() {
        return _M_failureDurations;
    }
}
