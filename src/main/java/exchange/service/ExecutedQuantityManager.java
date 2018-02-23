package exchange.service;

import exchange.domain.Order;

/**
 * Manages the executed quantity statistics.
 */
interface ExecutedQuantityManager {

    /**
     * Adds to the statistics the sell and buy order.
     */
    void addExecutedQuantity(Order sellOrder, Order buyOrder);

    /**
     * @return the quantity for the given ric and user.
     */
    long getExecutedQuantity(String ric, String user);
}
