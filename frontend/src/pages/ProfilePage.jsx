import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/header/Header';
import Footer from '../components/footer/Footer';
import styles from '../styles/profile_page.module.css';

export default function ProfilePage() {
    const navigate = useNavigate();
    const [user, setUser] = useState({ name: '', surname: '' });
    const [orders, setOrders] = useState([]);
    const [currentPage, setCurrentPage] = useState(0); // Current page number
    const [totalPages, setTotalPages] = useState(0); // Total pages available
    const [loading, setLoading] = useState(true); // Loading state

    useEffect(() => {
        const fetchData = async () => {
            const token = localStorage.getItem('accessToken');
            if (!token) {
                navigate('/login');
                return;
            }

            try {
                // Fetch user profile
                const profileResponse = await fetch('http://localhost:8080/api/profile', {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });

                if (!profileResponse.ok) {
                    throw new Error(`Profile fetch failed: ${profileResponse.status}`);
                }

                const profileData = await profileResponse.json();
                setUser(profileData);

                // Fetch orders for the initial page
                await fetchOrders(currentPage);
            } catch (error) {
                console.error('Error during data fetching:', error);
            }
        };

        fetchData();
    }, [navigate, currentPage]);


    const fetchOrders = async (page) => {
        const token = localStorage.getItem('accessToken');
        setLoading(true);

        try {
            const response = await fetch(`http://localhost:8080/api/patient/orders?page=${page}&size=5`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch orders: ${response.status}`);
            }

            const data = await response.json();
            setOrders(data.content);
            setTotalPages(data.totalPages);
        } catch (error) {
            console.error('Error fetching orders:', error);
        } finally {
            setLoading(false);
        }
    };


    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setUser((prevUser) => ({ ...prevUser, [name]: value }));
    };

    const handleSaveChanges = async () => {
        const token = localStorage.getItem('accessToken');

        // Ensure only name and surname are included in the payload
        const { name, surname } = user;
        const payload = { name, surname };

        try {
            const response = await fetch('http://localhost:8080/api/profile', {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                throw new Error('Failed to update profile');
            }
        } catch (error) {
            alert(error.message);
        }
    };


    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        navigate('/login');
    };

    const handleNextPage = () => {
        if (currentPage < totalPages - 1) {
            setCurrentPage((prev) => prev + 1);
            fetchOrders(currentPage + 1);
        }
    };

    const handlePrevPage = () => {
        if (currentPage > 0) {
            setCurrentPage((prev) => prev - 1);
            fetchOrders(currentPage - 1);
        }
    };

    return (
        <div className={styles.profile_page_container}>
            <Header />
            <main className={styles.content}>
                <div className={styles.user_section}>
                    <h2>Your Profile</h2>
                    <div className={styles.user_info}>
                        <label>Email: <strong>{user.email}</strong></label>
                        <label>
                            Name:
                            <input
                                type="text"
                                name="name"
                                value={user.name}
                                onChange={handleInputChange}
                                className={styles.input}
                            />
                        </label>
                        <label>
                            Surname:
                            <input
                                type="text"
                                name="surname"
                                value={user.surname}
                                onChange={handleInputChange}
                                className={styles.input}
                            />
                        </label>
                        <button onClick={handleSaveChanges} className={styles.save_button}>
                            Save Changes
                        </button>
                        <button onClick={handleLogout} className={styles.logout_button}>
                            Log Out
                        </button>
                    </div>
                </div>
                <div className={styles.orders_section}>
                    <h2>Your Orders</h2>
                    {loading ? (
                        <p>Loading orders...</p>
                    ) : orders == null || orders.length === 0 ? (
                        <p>No orders found.</p>
                    ) : (
                        <>
                            <ul className={styles.order_list}>
                                {orders.map((order) => (
                                    <li key={order.id} className={styles.order_item}>
                                        <p>
                                            <strong>Order ID:</strong> {order.id}
                                        </p>
                                        <p>
                                            <strong>Date:</strong>{' '}
                                            {new Date(order.orderDate).toLocaleString()}
                                        </p>
                                        <p>
                                            <strong>Paid:</strong> {order.isPaid ? 'Yes' : 'No'}
                                        </p>
                                        <h4>Test Results:</h4>
                                        <ul>
                                            {order.testResults.map((result) => (
                                                <li key={result.id}>
                                                    <p>
                                                        <strong>Test name:</strong> {result.test.name}
                                                    </p>
                                                    <p>
                                                        <strong>Test description:</strong> {result.test.description}
                                                    </p>
                                                    <p>
                                                        <strong>Test price:</strong> {`${result.test.price.toFixed(2)}$`}
                                                    </p>
                                                    <p>
                                                        <strong>Test date:</strong> {new Date(result.testDate).toLocaleString()}
                                                    </p>
                                                    <p>
                                                        <strong>Result:</strong> {result.result || "No result yet"}
                                                    </p>
                                                </li>
                                            ))}
                                        </ul>
                                    </li>
                                ))}
                            </ul>
                            <div className={styles.pagination}>
                                <button
                                    onClick={handlePrevPage}
                                    disabled={currentPage === 0}
                                    className={styles.page_button}
                                >
                                    Previous
                                </button>
                                <span>
                                    Page {currentPage + 1} of {totalPages}
                                </span>
                                <button
                                    onClick={handleNextPage}
                                    disabled={currentPage === totalPages - 1}
                                    className={styles.page_button}
                                >
                                    Next
                                </button>
                            </div>
                        </>
                    )}
                </div>
            </main>
            <Footer />
        </div>
    );
}
