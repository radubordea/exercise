package exchange.service;

import exchange.domain.Order;
import exchange.domain.OrderDirection;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public final class ExecutedQuantityManagerImplTest {

    private ExecutedQuantityManager manager;

    @Before
    public void setup(){
        manager = new ExecutedQuantityManagerImpl();
    }

    @Test
    public void testExecutedQuantityWithOneExecution() {
        manager.addExecutedQuantity(
                new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN),
                new Order("user2", OrderDirection.BUY, "ric1", 100L, BigDecimal.TEN));

        assertEquals(manager.getExecutedQuantity("ric1", "user1"), -100L);
        assertEquals(manager.getExecutedQuantity("ric1", "user2"), 100L);
    }

    @Test
    public void testExecutedQuantityWithTwoExecutions() {
        manager.addExecutedQuantity(
                new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN),
                new Order("user2", OrderDirection.BUY, "ric1", 100L, BigDecimal.TEN));

        manager.addExecutedQuantity(
                new Order("user2", OrderDirection.SELL, "ric1", 150L, BigDecimal.valueOf(100)),
                new Order("user1", OrderDirection.BUY, "ric1", 150L, BigDecimal.valueOf(100))
        );

        assertEquals(manager.getExecutedQuantity("ric1", "user1"), 50);
        assertEquals(manager.getExecutedQuantity("ric1", "user2"), -50);
    }
}
