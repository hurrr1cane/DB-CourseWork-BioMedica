import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import styles from '../styles/laboratory_page.module.css';

const LaboratoryPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [laboratory, setLaboratory] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [editing, setEditing] = useState(false);
    const [editedLaboratory, setEditedLaboratory] = useState(null);

    const api = "http://localhost:8080/api/";

    // State for managing test list dialog
    const [testsDialogOpen, setTestsDialogOpen] = useState(false);
    const [availableTests, setAvailableTests] = useState([]);
    const [selectedTests, setSelectedTests] = useState([]);

    // State for managing assistants dialog
    const [assistantsDialogOpen, setAssistantsDialogOpen] = useState(false);
    const [availableAssistants, setAvailableAssistants] = useState([]);
    const [selectedAssistants, setSelectedAssistants] = useState([]);

    useEffect(() => {
        const fetchLaboratory = async () => {
            try {
                setLoading(true);
                const response = await fetch(`${api}laboratories/${id}`);
                const data = await response.json();

                setLaboratory(data);
                setEditedLaboratory(data);
                setSelectedTests(data.tests || []);
                setSelectedAssistants(data.laboratoryAssistants || []);
            } catch (err) {
                setError('Failed to fetch laboratory details');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        if (id !== 'new') {
            fetchLaboratory();
        } else {
            setLaboratory({
                id: '',
                address: '',
                workingHours: '',
                phoneNumber: '',
                tests: [],
                laboratoryAssistants: []
            });
            setEditedLaboratory({
                id: '',
                address: '',
                workingHours: '',
                phoneNumber: '',
                tests: [],
                laboratoryAssistants: []
            });
            setLoading(false);
            setEditing(true);
        }
    }, [id]);

    useEffect(() => {
        // Fetch available tests for the dialog
        const fetchTests = async () => {
            try {
                const response = await axios.get('/api/tests');
                setAvailableTests(response.data);
            } catch (err) {
                console.error('Failed to fetch tests', err);
            }
        };

        // Fetch available laboratory assistants for the dialog
        const fetchAssistants = async () => {
            try {
                const response = await axios.get('/api/laboratory-assistants');
                setAvailableAssistants(response.data);
            } catch (err) {
                console.error('Failed to fetch laboratory assistants', err);
            }
        };

        if (testsDialogOpen) {
            fetchTests();
        }

        if (assistantsDialogOpen) {
            fetchAssistants();
        }
    }, [testsDialogOpen, assistantsDialogOpen]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setEditedLaboratory({
            ...editedLaboratory,
            [name]: value,
        });
    };

    const handleSave = async () => {
        try {
            const laboratoryToSave = {
                ...editedLaboratory,
                tests: selectedTests,
                laboratoryAssistants: selectedAssistants
            };

            const token = localStorage.getItem('accessToken');

            let response;
            if (id === 'new') {
                response = await fetch(`${api}admin/laboratories`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify(laboratoryToSave),
                });

            } else {
                response = await fetch(`${api}admin/laboratories/${id}`, {
                    method: 'PATCH',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify(laboratoryToSave),
                });
            }

            setLaboratory(response.data);
            setEditing(false);
            if (id === 'new') {
                navigate(`/laboratories/${response.data.id}`);
            }
        } catch (err) {
            console.error('Failed to save laboratory', err);
            setError('Failed to save laboratory details');
        }
    };

    const handleCancel = () => {
        if (id === 'new') {
            navigate('/laboratories');
        } else {
            setEditedLaboratory(laboratory);
            setSelectedTests(laboratory.tests || []);
            setSelectedAssistants(laboratory.laboratoryAssistants || []);
            setEditing(false);
        }
    };

    const handleTestSelection = (test) => {
        const testExists = selectedTests.some(t => t.id === test.id);
        if (testExists) {
            setSelectedTests(selectedTests.filter(t => t.id !== test.id));
        } else {
            setSelectedTests([...selectedTests, test]);
        }
    };

    const handleAssistantSelection = (assistant) => {
        const assistantExists = selectedAssistants.some(a => a.id === assistant.id);
        if (assistantExists) {
            setSelectedAssistants(selectedAssistants.filter(a => a.id !== assistant.id));
        } else {
            setSelectedAssistants([...selectedAssistants, assistant]);
        }
    };

    if (loading) {
        return (
            <div className={styles.loading_container}>
                <div className={styles.loading_spinner}></div>
            </div>
        );
    }

    if (error) {
        return (
            <div className={styles.container}>
                <div className={styles.error_card}>
                    <p className={styles.error_message}>{error}</p>
                    <button className={styles.button} onClick={() => navigate('/laboratories')}>
                        Back to Laboratories
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.container}>
            <div className={styles.paper}>
                <div className={styles.header}>
                    <h2 className={styles.title}>
                        {id === 'new' ? 'New Laboratory' : 'Laboratory Details'}
                    </h2>
                    <div className={styles.actions}>
                        {!editing && (
                            <button className={styles.button} onClick={() => setEditing(true)}>
                                Edit
                            </button>
                        )}
                        {editing && (
                            <>
                                <button className={styles.button} onClick={handleSave}>
                                    Save
                                </button>
                                <button className={styles.button_outline} onClick={handleCancel}>
                                    Cancel
                                </button>
                            </>
                        )}
                    </div>
                </div>

                <div className={styles.divider}></div>

                <div className={styles.grid_container}>
                    <div className={styles.grid_item}>
                        <div className={styles.card}>
                            <div className={styles.card_content}>
                                <h3 className={styles.section_title}>Basic Information</h3>

                                <div className={styles.form_field}>
                                    <label htmlFor="id">ID</label>
                                    <input
                                        id="id"
                                        name="id"
                                        type="text"
                                        value={editedLaboratory?.id || ''}
                                        onChange={handleInputChange}
                                        disabled={id !== 'new' || !editing}
                                        className={styles.input}
                                    />
                                    {id !== 'new' && <p className={styles.helper_text}>ID cannot be changed</p>}
                                </div>

                                <div className={styles.form_field}>
                                    <label htmlFor="address">Address</label>
                                    <input
                                        id="address"
                                        name="address"
                                        type="text"
                                        value={editedLaboratory?.address || ''}
                                        onChange={handleInputChange}
                                        disabled={!editing}
                                        className={styles.input}
                                        required
                                    />
                                </div>

                                <div className={styles.form_field}>
                                    <label htmlFor="workingHours">Working Hours</label>
                                    <input
                                        id="workingHours"
                                        name="workingHours"
                                        type="text"
                                        value={editedLaboratory?.workingHours || ''}
                                        onChange={handleInputChange}
                                        disabled={!editing}
                                        className={styles.input}
                                        placeholder="e.g. Mon-Fri: 8:00-17:00"
                                        required
                                    />
                                </div>

                                <div className={styles.form_field}>
                                    <label htmlFor="phoneNumber">Phone Number</label>
                                    <input
                                        id="phoneNumber"
                                        name="phoneNumber"
                                        type="text"
                                        value={editedLaboratory?.phoneNumber || ''}
                                        onChange={handleInputChange}
                                        disabled={!editing}
                                        className={styles.input}
                                        required
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className={styles.grid_item}>
                        <div className={styles.card}>
                            <div className={styles.card_content}>
                                <div className={styles.section_header}>
                                    <h3 className={styles.section_title}>Available Tests</h3>
                                    {editing && (
                                        <button
                                            className={styles.button_small}
                                            onClick={() => setTestsDialogOpen(true)}
                                        >
                                            Manage Tests
                                        </button>
                                    )}
                                </div>

                                {selectedTests.length === 0 ? (
                                    <p className={styles.empty_message}>No tests assigned to this laboratory</p>
                                ) : (
                                    <ul className={styles.item_list}>
                                        {selectedTests.map((test) => (
                                            <li key={test.id} className={styles.list_item}>
                                                <div className={styles.item_primary}>{test.name}</div>
                                                <div className={styles.item_secondary}>Code: {test.code} | Price: ${Number(test.price).toFixed(2)}</div>
                                            </li>
                                        ))}
                                    </ul>
                                )}
                            </div>
                        </div>

                        <div className={styles.card}>
                            <div className={styles.card_content}>
                                <div className={styles.section_header}>
                                    <h3 className={styles.section_title}>Laboratory Assistants</h3>
                                    {editing && (
                                        <button
                                            className={styles.button_small}
                                            onClick={() => setAssistantsDialogOpen(true)}
                                        >
                                            Manage Assistants
                                        </button>
                                    )}
                                </div>

                                {selectedAssistants.length === 0 ? (
                                    <p className={styles.empty_message}>No assistants assigned to this laboratory</p>
                                ) : (
                                    <ul className={styles.item_list}>
                                        {selectedAssistants.map((assistant) => (
                                            <li key={assistant.id} className={styles.list_item}>
                                                <div className={styles.item_primary}>{assistant.firstName} {assistant.lastName}</div>
                                                <div className={styles.item_secondary}>{assistant.email}</div>
                                            </li>
                                        ))}
                                    </ul>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Dialog for managing tests */}
            {testsDialogOpen && (
                <div className={styles.dialog_overlay}>
                    <div className={styles.dialog}>
                        <div className={styles.dialog_header}>
                            <h3 className={styles.dialog_title}>Manage Laboratory Tests</h3>
                            <button className={styles.close_button} onClick={() => setTestsDialogOpen(false)}>×</button>
                        </div>
                        <div className={styles.dialog_content}>
                            <ul className={styles.dialog_list}>
                                {availableTests.map((test) => {
                                    const isSelected = selectedTests.some(t => t.id === test.id);
                                    return (
                                        <li
                                            key={test.id}
                                            className={`${styles.dialog_list_item} ${isSelected ? styles.selected : ''}`}
                                            onClick={() => handleTestSelection(test)}
                                        >
                                            <div className={styles.item_primary}>{test.name}</div>
                                            <div className={styles.item_secondary}>Code: {test.code} | Price: ${Number(test.price).toFixed(2)}</div>
                                        </li>
                                    );
                                })}
                            </ul>
                        </div>
                        <div className={styles.dialog_actions}>
                            <button className={styles.button} onClick={() => setTestsDialogOpen(false)}>Done</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Dialog for managing laboratory assistants */}
            {assistantsDialogOpen && (
                <div className={styles.dialog_overlay}>
                    <div className={styles.dialog}>
                        <div className={styles.dialog_header}>
                            <h3 className={styles.dialog_title}>Manage Laboratory Assistants</h3>
                            <button className={styles.close_button} onClick={() => setAssistantsDialogOpen(false)}>×</button>
                        </div>
                        <div className={styles.dialog_content}>
                            <ul className={styles.dialog_list}>
                                {availableAssistants.map((assistant) => {
                                    const isSelected = selectedAssistants.some(a => a.id === assistant.id);
                                    return (
                                        <li
                                            key={assistant.id}
                                            className={`${styles.dialog_list_item} ${isSelected ? styles.selected : ''}`}
                                            onClick={() => handleAssistantSelection(assistant)}
                                        >
                                            <div className={styles.item_primary}>{assistant.firstName} {assistant.lastName}</div>
                                            <div className={styles.item_secondary}>{assistant.email}</div>
                                        </li>
                                    );
                                })}
                            </ul>
                        </div>
                        <div className={styles.dialog_actions}>
                            <button className={styles.button} onClick={() => setAssistantsDialogOpen(false)}>Done</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default LaboratoryPage;