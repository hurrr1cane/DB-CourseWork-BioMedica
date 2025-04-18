import React, { useState } from 'react';
import { format } from 'date-fns';
import styles from './test_item.module.css';

const TestItem = ({ test, onClick, showTimePicker, onTimeChange, index }) => {
    const [testTime, setTestTime] = useState(new Date());
    const [errorMessage, setErrorMessage] = useState('');

    const handleTimeChange = (event) => {
        const selectedDateTime = new Date(event.target.value); // Create a Date object from the input
        const now = new Date();

        // Validate if the selected time is in the future
        if (selectedDateTime <= now) {
            setErrorMessage('Time must be in the future.');
            return;
        }

        // Validate minutes
        const minutes = selectedDateTime.getMinutes();
        if (![0, 15, 30, 45].includes(minutes)) {
            setErrorMessage('Minutes must be 0, 15, 30, or 45.');
            return;
        }

        // Clear errors and update the time
        setErrorMessage('');
        setTestTime(selectedDateTime);

        if (onTimeChange) {
            onTimeChange(index, selectedDateTime); // Pass the full Date object back to the parent
        }
    };

    const handleStopPropagation = (event) => {
        event.stopPropagation();
    };

    return (
        <div className={styles.testItem} onClick={onClick}>
            <div className={styles.testInfo}>
                <div className={styles.testItemLabel}>{test.name}</div>
                <div>{test.description}</div>
                <div>{`${test.price.toFixed(2)}$`}</div>
            </div>
            {showTimePicker && (
                <div className={styles.timePicker} onClick={handleStopPropagation}>
                    <label>
                        Select Time:
                        <input
                            type="datetime-local"
                            value={format(testTime, "yyyy-MM-dd'T'HH:mm")}
                            onChange={handleTimeChange}
                            className={styles.testItemInput}
                        />
                    </label>
                    {errorMessage && (
                        <div className={styles.errorMessage}>{errorMessage}</div>
                    )}
                </div>
            )}
        </div>
    );
};

export default TestItem;
