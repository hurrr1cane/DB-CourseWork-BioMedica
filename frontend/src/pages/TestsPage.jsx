import { useState, useEffect } from 'react';
import Header from '../components/header/Header';
import Footer from '../components/footer/Footer';
import styles from '../styles/tests_page.module.css';
import { useNavigate } from 'react-router-dom';

export default function TestsPage() {
    const api = "http://localhost:8080/api/lab";

    const [testResults, setTestResults] = useState([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const navigate = useNavigate();

    const fetchData = async (page) => {
        setLoading(true);
        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch(`${api}/test-results?page=${page}&size=10`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to fetch test results');
            }
            
            const data = await response.json();
            setTestResults(data.content);
            setTotalPages(data.totalPages);
            setError(null);
        } catch (error) {
            console.error("Error fetching test results:", error);
            setError(error.message || 'An error occurred while fetching data');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData(currentPage);
    }, [currentPage]);

    const handleNextPage = () => {
        if (currentPage < totalPages - 1) setCurrentPage((prev) => prev + 1);
    };

    const handlePrevPage = () => {
        if (currentPage > 0) setCurrentPage((prev) => prev - 1);
    };

    const openTestDetails = (id) => {
        navigate(`/assistant-tests/${id}`);
    }

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    const getStatusBadge = (result) => {
        if (result) {
            return <div className={`${styles.status_badge} ${styles.completed}`}>Completed</div>;
        } else {
            return <div className={`${styles.status_badge} ${styles.pending}`}>Pending</div>;
        }
    }

    return (
        <div className={styles.page_container}>
            <Header />
            <main className={styles.content}>
                <div className={styles.page_header}>
                    <h1>My Assigned Tests</h1>
                </div>

                {error && (
                    <div className={styles.error_message}>
                        <p>{error}</p>
                        <button onClick={() => fetchData(currentPage)} className={styles.retry_button}>
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
                        {testResults.length === 0 ? (
                            <p className={styles.empty_message}>No tests assigned to you.</p>
                        ) : (
                            <div className={styles.tests_container}>
                                <table className={styles.tests_table}>
                                    <thead>
                                        <tr>
                                            <th>Test Name</th>
                                            <th>Patient</th>
                                            <th>Scheduled Date</th>
                                            <th>Status</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {testResults.map((testResult) => (
                                            <tr key={testResult.id} className={styles.test_row}>
                                                <td>{testResult.test.name}</td>
                                                <td>{`${testResult.patient.name} ${testResult.patient.surname}`}</td>
                                                <td>{formatDate(testResult.testDate)}</td>
                                                <td>{getStatusBadge(testResult.result)}</td>
                                                <td>
                                                    <button 
                                                        className={styles.view_button}
                                                        onClick={() => openTestDetails(testResult.id)}
                                                    >
                                                        View Details
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}

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