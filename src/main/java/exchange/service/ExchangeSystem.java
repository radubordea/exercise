package exchange.service;

import exchange.domain.Order;
import exchange.domain.OrderDirection;

import java.math.BigDecimal;
import java.util.List;

/**
 * Defines the contract of the exchange system.
 */
public interface ExchangeSystem {

  /**
   * Add an order to the exchange.
   */
  void addOrder(Order order);

  /**
   * Provide open interest for a given RIC and direction
   * Open interest is the total quantity of all open orders for the given RIC and direction at each price point.
   */
  List<Order> getOpenInterest(String ric, OrderDirection orderDirection);

  /**
   * Provide the average execution price for a given RIC
   * The average execution price is the average price per unit of all executions for the given RIC
   */
  BigDecimal averagePrice(String ric);

  /**
   * Provide executed quantity for a given RIC and user
   * Executed quantity is the sum of quantities of executed orders for the given RIC and user. The quantity of
   * sell orders should be negated
   */
  Long executedQuantity(String ric, String user);
}
