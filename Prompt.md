# Trip Expense Tracker App Prompt

## Context and Role

As a Senior Android Developer, your responsibility is to design and develop a modern, scalable, and production-ready Trip Expense Tracker Application that simplifies group expense management during trips, vacations, office tours, college outings, and family travel.

The application should provide users with a smooth, secure, and interactive mobile experience where they can easily:

- Track shared expenses
- Split bills fairly
- Monitor balances in real time
- Settle payments without confusion

The system should feel intuitive, fast, and reliable while following clean architecture principles, optimized performance practices, and production-level coding standards.

The application must ensure:

- Secure Firebase integration
- Accurate expense calculations
- Responsive Android UI
- Smooth user interactions
- Real-time data synchronization
- Scalable and maintainable architecture
- Production-quality implementation standards

---

# Objective

Develop a complete Android-based Trip Expense Tracker Application that includes:

- Create and manage trips
- Add friends and split expenses
- Track individual contributions and balances
- Expense summaries and analytics
- Responsive UI interactions
- Real-time synchronization
- Automatic expense calculations

---

# UI and Animation Requirements

## Modern Android UI

Implement smooth UI interactions using Android animations.

Use:
- RecyclerView animations
- Card-based layouts
- Smooth activity transitions
- Animated buttons and dialogs
- Progress indicators

Include transitions between:
- Splash Screen
- Login/Register Screen
- Dashboard
- Trip Details Screen
- Expense Management Screen
- Analytics Screen

Ensure UI:
- Responsive across Android devices
- Smooth scrolling
- Fast performance
- Material Design principles
  
# Navigation Requirements

Use:
- Activities and Fragments
- Bottom Navigation
- Smooth screen transitions

Use RecyclerView for:
- Trip lists
- Expense history
- Friend lists

Implement:
- Custom adapters
- Efficient ViewHolder pattern
- Smooth scrolling

---

# Layout Requirements

## Splash Screen
- Animated app logo
- App introduction

## Authentication Screen
- User login
- User signup
- Forgot password

## Dashboard Screen
- Total expense overview
- Recent trips
- Budget summary
- Expense cards

## Trip Management Screen
- Create trip
- Edit trip
- Delete trip
- Add friends

## Expense Management Screen
- Add expenses
- Edit expenses
- Delete expenses
- View expense history

## Analytics Screen
- Expense charts
- Spending reports
- Member balance summary

The layout must be:
- Fully responsive
- User-friendly
- Optimized for performance
- Accessible and clean

---

# Authentication Requirements

Use Firebase Authentication.

Features:
- User signup
- User login
- Forgot password
- Secure session handling

Validation:
- Email validation
- Password validation
- Required field validation

Optional:
- Google Sign-In
- Facebook Sign-In

# Session Management Requirements

Maintain secure user login sessions using Firebase Authentication persistence.

---

# Expense Management Requirements

Users must be able to:
- Create trips
- Add group members
- Add expenses
- View split expenses
- View total expenses
- Track who paid

Expense fields:
- Expense title
- Amount
- Paid by
- Category
- Date
- Notes / Description

Validation:
- Required field validation
- Numeric amount validation
- Empty field prevention
  
# Expense Summary Requirements

Display:
- Total trip expenses
- Individual balances
- Friend-wise contributions
- Expense breakdown summaries

---

# Firebase Backend Requirements

Use:
- Firebase Authentication
- Firebase Firestore
- Firebase Cloud Messaging

Store:
- User data
- Trip details
- Expenses
- Friend lists
- Payment summaries

Ensure:
- Real-time Firebase synchronization
- Automatic UI updates
- Secure Firebase access

---

# Calculation Requirements

Implement logic to calculate:
- Total trip expenses
- Individual paid amount
- Pending balances
- Who owes whom
- Equal and custom expense splitting

Example:

```text
Total Expense: ₹15,000
Each Share: ₹3,000
```

---

# Data Processing Requirements

Validate and sanitize all user inputs.

Prevent:
- Invalid data submission
- Empty fields
- Incorrect amount entries
- Duplicate entries
- Negative expense values
- Invalid friend data

Ensure:
- Secure Firebase data handling
- Secure database access

---

# Output Requirements

The application should provide:
- Responsive Android UI
- Secure authentication
- Real-time expense tracking
- Accurate expense splitting
- Analytics reports
- Fast performance
- User-friendly navigation

---

# Error Handling and Documentation

Handle:
- Login failures
- Firebase connection errors
- Invalid expense amounts
- Database failures

Provide:
- Toast messages
- Structured validation errors
- Firebase error handling

Document:
- Project structure
- Firebase setup
- Gradle dependencies
- Installation steps
- APK build process

---

# Performance and Scalability

Optimize:
- Firebase queries
- RecyclerView performance
- App startup speed
- Activity loading

Ensure:
- Smooth scrolling
- Fast UI rendering
- Low memory usage
- Real-time synchronization

# Code Structure Requirements

Organize code into:
- Activities
- Fragments
- Models
- Adapters
- Firebase services
- Utility classes

Maintain modular clean architecture.

---

# Technology Stack

## Frontend
- Java
- XML Layouts
- Android SDK
- Material Design

## Backend
- Firebase Authentication
- Firebase Firestore
- Firebase Cloud Messaging

## Libraries
- RecyclerView
- CardView
- Material Components
- Firebase SDK
- Glide/Picasso (optional)

---

# Explicit Constraints

1. Use Java and XML only
2. Use Firebase Firestore for storage
3. Use RecyclerView for lists
4. Use Material Design principles
5. Implement proper validation and error handling
6. Use modular clean architecture
7. Ensure responsive UI
8. Provide production-quality code
