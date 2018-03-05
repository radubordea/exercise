package exchange.service;

import exchange.domain.Order;

import java.math.BigDecimal;

/**
 * Manages the average execution operations and queries.
 */
interface AverageExecutionManager {

  /**
   * Computes the new average with the given order.
   *
   * @param order - the newly executed order.
   */
  void addExecutionPrice(Order order);

  /**
   * @param ric - the ric we need to get the average for
   * @return - the average execution price for a ric.
   */
  BigDecimal getAverageExecutionPrice(String ric);
}
