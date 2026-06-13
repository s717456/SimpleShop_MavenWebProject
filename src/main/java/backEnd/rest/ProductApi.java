package backEnd.rest;

import backEnd.dao.ProductDao;
import backEnd.dao.impl.ProductDaoImpl;
import backEnd.entity.Product;
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
import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductApi {

    private final ProductDao productDao = new ProductDaoImpl();

    @GET
    public List<ProductDto> findAll() {
        return productDao.findAll().stream().map(ProductDto::from).toList();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        return productDao.findById(id)
                .map(product -> Response.ok(ProductDto.from(product)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).entity(Message.error("商品不存在")).build());
    }

    @POST
    public Response create(ProductRequest request) {
        validate(request);
        if (productDao.existsByProductNo(request.productNo)) {
            return Response.status(Response.Status.CONFLICT).entity(Message.error("productNo 已存在")).build();
        }

        Product product = new Product();
        product.setProductNo(request.productNo);
        product.setName(request.name);
        product.setPrice(request.price);
        product.setStock(request.stock == null ? 0 : request.stock);
        product.setStatus(valueOrDefault(request.status, "ACTIVE"));

        Product saved = productDao.save(product);
        return Response.status(Response.Status.CREATED).entity(ProductDto.from(saved)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, ProductRequest request) {
        validate(request);
        return productDao.findById(id).map(product -> {
            product.setProductNo(request.productNo);
            product.setName(request.name);
            product.setPrice(request.price);
            product.setStock(request.stock == null ? 0 : request.stock);
            product.setStatus(valueOrDefault(request.status, "ACTIVE"));
            Product updated = productDao.update(product);
            return Response.ok(ProductDto.from(updated)).build();
        }).orElse(Response.status(Response.Status.NOT_FOUND).entity(Message.error("商品不存在")).build());
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        if (productDao.findById(id).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(Message.error("商品不存在")).build();
        }
        productDao.delete(id);
        return Response.ok(Message.ok("刪除成功")).build();
    }

    private void validate(ProductRequest request) {
        if (request == null || isBlank(request.productNo) || isBlank(request.name) || request.price == null) {
            throw new IllegalArgumentException("productNo, name, price 為必填");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static class ProductRequest {
        public String productNo;
        public String name;
        public BigDecimal price;
        public Integer stock;
        public String status;
    }

    public static class ProductDto {
        public Long id;
        public String productNo;
        public String name;
        public BigDecimal price;
        public Integer stock;
        public String status;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public static ProductDto from(Product product) {
            ProductDto dto = new ProductDto();
            dto.id = product.getId();
            dto.productNo = product.getProductNo();
            dto.name = product.getName();
            dto.price = product.getPrice();
            dto.stock = product.getStock();
            dto.status = product.getStatus();
            dto.createdAt = product.getCreatedAt();
            dto.updatedAt = product.getUpdatedAt();
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
