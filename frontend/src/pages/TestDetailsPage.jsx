import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Header from '../components/header/Header';
import Footer from '../components/footer/Footer';
import styles from '../styles/test_details.module.css';

export default function TestDetails() {
    const { id } = useParams();
    const navigate = useNavigate();
    const api = "http://localhost:8080/api/lab";

    const [testResult, setTestResult] = useState(null);
    const [resultData, setResultData] = useState('');
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [canceling, setCanceling] = useState(false);
    const [error, setError] = useState(null);
    const [cancelError, setCancelError] = useState(null);
    const [successMessage, setSuccessMessage] = useState(null);

    useEffect(() => {
        fetchTestResult();
    }, [id]);

    const fetchTestResult = async () => {
        setLoading(true);
        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch(`${api}/test-results/${id}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch test result details');
            }

            const data = await response.json();
            setTestResult(data);
            if (data.result) {
                setResultData(data.result);
            }
            setError(null);
        } catch (error) {
            console.error("Error fetching test result:", error);
            setError(error.message || 'An error occurred while fetching data');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch(`${api}/test-results/${id}/fill`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(resultData)
            });

            if (!response.ok) {
                throw new Error('Failed to submit test result');
            }

            const updatedTest = await response.json();
            setTestResult(updatedTest);
            setSuccessMessage('Test result successfully submitted!');

            setTimeout(() => {
                setSuccessMessage(null);
            }, 3000);
        } catch (error) {
            console.error("Error submitting test result:", error);
            setError(error.message || 'An error occurred while submitting data');
        } finally {
            setSubmitting(false);
        }
    };

    const handleCancel = async () => {
        setCanceling(true);
        setCancelError(null);

        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch(`${api}/test-results/${id}/cancel`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                // Parse the error response
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to cancel participation');
            }

            // Show success message and redirect after a short delay
            setSuccessMessage('Successfully canceled participation in the test');
            setTimeout(() => {
                navigate('/assistant-tests');
            }, 1500);

        } catch (error) {
            console.error("Error canceling participation:", error);
            setCancelError(error.message || 'An error occurred while canceling participation');
        } finally {
            setCanceling(false);
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    if (loading) {
        return (
            <div className={styles.page_container}>
                <Header />
                <main className={styles.content}>
                    <div className={styles.loading_container}>
                        <div className={styles.loading_spinner}></div>
                    </div>
                </main>
                <Footer />
            </div>
        );
    }

    if (error) {
        return (
            <div className={styles.page_container}>
                <Header />
                <main className={styles.content}>
                    <div className={styles.error_message}>
                        <h2>Error</h2>
                        <p>{error}</p>
                        <button onClick={fetchTestResult} className={styles.retry_button}>
                            Retry
                        </button>
                        <button onClick={() => navigate('/assistant-tests')} className={styles.back_button}>
                            Back to Tests
                        </button>
                    </div>
                </main>
                <Footer />
            </div>
        );
    }

    if (!testResult) {
        return (
            <div className={styles.page_container}>
                <Header />
                <main className={styles.content}>
                    <div className={styles.error_message}>
                        <h2>Test Not Found</h2>
                        <button onClick={() => navigate('/assistant-tests')} className={styles.back_button}>
                            Back to Tests
                        </button>
                    </div>
                </main>
                <Footer />
            </div>
        );
    }

    return (
        <div className={styles.page_container}>
            <Header />
            <main className={styles.content}>
                <div className={styles.page_header}>
                    <button onClick={() => navigate('/assistant-tests')} className={styles.back_button}>
                        ← Back to Tests
                    </button>
                    <h1>Test Details</h1>
                </div>

                {successMessage && (
                    <div className={styles.success_message}>
                        <p>{successMessage}</p>
                    </div>
                )}

                {cancelError && (
                    <div className={styles.error_message}>
                        <p>{cancelError}</p>
                    </div>
                )}

                <div className={styles.test_details_card}>
                    <div className={styles.test_info_section}>
                        <h2>{testResult.test.name}</h2>
                        <p className={styles.test_description}>{testResult.test.description}</p>

                        <div className={styles.details_grid}>
                            <div className={styles.detail_item}>
                                <span className={styles.detail_label}>Patient:</span>
                                <span className={styles.detail_value}>{`${testResult.patient.name} ${testResult.patient.surname}`}</span>
                            </div>
                            <div className={styles.detail_item}>
                                <span className={styles.detail_label}>Patient Email:</span>
                                <span className={styles.detail_value}>{testResult.patient.email}</span>
                            </div>
                            <div className={styles.detail_item}>
                                <span className={styles.detail_label}>Scheduled Date:</span>
                                <span className={styles.detail_value}>{formatDate(testResult.testDate)}</span>
                            </div>
                            <div className={styles.detail_item}>
                                <span className={styles.detail_label}>Price:</span>
                                <span className={styles.detail_value}>${testResult.test.price.toFixed(2)}</span>
                            </div>
                            <div className={styles.detail_item}>
                                <span className={styles.detail_label}>Status:</span>
                                <span className={styles.detail_value}>
                                    {testResult.result ? (
                                        <span className={styles.status_completed}>Completed</span>
                                    ) : (
                                        <span className={styles.status_pending}>Pending</span>
                                    )}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className={styles.result_section}>
                        <h3>Test Result</h3>
                        <form onSubmit={handleSubmit}>
                            <textarea
                                className={styles.result_input}
                                value={resultData}
                                onChange={(e) => setResultData(e.target.value)}
                                placeholder="Enter test results here..."
                                rows={10}
                                disabled={!!testResult.result}
                            ></textarea>

                            <div className={styles.action_buttons}>
                                {!testResult.result && (
                                    <>
                                        <button
                                            type="submit"
                                            className={styles.submit_button}
                                            disabled={submitting || canceling || !resultData.trim()}
                                        >
                                            {submitting ? 'Submitting...' : 'Submit Result'}
                                        </button>

                                        {/* Cancel Participation Modal */}
                                        {!canceling ? (
                                            <button
                                                type="button"
                                                className={styles.cancel_button}
                                                onClick={() => document.getElementById('cancelModal').showModal()}
                                                disabled={submitting}
                                            >
                                                Cancel Participation
                                            </button>
                                        ) : (
                                            <button
                                                type="button"
                                                className={styles.cancel_button}
                                                disabled={true}
                                            >
                                                Processing...
                                            </button>
                                        )}

                                        <dialog id="cancelModal" className={styles.modal}>
                                            <div className={styles.modal_content}>
                                                <h3>Cancel Participation</h3>
                                                <p>Are you sure you want to cancel your participation in this test?</p>
                                                <p className={styles.warning_text}>
                                                    Note: You cannot cancel if the test is scheduled to begin in less than 15 minutes.
                                                </p>
                                                <div className={styles.modal_buttons}>
                                                    <button
                                                        type="button"
                                                        className={styles.confirm_button}
                                                        onClick={handleCancel}
                                                        disabled={canceling}
                                                    >
                                                        {canceling ? 'Processing...' : 'Confirm'}
                                                    </button>
                                                    <button
                                                        type="button"
                                                        className={styles.cancel_modal_button}
                                                        onClick={() => document.getElementById('cancelModal').close()}
                                                        disabled={canceling}
                                                    >
                                                        Close
                                                    </button>
                                                </div>
                                            </div>
                                        </dialog>
                                    </>
                                )}
                            </div>
                        </form>
                    </div>
                </div>
            </main>
            <Footer />
        </div>
    );
}