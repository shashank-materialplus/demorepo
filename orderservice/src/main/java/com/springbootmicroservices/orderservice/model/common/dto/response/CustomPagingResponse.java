package com.springbootmicroservices.orderservice.model.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page; // If using the static factory method

import java.util.List;

/**
 * Represents a generic DTO for paginated API responses.
 * It includes the list of content for the current page and pagination metadata.
 *
 * @param <T> The type of the content in the page.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Optional: Don't include null fields in JSON
public class CustomPagingResponse<T> {

    private List<T> content;          // The list of items for the current page
    private Integer pageNumber;       // Current page number (typically 1-based for client)
    private Integer pageSize;         // Number of items requested per page
    private Long totalElementCount;   // Total number of items available across all pages
    private Integer totalPageCount;   // Total number of pages available
    private Boolean isFirstPage;      // True if this is the first page
    private Boolean isLastPage;       // True if this is the last page
    private Boolean hasNextPage;      // True if there is a next page
    private Boolean hasPreviousPage;  // True if there is a previous page

    /**
     * Static factory method to easily create a CustomPagingResponse from a Spring Data Page object
     * and a list of mapped DTOs.
     *
     * @param springDataPage The Page object from Spring Data JPA (e.g., Page<OrderEntity> or Page<OrderResponse>)
     * @param contentDtos    The list of DTOs for the current page's content (e.g., List<OrderResponse>)
     * @param <E>            The type of the entity or object in the Spring Data Page
     * @param <D>            The type of the DTO in the content list
     * @return A new CustomPagingResponse instance.
     */
    public static <E, D> CustomPagingResponse<D> fromPage(Page<E> springDataPage, List<D> contentDtos) {
        return CustomPagingResponse.<D>builder()
                .content(contentDtos)
                .pageNumber(springDataPage.getNumber() + 1) // Spring Page is 0-indexed, client usually expects 1-indexed
                .pageSize(springDataPage.getSize())
                .totalElementCount(springDataPage.getTotalElements())
                .totalPageCount(springDataPage.getTotalPages())
                .isFirstPage(springDataPage.isFirst())
                .isLastPage(springDataPage.isLast())
                .hasNextPage(springDataPage.hasNext())
                .hasPreviousPage(springDataPage.hasPrevious())
                .build();
    }

    /**
     * Static factory method if the Page already contains the DTOs.
     *
     * @param springDataPage The Page object from Spring Data JPA containing DTOs (e.g., Page<OrderResponse>)
     * @param <D>            The type of the DTO in the content list
     * @return A new CustomPagingResponse instance.
     */
    public static <D> CustomPagingResponse<D> fromPageOfDtos(Page<D> springDataPage) {
        return CustomPagingResponse.<D>builder()
                .content(springDataPage.getContent())
                .pageNumber(springDataPage.getNumber() + 1)
                .pageSize(springDataPage.getSize())
                .totalElementCount(springDataPage.getTotalElements())
                .totalPageCount(springDataPage.getTotalPages())
                .isFirstPage(springDataPage.isFirst())
                .isLastPage(springDataPage.isLast())
                .hasNextPage(springDataPage.hasNext())
                .hasPreviousPage(springDataPage.hasPrevious())
                .build();
    }

}