# SmartQueueSystem
Smart Queue Management System (JavaFX)
Overview
This project is a data structures and algorithms (DSA) based simulation of a real-world hospital queue management system. It implements a priority-driven smart queue that dynamically manages patient entries, service order, and waiting times. The system is designed using JavaFX for visualization and a multithreaded Java back-end for realistic simulation.
Project Context
This project was developed as part of the Data Structures and Algorithms (DSA) coursework. Although DSA is primarily taught in C, this implementation uses Java for the following reasons:
1. Object-Oriented Design
Java supports strong abstraction and encapsulation, enabling a modular and extensible system design. Core entities such as Patient, QueueManager, and UIController are implemented as distinct classes.
2. Built-in Concurrency
Multithreading and synchronization are handled efficiently using Java’s Thread, Runnable, and PriorityBlockingQueue. This enables real-time updates and concurrent background execution.
3. Rich UI Integration
Using JavaFX allows for an interactive graphical interface that displays live queue updates, service progress, and event-driven user interactions.
4. File I/O and Logging
Java provides straightforward APIs for reading and writing data, enabling detailed timestamped logs of all queue events for traceability.
Core Features
•	Priority-Based Scheduling: Patients are queued based on category — Emergency > Senior > Child > General.
•	Dynamic Queue Adjustment: Wait times update automatically when new patients are added or service durations vary.
•	Realistic Simulation: Each patient’s service duration includes a random ±10–20 second deviation to simulate real-world behavior.
•	Thread-Safe Operation: Uses PriorityBlockingQueue for concurrency and Platform.runLater() for UI synchronization.
•	Comprehensive Logging: Tracks all major events, including patient additions, service start/completion, delays, and early finishes.
Technical Stack
•	Language: Java 21

•	Framework: JavaFX 21

•	Key Concepts: Priority Queues, Multithreading, Comparators, File I/O, Event-driven architecture
System Structure
src/com/example/smartqueuesystem/
│
├── Main.java             → Application entry point
├── UIController.java     → Handles UI logic and user interactions
├── QueueManager.java     → Core logic, serving simulation, and logging
├── Patient.java          → Data model for patient attributes and priority logic
└── UI.fxml               → JavaFX layout

data/
└── queue_log.txt         → Persistent log of queue activity
Example Log Output
19:12:07 - Started serving Khadija [Expected=97s, Actual=85s]
19:27:52 - 🟢 Patient Added: Md Zakiur Rahman (General)
   Token: 1 | Queue: 0 | Expected: 97s
19:28:02 - 🟢 Patient Added: Subham (General)
   Token: 2 | Queue: 0 | Expected: 96s
19:28:11 - 🟢 Patient Added: Zaid (Child)
   Token: 3 | Queue: 0 | Expected: 66s
19:28:26 - 🟢 Patient Added: Khadija (Senior)
   Token: 4 | Queue: 0 | Expected: 66s
19:28:39 - 🟢 Patient Added: Zohair (Child)
   Token: 5 | Queue: 0 | Expected: 100s

==========================
📅 2025-11-04 | Queue Started
==========================
19:28:41 - 🟠 Serving started: Khadija (Senior)
   Token: 4 | Queue: 1 | Expected: 66s
19:28:56 - 🟢 Patient Added: Smaran (Child)
   Token: 6 | Queue: 0 | Expected: 82s
19:29:09 - 🟢 Patient Added: Nabila (Emergency)
   Token: 7 | Queue: 0 | Expected: 69s
19:29:27 - ✅ Served completed: Khadija (Senior)
   Token: 4 | Queue: 1 | Expected: 66s
19:29:27 - 🟠 Serving started: Nabila (Emergency)
   Token: 7 | Queue: 1 | Expected: 69s
19:30:37 - ⏳ +10s delay added due to overrun by Nabila
19:30:47 - ⏳ +10s delay added due to overrun by Nabila
19:30:53 - ✅ Served completed: Nabila (Emergency)
   Token: 7 | Queue: 1 | Expected: 69s
19:30:53 - 🔹 -3s deducted (early finish correction by Nabila)
19:30:53 - 🟠 Serving started: Zohair (Child)
   Token: 5 | Queue: 1 | Expected: 100s
19:32:13 - ✅ Served completed: Zohair (Child)
   Token: 5 | Queue: 1 | Expected: 100s
19:32:13 - 🟠 Serving started: Zaid (Child)
   Token: 3 | Queue: 1 | Expected: 66s
19:33:23 - ⏳ +10s delay added due to overrun by Zaid
19:33:32 - ✅ Served completed: Zaid (Child)
   Token: 3 | Queue: 1 | Expected: 66s
