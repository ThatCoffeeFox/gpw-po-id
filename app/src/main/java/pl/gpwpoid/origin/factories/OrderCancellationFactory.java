package pl.gpwpoid.origin.factories;

import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.models.keys.OrderCancellationId;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.OrderCancellation;

import java.util.Date;

@Component
public class OrderCancellationFactory {
     public OrderCancellation createOrderCancellation(Order order){
         OrderCancellation orderCancellation = new OrderCancellation();

         OrderCancellationId orderCancellationId = new OrderCancellationId();
         orderCancellationId.setOrderId(order.getOrderId());
         orderCancellationId.setDate(new Date());

         orderCancellation.setId(orderCancellationId);
         orderCancellation.setOrder(order);
         return orderCancellation;
    }
}
