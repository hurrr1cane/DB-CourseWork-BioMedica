package com.biomedica.service;

import com.biomedica.entity.Laboratory;
import com.biomedica.entity.Order;
import com.biomedica.entity.Test;
import com.biomedica.entity.TestResult;
import com.biomedica.entity.user.LaboratoryAssistant;
import com.biomedica.entity.user.Patient;
import com.biomedica.entity.user.Role;
import com.biomedica.repository.LaboratoryAssistantRepository;
import com.biomedica.repository.LaboratoryRepository;
import com.biomedica.repository.PatientRepository;
import com.biomedica.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TestDataGeneratorService {

    private final LaboratoryRepository laboratoryRepository;
    private final LaboratoryAssistantRepository laboratoryAssistantRepository;
    private final TestRepository testRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Transactional
    public int generateRandomData(int count) {
        // Get existing laboratories, laboratory assistants, and tests
        List<Laboratory> laboratories = laboratoryRepository.findAll();
        List<LaboratoryAssistant> laboratoryAssistants = laboratoryAssistantRepository.findAll();
        List<Test> tests = testRepository.findAll();

        // Check if we have the necessary data
        if (laboratories.isEmpty() || laboratoryAssistants.isEmpty() || tests.isEmpty()) {
            throw new IllegalStateException("Cannot generate random data: Missing laboratories, laboratory assistants, or tests");
        }

        // Generate the specified number of patients with orders and test results
        List<Patient> generatedPatients = IntStream.range(0, count)
                .mapToObj(i -> createPatientWithOrdersAndTests(
                        laboratories, laboratoryAssistants, tests))
                .collect(Collectors.toList());

        // Save all patients (cascades to orders and test results)
        patientRepository.saveAll(generatedPatients);

        return count;
    }

    private Patient createPatientWithOrdersAndTests(
            List<Laboratory> laboratories,
            List<LaboratoryAssistant> laboratoryAssistants,
            List<Test> tests) {

        // Create a patient
        Patient patient = new Patient();
        patient.setName("Patient" + UUID.randomUUID().toString().substring(0, 8));
        patient.setSurname("Surname" + UUID.randomUUID().toString().substring(0, 8));
        patient.setEmail("patient" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        patient.setPassword(passwordEncoder.encode("password"));
        patient.setRole(Role.PATIENT);
        patient.setVerified(true);

        // Create 1-3 orders for the patient
        int orderCount = random.nextInt(3) + 1;
        List<Order> orders = new ArrayList<>();

        for (int i = 0; i < orderCount; i++) {
            Order order = Order.builder()
                    .orderDate(OffsetDateTime.now().minusDays(random.nextInt(30)))
                    .isPaid(random.nextBoolean())
                    .patient(patient)
                    .testResults(new ArrayList<>())
                    .build();

            // Create 1-5 test results for each order
            int testResultCount = random.nextInt(5) + 1;
            for (int j = 0; j < testResultCount; j++) {
                // Randomly select a test, laboratory, and laboratory assistant
                Test test = tests.get(random.nextInt(tests.size()));
                Laboratory laboratory = laboratories.get(random.nextInt(laboratories.size()));

                // Select a laboratory assistant from the laboratory if possible, otherwise random
                List<LaboratoryAssistant> labAssistants = laboratory.getLaboratoryAssistants();
                LaboratoryAssistant labAssistant;
                if (labAssistants != null && !labAssistants.isEmpty()) {
                    labAssistant = labAssistants.get(random.nextInt(labAssistants.size()));
                } else {
                    labAssistant = laboratoryAssistants.get(random.nextInt(laboratoryAssistants.size()));
                }

                TestResult testResult = TestResult.builder()
                        .testDate(order.getOrderDate().plusDays(random.nextInt(10) + 1))
                        .result(generateRandomResult())
                        .order(order)
                        .laboratory(laboratory)
                        .test(test)
                        .laboratoryAssistant(labAssistant)
                        .build();

                order.getTestResults().add(testResult);
            }

            orders.add(order);
        }

        patient.setOrders(orders);
        return patient;
    }

    private String generateRandomResult() {
        String[] possibleResults = {
                "Normal", "Abnormal", "Positive", "Negative", "Inconclusive",
                "Within range", "Outside range", "12.5 mg/dL", "140/90 mmHg",
                "98.6°F", "3.5 mmol/L", "15.3 µg/L", "6.8 pH"
        };
        return possibleResults[random.nextInt(possibleResults.length)];
    }
}