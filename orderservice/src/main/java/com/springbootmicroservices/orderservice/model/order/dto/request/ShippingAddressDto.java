package com.springbootmicroservices.orderservice.model.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddressDto {

    @NotBlank(message = "Street address cannot be blank")
    @Size(max = 255, message = "Street address too long")
    private String street;

    @NotBlank(message = "City cannot be blank")
    @Size(max = 100, message = "City name too long")
    private String city;

    @NotBlank(message = "State/Province cannot be blank")
    @Size(max = 100, message = "State/Province name too long")
    private String state; // Or province

    @NotBlank(message = "Postal code cannot be blank")
    @Size(max = 20, message = "Postal code too long")
    private String postalCode;

    @NotBlank(message = "Country cannot be blank")
    @Size(max = 100, message = "Country name too long")
    private String country;

    private String apartmentSuiteEtc; // Optional
}