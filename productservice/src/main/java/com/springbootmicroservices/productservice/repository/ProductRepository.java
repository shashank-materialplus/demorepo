package com.springbootmicroservices.productservice.repository;

import com.springbootmicroservices.productservice.model.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface named {@link ProductRepository} for managing ProductEntity objects in the database.
 */
public interface ProductRepository extends JpaRepository<ProductEntity, String> {

    /**
     * Checks if a product entity with the given name exists in the database.
     *
     * @param name the name of the product to check for existence
     * @return true if a product entity with the given name exists, false otherwise
     */
    boolean existsProductEntityByName(final String name);

    List<ProductEntity> findByCategory(String category);


}
