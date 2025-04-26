import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from '../styles/tests_management_page.module.css';
import Header from '../components/header/Header';
import Footer from '../components/footer/Footer';

export default function TestsManagementPage() {
    const [tests, setTests] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentPage, setCurrentPage] = useState(0); // Current page number (starts from 0)
    const [totalPages, setTotalPages] = useState(0); // Total pages available
    const api = "http://localhost:8080/api/admin/tests";
    const navigate = useNavigate();

    const fetchTests = async (page) => {
        setLoading(true);
        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch(`${api}?page=${page}&size=10`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch tests');
            }

            const data = await response.json();
            setTests(data.content); // Set tests from the content
            setTotalPages(data.totalPages); // Set total number of pages
            setError(null);
        } catch (error) {
            console.error("Error fetching tests:", error);
            setError(error.message || 'An error occurred while fetching data');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchTests(currentPage);
    }, [currentPage]);

    const handleAddTest = () => {
        navigate('/tests/new');
    };

    const openTestDetails = (id) => {
        navigate(`/tests/${id}`);
    };

    const handleNextPage = () => {
        if (currentPage < totalPages - 1) setCurrentPage((prev) => prev + 1);
    };

    const handlePrevPage = () => {
        if (currentPage > 0) setCurrentPage((prev) => prev - 1);
    };

    return (
        <div className={styles.page_container}>
            <Header />
            <main className={styles.content}>
                <div className={styles.page_header}>
                    <h1>Tests Management</h1>
                    <button className={styles.add_button} onClick={handleAddTest}>
                        Add Test
                    </button>
                </div>

                {error && (
                    <div className={styles.error_message}>
                        <p>{error}</p>
                        <button onClick={() => fetchTests(currentPage)} className={styles.retry_button}>
                            Retry
                        </button>
                    </div>
                )}

                {loading ? (
                    <div className={styles.loading_container}>
                        <div className={styles.loading_spinner}></div>
                    </div>
                ) : (
                    <>
                        <div className={styles.tests_grid}>
                            {tests.length === 0 ? (
                                <p className={styles.empty_message}>No tests found.</p>
                            ) : (
                                tests.map((test) => (
                                    <div 
                                        key={test.id} 
                                        className={styles.test_card}
                                        onClick={() => openTestDetails(test.id)}
                                    >
                                        <h3>{test.name}</h3>
                                        <p className={styles.description}>{test.description}</p>
                                        <p className={styles.price}>Price: ${test.price.toFixed(2)}</p>
                                    </div>
                                ))
                            )}
                        </div>

                        <div className={styles.pagination}>
                            <button
                                onClick={handlePrevPage}
                                disabled={currentPage === 0}
                                className={styles.page_button}
                            >
                                Previous
                            </button>
                            <span className={styles.page_info}>
                                Page {currentPage + 1} of {totalPages || 1}
                            </span>
                            <button
                                onClick={handleNextPage}
                                disabled={currentPage === totalPages - 1 || totalPages === 0}
                                className={styles.page_button}
                            >
                                Next
                            </button>
                        </div>
                    </>
                )}
            </main>
            <Footer />
        </div>
    );
}