import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "../styles/login_page.module.css";

export default function LoginPage() {
    const navigate = useNavigate();
    const [isLogin, setIsLogin] = useState(true);
    const [step, setStep] = useState(1); // Step 1: Register, Step 2: Email Verification
    const [loginData, setLoginData] = useState({ email: "", password: "" });
    const [registerData, setRegisterData] = useState({
        email: "",
        password: "",
        name: "",
        surname: "",
    });
    const [verificationData, setVerificationData] = useState({ email: "", code: "" });
    const [errorMessage, setErrorMessage] = useState("");

    // Handle login form submission
    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch("http://localhost:8080/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(loginData),
            });
            if (response.ok) {
                const data = await response.json();
                localStorage.setItem("accessToken", data.accessToken);
                localStorage.setItem("refreshToken", data.refreshToken);

                // Parse the JWT token to get the role
                try {
                    const tokenPayload = data.accessToken.split(".")[1];
                    // Handle base64url format by replacing non-base64 chars and adding padding if needed
                    const base64 = tokenPayload.replace(/-/g, '+').replace(/_/g, '/');
                    const payload = JSON.parse(window.atob(base64));
                    
                    // Check if role exists directly in payload
                    if (payload && payload.role) {
                        localStorage.setItem("userRole", payload.role);
                    } else {
                        console.warn("User role information not found in token");
                    }
                } catch (error) {
                    console.error("Error parsing JWT token:", error);
                }

                navigate("/");
            } else {
                const error = await response.json();
                setErrorMessage(error.message || "Login failed. Try again.");
            }
        } catch (error) {
            setErrorMessage("An error occurred. Please try again.");
        }
    };

    // Handle registration form submission
    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch("http://localhost:8080/api/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(registerData),
            });
            if (response.ok) {
                const data = await response.json();
                setVerificationData({ email: registerData.email, code: "" });
                setStep(2); // Proceed to email verification step
            } else {
                const error = await response.json();
                setErrorMessage(error.message || "Registration failed. Try again.");
            }
        } catch (error) {
            setErrorMessage("An error occurred. Please try again.");
        }
    };

    // Handle email verification
    const handleVerifyEmail = async (e) => {
        e.preventDefault();
        try {
            const encodedEmail = encodeURIComponent(verificationData.email);
            const encodedCode = encodeURIComponent(verificationData.code);

            const response = await fetch(
                `http://localhost:8080/api/auth/verify-email?email=${encodedEmail}&code=${encodedCode}`,
                { method: "POST" }
            );

            if (response.ok) {
                setIsLogin(true); // Go back to login
                setStep(1);
                setErrorMessage(""); // Clear error message
            } else {
                const error = await response.json();
                setErrorMessage(error.message || "Verification failed. Try again.");
            }
        } catch (error) {
            setErrorMessage("An error occurred. Please try again.");
        }
    };


    return (
        <div className={styles.login_page_container}>
            <div className={styles.form_container}>
                {step === 1 ? (
                    <>
                        <h1>{isLogin ? "Login" : "Register"}</h1>
                        {errorMessage && <p className={styles.error}>{errorMessage}</p>}
                        {isLogin ? (
                            <form onSubmit={handleLogin}>
                                <input
                                    type="email"
                                    placeholder="Email"
                                    value={loginData.email}
                                    onChange={(e) =>
                                        setLoginData({ ...loginData, email: e.target.value })
                                    }
                                    required
                                />
                                <input
                                    type="password"
                                    placeholder="Password"
                                    value={loginData.password}
                                    onChange={(e) =>
                                        setLoginData({ ...loginData, password: e.target.value })
                                    }
                                    required
                                />
                                <button type="submit" className={styles.submit_button}>
                                    Login
                                </button>
                            </form>
                        ) : (
                            <form onSubmit={handleRegister}>
                                <input
                                    type="email"
                                    placeholder="Email"
                                    value={registerData.email}
                                    onChange={(e) =>
                                        setRegisterData({ ...registerData, email: e.target.value })
                                    }
                                    required
                                />
                                <input
                                    type="password"
                                    placeholder="Password"
                                    value={registerData.password}
                                    onChange={(e) =>
                                        setRegisterData({ ...registerData, password: e.target.value })
                                    }
                                    required
                                />
                                <input
                                    type="text"
                                    placeholder="Name"
                                    value={registerData.name}
                                    onChange={(e) =>
                                        setRegisterData({ ...registerData, name: e.target.value })
                                    }
                                    required
                                />
                                <input
                                    type="text"
                                    placeholder="Surname"
                                    value={registerData.surname}
                                    onChange={(e) =>
                                        setRegisterData({ ...registerData, surname: e.target.value })
                                    }
                                    required
                                />
                                <button type="submit" className={styles.submit_button}>
                                    Register
                                </button>
                            </form>
                        )}
                        <button
                            className={styles.toggle_button}
                            onClick={() => {
                                setIsLogin(!isLogin);
                                setErrorMessage("");
                            }}
                        >
                            {isLogin ? "Don't have an account? Register" : "Already have an account? Login"}
                        </button>
                    </>
                ) : (
                    <>
                        <h1>Email Verification</h1>
                        {errorMessage && <p className={styles.error}>{errorMessage}</p>}
                        <form onSubmit={handleVerifyEmail}>
                            <p>A verification code has been sent to your email. Enter it below:</p>
                            <input
                                type="text"
                                placeholder="Verification Code"
                                value={verificationData.code}
                                onChange={(e) =>
                                    setVerificationData({ ...verificationData, code: e.target.value })
                                }
                                required
                            />
                            <button type="submit" className={styles.submit_button}>
                                Verify Email
                            </button>
                        </form>
                        <button
                            className={styles.toggle_button}
                            onClick={() => {
                                setStep(1);
                                setErrorMessage("");
                            }}
                        >
                            Back to Registration
                        </button>
                    </>
                )}
            </div>
        </div>
    );
}
