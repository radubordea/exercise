package exchange.service;

import static org.junit.Assert.assertTrue;

import exchange.domain.Order;
import exchange.domain.OrderDirection;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public final class ExchangeSystemImplTest {

  private ExchangeSystem exchangeSystem;

  @Before
  public void setup(){
    exchangeSystem = new ExchangeSystemImpl();
  }

  @Test
  public void addOneOrder() {
    Order order = new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN);

    exchangeSystem.addOrder(order);

    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).size() == 1);
    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).contains(order));
    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).isEmpty());

    assertTrue(exchangeSystem.averagePrice("ric1").compareTo(BigDecimal.ZERO) == 0);
    assertTrue(exchangeSystem.executedQuantity("ric1", "user1").equals(0L));
  }

  @Test
  public void addTwoNonMatchingOrders() {
    Order sellOrder = new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN);
    Order buyOrder = new Order("user2", OrderDirection.BUY, "ric1", 99L, BigDecimal.TEN);

    exchangeSystem.addOrder(sellOrder);
    exchangeSystem.addOrder(buyOrder);

    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).contains(sellOrder));
    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).contains(buyOrder));

    assertTrue(exchangeSystem.averagePrice("ric1").compareTo(BigDecimal.ZERO) == 0);
    assertTrue(exchangeSystem.executedQuantity("ric1", "user1").equals(0L));
    assertTrue(exchangeSystem.executedQuantity("ric1", "user2").equals(0L));
  }

  @Test
  public void addThreeNonMatchingOrdersDifferentRic() {
    Order sellOrder = new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN);
    Order buyOrderRic1 = new Order("user2", OrderDirection.BUY, "ric1", 99L, BigDecimal.TEN);
    Order buyOrderRic2 = new Order("user2", OrderDirection.BUY, "ric2", 100L, BigDecimal.TEN);

    exchangeSystem.addOrder(sellOrder);
    exchangeSystem.addOrder(buyOrderRic1);
    exchangeSystem.addOrder(buyOrderRic2);

    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).contains(sellOrder));
    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).contains(buyOrderRic1));
    assertTrue(exchangeSystem.getOpenInterest("ric2", OrderDirection.BUY).contains(buyOrderRic2));

    assertTrue(exchangeSystem.averagePrice("ric1").compareTo(BigDecimal.ZERO) == 0);
    assertTrue(exchangeSystem.averagePrice("ric2").compareTo(BigDecimal.ZERO) == 0);

    assertTrue(exchangeSystem.executedQuantity("ric1", "user1").equals(0L));
    assertTrue(exchangeSystem.executedQuantity("ric1", "user2").equals(0L));
    assertTrue(exchangeSystem.executedQuantity("ric2", "user2").equals(0L));
  }

  @Test
  public void addThreeNonMatchingOrdersSameRic() {
    Order sellOrder = new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN);
    Order buyOrderDifferentQuantity1 = new Order("user2", OrderDirection.BUY, "ric1", 99L, BigDecimal.TEN);
    Order buyOrderDifferentQuantity2 = new Order("user2", OrderDirection.BUY, "ric1", 101L, BigDecimal.TEN);

    exchangeSystem.addOrder(sellOrder);
    exchangeSystem.addOrder(buyOrderDifferentQuantity1);
    exchangeSystem.addOrder(buyOrderDifferentQuantity2);

    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).contains(sellOrder));
    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).containsAll(Arrays.asList(buyOrderDifferentQuantity1, buyOrderDifferentQuantity2)));

    assertTrue(exchangeSystem.averagePrice("ric1").compareTo(BigDecimal.ZERO) == 0);

    assertTrue(exchangeSystem.executedQuantity("ric1", "user1").equals(0L));
    assertTrue(exchangeSystem.executedQuantity("ric1", "user2").equals(0L));
  }

  @Test
  public void addTwoMatchingOrdersSamePrice() {
    Order sellOrder = new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.TEN);
    Order buyOrder = new Order("user2", OrderDirection.BUY, "ric1", 100L, BigDecimal.TEN);

    exchangeSystem.addOrder(sellOrder);
    exchangeSystem.addOrder(buyOrder);

    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).isEmpty());
    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).isEmpty());

    assertTrue(exchangeSystem.averagePrice("ric1").compareTo(BigDecimal.TEN) == 0);

    assertTrue(exchangeSystem.executedQuantity("ric1", "user1").equals(-100L));
    assertTrue(exchangeSystem.executedQuantity("ric1", "user2").equals(100L));
  }

  /**
   * Check that the alg works also for different prices.
   */
  @Test
  public void addTwoMatchingOrdersDifferentPrice() {
    Order sellOrder = new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.valueOf(9));
    Order buyOrder = new Order("user2", OrderDirection.BUY, "ric1", 100L, BigDecimal.TEN);

    exchangeSystem.addOrder(sellOrder);
    exchangeSystem.addOrder(buyOrder);

    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).isEmpty());
    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).isEmpty());

    assertTrue(exchangeSystem.averagePrice("ric1").compareTo(BigDecimal.TEN) == 0);

    assertTrue(exchangeSystem.executedQuantity("ric1", "user1").equals(-100L));
    assertTrue(exchangeSystem.executedQuantity("ric1", "user2").equals(100L));
  }

  /**
   * Check that a match won't be made if the SELL price is higher than the BUY price.
   */
  @Test
  public void addTwoMatchingOrdersSellPriceHigherThanBuyPrice() {
    Order sellOrderPriceToHigh = new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.valueOf(11));
    Order buyOrder = new Order("user2", OrderDirection.BUY, "ric1", 100L, BigDecimal.TEN);

    exchangeSystem.addOrder(sellOrderPriceToHigh);
    exchangeSystem.addOrder(buyOrder);

    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).contains(sellOrderPriceToHigh));
    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).contains(buyOrder));

    assertTrue(exchangeSystem.averagePrice("ric1").compareTo(BigDecimal.ZERO) == 0);

    assertTrue(exchangeSystem.executedQuantity("ric1", "user1").equals(0L));
    assertTrue(exchangeSystem.executedQuantity("ric1", "user2").equals(0L));
  }

  /**
   * Check that a match won't be made if the BUY price is less than the SELL price.
   */
  @Test
  public void addTwoMatchingOrdersBuyPriceLessThanBuyPrice() {
    Order buyOrderPriceToSmall = new Order("user2", OrderDirection.BUY, "ric1", 100L, BigDecimal.TEN);
    Order sellOrderPriceToHigh = new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.valueOf(11));

    exchangeSystem.addOrder(sellOrderPriceToHigh);
    exchangeSystem.addOrder(buyOrderPriceToSmall);

    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).contains(sellOrderPriceToHigh));
    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).contains(buyOrderPriceToSmall));

    assertTrue(exchangeSystem.averagePrice("ric1").compareTo(BigDecimal.ZERO) == 0);

    assertTrue(exchangeSystem.executedQuantity("ric1", "user1").equals(0L));
    assertTrue(exchangeSystem.executedQuantity("ric1", "user2").equals(0L));
  }

  /**
   * Check that the order is preserved when the price is that same.
   */
  @Test
  public void addOrderMatchesTwoPreviousOrdersSamePrice() {
    Order firstSellOrderPrice9 = new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.valueOf(9));
    Order secondSellOrderPrice9 = new Order("user2", OrderDirection.SELL, "ric1", 100L, BigDecimal.valueOf(9));
    Order buyOrder = new Order("user3", OrderDirection.BUY, "ric1", 100L, BigDecimal.TEN);

    exchangeSystem.addOrder(firstSellOrderPrice9);
    exchangeSystem.addOrder(secondSellOrderPrice9);
    exchangeSystem.addOrder(buyOrder);

    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).contains(secondSellOrderPrice9));
    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).isEmpty());

    assertTrue(exchangeSystem.averagePrice("ric1").compareTo(BigDecimal.TEN) == 0);

    assertTrue(exchangeSystem.executedQuantity("ric1", "user1").equals(-100L));
    assertTrue(exchangeSystem.executedQuantity("ric1", "user2").equals(0L));
    assertTrue(exchangeSystem.executedQuantity("ric1", "user3").equals(100L));
  }

  /**
   * Check that the SELL order with the smallest values is executed.
   */
  @Test
  public void addOrderMatchesTwoPreviousOrdersDifferentPrice() {
    Order firstSellOrderPrice9 = new Order("user1", OrderDirection.SELL, "ric1", 100L, BigDecimal.valueOf(9));
    Order secondSellOrderPrice8 = new Order("user2", OrderDirection.SELL, "ric1", 100L, BigDecimal.valueOf(8));
    Order buyOrder = new Order("user3", OrderDirection.BUY, "ric1", 100L, BigDecimal.TEN);

    exchangeSystem.addOrder(firstSellOrderPrice9);
    exchangeSystem.addOrder(secondSellOrderPrice8);
    exchangeSystem.addOrder(buyOrder);

    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.SELL).contains(firstSellOrderPrice9));
    assertTrue(exchangeSystem.getOpenInterest("ric1", OrderDirection.BUY).isEmpty());

    assertTrue(exchangeSystem.averagePrice("ric1").compareTo(BigDecimal.TEN) == 0);

    assertTrue(exchangeSystem.executedQuantity("ric1", "user1").equals(0L));
    assertTrue(exchangeSystem.executedQuantity("ric1", "user2").equals(-100L));
    assertTrue(exchangeSystem.executedQuantity("ric1", "user3").equals(100L));
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

    assertTrue(exchangeSystem.averagePrice("ric1").compareTo(BigDecimal.valueOf(99.8800))== 0);

    assertTrue(exchangeSystem.executedQuantity("ric1", "user1").equals(500L));
    assertTrue(exchangeSystem.executedQuantity("ric1", "user2").equals(-500L));
  }
}
