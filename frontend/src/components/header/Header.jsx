import React from 'react';
import styles from './header.module.css';
import { NavLink } from 'react-router-dom';

export default function Header() {
    const token = localStorage.getItem("accessToken");
    const user = token ? JSON.parse(localStorage.getItem("user")) : null;

    const userRole = localStorage.getItem("userRole");

    return (
        <header className={styles.header}>
            <NavLink to="/" className={styles.nav_link + " " + styles.nav_logo}>
                <h1>Biomedica</h1>
            </NavLink>
            <nav className={styles.nav}>
                {!(userRole === "ADMINISTRATOR" || userRole === "LABORATORY_ASSISTANT") && <NavLink to="/make-order" className={styles.order_button}>
                    Make Order
                </NavLink> }
                {userRole === "ADMINISTRATOR" && <NavLink to="/assistants" className={styles.nav_link}>
                    Assistants
                </NavLink>}
                {userRole === "ADMINISTRATOR" && <NavLink to="/laboratories" className={styles.nav_link}>
                    Laboratories
                </NavLink>}
                {userRole === "LABORATORY_ASSISTANT" && <NavLink to="/tests" className={styles.nav_link}>
                    Tests
                </NavLink>}
                {token ? (
                    <div className={styles.user_info}>
                        {/* <span className={styles.user_name}>
                            {user.name} {user.surname}
                        </span> */}
                        <NavLink to="/profile" className={styles.nav_link}>
                            Profile
                        </NavLink>
                    </div>
                ) : (
                    <NavLink to="/login" className={styles.auth_button}>
                        Authorize
                    </NavLink>
                )}
            </nav>
        </header>
    );
}
