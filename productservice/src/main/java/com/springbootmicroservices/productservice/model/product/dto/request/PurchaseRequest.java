// src/main/java/com/springbootmicroservices/productservice/model/product/dto/request/PurchaseRequest.java
package com.springbootmicroservices.productservice.model.product.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

}