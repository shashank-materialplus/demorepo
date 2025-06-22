package com.springbootmicroservices.orderservice.model.order.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponseDto {

    private String redirectUrl;
}
