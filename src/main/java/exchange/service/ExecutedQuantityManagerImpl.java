package exchange.service;

import com.google.common.util.concurrent.AtomicLongMap;
import exchange.domain.Order;
import exchange.domain.RicUserKey;

public class ExecutedQuantityManagerImpl implements ExecutedQuantityManager {

  private AtomicLongMap<RicUserKey> ricUserQuantityMap = AtomicLongMap.create();

  @Override
  public void addExecutedQuantity(Order sellOrder, Order buyOrder) {
    ricUserQuantityMap.addAndGet(buyOrder.getRicUserKey(), buyOrder.getQuantity());
    ricUserQuantityMap.addAndGet(sellOrder.getRicUserKey(), -sellOrder.getQuantity());
  }

  @Override
  public long getExecutedQuantity(String ric, String user) {
    return ricUserQuantityMap.get(new RicUserKey(ric, user));
  }
}
