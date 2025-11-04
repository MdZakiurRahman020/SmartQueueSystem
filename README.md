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
==========================
2025-11-04 | Queue Started
==========================
19:28:41 - Serving started: Khadija (Senior)
19:30:53 - +10s delay added due to overrun by Nabila
19:30:53 - -3s deducted (early finish correction by Nabila)
19:34:35 - Served completed: Smaran (Child)
Queue Summary:
Patients Served: 8
Total Serve Time: 553s
Average Serve Time: 69.12s
==========================
2025-11-04 | Queue Completed
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
