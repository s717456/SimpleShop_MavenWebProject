package backEnd.rest;

import backEnd.dao.OrderDao;
import backEnd.dao.impl.OrderDaoImpl;
import backEnd.entity.OrderItem;
import backEnd.entity.Orders;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderApi {

    private final OrderDao orderDao = new OrderDaoImpl();

    @GET
    public List<OrderDto> findAll() {
        return orderDao.findAll().stream().map(OrderDto::from).toList();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        return orderDao.findById(id)
                .map(order -> Response.ok(OrderDto.from(order)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).entity(Message.error("訂單不存在")).build());
    }

    @POST
    public Response create(CreateOrderRequest request) {
        if (request == null || request.memberId == null || request.items == null || request.items.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Message.error("memberId 與 items 為必填")).build();
        }

        List<OrderDao.OrderItemData> itemDataList = request.items.stream()
                .map(item -> new OrderDao.OrderItemData(item.productId, item.quantity))
                .toList();

        Orders created = orderDao.createOrder(request.memberId, request.employeeNo, itemDataList);
        return Response.status(Response.Status.CREATED).entity(OrderDto.from(created)).build();
    }

    @PUT
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") Long id, UpdateStatusRequest request) {
        return orderDao.findById(id).map(order -> {
            order.setStatus(request == null || request.status == null || request.status.isBlank() ? "NEW" : request.status);
            Orders updated = orderDao.update(order);
            return Response.ok(OrderDto.from(updated)).build();
        }).orElse(Response.status(Response.Status.NOT_FOUND).entity(Message.error("訂單不存在")).build());
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        if (orderDao.findById(id).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(Message.error("訂單不存在")).build();
        }
        orderDao.delete(id);
        return Response.ok(Message.ok("刪除成功")).build();
    }

    public static class CreateOrderRequest {
        public Long memberId;
        public String employeeNo;
        public List<ItemRequest> items;
    }

    public static class ItemRequest {
        public Long productId;
        public Integer quantity;
    }

    public static class UpdateStatusRequest {
        public String status;
    }

    public static class OrderDto {
        public Long id;
        public String orderNo;
        public Long memberId;
        public String memberName;
        public String employeeNo;
        public BigDecimal totalAmount;
        public String status;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public List<OrderItemDto> items = new ArrayList<>();

        public static OrderDto from(Orders order) {
            OrderDto dto = new OrderDto();
            dto.id = order.getId();
            dto.orderNo = order.getOrderNo();
            dto.memberId = order.getMember() == null ? null : order.getMember().getId();
            dto.memberName = order.getMember() == null ? null : order.getMember().getName();
            dto.employeeNo = order.getEmployeeNo();
            dto.totalAmount = order.getTotalAmount();
            dto.status = order.getStatus();
            dto.createdAt = order.getCreatedAt();
            dto.updatedAt = order.getUpdatedAt();
            if (order.getOrderItems() != null) {
                dto.items = order.getOrderItems().stream().map(OrderItemDto::from).toList();
            }
            return dto;
        }
    }

    public static class OrderItemDto {
        public Long id;
        public Long productId;
        public String productName;
        public Integer quantity;
        public BigDecimal price;
        public BigDecimal subtotal;

        public static OrderItemDto from(OrderItem item) {
            OrderItemDto dto = new OrderItemDto();
            dto.id = item.getId();
            dto.productId = item.getProduct() == null ? null : item.getProduct().getId();
            dto.productName = item.getProduct() == null ? null : item.getProduct().getName();
            dto.quantity = item.getQuantity();
            dto.price = item.getPrice();
            dto.subtotal = item.getSubtotal();
            return dto;
        }
    }

    public static class Message {
        public boolean success;
        public String message;

        public static Message ok(String message) {
            Message result = new Message();
            result.success = true;
            result.message = message;
            return result;
        }

        public static Message error(String message) {
            Message result = new Message();
            result.success = false;
            result.message = message;
            return result;
        }
    }
}
