package com.biomedica.dto.mapper;

import com.biomedica.dto.OrderDto;
import com.biomedica.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final TestMapper testMapper;
    private final TestResultMapper testResultMapper;

    public OrderDto toDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .isPaid(order.isPaid())
                .orderDate(order.getOrderDate())
                .testResults(testResultMapper.toDtoList(order.getTestResults()))
                .build();
    }
}
