package com.example.redis_lab_prj.product;

import com.example.redis_lab_prj.common.exception.ResourceNotFoundException;
import com.example.redis_lab_prj.product.dto.ProductRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    @Test
    void create_savesProductWithGivenFields() {
        ProductRequest request = new ProductRequest("Keyboard", "Mechanical", new BigDecimal("49.90"), 10);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.create(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Keyboard");
        assertThat(captor.getValue().getStock()).isEqualTo(10);
        assertThat(result.getName()).isEqualTo("Keyboard");
    }

    @Test
    void findById_returnsProduct_whenExists() {
        Product product = new Product("Mouse", "Wireless", new BigDecimal("19.90"), 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.findById(1L);

        assertThat(result).isSameAs(product);
    }

    @Test
    void findById_throwsResourceNotFound_whenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_overwritesExistingFields() {
        Product existing = new Product("Old name", "Old desc", new BigDecimal("10.00"), 3);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductRequest request = new ProductRequest("New name", "New desc", new BigDecimal("20.00"), 7);
        Product updated = productService.update(1L, request);

        assertThat(updated.getName()).isEqualTo("New name");
        assertThat(updated.getPrice()).isEqualTo(new BigDecimal("20.00"));
        assertThat(updated.getStock()).isEqualTo(7);
    }

    @Test
    void delete_throwsResourceNotFound_whenMissing() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(42L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(productRepository, never()).delete(any());
    }
}
