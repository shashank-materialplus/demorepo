package com.springbootmicroservices.productservice.controller;

import com.springbootmicroservices.productservice.base.AbstractRestControllerTest;
import com.springbootmicroservices.productservice.model.common.CustomPage;
import com.springbootmicroservices.productservice.model.product.Product;
import com.springbootmicroservices.productservice.model.product.dto.request.ProductCreateRequest;
import com.springbootmicroservices.productservice.model.product.dto.request.ProductPagingRequest;
import com.springbootmicroservices.productservice.model.product.dto.request.ProductUpdateRequest;
import com.springbootmicroservices.productservice.service.ProductCreateService;
import com.springbootmicroservices.productservice.service.ProductDeleteService;
import com.springbootmicroservices.productservice.service.ProductReadService;
import com.springbootmicroservices.productservice.service.ProductUpdateService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // For simpler public endpoint tests
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest extends AbstractRestControllerTest {

    @MockBean
    private ProductCreateService productCreateService;
    @MockBean
    private ProductReadService productReadService;
    @MockBean
    private ProductUpdateService productUpdateService;
    @MockBean
    private ProductDeleteService productDeleteService;

    @Test
    void createProduct_AsAdmin_ShouldReturnSuccess() throws Exception {
        // Given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("New Gadget")
                .unitPrice(new BigDecimal("99.99"))
                .amount(new BigDecimal("100"))
                .description("A test product")
                .category("Electronics")
                .authorName("Creator")
                .imageUrl("http://example.com/img.png")
                .build();

        Product createdProduct = Product.builder().id(UUID.randomUUID().toString()).build();
        when(productCreateService.createProduct(any(ProductCreateRequest.class))).thenReturn(createdProduct);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + MOCK_ADMIN_TOKEN) // Use the mocked ADMIN token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.response").value(createdProduct.getId()));

        verify(productCreateService).createProduct(any(ProductCreateRequest.class));
    }

    @Test
    void createProduct_AsUser_ShouldReturnForbidden() throws Exception {
        // Given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("Illegal Gadget")
                .unitPrice(new BigDecimal("99.99"))
                .amount(new BigDecimal("100"))
                .category("Forbidden Category")
                .description("A test product")
                .authorName("A User")
                .imageUrl("http://example.com/image.png")
                .build();
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + MOCK_USER_TOKEN) // Use the mocked USER token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // Expect a 403 Forbidden
    }

    @Test
    @WithMockUser // Simulates a logged-in user for a public endpoint, good practice.
    void getProductById_PublicEndpoint_ShouldReturnProduct() throws Exception {
        // Given
        String productId = UUID.randomUUID().toString();
        Product product = Product.builder().id(productId).name("Public Product").build();
        when(productReadService.getProductById(productId)).thenReturn(product);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.response.id").value(productId));

        verify(productReadService).getProductById(productId);
    }

    @Test
    @WithMockUser
    void getProducts_AsGetRequestWithParams_ShouldReturnPagedProducts() throws Exception {
        // Given
        Product product = Product.builder().id(UUID.randomUUID().toString()).name("Test Product").build();
        List<Product> products = Collections.singletonList(product);
        PageRequest pageRequest = PageRequest.of(0, 1); // page=1, size=1 -> 0-based index
        PageImpl<Product> productPage = new PageImpl<>(products, pageRequest, 1);

        CustomPage<Product> customPage = CustomPage.of(products, productPage);

        when(productReadService.getProducts(any(ProductPagingRequest.class))).thenReturn(customPage);

        // When & Then: Paging info is sent as URL parameters for GET requests
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products")
                        .param("page", "1")
                        .param("size", "1")
                        .param("sortBy", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.response.content[0].id").value(product.getId()));

        verify(productReadService).getProducts(any(ProductPagingRequest.class));
    }

    @Test
    void deleteProduct_AsAdmin_ShouldReturnSuccess() throws Exception {
        // Given
        String productId = UUID.randomUUID().toString();
        doNothing().when(productDeleteService).deleteProductById(productId);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/products/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + MOCK_ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(productDeleteService).deleteProductById(productId);
    }
}