19:33:32 - 🟠 Serving started: Smaran (Child)
   Token: 6 | Queue: 1 | Expected: 82s
19:34:29 - 🟢 Patient Added: Atiqur (Senior)
   Token: 8 | Queue: 0 | Expected: 92s
19:34:35 - ✅ Served completed: Smaran (Child)
   Token: 6 | Queue: 1 | Expected: 82s
19:34:35 - 🟠 Serving started: Atiqur (Senior)
   Token: 8 | Queue: 1 | Expected: 92s
19:36:15 - ⏳ +10s delay added due to overrun by Atiqur
19:36:23 - ✅ Served completed: Atiqur (Senior)
   Token: 8 | Queue: 1 | Expected: 92s
19:36:23 - 🟠 Serving started: Md Zakiur Rahman (General)
   Token: 1 | Queue: 1 | Expected: 97s
19:37:41 - ✅ Served completed: Md Zakiur Rahman (General)
   Token: 1 | Queue: 1 | Expected: 97s
19:37:41 - 🟠 Serving started: Subham (General)
   Token: 2 | Queue: 1 | Expected: 96s
19:38:59 - ✅ Served completed: Subham (General)
   Token: 2 | Queue: 1 | Expected: 96s

==========================
📅 2025-11-04 | Queue Completed
==========================
20:04:44 - 🟢 Patient Added: Md Zakiur Rahman (General)
   Token: 1 | Queue: 0 | Expected: 98s
20:04:56 - 🟢 Patient Added: Smaran (General)
   Token: 2 | Queue: 0 | Expected: 58s

==========================
📅 2025-11-04 | Queue Started
==========================
20:04:56 - 🟠 Serving started: Md Zakiur Rahman (General)
   Token: 1 | Queue: 1 | Expected: 98s
20:05:09 - 🟢 Patient Added: Zaid (Child)
   Token: 3 | Queue: 0 | Expected: 70s
20:06:36 - ⏳ +10s delay added due to overrun by Md Zakiur Rahman
20:06:46 - ⏳ +10s delay added due to overrun by Md Zakiur Rahman
20:06:50 - ✅ Served completed: Md Zakiur Rahman (General)
   Token: 1 | Queue: 1 | Expected: 98s
20:06:50 - 🔹 -4s deducted (early finish correction by Md Zakiur Rahman)
20:06:50 - 🟠 Serving started: Zaid (Child)
   Token: 3 | Queue: 1 | Expected: 70s
20:07:08 - 🟢 Patient Added: Nabila (Emergency)
   Token: 4 | Queue: 0 | Expected: 48s
20:07:34 - 🟢 Patient Added: Subham (General)
   Token: 5 | Queue: 0 | Expected: 99s
20:08:10 - ⏳ +10s delay added due to overrun by Zaid
20:08:15 - ✅ Served completed: Zaid (Child)
   Token: 3 | Queue: 1 | Expected: 70s
20:08:15 - 🟠 Serving started: Nabila (Emergency)
   Token: 4 | Queue: 1 | Expected: 48s
20:09:06 - ⏳ +10s delay added due to overrun by Nabila
20:09:14 - ✅ Served completed: Nabila (Emergency)
   Token: 4 | Queue: 1 | Expected: 48s
20:09:14 - 🟠 Serving started: Smaran (General)
   Token: 2 | Queue: 1 | Expected: 58s
20:10:14 - ⏳ +10s delay added due to overrun by Smaran
20:10:22 - ✅ Served completed: Smaran (General)
   Token: 2 | Queue: 1 | Expected: 58s
20:10:22 - 🟠 Serving started: Subham (General)
   Token: 5 | Queue: 1 | Expected: 99s
20:11:41 - ✅ Served completed: Subham (General)
   Token: 5 | Queue: 1 | Expected: 99s

📊 Queue Summary:
Patients Served: 5
Total Serve Time: 404s
Average Serve Time: 80.80s

==========================
📅 2025-11-04 | Queue Completed
==========================

How to Run
1.	Clone the repository.
2.	Open the project in IntelliJ IDEA or another JavaFX-compatible IDE.
3.	Verify that the JavaFX SDK is correctly configured.
4.	Run Main.java.
5.	Add patients, start the queue, and observe real-time behavior.
6.	All logs are automatically saved in data/queue_log.txt.
Future Enhancements
•	Integration with a local or remote database for persistent queue storage.
•	Visualization of queue statistics and average waiting time graphs.
•	Implementation of patient cancellation and priority reordering.
•	Development of an API or web-based interface for remote queue monitoring.
________________________________________
Author: Md Zakiur Rahman & Subhrayoti Samal
Purpose: DSA Practical Implementation Project
Institution: [Your College/University Name Here]
