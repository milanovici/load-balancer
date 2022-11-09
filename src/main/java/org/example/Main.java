package org.example;

import org.example.abc.RandomLoadBalancer;

public class Main {
  public static void main(String[] args) {
    RandomLoadBalancer loadBalancer = new RandomLoadBalancer();
    loadBalancer.get();
    System.out.println("Hello world!");
  }

  /***
   * 1. Create a LoadBalancer class that has a method to register backend instances
   * 	Each backend instance address should be unique, it should not be possible to register the same address two times
   *
   * 	The code should be production ready
   * 	The Loadbalancer will be released as a library
   *
   * 2.Develop an algorithm that, when invoking the Load Balancer's get() method multiple times,should return one backend-instance choosing
   * 	between the registered ones randomly.
   */
}