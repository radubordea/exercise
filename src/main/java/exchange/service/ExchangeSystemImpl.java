package exchange.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import exchange.domain.Order;
import exchange.domain.OrderDirection;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

final class ExchangeSystemImpl implements ExchangeSystem{

    private static final Comparator<Order> PRICE_COMPARATOR = (Order o1, Order o2) -> o1.getPrice().compareTo(o2.getPrice());

    private ListMultimap<String, Order> ricOrdersMappings = ArrayListMultimap.create();

    private final AverageExecutionManager averageExecutionManager;
    private final ExecutedQuantityManager executedQuantityManager;

    ExchangeSystemImpl(AverageExecutionManager averageExecutionCalculator, ExecutedQuantityManager executedQuantityCalculator) {
        this.averageExecutionManager = averageExecutionCalculator;
        this.executedQuantityManager = executedQuantityCalculator;
    }

    public synchronized void addOrder(Order order) {
        // fetch all the orders for the given order ric
        List<Order> ricOrders = ricOrdersMappings.get(order.getRic());
        List<Order> matchingOrders = ricOrders.stream()
                .filter(currentOrder -> !currentOrder.getOrderDirection().equals(order.getOrderDirection()))
                .filter(currentOrder -> currentOrder.getQuantity().equals(order.getQuantity()))
                .filter(currentOrder -> !currentOrder.getUser().equals(order.getUser()))
                .sorted(PRICE_COMPARATOR)
                .collect(Collectors.toList());

        if (!matchingOrders.isEmpty()) {
            switch (order.getOrderDirection()) {
                case BUY:
                    // we pick the order with the smallest price
                    Order sellOrder = ricOrders.get(0);

                    // remove the sell order;
                    ricOrdersMappings.remove(sellOrder.getRic(), sellOrder);

                    averageExecutionManager.addExecutionPrice(order);
                    executedQuantityManager.addExecutedQuantity(sellOrder, order);
                    break;
                case SELL:
                    // we pick the order with the largest price
                    Order buyOrder = ricOrders.get(ricOrders.size() - 1);

                    // remove the sell order;
                    ricOrdersMappings.remove(buyOrder.getRic(), buyOrder);

                    averageExecutionManager.addExecutionPrice(order);
                    executedQuantityManager.addExecutedQuantity(order, buyOrder);
                    break;
                default:
                    throw new RuntimeException("This should never happen!");
            }
        }
        else {
            ricOrdersMappings.put(order.getRic(), order);
        }
    }

    @Override
    public List<Order> getOpenInterest(String ric, OrderDirection orderDirection) {
        return ricOrdersMappings.get(ric).stream()
                .filter(order -> order.getOrderDirection().equals(orderDirection))
                .collect(Collectors.toList());
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
