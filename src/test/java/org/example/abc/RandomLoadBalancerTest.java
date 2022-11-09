package org.example.abc;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomLoadBalancerTest {

  private final RandomLoadBalancer loadBalancer = new RandomLoadBalancer();

  @Test
  public void testShouldReturnsEmptyOptionalIfLoadBalancerIsEmpty() {
    assertEquals(Optional.empty(), loadBalancer.get());
  }

  @Test
  public void testShouldGetRegisteredInstance() {
    BackendInstance backendInstance = new BackendInstance("1.2.3.4");
    assertTrue(loadBalancer.register(backendInstance));
    assertEquals(backendInstance, loadBalancer.get().get());
  }

  @Test
  public void testShouldNotContainDuplicates() {
    BackendInstance backendInstance = new BackendInstance("1.2.3.4");

    assertTrue(loadBalancer.register(backendInstance));
    assertFalse(loadBalancer.register(backendInstance));
  }

  @Test
  public void testShouldNotAcceptMoreThen10Instances() {
    BackendInstance backendInstance = new BackendInstance("1.2.3.4");
    for (int i = 0; i < 10; i++) {
      backendInstance = new BackendInstance("1.2.3." + (i + 5));
      assertTrue(loadBalancer.register(backendInstance));
    }
    assertFalse(loadBalancer.register(backendInstance));
  }

  @Test
  public void testShouldBeResistantToConcurrentRegisterAndGet() throws InterruptedException {
    final ExecutorService executorService = Executors.newFixedThreadPool(50);
    final AtomicLong atomicLong = new AtomicLong(1);
    for (int i = 0; i < 1000 ; ++i) {
      executorService.submit(() -> {
        loadBalancer.register(new BackendInstance(atomicLong.incrementAndGet() + ""));
      });
    }

    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.SECONDS);
    assertEquals(loadBalancer.getCapacity(), loadBalancer.getOccupiedCapacity());
  }

  @Test
  public void shouldHaveNormalDistribution() {
    final AtomicLong atomicLong = new AtomicLong(0);
    for (int i = 0; i < 10 ; ++i) {
        loadBalancer.register(new BackendInstance(atomicLong.incrementAndGet() + ""));
    }

    final Map<String, Long> data = new HashMap<>();
    final int ITERATIONS = 1_000_000;
    final int LOWER_MARGIN = 99;
    final int UPPER_MARGIN = 101;

    for (int i = 0; i < ITERATIONS; ++i) {
      Optional<BackendInstance> backendInstance = loadBalancer.get();
      String ipAddress = backendInstance.get().getAddress();
      if (data.containsKey(ipAddress)) {
        Long count = data.get(ipAddress);
        count++;
        data.put(ipAddress, count);
      } else {
        data.put(ipAddress, 1L);
      }
    }

    for (long occurrences : data.values()) {
      assertTrue(occurrences >= (ITERATIONS / loadBalancer.getCapacity() * LOWER_MARGIN / 100));
      assertTrue(occurrences <= (ITERATIONS / loadBalancer.getCapacity() * UPPER_MARGIN / 100));
    }
  }
}