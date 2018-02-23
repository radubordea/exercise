package exchange.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import exchange.domain.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class AverageExecutionManagerImpl implements AverageExecutionManager {

    private ListMultimap<String, Order> ricData = ArrayListMultimap.create();

    @Override
    public void addExecutionPrice(Order order) {
        ricData.put(order.getRic(), order);
    }

    @Override
    public BigDecimal getAverageExecutionPrice(String ric) {
        BigDecimal total = ricData.get(ric).stream()
                .map(order -> order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(ricData.get(ric).stream().mapToLong(Order::getQuantity).sum()), 4, RoundingMode.HALF_UP);
    }
}
