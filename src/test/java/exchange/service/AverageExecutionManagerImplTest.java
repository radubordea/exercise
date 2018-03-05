package exchange.service;

import exchange.domain.Order;
import exchange.domain.OrderDirection;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public final class AverageExecutionManagerImplTest {

  private AverageExecutionManager manager;

  @Before
  public void setup(){
    manager = new AverageExecutionManagerImpl();
  }

  @Test
  public void getAverageExecutionPriceNoOrders() {
    assertEquals(manager.getAverageExecutionPrice("ric1").compareTo(BigDecimal.ZERO) ,0);
  }

  @Test
  public void getAverageExecutionPriceOneOrders() {
    manager.addExecutionPrice(new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN));

    assertEquals(manager.getAverageExecutionPrice("ric1").compareTo(BigDecimal.TEN) ,0);
  }

  @Test
  public void getAverageExecutionPriceTwoOrders() {
    manager.addExecutionPrice(new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN));
    manager.addExecutionPrice(new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.valueOf(20)));

    assertEquals(manager.getAverageExecutionPrice("ric1").compareTo(BigDecimal.valueOf(15)) ,0);
  }

  @Test
  public void getAverageExecutionPriceThreeOrders() {
    manager.addExecutionPrice(new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN));
    manager.addExecutionPrice(new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.valueOf(20)));
    manager.addExecutionPrice(new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.valueOf(20)));

    assertEquals(manager.getAverageExecutionPrice("ric1").compareTo(BigDecimal.valueOf(16.6667)) ,0);
  }

  @Test
  public void getAverageExecutionPriceThreeOrdersTwoRic() {
    manager.addExecutionPrice(new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN));
    manager.addExecutionPrice(new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.valueOf(20)));
    manager.addExecutionPrice(new Order("user2", OrderDirection.SELL, "ric2", 100L, BigDecimal.valueOf(20)));

    assertEquals(manager.getAverageExecutionPrice("ric1").compareTo(BigDecimal.valueOf(15)) ,0);
    assertEquals(manager.getAverageExecutionPrice("ric2").compareTo(BigDecimal.valueOf(20)) ,0);
  }
}
