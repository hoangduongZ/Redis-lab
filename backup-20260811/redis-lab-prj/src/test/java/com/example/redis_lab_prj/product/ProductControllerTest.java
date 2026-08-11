package com.example.redis_lab_prj.product;

import com.example.redis_lab_prj.common.exception.ResourceNotFoundException;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void create_returns201_whenRequestIsValid() throws Exception {
        Product saved = new Product("Keyboard", "Mechanical", new BigDecimal("49.90"), 10);
        when(productService.create(any())).thenReturn(saved);

        String body = "{\"name\":\"Keyboard\",\"description\":\"Mechanical\",\"price\":49.90,\"stock\":10}";

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void create_returns400_whenNameIsBlank() throws Exception {
        String body = "{\"name\":\"\",\"price\":49.90,\"stock\":10}";

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_returns404_whenProductMissing() throws Exception {
        when(productService.findById(99L)).thenThrow(new ResourceNotFoundException("Product not found: 99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_returnsList() throws Exception {
        Product product = new Product("Mouse", "Wireless", new BigDecimal("19.90"), 5);
        when(productService.findAll()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mouse"));
    }
}
