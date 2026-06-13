package backEnd.dao.impl;

import backEnd.dao.OrderDao;
import backEnd.entity.Member;
import backEnd.entity.OrderItem;
import backEnd.entity.Orders;
import backEnd.entity.Product;
import backEnd.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class OrderDaoImpl implements OrderDao {

    @Override
    public Orders save(Orders order) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(order);
            tx.commit();
            return order;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Orders update(Orders order) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Orders merged = em.merge(order);
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Orders createOrder(Long memberId, String employeeNo, List<OrderItemData> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Member member = em.find(Member.class, memberId);
            if (member == null) {
                throw new IllegalArgumentException("Member not found: " + memberId);
            }

            Orders order = new Orders();
            order.setOrderNo(generateOrderNo());
            order.setMember(member);
            order.setEmployeeNo(employeeNo);
            order.setStatus("NEW");

            BigDecimal total = BigDecimal.ZERO;
            for (OrderItemData data : items) {
                if (data == null || data.getProductId() == null || data.getQuantity() == null || data.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Invalid order item");
                }

                Product product = em.find(Product.class, data.getProductId());
                if (product == null) {
                    throw new IllegalArgumentException("Product not found: " + data.getProductId());
                }
                if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
                    throw new IllegalArgumentException("Product is not active: " + product.getName());
                }
                if (product.getStock() == null || product.getStock() < data.getQuantity()) {
                    throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
                }

                product.setStock(product.getStock() - data.getQuantity());
                BigDecimal price = product.getPrice();
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(data.getQuantity()));
                total = total.add(subtotal);

                OrderItem item = new OrderItem(product, data.getQuantity(), price, subtotal);
                order.addOrderItem(item);
            }

            order.setTotalAmount(total);
            em.persist(order);
            tx.commit();
            return order;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Orders> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Orders.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Orders> findByOrderNo(String orderNo) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Orders order = em.createQuery("SELECT o FROM Orders o WHERE o.orderNo = :orderNo", Orders.class)
                    .setParameter("orderNo", orderNo)
                    .getSingleResult();
            return Optional.of(order);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Orders> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT DISTINCT o FROM Orders o LEFT JOIN FETCH o.orderItems i LEFT JOIN FETCH i.product ORDER BY o.id DESC", Orders.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Orders order = em.find(Orders.class, id);
            if (order != null) {
                em.remove(order);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD" + time + random;
    }
}
