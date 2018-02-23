package exchange.service;

import exchange.domain.Order;
import exchange.domain.RicUserKey;

import java.util.HashMap;
import java.util.Map;

public class ExecutedQuantityManagerImpl implements ExecutedQuantityManager {

    Map<RicUserKey, Long> ricUserQuantityMap = new HashMap<>();

    @Override
    public void addExecutedQuantity(Order sellOrder, Order buyOrder) {
        ricUserQuantityMap.put(buyOrder.getRicUserKey(),
                ricUserQuantityMap.get(buyOrder.getRicUserKey()) == null ?
                        buyOrder.getQuantity() :
                        ricUserQuantityMap.get(buyOrder.getRicUserKey()) + buyOrder.getQuantity());
        ricUserQuantityMap.put(sellOrder.getRicUserKey(),
                ricUserQuantityMap.get(sellOrder.getRicUserKey()) == null ?
                -sellOrder.getQuantity() :
                ricUserQuantityMap.get(sellOrder.getRicUserKey()) - sellOrder.getQuantity());
    }

    @Override
    public long getExecutedQuantity(String ric, String user) {
        return ricUserQuantityMap.get(new RicUserKey(ric, user)) == null ?
                0 : ricUserQuantityMap.get(new RicUserKey(ric, user));
    }
}
