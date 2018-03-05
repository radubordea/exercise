package exchange.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import exchange.domain.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class AverageExecutionManagerImpl implements AverageExecutionManager {

  private ListMultimap<String, Order> ricOrderListMapping = ArrayListMultimap.create();

  private static final ReadWriteLock lock = new ReentrantReadWriteLock();

  @Override
  public void addExecutionPrice(Order order) {
    lock.writeLock().lock();
    try {
      ricOrderListMapping.put(order.getRic(), order);
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public BigDecimal getAverageExecutionPrice(String ric) {
    lock.readLock().lock();
    try {
      if (ricOrderListMapping.get(ric).isEmpty()) {
        return BigDecimal.ZERO;
      }
      BigDecimal total = ricOrderListMapping.get(ric).stream()
          .map(order -> order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())))
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      BigDecimal quantityTotal = BigDecimal.valueOf(ricOrderListMapping.get(ric).stream().mapToLong(Order::getQuantity).sum());
      if (quantityTotal.equals(BigDecimal.ZERO)){
        return BigDecimal.ZERO;
      }
      return total.divide(quantityTotal,4, RoundingMode.HALF_UP);
    } finally {
      lock.readLock().unlock();
    }
  }
}
