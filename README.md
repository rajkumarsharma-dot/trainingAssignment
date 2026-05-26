# Trip Expense Tracker Application

## Project Overview

Trip Expense Tracker is a production-ready Android application developed using Java, XML, and Firebase technologies. The application is designed to simplify group expense management during trips, vacations, office tours, college outings, and family travel.

The system allows users to:
- Create and manage trips
- Add group members
- Track shared expenses
- Split bills equally or custom-wise
- Monitor balances in real time
- View analytics and spending summaries
- Synchronize data using Firebase Firestore

The application follows clean architecture principles and provides a responsive Material Design UI with optimized RecyclerView performance, secure Firebase Authentication, and real-time database synchronization.

---

# Repository Structure

```text
Trip Expense Tracker/
│
├── app/
│   ├── src/main/
│   │   ├── java/com/tripexpense/tracker/
│   │   │   ├── adapter/
│   │   │   ├── model/
│   │   │   ├── service/
│   │   │   ├── ui/
│   │   │   │   ├── analytics/
│   │   │   │   ├── auth/
│   │   │   │   ├── dashboard/
│   │   │   │   ├── expense/
│   │   │   │   ├── splash/
│   │   │   │   └── trip/
│   │   │   └── util/
│   │   │
│   │   ├── res/
│   │   │   ├── drawable/
│   │   │   ├── layout/
│   │   │   ├── menu/
│   │   │   └── values/
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   └── test/
│
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md
