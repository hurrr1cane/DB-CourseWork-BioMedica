import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Header from '../components/header/Header';
import Footer from '../components/footer/Footer';
import styles from '../styles/assistant_detail_page.module.css';

export default function AssistantDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [assistant, setAssistant] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [editing, setEditing] = useState(false);
    const [editedAssistant, setEditedAssistant] = useState(null);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [password, setPassword] = useState('password'); // Default password
    
    const api = "http://localhost:8080/api/admin";
    
    const isNewAssistant = id === 'new';

    useEffect(() => {
        const fetchAssistant = async () => {
            try {
                setLoading(true);
                if (isNewAssistant) {
                    const newAssistant = {
                        id: '',
                        firstName: '',
                        lastName: '',
                        email: '',
                        phoneNumber: '',
                        laboratoryId: null
                    };
                    setAssistant(newAssistant);
                    setEditedAssistant(newAssistant);
                    setEditing(true);
                    setLoading(false);
                    return;
                }

                const token = localStorage.getItem('accessToken');
                const response = await fetch(`${api}/laboratory-assistants/${id}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch assistant details');
                }

                const data = await response.json();
                setAssistant(data);
                setEditedAssistant({...data});
                setError(null);
            } catch (err) {
                console.error("Error fetching laboratory assistant:", err);
                setError(err.message || 'Failed to load assistant data');
            } finally {
                setLoading(false);
            }
        };

        fetchAssistant();
    }, [id, isNewAssistant, api]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setEditedAssistant(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSave = async () => {
        try {
            const token = localStorage.getItem('accessToken');
            
            // Prepare data for the request based on RegisterLabAssistantRequest structure
            const assistantData = {
                email: editedAssistant.email,
                name: editedAssistant.firstName,
                surname: editedAssistant.lastName,
                phoneNumber: editedAssistant.phoneNumber,
                password: isNewAssistant ? password : undefined, // Use the password value for new assistants
                laboratoryId: editedAssistant.laboratoryId || null
            };
            
            let response;
            
            if (isNewAssistant) {
                // Use the correct endpoint for registering new lab assistants
                response = await fetch(`${api}/laboratory-assistants`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify(assistantData)
                });
            } else {
                response = await fetch(`${api}/laboratory-assistants/${id}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify(assistantData)
                });
            }

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to save assistant');
            }

            const data = await response.json();
            setAssistant(data);
            setEditedAssistant({...data});
            setEditing(false);
            
            if (isNewAssistant) {
                navigate(`/assistants/${data.id}`);
            }
        } catch (err) {
            console.error("Error saving laboratory assistant:", err);
            setError(err.message || 'Failed to save assistant data');
        }
    };

    const handleDelete = async () => {
        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch(`${api}/laboratory-assistants/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to delete assistant');
            }

            navigate('/assistants');
        } catch (err) {
            console.error("Error deleting laboratory assistant:", err);
            setError(err.message || 'Failed to delete assistant');
            setShowDeleteConfirm(false);
        }
    };

    const handleCancel = () => {
        if (isNewAssistant) {
            navigate('/assistants');
        } else {
            setEditedAssistant({...assistant});
            setEditing(false);
        }
    };

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

    // Add this check to prevent "Cannot read properties of null" error
    if (!assistant && !isNewAssistant) {
        return (
            <div className={styles.page_container}>
                <Header />
                <main className={styles.content}>
                    <div className={styles.back_navigation}>
                        <button className={styles.back_button} onClick={() => navigate('/assistants')}>
                            ← Back to Assistants
                        </button>
                    </div>
                    <div className={styles.error_message}>
                        <p>Assistant not found or could not be loaded.</p>
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
                <div className={styles.back_navigation}>
                    <button className={styles.back_button} onClick={() => navigate('/assistants')}>
                        ← Back to Assistants
                    </button>
                </div>
                
                <div className={styles.detail_card}>
                    <div className={styles.card_header}>
                        <div className={styles.header_left}>
                            <h1 className={styles.title}>
                                {isNewAssistant ? 'New Laboratory Assistant' : 'Laboratory Assistant Details'}
                            </h1>
                        </div>
                        <div className={styles.actions}>
                            {!editing && !isNewAssistant && (
                                <>
                                    <button 
                                        className={styles.button} 
                                        onClick={() => setEditing(true)}
                                    >
                                        Edit
                                    </button>
                                    <button 
                                        className={styles.button_danger} 
                                        onClick={() => setShowDeleteConfirm(true)}
                                    >
                                        Delete
                                    </button>
                                </>
                            )}
                            {editing && (
                                <>
                                    <button 
                                        className={styles.button} 
                                        onClick={handleSave}
                                    >
                                        Save
                                    </button>
                                    <button 
                                        className={styles.button_outline} 
                                        onClick={handleCancel}
                                    >
                                        Cancel
                                    </button>
                                </>
                            )}
                        </div>
                    </div>

                    {error && (
                        <div className={styles.error_message}>
                            <p>{error}</p>
                        </div>
                    )}

                    <div className={styles.card_content}>
                        <div className={styles.profile_section}>
                            <div className={styles.large_avatar}>
                                {editedAssistant?.firstName?.charAt(0) || '?'}{editedAssistant?.lastName?.charAt(0) || '?'}
                            </div>
                            
                            {!editing && editedAssistant && (
                                <div className={styles.profile_info}>
                                    <h2>{`${editedAssistant.firstName} ${editedAssistant.lastName}`}</h2>
                                    <p className={styles.email}>{editedAssistant.email}</p>
                                    {editedAssistant.phoneNumber && (
                                        <p className={styles.phone}>{editedAssistant.phoneNumber}</p>
                                    )}
                                    {editedAssistant.laboratoryId && (
                                        <div className={styles.lab_badge}>
                                            Assigned to laboratory
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                        
                        <div className={styles.divider}></div>
                        
                        {editing && editedAssistant && (
                            <div className={styles.edit_form}>
                                <div className={styles.form_row}>
                                    <div className={styles.form_group}>
                                        <label htmlFor="firstName">First Name</label>
                                        <input 
                                            type="text"
                                            id="firstName"
                                            name="firstName"
                                            value={editedAssistant.firstName || ''}
                                            onChange={handleInputChange}
                                            className={styles.input}
                                            placeholder="Enter first name"
                                            required
                                        />
                                    </div>
                                    
                                    <div className={styles.form_group}>
                                        <label htmlFor="lastName">Last Name</label>
                                        <input 
                                            type="text"
                                            id="lastName"
                                            name="lastName"
                                            value={editedAssistant.lastName || ''}
                                            onChange={handleInputChange}
                                            className={styles.input}
                                            placeholder="Enter last name"
                                            required
                                        />
                                    </div>
                                </div>
                                
                                <div className={styles.form_group}>
                                    <label htmlFor="email">Email Address</label>
                                    <input 
                                        type="email"
                                        id="email"
                                        name="email"
                                        value={editedAssistant.email || ''}
                                        onChange={handleInputChange}
                                        className={styles.input}
                                        placeholder="example@email.com"
                                        required
                                    />
                                </div>
                                
                                <div className={styles.form_group}>
                                    <label htmlFor="phoneNumber">Phone Number</label>
                                    <input 
                                        type="tel"
                                        id="phoneNumber"
                                        name="phoneNumber"
                                        value={editedAssistant.phoneNumber || ''}
                                        onChange={handleInputChange}
                                        className={styles.input}
                                        placeholder="+1 (555) 123-4567"
                                        required
                                    />
                                </div>
                                
                                {isNewAssistant && (
                                    <div className={styles.form_group}>
                                        <label htmlFor="password">Password</label>
                                        <div className={styles.password_field}>
                                            <input 
                                                type="text"
                                                id="password"
                                                name="password"
                                                value={password}
                                                onChange={(e) => setPassword(e.target.value)}
                                                className={styles.input}
                                                readOnly
                                                required
                                            />
                                            <div className={styles.password_info}>Default password (readonly)</div>
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                        
                        {!editing && editedAssistant && (
                            <div className={styles.details_section}>
                                <div className={styles.detail_row}>
                                    <div className={styles.detail_label}>First Name:</div>
                                    <div className={styles.detail_value}>{editedAssistant.firstName}</div>
                                </div>
                                
                                <div className={styles.detail_row}>
                                    <div className={styles.detail_label}>Last Name:</div>
                                    <div className={styles.detail_value}>{editedAssistant.lastName}</div>
                                </div>
                                
                                <div className={styles.detail_row}>
                                    <div className={styles.detail_label}>Email:</div>
                                    <div className={styles.detail_value}>{editedAssistant.email}</div>
                                </div>
                                
                                <div className={styles.detail_row}>
                                    <div className={styles.detail_label}>Phone:</div>
                                    <div className={styles.detail_value}>{editedAssistant.phoneNumber || 'Not provided'}</div>
                                </div>
                                
                                <div className={styles.detail_row}>
                                    <div className={styles.detail_label}>Laboratory:</div>
                                    <div className={styles.detail_value}>
                                        {editedAssistant.laboratoryId 
                                            ? <span>Assigned (ID: {editedAssistant.laboratoryId})</span> 
                                            : <span className={styles.not_assigned}>Not assigned</span>}
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
                
                {/* Delete confirmation dialog */}
                {showDeleteConfirm && (
                    <div className={styles.dialog_overlay}>
                        <div className={styles.dialog}>
                            <div className={styles.dialog_header}>
                                <h3 className={styles.dialog_title}>Confirm Deletion</h3>
                            </div>
                            <div className={styles.dialog_content}>
                                <p>Are you sure you want to delete this laboratory assistant?</p>
                                <p className={styles.warning_text}>This action cannot be undone.</p>
                            </div>
                            <div className={styles.dialog_actions}>
                                <button 
                                    className={styles.button_danger} 
                                    onClick={handleDelete}
                                >
                                    Delete
                                </button>
                                <button 
                                    className={styles.button_outline} 
                                    onClick={() => setShowDeleteConfirm(false)}
                                >
                                    Cancel
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </main>
            <Footer />
        </div>
    );
}