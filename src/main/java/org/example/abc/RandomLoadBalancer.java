package org.example.abc;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalancer extends LoadBalancer {

  private final Random random = ThreadLocalRandom.current();

  @Override
  public Optional<BackendInstance> get() {
    try {
      lock.readLock().lock();
      if (data.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(
          data.get(
              Math.min(random.nextInt(LOAD_BALANCER_CAPACITY), (getOccupiedCapacity() - 1))
          )
      );
    } finally {
      lock.readLock().unlock();
    }
  }

  int getCapacity() {
    return LOAD_BALANCER_CAPACITY;
  }

  int getOccupiedCapacity() {
    return data.size();
  }
}
