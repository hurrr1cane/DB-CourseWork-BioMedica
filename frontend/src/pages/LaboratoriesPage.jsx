import { useState, useEffect } from 'react';
import Header from '../components/header/Header';
import Footer from '../components/footer/Footer';
import LaboratoryItem from '../components/laboratory_item/LaboratoryItem';
import styles from '../styles/laboratories_page.module.css';
import { ro } from 'date-fns/locale';
import { useNavigate } from 'react-router-dom';

export default function LaboratoriesPage() {
    const api = "http://localhost:8080/api/laboratories";

    const [laboratories, setLaboratories] = useState([]);
    const [currentPage, setCurrentPage] = useState(0); // Current page number (starts from 0)
    const [totalPages, setTotalPages] = useState(0); // Total pages available
    const [loading, setLoading] = useState(true);

    const navigate = useNavigate();

    const fetchData = async (page) => {
        setLoading(true);
        try {
            const response = await fetch(`${api}?page=${page}&size=10`); // Fetch paginated data
            const data = await response.json();
            setLaboratories(data.content); // Set laboratories from the content
            setTotalPages(data.totalPages); // Set total number of pages
        } catch (error) {
            console.error("Error fetching laboratories:", error);
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

    const openLaboratoryForm = (id) => {
        navigate(`/laboratories/edit/${id}`);
    }

    const handleAddLaboratory = () => {
        navigate('/laboratories/edit/new');
    }

    return (
        <div className={styles.home_page_container}>
            <Header />
            <main className={styles.content}>
                <h1>Laboratories management</h1>
                <button className={styles.add_laboratory_button} onClick={handleAddLaboratory}>
                    Add Laboratory
                </button>

                {loading ? (
                    <h2>Loading laboratories...</h2>
                ) : (
                    <>
                        <ul className={styles.laboratories}>
                            {laboratories.map((lab) => (
                                <LaboratoryItem
                                    key={lab.id}
                                    address={lab.address}
                                    phone={lab.phoneNumber}
                                    onClick={() => openLaboratoryForm(lab.id)}
                                />
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
                            <span className={styles.page_info}>
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
            </main>
            <Footer />
        </div>
    );
}
