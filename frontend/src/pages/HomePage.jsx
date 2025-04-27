import { useState, useEffect } from 'react';
import Header from '../components/header/Header';
import Footer from '../components/footer/Footer';
import LaboratoryItem from '../components/laboratory_item/LaboratoryItem';
import styles from '../styles/home_page.module.css';

export default function HomePage() {
    const api = "http://localhost:8080/api/laboratories";

    const [laboratories, setLaboratories] = useState([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    
    // Search states
    const [searchParams, setSearchParams] = useState({
        address: '',
        workingHours: '',
        phoneNumber: ''
    });
    const [isSearching, setIsSearching] = useState(false);
    const [searchCriteria, setSearchCriteria] = useState('address'); // Default search criteria

    // Helper to check if any search param has a value
    const hasActiveSearchParams = () => {
        return Object.values(searchParams).some(param => param.trim() !== '');
    };

    // Modify fetchData to accept optional parameters that override the state
    const fetchData = async (page, forceSearch = null) => {
        setLoading(true);
        try {
            // Use forceSearch parameter if provided, otherwise use state
            const shouldUseSearchEndpoint = forceSearch !== null
                ? forceSearch
                : isSearching && hasActiveSearchParams();
            
            let url = shouldUseSearchEndpoint 
                ? `${api}/search?page=${page}&size=10` 
                : `${api}?page=${page}&size=10`;
            
            if (shouldUseSearchEndpoint) {
                // Add search parameters that have values
                if (searchParams.address) {
                    url += `&address=${encodeURIComponent(searchParams.address)}`;
                }
                if (searchParams.workingHours) {
                    url += `&workingHours=${encodeURIComponent(searchParams.workingHours)}`;
                }
                if (searchParams.phoneNumber) {
                    url += `&phoneNumber=${encodeURIComponent(searchParams.phoneNumber)}`;
                }
            }
            
            const response = await fetch(url);
            
            if (!response.ok) {
                throw new Error('Failed to fetch laboratories');
            }
            
            const data = await response.json();
            setLaboratories(data.content);
            setTotalPages(data.totalPages || 1); // Default to 1 if totalPages is undefined
            
            // If we get an empty result and we're on a page > 0, go back to page 0
            if (data.content.length === 0 && page > 0) {
                setCurrentPage(0);
            }
        } catch (error) {
            console.error("Error fetching laboratories:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData(currentPage);
    }, [currentPage]);

    const handleSearchInputChange = (e) => {
        const { name, value } = e.target;
        setSearchParams({
            ...searchParams,
            [name]: value
        });
    };

    // Modify handleSearch to pass the search state explicitly
    const handleSearch = (e) => {
        e.preventDefault();
        
        if (hasActiveSearchParams()) {
            setIsSearching(true);
            setCurrentPage(0);
            // Pass true to force using search endpoint
            fetchData(0, true);
        } else {
            setIsSearching(false);
            setCurrentPage(0);
            // Pass false to force NOT using search endpoint
            fetchData(0, false);
        }
    };

    // Modify handleClearSearch to pass false explicitly
    const handleClearSearch = () => {
        setSearchParams({
            address: '',
            workingHours: '',
            phoneNumber: ''
        });
        setIsSearching(false);
        setCurrentPage(0);
        // Pass false to force NOT using search endpoint
        fetchData(0, false);
    };

    const handleNextPage = () => {
        if (currentPage < totalPages - 1) setCurrentPage((prev) => prev + 1);
    };

    const handlePrevPage = () => {
        if (currentPage > 0) setCurrentPage((prev) => prev - 1);
    };

    const handleSearchCriteriaChange = (e) => {
        const criteria = e.target.value;
        setSearchCriteria(criteria);
        // Clear all search params
        setSearchParams({
            address: '',
            workingHours: '',
            phoneNumber: ''
        });
    };

    return (
        <div className={styles.home_page_container}>
            <Header />
            <main className={styles.content}>
                <h1>Our Laboratories</h1>
                
                <div className={styles.search_container}>
                    <form onSubmit={handleSearch} className={styles.search_form}>
                        <div className={styles.search_row}>
                            <select 
                                className={styles.search_select}
                                value={searchCriteria}
                                onChange={handleSearchCriteriaChange}
                            >
                                <option value="address">Search by Address</option>
                                <option value="workingHours">Search by Working Hours</option>
                                <option value="phoneNumber">Search by Phone Number</option>
                            </select>
                            
                            {searchCriteria === 'address' && (
                                <input
                                    type="text"
                                    name="address"
                                    value={searchParams.address}
                                    onChange={handleSearchInputChange}
                                    placeholder="Enter address..."
                                    className={styles.search_input}
                                />
                            )}
                            
                            {searchCriteria === 'workingHours' && (
                                <input
                                    type="text"
                                    name="workingHours"
                                    value={searchParams.workingHours}
                                    onChange={handleSearchInputChange}
                                    placeholder="Enter working hours (e.g. 9:00-18:00)..."
                                    className={styles.search_input}
                                />
                            )}
                            
                            {searchCriteria === 'phoneNumber' && (
                                <input
                                    type="text"
                                    name="phoneNumber"
                                    value={searchParams.phoneNumber}
                                    onChange={handleSearchInputChange}
                                    placeholder="Enter phone number..."
                                    className={styles.search_input}
                                />
                            )}
                            
                            <button type="submit" className={styles.search_button}>
                                {hasActiveSearchParams() ? 'Search' : 'Show All'}
                            </button>
                            
                            {hasActiveSearchParams() && (
                                <button 
                                    type="button" 
                                    onClick={handleClearSearch}
                                    className={styles.clear_button}
                                >
                                    Clear
                                </button>
                            )}
                        </div>
                    </form>
                    
                    {isSearching && hasActiveSearchParams() && (
                        <div className={styles.active_filters}>
                            <span>Active filters:</span>
                            {searchParams.address && (
                                <span className={styles.filter_tag}>
                                    Address: {searchParams.address}
                                </span>
                            )}
                            {searchParams.workingHours && (
                                <span className={styles.filter_tag}>
                                    Working Hours: {searchParams.workingHours}
                                </span>
                            )}
                            {searchParams.phoneNumber && (
                                <span className={styles.filter_tag}>
                                    Phone: {searchParams.phoneNumber}
                                </span>
                            )}
                        </div>
                    )}
                </div>
                
                {loading ? (
                    <div className={styles.loading_container}>
                        <div className={styles.loading_spinner}></div>
                    </div>
                ) : (
                    <>
                        {laboratories.length === 0 ? (
                            <div className={styles.no_results}>
                                <p>No laboratories found matching your search criteria.</p>
                                {isSearching && (
                                    <button 
                                        className={styles.clear_button}
                                        onClick={handleClearSearch}
                                    >
                                        Clear Search
                                    </button>
                                )}
                            </div>
                        ) : (
                            <ul className={styles.laboratories}>
                                {laboratories.map((lab) => (
                                    <LaboratoryItem
                                        key={lab.id}
                                        address={lab.address}
                                        phone={lab.phoneNumber}
                                    />
                                ))}
                            </ul>
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
