package org.sqg;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

public final class RingBuffer<E> implements Iterable<E> {

    private ArrayBlockingQueue<E> _M_queue;

    public RingBuffer(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException(
                    "parameter \"size\" should be greater thant zero");
        _M_queue = new ArrayBlockingQueue<>(capacity + 1);
    }

    public void write(E e) {
        if (!_M_queue.offer(e)) {
            _M_queue.remove();
            _M_queue.offer(e);
        }
    }

    public Object[] toArray() {
        return _M_queue.toArray();
    }

    public E[] toArray(E[] a) {
        return _M_queue.toArray(a);
    }

    @Override
    public Iterator<E> iterator() {
        return _M_queue.iterator();
    }

    public int size() {
        return _M_queue.size();
    }
}