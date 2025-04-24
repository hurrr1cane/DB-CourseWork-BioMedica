import { useState, useEffect } from 'react';
import Header from '../components/header/Header';
import Footer from '../components/footer/Footer';
import styles from '../styles/assistants_page.module.css';
import { useNavigate } from 'react-router-dom';

export default function AssistantsPage() {
    const api = "http://localhost:8080/api/admin";

    const [assistants, setAssistants] = useState([]);
    const [currentPage, setCurrentPage] = useState(0); // Current page number (starts from 0)
    const [totalPages, setTotalPages] = useState(0); // Total pages available
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const navigate = useNavigate();

    const fetchData = async (page) => {
        setLoading(true);
        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch(`${api}/laboratory-assistants?page=${page}&size=10`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to fetch laboratory assistants');
            }
            
            const data = await response.json();
            setAssistants(data.content); // Set assistants from the content
            setTotalPages(data.totalPages); // Set total number of pages
            setError(null);
        } catch (error) {
            console.error("Error fetching laboratory assistants:", error);
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

    const openAssistantDetails = (id) => {
        navigate(`/assistants/${id}`);
    }

    const handleAddAssistant = () => {
        navigate('/assistants/new');
    }

    return (
        <div className={styles.page_container}>
            <Header />
            <main className={styles.content}>
                <div className={styles.page_header}>
                    <h1>Laboratory Assistants Management</h1>
                    <button className={styles.add_button} onClick={handleAddAssistant}>
                        Add Laboratory Assistant
                    </button>
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
                        {assistants.length === 0 ? (
                            <p className={styles.empty_message}>No laboratory assistants found.</p>
                        ) : (
                            <div className={styles.assistants_grid}>
                                {assistants.map((assistant) => (
                                    <div 
                                        key={assistant.id} 
                                        className={styles.assistant_card}
                                        onClick={() => openAssistantDetails(assistant.id)}
                                    >
                                        <div className={styles.assistant_avatar}>
                                            {assistant.firstName.charAt(0)}{assistant.lastName.charAt(0)}
                                        </div>
                                        <div className={styles.assistant_info}>
                                            <h3>{`${assistant.firstName} ${assistant.lastName}`}</h3>
                                            <p>{assistant.email}</p>
                                            {assistant.laboratoryId && (
                                                <div className={styles.lab_badge}>
                                                    Assigned to laboratory
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                ))}
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
