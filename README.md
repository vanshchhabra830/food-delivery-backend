# 🍔 Food Delivery Backend API

A production-style RESTful backend for a food delivery application built with **Spring Boot**, following a layered architecture and industry-standard development practices. The application provides secure authentication, restaurant and menu management, shopping cart functionality, order processing, address management, and a mock payment system.

---

## 🚀 Features

### Authentication
- User Registration
- User Login
- JWT-based Authentication
- Spring Security Authorization
- Password Encryption using BCrypt

### Restaurant Management
- Create Restaurant
- Update Restaurant
- Delete Restaurant
- Search Restaurants
- Filter Restaurants by Cuisine
- View Restaurant Details

### Menu Management
- Create Menu Items
- Update Menu Items
- Delete Menu Items
- Browse Available Menus
- Search Menus
- Filter by Category
- Filter by Price

### Shopping Cart
- Add Items to Cart
- Update Item Quantity
- Remove Cart Items
- Clear Cart
- View Current Cart

### Address Management
- Add Address
- Update Address
- Delete Address
- Set Default Address
- View Saved Addresses

### Order Management
- Create Order from Cart
- View Order History
- View Order Details
- Cancel Orders

### Mock Payment System
- Simulated Payment Processing
- Payment History
- Payment Details
- Order Payment Tracking

---

## 🛠 Tech Stack

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- JWT Authentication
- Swagger / OpenAPI

---

## 🏗 Architecture

The project follows a layered architecture:

```
Controller
    ↓
Service
    ↓
Repository
    ↓
Database (MySQL)
```

This separation keeps the code modular, maintainable, and easy to extend.

---

## 📚 API Documentation

Swagger UI is available after starting the application:

```
http://localhost:8080/swagger-ui/index.html
```

---

## ⚙ Prerequisites

- Java 17+ (or your project version)
- Maven
- MySQL

---

## ▶️ Running the Project

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/food-delivery-backend.git
```

### 2. Navigate into the project

```bash
cd food-delivery-backend
```

### 3. Configure MySQL

Update the database configuration in:

```
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/food_delivery
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

### 4. Run the application

```bash
mvn spring-boot:run
```

---

## 📦 Main Modules

- Authentication
- Restaurants
- Menus
- Cart
- Orders
- Addresses
- Payments

---

## 🧪 Testing

The APIs can be tested using:

- Swagger UI
- Postman

---

## 📁 Project Structure

```
src
├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
├── config
├── security
├── exception
└── util
```

---

## 🔐 Security

- JWT Authentication
- BCrypt Password Encryption
- Role-based Authorization
- Protected REST Endpoints

---

## 🚀 Future Improvements

- Docker Support
- Redis Caching
- Payment Gateway Integration (Stripe/Razorpay)
- Email Notifications
- Order Tracking
- Admin Dashboard
- Unit & Integration Test Coverage

---

## 👨‍💻 Author

**Vansh Chhabra**

If you found this project useful, feel free to ⭐ the repository.
