package exchange.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import exchange.domain.Order;
import exchange.domain.OrderDirection;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

final class ExchangeSystemImpl implements ExchangeSystem{

  private static final Comparator<Order> PRICE_COMPARATOR = Comparator.comparing(exchange.domain.Order::getPrice);

  private ListMultimap<String, Order> ricOrdersMappings = ArrayListMultimap.create();

  private ConcurrentHashMap<String, ReadWriteLock> ricLockMapping = new ConcurrentHashMap<>();

  private final AverageExecutionManager averageExecutionManager;
  private final ExecutedQuantityManager executedQuantityManager;

  ExchangeSystemImpl() {
    this.averageExecutionManager = new AverageExecutionManagerImpl();
    this.executedQuantityManager = new ExecutedQuantityManagerImpl();
  }

  @Override
  public void addOrder(Order order) {
    ReadWriteLock lock = getLock(order.getRic());
    Order matchingOrder = null;
    lock.writeLock().lock();
    try {
      // fetch all the matching orders for the given order
      List<Order> matchingOrders = ricOrdersMappings.get(order.getRic()).stream()
          .filter(currentOrder -> !currentOrder.getOrderDirection().equals(order.getOrderDirection()))
          .filter(currentOrder -> currentOrder.getQuantity().equals(order.getQuantity()))
          .filter(currentOrder -> !currentOrder.getUser().equals(order.getUser()))
          .filter(currentOrder -> {
            // we can't match BUY orders with SELL orders that have a higher price.
            if (order.getOrderDirection().equals(OrderDirection.BUY) && order.getPrice().compareTo(currentOrder.getPrice()) < 0 ) {
              return false;
            }
            // we can't match SELL orders with BUY orders that have a higher price.
            if (order.getOrderDirection().equals(OrderDirection.SELL) && order.getPrice().compareTo(currentOrder.getPrice()) > 0 ) {
              return false;
            }
            return true;
          })
          .collect(Collectors.toList());
      if (!matchingOrders.isEmpty()) {
        // stable sorting the matching orders
        matchingOrders.sort(PRICE_COMPARATOR);
        if (OrderDirection.BUY.equals(order.getOrderDirection())) {
          // pick the order with the smallest price
          matchingOrder = matchingOrders.get(0);
        }
        else {
          // pick the order with the largest price
          matchingOrder = matchingOrders.get(matchingOrders.size() - 1);
        }
        ricOrdersMappings.remove(matchingOrder.getRic(), matchingOrder);
      }
      else {
        ricOrdersMappings.put(order.getRic(), order);
      }
    }
    finally {
      lock.writeLock().unlock();
    }
    if (matchingOrder != null) { // if a match was found and an execution performed then we need to
      // calculate the average execution price and executed quantity
      averageExecutionManager.addExecutionPrice(order);
      if (OrderDirection.SELL.equals(order.getOrderDirection())) {
        executedQuantityManager.addExecutedQuantity(order, matchingOrder);
      }
      else {
        executedQuantityManager.addExecutedQuantity(matchingOrder, order);
      }
    }
  }

  @Override
  public List<Order> getOpenInterest(String ric, OrderDirection orderDirection) {
    ReadWriteLock lock = getLock(ric);
    lock.readLock().lock();
    try {
      return ricOrdersMappings.get(ric).stream()
          .filter(order -> order.getOrderDirection().equals(orderDirection))
          .collect(Collectors.toList());
    } finally {
      lock.readLock().unlock();
    }
  }

  private ReadWriteLock getLock(String ric) {
    ricLockMapping.putIfAbsent(ric, new ReentrantReadWriteLock());
    return ricLockMapping.get(ric);
  }

  @Override
  public BigDecimal averagePrice(String ric) {
    return averageExecutionManager.getAverageExecutionPrice(ric);
  }

  @Override
  public Long executedQuantity(String ric, String user) {
    return executedQuantityManager.getExecutedQuantity(ric, user);
  }
}
