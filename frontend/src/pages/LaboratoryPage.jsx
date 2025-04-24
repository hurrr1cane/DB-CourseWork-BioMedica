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
    const [assistantSearchQuery, setAssistantSearchQuery] = useState('');
    const [assistantsPage, setAssistantsPage] = useState(0);
    const [assistantsPageSize, setAssistantsPageSize] = useState(5);
    const [totalAssistantPages, setTotalAssistantPages] = useState(0);
    const [totalAssistantCount, setTotalAssistantCount] = useState(0);

    // New states for test search and pagination
    const [testSearchQuery, setTestSearchQuery] = useState('');
    const [testsPage, setTestsPage] = useState(0);
    const [testsPageSize, setTestsPageSize] = useState(5);
    const [totalTestPages, setTotalTestPages] = useState(0);
    const [totalTestCount, setTotalTestCount] = useState(0);

    useEffect(() => {
        const fetchLaboratory = async () => {
            try {
                setLoading(true);
                const token = localStorage.getItem('accessToken');
                const response = await fetch(`${api}admin/laboratories/${id}`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );
                const data = await response.json();

                setLaboratory(data);
                setEditedLaboratory(data);
                setSelectedTests(data.tests || []);
                setSelectedAssistants(data.assistants || []);
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
                const token = localStorage.getItem('accessToken');
                const response = await fetch(
                    `${api}admin/tests?page=${testsPage}&size=${testsPageSize}&sort=name,asc${
                        testSearchQuery ? `&name=${encodeURIComponent(testSearchQuery)}` : ''
                    }`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );
                const data = await response.json();
                setAvailableTests(data.content || []);
                setTotalTestPages(data.totalPages || 0);
                setTotalTestCount(data.totalElements || 0);
            } catch (err) {
                console.error('Failed to fetch tests', err);
            }
        };

        // Fetch available laboratory assistants for the dialog
        const fetchAssistants = async () => {
            try {
                const token = localStorage.getItem('accessToken');
                const response = await fetch(
                    `${api}admin/laboratory-assistants?page=${assistantsPage}&size=${assistantsPageSize}&sort=surname,asc${
                        assistantSearchQuery ? `&lastName=${encodeURIComponent(assistantSearchQuery)}` : ''
                    }`,
                    {
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    }
                );
                const data = await response.json();
                setAvailableAssistants(data.content || []);
                setTotalAssistantPages(data.totalPages || 0);
                setTotalAssistantCount(data.totalElements || 0);
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
    }, [testsDialogOpen, assistantsDialogOpen, testsPage, testsPageSize, testSearchQuery, 
        assistantsPage, assistantsPageSize, assistantSearchQuery]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setEditedLaboratory({
            ...editedLaboratory,
            [name]: value,
        });
    };

    const handleSave = async () => {
        try {
            // Extract IDs from selected tests and assistants
            const testIds = selectedTests.map(test => test.id);
            const assistantIds = selectedAssistants.map(assistant => assistant.id);
            
            const laboratoryToSave = {
                ...editedLaboratory,
                testIds: testIds,
                laboratoryAssistantIds: assistantIds
            };
            
            // Remove the full objects as they aren't needed in the request
            delete laboratoryToSave.tests;
            delete laboratoryToSave.laboratoryAssistants;

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

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to save laboratory');
            }

            const data = await response.json();
            setLaboratory(data);
            setEditing(false);
            if (id === 'new') {
                navigate(`/laboratories/edit/${data.id}`);
            }
        } catch (err) {
            console.error('Failed to save laboratory', err);
            setError(err.message || 'Failed to save laboratory details');
        }
    };

    const handleCancel = () => {
        if (id === 'new') {
            navigate('/laboratories');
        } else {
            setEditedLaboratory(laboratory);
            setSelectedTests(laboratory.tests || []);
            setSelectedAssistants(laboratory.фssistants || []);
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

    const handlePreviousTestsPage = () => {
        if (testsPage > 0) {
            setTestsPage(testsPage - 1);
        }
    };

    const handleNextTestsPage = () => {
        if (testsPage < totalTestPages - 1) {
            setTestsPage(testsPage + 1);
        }
    };

    const handlePreviousAssistantsPage = () => {
        if (assistantsPage > 0) {
            setAssistantsPage(assistantsPage - 1);
        }
    };

    const handleNextAssistantsPage = () => {
        if (assistantsPage < totalAssistantPages - 1) {
            setAssistantsPage(assistantsPage + 1);
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
                    <div className={styles.header_left}>
                        <button 
                            className={styles.back_button} 
                            onClick={() => navigate('/laboratories')}
                        >
                            ← Back to Laboratories
                        </button>
                        <h2 className={styles.title}>
                            {id === 'new' ? 'New Laboratory' : 'Laboratory Details'}
                        </h2>
                    </div>
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

                                {id !== 'new' ? (
                                    <div className={styles.form_field}>
                                        <label htmlFor="id">ID</label>
                                        <input
                                            id="id"
                                            name="id"
                                            type="text"
                                            value={editedLaboratory?.id || ''}
                                            className={styles.input}
                                            disabled={true}
                                        />
                                        <p className={styles.helper_text}>ID is automatically assigned and cannot be changed</p>
                                    </div>
                                ) : null}  {/* Don't show ID field at all for new laboratory */}

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
                                                <div className={styles.item_secondary}>Description: {test.description}</div>
                                                <div className={styles.item_secondary}> Price: ${Number(test.price).toFixed(2)}</div>
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
                        
                        {/* Selected tests section */}
                        <div className={styles.dialog_section}>
                            <h4 className={styles.dialog_section_title}>Selected Tests</h4>
                            {selectedTests.length === 0 ? (
                                <p className={styles.empty_message}>No tests selected for this laboratory</p>
                            ) : (
                                <ul className={styles.dialog_selected_list}>
                                    {selectedTests.map((test) => (
                                        <li key={test.id} className={styles.dialog_selected_item}>
                                            <div className={styles.item_content}>
                                                <div className={styles.item_primary}>{test.name}</div>
                                                <div className={styles.item_secondary}>
                                                    {test.code && `Code: ${test.code} | `}
                                                    Price: ${Number(test.price).toFixed(2)}
                                                </div>
                                            </div>
                                            <button 
                                                className={styles.remove_button} 
                                                onClick={() => handleTestSelection(test)}
                                                title="Remove from laboratory"
                                            >
                                                ✕
                                            </button>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </div>
                        
                        <div className={styles.dialog_divider}></div>
                        
                        {/* Available tests section */}
                        <div className={styles.dialog_section}>
                            <div className={styles.dialog_section_header}>
                                <h4 className={styles.dialog_section_title}>Available Tests</h4>
                            </div>
                            
                            <div className={styles.dialog_content}>
                                {availableTests.length === 0 ? (
                                    <p className={styles.empty_message}>
                                        {testSearchQuery ? "No tests match your search" : "No tests available"}
                                    </p>
                                ) : (
                                    <ul className={styles.dialog_list}>
                                        {availableTests.map((test) => {
                                            const isSelected = selectedTests.some(t => t.id === test.id);
                                            return (
                                                <li
                                                    key={test.id}
                                                    className={`${styles.dialog_list_item} ${isSelected ? styles.selected : ''}`}
                                                    onClick={() => !isSelected && handleTestSelection(test)}
                                                >
                                                    <div className={styles.item_selection}>
                                                        <button
                                                            className={`${styles.add_button} ${isSelected ? styles.added : ''}`}
                                                            disabled={isSelected}
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                if (!isSelected) handleTestSelection(test);
                                                            }}
                                                            title={isSelected ? "Already added" : "Add to laboratory"}
                                                        >
                                                            {isSelected ? '✓' : '+'}
                                                        </button>
                                                    </div>
                                                    <div className={styles.item_content}>
                                                        <div className={styles.item_primary}>{test.name}</div>
                                                        <div className={styles.item_secondary}>
                                                            {test.code && `Code: ${test.code} | `}
                                                            Price: ${Number(test.price).toFixed(2)}
                                                        </div>
                                                        {test.description && (
                                                            <div className={styles.item_description}>{test.description}</div>
                                                        )}
                                                    </div>
                                                </li>
                                            );
                                        })}
                                    </ul>
                                )}
                            </div>
                            
                            {/* Pagination controls */}
                            <div className={styles.dialog_pagination}>
                                <button 
                                    className={styles.page_button}
                                    onClick={handlePreviousTestsPage}
                                    disabled={testsPage === 0}
                                >
                                    Previous
                                </button>
                                <span className={styles.page_info}>
                                    Page {testsPage + 1} of {totalTestPages || 1} 
                                    ({totalTestCount} total)
                                </span>
                                <button 
                                    className={styles.page_button}
                                    onClick={handleNextTestsPage}
                                    disabled={testsPage >= totalTestPages - 1}
                                >
                                    Next
                                </button>
                            </div>
                        </div>
                        
                        <div className={styles.dialog_actions}>
                            <div className={styles.dialog_info}>
                                <span>{selectedTests.length} tests selected</span>
                            </div>
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
                            <div className={styles.dialog_section_header}>
                                <h4 className={styles.dialog_section_title}>Available Assistants</h4>
                            </div>
                            <ul className={styles.dialog_list}>
                                {availableAssistants.map((assistant) => {
                                    const isSelected = selectedAssistants.some(a => a.id === assistant.id);
                                    const isAssignedElsewhere = assistant.laboratoryId && 
                                        (!editedLaboratory?.id || assistant.laboratoryId !== editedLaboratory.id);
                                    
                                    return (
                                        <li
                                            key={assistant.id}
                                            className={`${styles.dialog_list_item} ${isSelected ? styles.selected : ''}`}
                                            onClick={() => !isSelected && handleAssistantSelection(assistant)}
                                        >
                                            <div className={styles.item_selection}>
                                                <button
                                                    className={`${styles.add_button} ${isSelected ? styles.added : ''}`}
                                                    disabled={isSelected}
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        if (!isSelected) handleAssistantSelection(assistant);
                                                    }}
                                                    title={isSelected ? "Already assigned" : "Add to laboratory"}
                                                >
                                                    {isSelected ? '✓' : '+'}
                                                </button>
                                            </div>
                                            <div className={styles.item_avatar}>
                                                {assistant.firstName.charAt(0)}{assistant.lastName.charAt(0)}
                                            </div>
                                            <div className={styles.item_content}>
                                                <div className={styles.item_primary_container}>
                                                    <div className={styles.item_primary}>{assistant.firstName} {assistant.lastName}</div>
                                                    {isAssignedElsewhere && (
                                                        <div className={styles.assignment_badge} title="Currently assigned to another laboratory">
                                                            Assigned elsewhere
                                                        </div>
                                                    )}
                                                </div>
                                                <div className={styles.item_secondary}>{assistant.email}</div>
                                            </div>
                                        </li>
                                    );
                                })}
                            </ul>
                            <div className={styles.dialog_pagination}>
                                <button 
                                    className={styles.page_button}
                                    onClick={handlePreviousAssistantsPage}
                                    disabled={assistantsPage === 0}
                                >
                                    Previous
                                </button>
                                <span className={styles.page_info}>
                                    Page {assistantsPage + 1} of {totalAssistantPages || 1} 
                                    ({totalAssistantCount} total)
                                </span>
                                <button 
                                    className={styles.page_button}
                                    onClick={handleNextAssistantsPage}
                                    disabled={assistantsPage >= totalAssistantPages - 1}
                                >
                                    Next
                                </button>
                            </div>
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