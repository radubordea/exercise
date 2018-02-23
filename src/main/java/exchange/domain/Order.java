package exchange.domain;

import java.math.BigDecimal;

/**
 * Domain class that represents an order.
 */
public class Order extends RicUserKey {

    private Long quantity;
    private OrderDirection orderDirection;
    private BigDecimal price;

    public Order(String user, OrderDirection orderDirection, String ric, Long quantity, BigDecimal price) {
        super(ric, user);
        this.orderDirection = orderDirection;
        this.price = price;
        this.quantity = quantity;
    }

    public RicUserKey getRicUserKey() {
        return new RicUserKey(ric, user);
    }

    public String getUser() {
        return user;
    }

    public OrderDirection getOrderDirection() {
        return orderDirection;
    }

    public String getRic() {
        return ric;
    }

    public Long getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
