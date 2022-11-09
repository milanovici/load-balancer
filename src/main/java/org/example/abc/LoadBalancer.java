package org.example.abc;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class LoadBalancer {

  final List<BackendInstance> data = new CopyOnWriteArrayList<>();

  final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  static final int LOAD_BALANCER_CAPACITY = 10;

  public boolean register(final BackendInstance backendInstance) {
    try {
      lock.writeLock().lock();;
      if (data.size() >= LOAD_BALANCER_CAPACITY) {
        return false;
      }

      if (data.contains(backendInstance)) {
        return false;
      }

      return data.add(backendInstance);
    } finally {
      lock.writeLock().unlock();;
    }
  }

  public abstract Optional<BackendInstance> get();
}
