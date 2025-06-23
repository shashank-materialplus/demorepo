package com.springbootmicroservices.productservice.model.product.mapper;

import com.springbootmicroservices.productservice.client.dto.IndexableProductDto;
import com.springbootmicroservices.productservice.model.product.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

// Add BigDecimal to the imports list for the expression
@Mapper(imports = {BigDecimal.class})
public interface ProductToIndexableProductDtoMapper {

    ProductToIndexableProductDtoMapper INSTANCE = Mappers.getMapper(ProductToIndexableProductDtoMapper.class);

    @Mapping(source = "id", target = "documentId")
    @Mapping(source = "name", target = "title")
    @Mapping(source = "unitPrice", target = "price")
    @Mapping(source = "amount", target = "stock")
    @Mapping(source = "authorName", target = "author")
    // This expression correctly calculates the inStock status
    @Mapping(target = "inStock", expression = "java(product.getAmount() != null && product.getAmount().compareTo(BigDecimal.ZERO) > 0)")
    IndexableProductDto productToIndexableProductDto(Product product);
}