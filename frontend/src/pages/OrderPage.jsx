import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/header/Header';
import Footer from '../components/footer/Footer';
import LaboratoryItem from '../components/laboratory_item/LaboratoryItem';
import TestItem from '../components/test_item/TestItem';
import styles from '../styles/order_page.module.css';

export default function OrderPage() {
    const navigate = useNavigate();
    const [isAuthorized, setIsAuthorized] = useState(false);
    const [laboratories, setLaboratories] = useState([]);
    const [selectedLab, setSelectedLab] = useState(null);
    const [selectedTests, setSelectedTests] = useState([]);  // Store each test instance with time
    const [labTests, setLabTests] = useState([]); // List of tests for selected lab
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if (!token) {
            navigate('/login');
        } else {
            setIsAuthorized(true);
        }
    }, [navigate]);

    const fetchLaboratories = (page) => {
        fetch(`http://localhost:8080/api/laboratories?page=${page}`)
            .then((res) => res.json())
            .then((data) => {
                setLaboratories((prev) => [...prev, ...data.content]);
                setTotalPages(data.totalPages);
            })
            .catch((error) => console.error('Error fetching laboratories:', error));
    };

    useEffect(() => {
        if (isAuthorized) {
            fetchLaboratories(currentPage);
        }
    }, [isAuthorized, currentPage]);

    const handleSelectLab = (lab) => {
        setSelectedLab(lab);
        setLabTests(lab.tests); // Fetch tests from selected lab
        setSelectedTests([]); // Reset selected tests
    };

    const handleAddTest = (test) => {

        setSelectedTests((prev) => [
            ...prev,
            { ...test, testTime: new Date() }
        ]);
    };

    const handleRemoveTest = (indexToRemove) => () => {
        setSelectedTests((prev) => prev.filter((_, index) => index !== indexToRemove));
    };

    const handleTestTimeChange = (testIndex, updatedTime) => {
        setSelectedTests((prevTests) =>
            prevTests.map((test, index) =>
                index === testIndex ? { ...test, testTime: updatedTime } : test
            )
        );
    };

    const handleCreateOrder = async () => {
        const orderRequest = {
            tests: selectedTests.map((test) => ({
                testDate: test.testTime.toISOString(),
                testId: test.id,
                laboratoryId: selectedLab.id,
            })),
        };
    
        try {
            const response = await fetch('http://localhost:8080/api/patient/orders', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
                },
                body: JSON.stringify(orderRequest),
            });
    
            if (!response.ok) {
                const errorData = await response.json();
                console.error('Error response:', errorData);
                throw new Error('Failed to create order');
            }
    
            const data = await response.json();
            alert('Order created successfully');
            navigate('/profile');
        } catch (error) {
            console.error('Error creating order:', error);
            alert('Failed to create order');
        }
    };
    

    const loadMoreLaboratories = () => {
        if (currentPage + 1 < totalPages) {
            setCurrentPage((prevPage) => prevPage + 1);
        }
    };

    return (
        <div className={styles.order_page_container}>
            <Header />
            <main className={styles.content}>
                <h1>Create Your Order</h1>
                {!selectedLab ? (
                    <div>
                        <h2>Select a Laboratory:</h2>
                        <ul className={styles.laboratories}>
                            {laboratories.map((lab) => (
                                <LaboratoryItem
                                    key={lab.id}
                                    address={lab.address}
                                    phone={lab.phoneNumber}
                                    onClick={() => handleSelectLab(lab)}
                                />
                            ))}
                        </ul>
                        {currentPage + 1 < totalPages && (
                            <button className={styles.load_more_button} onClick={loadMoreLaboratories}>
                                Load More
                            </button>
                        )}
                    </div>
                ) : (
                    <div className={styles.selected_lab}>
                        <h2>Selected Laboratory:</h2>
                        <p>
                            <strong>Address:</strong> {selectedLab.address}
                        </p>
                        <p>
                            <strong>Phone:</strong> {selectedLab.phoneNumber}
                        </p>
                        <h3>Available Tests:</h3>
                        <ul className={styles.test_list}>
                            {labTests.map((test) => (
                                <TestItem
                                    key={test.id}
                                    test={test}
                                    onClick={() => handleAddTest(test)}
                                />
                            ))}
                        </ul>
                        <h3>Chosen Tests:</h3>
                        {selectedTests.length > 0 && (
                            <ul>
                                {selectedTests.map((test, index) => (
                                    <TestItem
                                        key={test.id}
                                        test={test}
                                        onClick={handleRemoveTest(index)}
                                        showTimePicker={true}
                                        index={index}
                                        onTimeChange={handleTestTimeChange}
                                    />
                                ))}
                            </ul>
                        )}
                        {selectedTests.length > 0 && (
                            <div>
                                <button className={styles.order_button} onClick={handleCreateOrder}>
                                    Create Order
                                </button>
                            </div>
                        )}

                    </div>
                )}
            </main>
            <Footer />
        </div>
    );
}
