package backEnd.dao;

import backEnd.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDao {
    Product save(Product product);
    Product update(Product product);
    Optional<Product> findById(Long id);
    Optional<Product> findByProductNo(String productNo);
    List<Product> findAll();
    void delete(Long id);
    boolean existsByProductNo(String productNo);
}
