# Potuzhno Ticat

Potuzhno Ticat is a Spring Boot application for booking railway tickets.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Introduction

Potuzhno Ticat is designed to help users book railway tickets efficiently. It leverages Spring Boot for rapid development and PostgreSQL for data storage.

## Features

- User authentication and authorization
- Ticket booking and management
- Validation of user inputs
- RESTful API endpoints

## Requirements

- Java 17
- Maven
- PostgreSQL

## Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/yourusername/potuzhno-ticat.git
    ```
2. Navigate to the project directory:
    ```sh
    cd potuzhno-ticat
    ```
3. Update the `application.properties` file with your PostgreSQL database credentials.

4. Build the project using Maven:
    ```sh
    mvn clean install
    ```

## Usage

1. Run the application:
    ```sh
    mvn spring-boot:run
    ```
2. Access the application at `http://localhost:8080`.

## Contributing

Contributions are welcome! Please fork the repository and create a pull request with your changes.

## License

This project is licensed under the GPL v3 license.