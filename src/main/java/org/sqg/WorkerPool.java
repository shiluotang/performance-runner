package org.sqg;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class WorkerPool implements AutoCloseable {

    final class Sampler implements AutoCloseable {

        private ScheduledExecutorService _M_threadPool;

        long _M_interval;
        TimeUnit _M_timeunit;

        public Sampler(long interval, TimeUnit unit) {
            _M_interval = interval;
            _M_timeunit = unit;
        }

        public void start() {
            synchronized (this) {
                if (_M_threadPool == null) {
                    _M_threadPool = Executors.newScheduledThreadPool(1,
                            new NamedThreadFactory("stats-sampler"));
                    _M_threadPool.scheduleWithFixedDelay(new Runnable() {
                        @Override
                        public void run() {
                            WorkerPool.this.takeSample();
                        }
                    }, 0L, _M_interval, _M_timeunit);
                }
            }
        }

        public void stop() {
            synchronized (this) {
                if (_M_threadPool != null) {
                    _M_threadPool.shutdown();
                    try {
                        _M_threadPool.awaitTermination(1, TimeUnit.SECONDS);
                    } catch (InterruptedException ignore) {
                    }
                    _M_threadPool.shutdownNow();
                }
            }
        }

        @Override
        public void close() {
            stop();
        }
    }

    

    private ExecutorService _M_threadPool;
    private Sampler _M_sampler;
    private TesterFactory _M_factory;

    private QPS _M_totalQPS;
    private QPS _M_successQPS;
    private QPS _M_failureQPS;
    private Durations _M_totalDurations;
    private Durations _M_successDurations;
    private Durations _M_failureDurations;

    private final Queue<Worker> _M_workers;
    private final AtomicInteger _M_size;

    public WorkerPool(TesterFactory factory) {
        _M_factory = factory;
        _M_workers = new ConcurrentLinkedQueue<>();
        _M_size = new AtomicInteger(0);
        _M_threadPool = Executors.newCachedThreadPool(new NamedThreadFactory(
                "qps-worker-pool"));

        _M_totalQPS = new QPS();
        _M_successQPS = new QPS();
        _M_failureQPS = new QPS();

        _M_totalDurations = new Durations();
        _M_successDurations = new Durations();
        _M_failureDurations = new Durations();

        _M_sampler = new Sampler(200L, TimeUnit.MILLISECONDS);
        _M_sampler.start();
    }

    public void changeWorkersBy(int delta) {
        if (delta > 0) {
            for (int i = 0; i < delta; ++i) {
                Worker worker = new Worker(_M_factory.newTester());
                if (_M_workers.offer(worker)) {
                    _M_size.incrementAndGet();
                }
                _M_threadPool.execute(worker);
            }
        } else {
            for (int i = 0; i > delta; --i) {
                Worker worker = _M_workers.poll();
                if (worker != null) {
                    _M_size.decrementAndGet();
                    worker.stop();
                } else
                    break;
            }
        }
    }

    @Override
    public void close() {
        changeWorkersBy(Integer.MIN_VALUE);
        if (_M_sampler != null) {
            _M_sampler.close();
            _M_sampler = null;
        }
        if (_M_threadPool != null) {
            _M_threadPool.shutdown();
            try {
                _M_threadPool.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
            // shutdown anyway.
            _M_threadPool.shutdownNow();
            _M_threadPool = null;
        }
    }

    public int size() {
        return _M_size.get();
    }

    public Stats getStats() {
        Stats stats = new Stats();
        for (Worker worker : _M_workers) {
            stats.setTotal(stats.getTotal() + worker.getTotal());
            stats.setSuccess(stats.getSuccess() + worker.getSuccess());
            stats.setFailure(stats.getFailure() + worker.getFailure());
        }
        return stats;
    }

    protected Stats takeSample() {
        long total = 0, success = 0, failure = 0;
        Stats stats = null;
        for (Worker worker : _M_workers) {
            stats = worker.takeSample();
            total += stats.getTotal();
            success += stats.getSuccess();
            failure += stats.getFailure();
        }
        stats = new Stats();
        stats.setTotal(total);
        stats.setSuccess(success);
        stats.setFailure(failure);
        return stats;
    }

    protected void updateQPS() {
        QPS totalQPS = new QPS();
        QPS successQPS = new QPS();
        QPS failureQPS = new QPS();

        for (Worker worker : _M_workers) {
            worker.updateQPS();
            totalQPS.update(worker.getCurrentTotalQPS());
            successQPS.update(worker.getCurrentSuccessQPS());
            failureQPS.update(worker.getCurrentFailureQPS());
            _M_totalDurations.update(worker.getTotalDurations());
            _M_successDurations.update(worker.getSuccessDurations());
            _M_failureDurations.update(worker.getFailureDurations());
        }
        _M_totalQPS.assign(totalQPS);
        _M_successQPS.assign(successQPS);
        _M_failureQPS.assign(failureQPS);
    }

    public QPS getTotalQPS() {
        return _M_totalQPS;
    }

    public QPS getSuccessQPS() {
        return _M_successQPS;
    }

    public QPS getFailureQPS() {
        return _M_failureQPS;
    }

    public Durations getTotalDurations() {
        return _M_totalDurations;
    }

    public Durations getSuccessDuration() {
        return _M_successDurations;
    }

    public Durations getFailureDurations() {
        return _M_failureDurations;
    }
}
