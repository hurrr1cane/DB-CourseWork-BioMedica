import React, { useState, useEffect } from 'react';
import { format, addDays, setHours, setMinutes, isAfter } from 'date-fns';
import styles from './test_item.module.css';

const TestItem = ({ test, onClick, showTimePicker, onTimeChange, index }) => {
    // Initialize with current date and next available 15-min slot
    const [selectedDate, setSelectedDate] = useState(new Date());
    const [selectedTime, setSelectedTime] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [availableTimes, setAvailableTimes] = useState([]);

    // Generate available times on component mount and when date changes
    useEffect(() => {
        generateAvailableTimes(selectedDate);
    }, [selectedDate]);

    // Function to generate available time slots
    const generateAvailableTimes = (date) => {
        const times = [];
        const now = new Date();
        const isToday = date.toDateString() === now.toDateString();
        
        // Start from 8:00 AM to 8:00 PM
        for (let hour = 8; hour < 20; hour++) {
            for (let minute of [0, 15, 30, 45]) {
                const timeSlot = setMinutes(setHours(new Date(date), hour), minute);
                
                // Only include future times
                if (!isToday || isAfter(timeSlot, now)) {
                    const formattedTime = format(timeSlot, "HH:mm");
                    times.push({
                        label: format(timeSlot, "h:mm a"), // 1:30 PM format
                        value: formattedTime, // 13:30 format
                        dateTime: timeSlot // Full date object
                    });
                }
            }
        }
        
        setAvailableTimes(times);
        
        // Auto-select first available time if none selected
        if (times.length > 0 && !selectedTime) {
            handleTimeSelection(times[0].value, times[0].dateTime);
        }
    };

    // Handle date change
    const handleDateChange = (event) => {
        const newDate = new Date(event.target.value + 'T00:00:00');
        setSelectedDate(newDate);
        setSelectedTime(''); // Reset time when date changes
    };

    // Handle time selection
    const handleTimeSelection = (timeValue, dateTime) => {
        setSelectedTime(timeValue);
        setErrorMessage('');
        
        if (onTimeChange) {
            onTimeChange(index, dateTime);
        }
    };

    const handleStopPropagation = (event) => {
        event.stopPropagation();
    };

    // Generate date options for next 14 days
    const dateOptions = [];
    const today = new Date();
    for (let i = 0; i < 14; i++) {
        const date = addDays(today, i);
        dateOptions.push({
            value: format(date, 'yyyy-MM-dd'),
            label: format(date, 'EEE, MMM d') // Mon, May 6 format
        });
    }

    return (
        <div className={styles.testItem} onClick={onClick}>
            <div className={styles.testInfo}>
                <h3 className={styles.testName}>{test.name}</h3>
                <p className={styles.testDescription}>{test.description}</p>
                <p className={styles.testPrice}>${test.price.toFixed(2)}</p>
            </div>
            
            {showTimePicker && (
                <div className={styles.dateTimePicker} onClick={handleStopPropagation}>
                    <div className={styles.datePickerContainer}>
                        <label className={styles.dateTimeLabel}>Select date:</label>
                        <select 
                            className={styles.dateSelect}
                            value={format(selectedDate, 'yyyy-MM-dd')}
                            onChange={handleDateChange}
                        >
                            {dateOptions.map(option => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </div>
                    
                    <div className={styles.timePickerContainer}>
                        <label className={styles.dateTimeLabel}>Select time:</label>
                        <div className={styles.timeButtonsGrid}>
                            {availableTimes.map(time => (
                                <button
                                    key={time.value}
                                    type="button"
                                    className={`${styles.timeButton} ${selectedTime === time.value ? styles.selectedTime : ''}`}
                                    onClick={() => handleTimeSelection(time.value, time.dateTime)}
                                >
                                    {time.label}
                                </button>
                            ))}
                        </div>
                        {availableTimes.length === 0 && (
                            <p className={styles.noTimesMessage}>
                                No available times for this date. Please select another date.
                            </p>
                        )}
                    </div>
                    
                    {errorMessage && (
                        <div className={styles.errorMessage}>{errorMessage}</div>
                    )}
                    
                    {selectedTime && (
                        <div className={styles.selectedDateTime}>
                            Selected: {format(selectedDate, 'EEE, MMM d')} at {
                                availableTimes.find(t => t.value === selectedTime)?.label
                            }
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default TestItem;
