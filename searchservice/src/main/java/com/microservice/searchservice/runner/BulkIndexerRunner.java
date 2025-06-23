//package com.microservice.searchservice.runner;
//
//import com.microservice.searchservice.dto.IndexableProduct;
//import com.microservice.searchservice.service.SearchService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//
//import java.sql.Timestamp;
//import java.util.List;
//import java.util.Objects;
//import java.util.regex.Pattern; // <-- NEW IMPORT
//
////@Component
//@Slf4j
//@RequiredArgsConstructor
//public class BulkIndexerRunner implements CommandLineRunner {
//
//    private final JdbcTemplate jdbcTemplate;
//    private final SearchService searchService;
//    // A regular expression to find any character that is NOT a letter, number, or hyphen
//    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9-]");
//    // A regular expression to find multiple hyphens in a row
//    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-+");
//
//    // --- ✅ THE REAL FIX IS HERE: A robust method to create a clean ID ---
//    private String createSafeDocumentId(String originalId) {
//        if (originalId == null || originalId.isBlank()) {
//            // Return a unique ID if the original is empty to avoid errors
//            return "product-" + System.currentTimeMillis() + "-" + Math.random();
//        }
//        // 1. Convert to lowercase
//        String cleaned = originalId.toLowerCase();
//        // 2. Replace all non-alphanumeric characters with a hyphen
//        cleaned = NON_ALPHANUMERIC.matcher(cleaned).replaceAll("-");
//        // 3. Replace multiple hyphens with a single one
//        cleaned = MULTIPLE_HYPHENS.matcher(cleaned).replaceAll("-");
//        // 4. Remove leading or trailing hyphens
//        if (cleaned.startsWith("-")) {
//            cleaned = cleaned.substring(1);
//        }
//        if (cleaned.endsWith("-")) {
//            cleaned = cleaned.substring(0, cleaned.length() - 1);
//        }
//        return cleaned;
//    }
//
//
//    @Override
//    public void run(String... args) throws Exception {
//        log.info("======================================================");
//        log.info("      STARTING: BULK PRODUCT INDEXING (Final Version)  ");
//        log.info("======================================================");
//
//        String sql = "SELECT id, name, description, unit_price, category, image_url, author_name, amount, created_at FROM product";
//
//        List<IndexableProduct> productsToIndex = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
//            try {
//                String originalId = resultSet.getString("id");
//                // --- ✅ USE THE NEW METHOD TO CREATE A CLEAN ID ---
//                String safeId = createSafeDocumentId(originalId);
//
//                Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
//
//                return IndexableProduct.builder()
//                        .documentId(safeId) // Use the new, safe ID
//                        .title(resultSet.getString("name"))
//                        .description(resultSet.getString("description"))
//                        .price(resultSet.getDouble("unit_price"))
//                        .imageUrl(resultSet.getString("image_url"))
//                        .category(resultSet.getString("category"))
//                        .author(resultSet.getString("author_name"))
//                        .stock(resultSet.getInt("amount"))
//                        .createdAt(createdAtTimestamp != null ? createdAtTimestamp.toInstant() : null)
//                        .inStock(resultSet.getInt("amount") > 0)
//                        .build();
//
//            } catch (Exception e) {
//                log.error("Error processing row number {}. Skipping this record. Error: {}", rowNum, e.getMessage());
//                return null;
//            }
//        });
//
//        productsToIndex.removeIf(Objects::isNull);
//
//        log.info("Successfully parsed {} products from the database to be indexed.", productsToIndex.size());
//
//        for (IndexableProduct product : productsToIndex) {
//            try {
//                searchService.indexDocument(product);
//                log.info("SUCCESS: Queued indexing for product ID '{}'", product.getDocumentId());
//            } catch (Exception e) {
//                log.error("FAILURE: Could not queue indexing for product ID '{}'. Reason: {}", product.getDocumentId(), e.getMessage(), e);
//            }
//        }
//
//        log.info("======================================================");
//        log.info("           BULK INDEXING COMPLETE                   ");
//        log.info("======================================================");
//    }
//}