package exchange.service;

import exchange.domain.Order;
import exchange.domain.OrderDirection;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ExchangeSystemImplTest {

    private AverageExecutionManager mockAverageExecutionManager;
    private ExecutedQuantityManager mockExecutedQuantityManager;
    private ExchangeSystem exchangeSystem;

    @Before
    public void setup(){
        mockAverageExecutionManager = mock(AverageExecutionManager.class);
        mockExecutedQuantityManager = mock(ExecutedQuantityManager.class);
        exchangeSystem = new ExchangeSystemImpl(mockAverageExecutionManager, mockExecutedQuantityManager);
    }

    @Test
    public void averagePrice() {
        when(mockAverageExecutionManager.getAverageExecutionPrice("ric1")).thenReturn(BigDecimal.TEN);

        assertTrue(exchangeSystem.averagePrice("ric1").equals(BigDecimal.TEN));
    }

    @Test
    public void executedQuantity() {
        when(mockExecutedQuantityManager.getExecutedQuantity("ric1", "user1")).thenReturn(10L);

        assertTrue(exchangeSystem.executedQuantity("ric1", "user1").equals(10L));
    }

    @Test
    public void testAdd() {
        Order order1 = new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN);

        exchangeSystem.addOrder(order1);

        verify(mockAverageExecutionManager, never()).addExecutionPrice(any());
        verify(mockExecutedQuantityManager, never()).addExecutedQuantity(any(), any());

        assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).size() == 1);
        assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).contains(order1));
        assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).isEmpty());

        Order order2 = new Order("user2", OrderDirection.BUY, "ric1", 100L, BigDecimal.TEN);

        exchangeSystem.addOrder(order2);

        verify(mockAverageExecutionManager).addExecutionPrice(order2);
        verify(mockExecutedQuantityManager).addExecutedQuantity(order1, order2);

        assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).isEmpty());
        assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).isEmpty());
    }

    @Test
    public void testAddSevenOrders() {
        Order order1 = new Order("user1", OrderDirection.SELL, "ric1", 1000L, BigDecimal.valueOf(100.2));
        Order order2 = new Order("user2", OrderDirection.BUY, "ric1", 1000L, BigDecimal.valueOf(100.2));
        Order order3 = new Order("user1", OrderDirection.BUY, "ric1", 1000L, BigDecimal.valueOf(99));
        Order order4 = new Order("user1", OrderDirection.BUY, "ric1", 1000L, BigDecimal.valueOf(101));
        Order order5 = new Order("user2", OrderDirection.SELL, "ric1", 500L, BigDecimal.valueOf(102));
        Order order6 = new Order("user1", OrderDirection.BUY, "ric1", 500L, BigDecimal.valueOf(103));
        Order order7 = new Order("user2", OrderDirection.SELL, "ric1", 1000L, BigDecimal.valueOf(98));

        exchangeSystem.addOrder(order1);
        exchangeSystem.addOrder(order2);
        exchangeSystem.addOrder(order3);
        exchangeSystem.addOrder(order4);
        exchangeSystem.addOrder(order5);
        exchangeSystem.addOrder(order6);
        exchangeSystem.addOrder(order7);

        assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).isEmpty());
        assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).size() == 1);
        assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).contains(order3));
    }
}
