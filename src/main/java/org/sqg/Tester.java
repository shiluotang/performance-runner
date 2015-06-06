package org.sqg;

public interface Tester extends AutoCloseable {

    boolean test();

    @Override
    void close();
}
