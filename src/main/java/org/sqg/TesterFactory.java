package org.sqg;

public interface TesterFactory extends AutoCloseable {

    Tester newTester(); 

    @Override
    void close();
} 
