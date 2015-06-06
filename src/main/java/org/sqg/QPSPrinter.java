package org.sqg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface QPSPrinter {

    public static final QPSPrinter SLF4J = new SLF4JLoggerPrinter();
    public static final QPSPrinter STDOUT = new StdOutLoggerPrinter();

    void print(int nworkers,
            QPS totalQPS,
            QPS successQPS,
            QPS failureQPS,
            Stats stats,
            Durations totalDurations,
            Durations successDurations,
            Durations failureDurations);

    static final class SLF4JLoggerPrinter implements QPSPrinter {

        static final Logger LOGGER = LoggerFactory.getLogger(SLF4JLoggerPrinter.class);

        @Override
        public void print(int nworkers,
            QPS totalQPS,
            QPS successQPS,
            QPS failureQPS,
            Stats stats,
            Durations totalDurations,
            Durations successDurations,
            Durations failureDurations) {

            LOGGER.info("Workers        = {}", nworkers);
            LOGGER.info("QPS(Total)     = {}", totalQPS);
            LOGGER.info("QPS(Success)   = {}", successQPS);
            LOGGER.info("QPS(Failure)   = {}", failureQPS);
            LOGGER.info("Stats          = {}", stats);
            LOGGER.info("Duration(T)    = {}", totalDurations);
            LOGGER.info("Duration(S)    = {}", successDurations);
            LOGGER.info("Duration(F)    = {}", failureDurations);
        }
    }

    static final class StdOutLoggerPrinter implements QPSPrinter {

        @Override
        public void print(int nworkers,
            QPS totalQPS,
            QPS successQPS,
            QPS failureQPS,
            Stats stats,
            Durations totalDurations,
            Durations successDurations,
            Durations failureDurations) {

            System.out.println("Workers        = " + nworkers);
            System.out.println("QPS(Total)     = " + totalQPS);
            System.out.println("QPS(Success)   = " + successQPS);
            System.out.println("QPS(Failure)   = " + failureQPS);
            System.out.println("Stats          = " + stats);
            System.out.println("Duration(T)    = " + totalDurations);
            System.out.println("Duration(S)    = " + successDurations);
            System.out.println("Duration(F)    = " + failureDurations);
        }
    }
}
