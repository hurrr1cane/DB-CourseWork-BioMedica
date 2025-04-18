import React from 'react';
import styles from './laboratory_item.module.css';

export default function LaboratoryItem({ address, phone, onClick }) {
    return (
        <li className={styles.item} onClick={onClick}>
            <span className={styles.address}>{address}</span>
            <span className={styles.phone}>{phone}</span>
        </li>
    );
}
