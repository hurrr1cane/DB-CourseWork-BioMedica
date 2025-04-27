import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from '../styles/tests_management_page.module.css';
import Header from '../components/header/Header';
import Footer from '../components/footer/Footer';

export default function TestDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [test, setTest] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    
    const isNewTest = id === 'new';

    // Move fetchTest outside useEffect and make it a useCallback
    const fetchTest = useCallback(async () => {
        // If it's a new test, initialize an empty test object
        if (isNewTest) {
            setTest({
                name: '',
                description: '',
                price: 0
            });
            setLoading(false);
            return;
        }
        
        try {
            setLoading(true);
            // Get access token from localStorage
            const token = localStorage.getItem('accessToken');
            
            const response = await fetch(`http://localhost:8080/api/admin/tests/${id}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to fetch test details');
            }
            
            const data = await response.json();
            setTest(data);
            setError(null);
        } catch (error) {
            console.error("Error fetching test details:", error);
            setError(error.message || 'An error occurred while fetching test details');
        } finally {
            setLoading(false);
        }
    }, [id, isNewTest]);

    useEffect(() => {
        fetchTest();
    }, [fetchTest]);

    const handleSave = async () => {
        try {
            // Get access token from localStorage
            const token = localStorage.getItem('accessToken');
            
            const response = await fetch(
                isNewTest 
                    ? 'http://localhost:8080/api/admin/tests' 
                    : `http://localhost:8080/api/admin/tests/${id}`, 
                {
                    method: isNewTest ? 'POST' : 'PATCH', // Use POST for new, PATCH for edit
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}` // Add the authorization header
                    },
                    body: JSON.stringify(test),
                }
            );

            if (!response.ok) {
                throw new Error(`Failed to ${isNewTest ? 'create' : 'update'} test details`);
            }

            navigate('/tests');
        } catch (error) {
            console.error("Error saving test details:", error);
            setError(error.message || 'An error occurred while saving test details');
        }
    };

    // Add delete handler
    const handleDelete = async () => {
        try {
            const token = localStorage.getItem('accessToken');
            
            const response = await fetch(`http://localhost:8080/api/admin/tests/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to delete test');
            }

            navigate('/tests');
        } catch (error) {
            console.error("Error deleting test:", error);
            setError(error.message || 'An error occurred while deleting the test');
        } finally {
            setShowDeleteConfirm(false);
        }
    };

    const handleCancel = () => {
        navigate('/tests');
    };

    if (loading) {
        return (
            <div className={styles.page_container}>
                <Header />
                <div className={styles.detail_content}>
                    <div className={styles.loading_container}>
                        <div className={styles.loading_spinner}></div>
                    </div>
                </div>
                <Footer />
            </div>
        );
    }

    if (error) {
        return (
            <div className={styles.page_container}>
                <Header />
                <div className={styles.detail_content}>
                    <div className={styles.error_message}>
                        <p>{error}</p>
                        <button 
                            onClick={() => isNewTest ? navigate('/tests') : fetchTest()} 
                            className={styles.retry_button}
                        >
                            {isNewTest ? 'Go Back' : 'Retry'}
                        </button>
                    </div>
                </div>
                <Footer />
            </div>
        );
    }

    return (
        <div className={styles.page_container}>
            <Header />
            <div className={styles.detail_content}>
                <h1 className={styles.detail_title}>
                    {isNewTest ? 'Create New Test' : `Edit Test: ${test.name}`}
                </h1>
                
                <div className={styles.form_group}>
                    <label className={styles.form_label}>Name:</label>
                    <input
                        type="text"
                        className={styles.form_input}
                        value={test?.name || ''}
                        onChange={(e) => setTest({ ...test, name: e.target.value })}
                        placeholder="Enter test name"
                    />
                </div>
                
                <div className={styles.form_group}>
                    <label className={styles.form_label}>Description:</label>
                    <textarea
                        className={styles.form_textarea}
                        value={test?.description || ''}
                        onChange={(e) => setTest({ ...test, description: e.target.value })}
                        placeholder="Enter test description"
                    />
                </div>
                
                <div className={styles.form_group}>
                    <label className={styles.form_label}>Price ($):</label>
                    <input
                        type="number"
                        step="0.01"
                        className={styles.form_input}
                        value={test?.price || 0}
                        onChange={(e) => setTest({ ...test, price: parseFloat(e.target.value) })}
                        min="0"
                    />
                </div>
                
                <div className={styles.button_group}>
                    <button onClick={handleSave} className={styles.save_button}>
                        {isNewTest ? 'Create Test' : 'Update Test'}
                    </button>
                    {!isNewTest && (
                        <button 
                            onClick={() => setShowDeleteConfirm(true)} 
                            className={styles.delete_button}
                        >
                            Delete Test
                        </button>
                    )}
                    <button onClick={handleCancel} className={styles.cancel_button}>
                        Cancel
                    </button>
                </div>
            </div>
            
            {/* Delete confirmation modal */}
            {showDeleteConfirm && (
                <div className={styles.modal_overlay}>
                    <div className={styles.modal}>
                        <h3>Delete Test</h3>
                        <p>Are you sure you want to delete this test? This action cannot be undone.</p>
                        <div className={styles.modal_buttons}>
                            <button 
                                onClick={handleDelete} 
                                className={styles.delete_confirm_button}
                            >
                                Yes, Delete Test
                            </button>
                            <button 
                                onClick={() => setShowDeleteConfirm(false)} 
                                className={styles.cancel_button}
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
            
            <Footer />
        </div>
    );
}