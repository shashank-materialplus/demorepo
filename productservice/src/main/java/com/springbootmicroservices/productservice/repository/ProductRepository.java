// com.springbootmicroservices.productservice.repository.ProductRepository
package com.springbootmicroservices.productservice.repository;

import com.springbootmicroservices.productservice.model.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Add this
import java.util.Optional;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, String>, JpaSpecificationExecutor<ProductEntity> { // Implement JpaSpecificationExecutor
    boolean existsProductEntityByName(final String name);
    boolean existsProductEntityByNameAndIdNot(String name, String id); // For updates
    List<ProductEntity> findByCategory(String category);
    Optional<ProductEntity> findByNameAndIdNot(String name, String id);

}