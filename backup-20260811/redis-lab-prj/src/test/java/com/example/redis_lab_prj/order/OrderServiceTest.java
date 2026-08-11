package com.example.redis_lab_prj.order;

import com.example.redis_lab_prj.common.exception.InsufficientStockException;
import com.example.redis_lab_prj.common.exception.ResourceNotFoundException;
import com.example.redis_lab_prj.order.dto.OrderRequest;
import com.example.redis_lab_prj.product.Product;
import com.example.redis_lab_prj.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, productRepository);
    }

    @Test
    void create_decrementsStock_whenEnoughAvailable() {
        Product product = new Product("Keyboard", "Mechanical", new BigDecimal("49.90"), 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.create(new OrderRequest(1L, 3));

        assertThat(product.getStock()).isEqualTo(7);
        assertThat(order.getQuantity()).isEqualTo(3);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void create_throwsInsufficientStock_whenQuantityExceedsStock() {
        Product product = new Product("Keyboard", "Mechanical", new BigDecimal("49.90"), 2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.create(new OrderRequest(1L, 5)))
                .isInstanceOf(InsufficientStockException.class);
        assertThat(product.getStock()).isEqualTo(2);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_throwsResourceNotFound_whenProductMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(new OrderRequest(99L, 1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_restoresStock_whenCancelled() {
        Product product = new Product("Keyboard", "Mechanical", new BigDecimal("49.90"), 7);
        Order order = new Order(product, 3);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateStatus(1L, OrderStatus.CANCELLED);

        assertThat(product.getStock()).isEqualTo(10);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void updateStatus_doesNotTouchStock_whenConfirmed() {
        Product product = new Product("Keyboard", "Mechanical", new BigDecimal("49.90"), 7);
        Order order = new Order(product, 3);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        assertThat(product.getStock()).isEqualTo(7);
        verify(productRepository, never()).save(any());
    }
}
