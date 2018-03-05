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
  public void addExecutedQuantityNoOrders() {
    assertEquals(manager.getExecutedQuantity("ric1", "user1"), 0);
  }

  @Test
  public void addExecutedQuantityOneExecution() {
    manager.addExecutedQuantity(
        new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN),
        new Order("user2", OrderDirection.BUY, "ric1", 100L, BigDecimal.TEN));

    assertEquals(manager.getExecutedQuantity("ric1", "user1"), -100L);
    assertEquals(manager.getExecutedQuantity("ric1", "user2"), 100L);
    // assert absent user
    assertEquals(manager.getExecutedQuantity("ric1", "user3"), 0L);
  }

  @Test
  public void addExecutedQuantityTwoExecutions() {
    manager.addExecutedQuantity(
        new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN),
        new Order("user2", OrderDirection.BUY, "ric1", 100L, BigDecimal.TEN));

    manager.addExecutedQuantity(
        new Order("user2", OrderDirection.SELL, "ric1", 150L, BigDecimal.valueOf(100)),
        new Order("user1", OrderDirection.BUY, "ric1", 150L, BigDecimal.valueOf(100)));

    assertEquals(manager.getExecutedQuantity("ric1", "user1"), 50L);
    assertEquals(manager.getExecutedQuantity("ric1", "user2"), -50L);
  }

  @Test
  public void addExecutedQuantityTwoExecutionsThreeUsers() {
    manager.addExecutedQuantity(
        new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN),
        new Order("user2", OrderDirection.BUY, "ric1", 100L, BigDecimal.TEN));

    manager.addExecutedQuantity(
        new Order("user2", OrderDirection.SELL, "ric1", 50L, BigDecimal.valueOf(100)),
        new Order("user3", OrderDirection.BUY, "ric1", 50L, BigDecimal.valueOf(100)));

    assertEquals(manager.getExecutedQuantity("ric1", "user1"), -100L);
    assertEquals(manager.getExecutedQuantity("ric1", "user2"), 50L);
    assertEquals(manager.getExecutedQuantity("ric1", "user3"), 50L);
  }
}
