import React from 'react';
import styles from './footer.module.css';

export default function Footer() {
    return (
        <footer className={styles.footer}>
            <div className={styles.container}>
                <p className={styles.text}>© 2024 Biomedica. All rights reserved.</p>
                <nav className={styles.nav}>
                    <a href="/privacy" className={styles.link}>Privacy Policy</a>
                    <a href="/terms" className={styles.link}>Terms of Service</a>
                    <a href="/contact" className={styles.link}>Contact Us</a>
                </nav>
            </div>
        </footer>
    );
}
