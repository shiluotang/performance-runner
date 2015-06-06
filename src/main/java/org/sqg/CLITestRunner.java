package org.sqg;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Objects;

public final class CLITestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLITestRunner.class);

    TesterFactory _M_factory;
    QPSPrinter _M_printer;

    public CLITestRunner(TesterFactory factory, QPSPrinter printer) {
        _M_factory = factory;
        _M_printer = printer == null ? QPSPrinter.SLF4J : printer;
    }

    public CLITestRunner(TesterFactory factory) {
        this(Objects.requireNonNull(factory), null);
    }

    public void startTest() throws InterruptedException {
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(
                1, new NamedThreadFactory("qps-printer"));
        try (final WorkerPool pool = new WorkerPool(_M_factory)) {
            pool.changeWorkersBy(1);
            threadPool.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        pool.updateQPS();
                        Stats stats = pool.getStats();

                        _M_printer.print(
                            pool.size(),
                            pool.getTotalQPS(),
                            pool.getSuccessQPS(),
                            pool.getFailureQPS(),
                            stats,
                            pool.getTotalDurations(),
                            pool.getSuccessDuration(),
                            pool.getFailureDurations());
                    } catch (Exception e) {
                        LOGGER.warn(e.getMessage(), e);
                    }
                }
            }, 0L, 1L, TimeUnit.SECONDS);
            String line = null;
            do {
                try {
                    line = readLine();
                } catch (IOException e) {
                    LOGGER.info(e.getMessage(), e);
                }
                if (line == null)
                    continue;
                line = line.trim();
                if (line.startsWith("+") || line.startsWith("-")) {
                    int delta = 0;
                    try {
                        delta = Integer.parseInt(line);
                        if (delta != 0)
                            pool.changeWorkersBy(delta);
                    } catch (NumberFormatException e) {
                    }
                } else if ("quit".equalsIgnoreCase(line)
                        || "q".equalsIgnoreCase(line)) {
                    break;
                }
            } while (true);
        } finally {
            threadPool.shutdown();
            threadPool.awaitTermination(2, TimeUnit.SECONDS);
            threadPool.shutdownNow();
        }
    }

    private String readLine() throws IOException {
        return new BufferedReader(new InputStreamReader(System.in)).readLine();
    }
}
