package com.biomedica.service;

import com.biomedica.dto.CreateOrderRequest;
import com.biomedica.dto.OrderDto;
import com.biomedica.dto.mapper.OrderMapper;
import com.biomedica.entity.Laboratory;
import com.biomedica.entity.Order;
import com.biomedica.entity.Test;
import com.biomedica.entity.TestResult;
import com.biomedica.entity.user.LaboratoryAssistant;
import com.biomedica.entity.user.Patient;
import com.biomedica.entity.user.User;
import com.biomedica.repository.LaboratoryAssistantRepository;
import com.biomedica.repository.LaboratoryRepository;
import com.biomedica.repository.OrderRepository;
import com.biomedica.repository.TestRepository;
import com.biomedica.repository.TestResultRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final AuditService auditService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final LaboratoryRepository laboratoryRepository;
    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final LaboratoryAssistantRepository laboratoryAssistantRepository;

    public Page<OrderDto> getOrders(Pageable pageable) {
        User user = auditService.getPrincipal();

        Patient patient = (Patient) user;

        return orderRepository.findAllByPatient(patient, pageable).map(orderMapper::toDto);
    }

    public OrderDto getOrder(UUID id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getPatient().getId().equals(auditService.getPrincipal().getId())) {
            throw new EntityNotFoundException("Order not found");
        }

        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto createOrder(CreateOrderRequest createOrderRequest) {
        log.info("Creating order with request: {}", createOrderRequest);
        User user = auditService.getPrincipal();

        Patient patient = (Patient) user;

        log.info("Creating order for patient: {}", patient.getId());

        Order order = Order.builder()
                .orderDate(OffsetDateTime.now())
                .isPaid(false)
                .patient(patient)
                .build();

        Order savedOrder = orderRepository.save(order);

        List<TestResult> testResults = new ArrayList<>();

        createOrderRequest.getTests().forEach(testRequest -> {
            // Test date must be 0, 15, 30, 45
            if (testRequest.getTestDate().getMinute() % 15 != 0) {
                throw new IllegalArgumentException("Test date must be 0, 15, 30, 45");
            }

            Laboratory laboratory = laboratoryRepository.findById(testRequest.getLaboratoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Laboratory not found"));
            Test test = testRepository.findById(testRequest.getTestId())
                    .orElseThrow(() -> new EntityNotFoundException("Test not found"));
            OffsetDateTime trueTestDate = testRequest.getTestDate().withSecond(0).withNano(0);

            if (!laboratory.getTests().contains(test)) {
                throw new EntityNotFoundException("This laboratory does not provide this test");
            }

            if (!testResultRepository.findByLaboratoryAndTestAndTestDate(laboratory, test, trueTestDate).isEmpty()) {
                throw new EntityNotFoundException("This test is already ordered");
            }

            // Find an available laboratory assistant for this test
            LaboratoryAssistant availableAssistant = laboratoryAssistantRepository.findByLaboratory(laboratory)
                    .stream()
                    .filter(assistant -> testResultRepository.findByLaboratoryAssistantAndTestDate(assistant, trueTestDate).isEmpty())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No available laboratory assistants at the requested time"));

            TestResult testResult = TestResult.builder()
                    .testDate(trueTestDate)
                    .test(test)
                    .laboratory(laboratory)
                    .laboratoryAssistant(availableAssistant)
                    .order(savedOrder)
                    .build();

            testResults.add(testResult);
        });

        testResultRepository.saveAll(testResults);

        savedOrder.getTestResults().addAll(testResults);

        var newOrder = orderRepository.save(savedOrder);

        return orderMapper.toDto(newOrder);
    }

    @Transactional
    public OrderDto payOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getPatient().getId().equals(auditService.getPrincipal().getId())) {
            throw new EntityNotFoundException("Order not found");
        }

        order.setPaid(true);

        return orderMapper.toDto(orderRepository.save(order));
    }
}
