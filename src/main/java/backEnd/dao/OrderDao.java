package backEnd.dao;

import backEnd.entity.Orders;

import java.util.List;
import java.util.Optional;

public interface OrderDao {
    Orders save(Orders order);
    Orders update(Orders order);
    Orders createOrder(Long memberId, String employeeNo, List<OrderItemData> items);
    Optional<Orders> findById(Long id);
    Optional<Orders> findByOrderNo(String orderNo);
    List<Orders> findAll();
    void delete(Long id);

    class OrderItemData {
        private Long productId;
        private Integer quantity;

        public OrderItemData() {
        }

        public OrderItemData(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
