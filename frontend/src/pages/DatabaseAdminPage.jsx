import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/header/Header';
import Footer from '../components/footer/Footer';
import styles from '../styles/database_admin_page.module.css';

export default function DatabaseAdminPage() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ text: '', isError: false });
    const [testDataCount, setTestDataCount] = useState(10);
    
    const userRole = localStorage.getItem("userRole");
    const token = localStorage.getItem("accessToken");
    
    // Check if user is authorized and is an admin
    if (!token || userRole !== "ADMINISTRATOR") {
        navigate('/');
        return null;
    }

    const handleCSVUpload = async (event, endpoint) => {
        event.preventDefault();
        const file = event.target.file.files[0];
        
        if (!file) {
            setMessage({ text: 'Please select a file', isError: true });
            return;
        }

        if (file.type !== 'text/csv' && file.type !== 'application/vnd.ms-excel') {
            setMessage({ text: 'Please upload a valid CSV file', isError: true });
            return;
        }

        setLoading(true);
        setMessage({ text: '', isError: false });

        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await fetch(`http://localhost:8080/api/admin/upload-csv/${endpoint}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            const data = await response.text();
            
            if (response.ok) {
                setMessage({ text: data, isError: false });
                // Reset the file input
                event.target.file.value = '';
            } else {
                setMessage({ text: data || 'Upload failed', isError: true });
            }
        } catch (error) {
            setMessage({ text: `Error: ${error.message}`, isError: true });
        } finally {
            setLoading(false);
        }
    };

    const handleGenerateTestData = async (event) => {
        event.preventDefault();
        
        if (testDataCount <= 0) {
            setMessage({ text: 'Count must be greater than 0', isError: true });
            return;
        }

        setLoading(true);
        setMessage({ text: '', isError: false });

        try {
            const response = await fetch(`http://localhost:8080/api/admin/generate-test-data?count=${testDataCount}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            const data = await response.text();
            
            if (response.ok) {
                setMessage({ text: data, isError: false });
            } else {
                setMessage({ text: data || 'Generation failed', isError: true });
            }
        } catch (error) {
            setMessage({ text: `Error: ${error.message}`, isError: true });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.page_container}>
            <Header />
            <main className={styles.content}>
                <h1 className={styles.title}>Database Administration Tools</h1>
                
                {message.text && (
                    <div className={`${styles.message} ${message.isError ? styles.error : styles.success}`}>
                        {message.text}
                    </div>
                )}

                <div className={styles.admin_sections}>
                    <section className={styles.section}>
                        <h2>Upload CSV Data</h2>
                        <p className={styles.description}>
                            Upload CSV files to populate the database with laboratories, assistants, or tests.
                            Make sure your CSV files have the correct format.
                        </p>
                        
                        <div className={styles.upload_forms}>
                            <form 
                                className={styles.upload_form} 
                                onSubmit={(e) => handleCSVUpload(e, 'laboratories')}
                            >
                                <h3>Upload Laboratories</h3>
                                <div className={styles.form_group}>
                                    <label htmlFor="labFile">Select CSV file:</label>
                                    <input 
                                        type="file" 
                                        id="labFile" 
                                        name="file" 
                                        accept=".csv" 
                                        className={styles.file_input} 
                                    />
                                </div>
                                <button 
                                    type="submit" 
                                    className={styles.upload_button}
                                    disabled={loading}
                                >
                                    {loading ? 'Uploading...' : 'Upload Laboratories'}
                                </button>
                            </form>
                            
                            <form 
                                className={styles.upload_form} 
                                onSubmit={(e) => handleCSVUpload(e, 'laboratory-assistants')}
                            >
                                <h3>Upload Laboratory Assistants</h3>
                                <div className={styles.form_group}>
                                    <label htmlFor="assistantFile">Select CSV file:</label>
                                    <input 
                                        type="file" 
                                        id="assistantFile" 
                                        name="file" 
                                        accept=".csv" 
                                        className={styles.file_input} 
                                    />
                                </div>
                                <button 
                                    type="submit" 
                                    className={styles.upload_button}
                                    disabled={loading}
                                >
                                    {loading ? 'Uploading...' : 'Upload Assistants'}
                                </button>
                            </form>
                            
                            <form 
                                className={styles.upload_form} 
                                onSubmit={(e) => handleCSVUpload(e, 'tests')}
                            >
                                <h3>Upload Tests</h3>
                                <div className={styles.form_group}>
                                    <label htmlFor="testFile">Select CSV file:</label>
                                    <input 
                                        type="file" 
                                        id="testFile" 
                                        name="file" 
                                        accept=".csv" 
                                        className={styles.file_input} 
                                    />
                                </div>
                                <button 
                                    type="submit" 
                                    className={styles.upload_button}
                                    disabled={loading}
                                >
                                    {loading ? 'Uploading...' : 'Upload Tests'}
                                </button>
                            </form>
                        </div>
                    </section>
                    
                    <section className={styles.section}>
                        <h2>Generate Test Data</h2>
                        <p className={styles.description}>
                            Generate random test data including patients, orders, and test results.
                            This will use existing laboratories, assistants, and tests in the database.
                        </p>
                        
                        <form 
                            className={styles.generator_form} 
                            onSubmit={handleGenerateTestData}
                        >
                            <div className={styles.form_group}>
                                <label htmlFor="countInput">Number of patients to generate:</label>
                                <input 
                                    type="number" 
                                    id="countInput" 
                                    value={testDataCount}
                                    onChange={(e) => setTestDataCount(parseInt(e.target.value))}
                                    min="1"
                                    className={styles.number_input} 
                                />
                            </div>
                            <button 
                                type="submit" 
                                className={styles.generate_button}
                                disabled={loading}
                            >
                                {loading ? 'Generating...' : 'Generate Test Data'}
                            </button>
                        </form>
                    </section>
                </div>
            </main>
            <Footer />
        </div>
    );
}