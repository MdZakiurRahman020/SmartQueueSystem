package com.example.smartqueuesystem;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class Patient {
    private static final AtomicInteger tokenCounter = new AtomicInteger(1);

    private String name;
    private int age;
    private String sex;
    private String category;
    private int tokenNumber;
    private int queueNumber;

    private LocalTime addedTime;
    private long approxWaitTime;      // Dynamic, changes with queue updates
    private long expectedServeTime;   // Static, set once (estimated duration)
    private boolean beingServed;
    private boolean served;

    private LocalTime servedTime;     // Time when serving completed

    // Constructor
    public Patient(String name, int age, String sex, String category) {
        this.name = name;
        this.age = age;
        this.sex = sex;

        // Auto-classify category if not emergency
        if (category.equalsIgnoreCase("Emergency")) {
            this.category = "Emergency";
        } else if (age >= 60) {
            this.category = "Senior";
        } else if (age <= 15) {
            this.category = "Child";
        } else {
            this.category = "General";
        }

        this.tokenNumber = tokenCounter.getAndIncrement();
        this.addedTime = LocalTime.now();

        // Default expected serve time (randomized)
        this.expectedServeTime = 60 + (long) (Math.random() * 40); // 60–100s
    }

    // -------------------- PRIORITY LOGIC --------------------
    public int getPriority() {
        if (category == null) return 4;
        switch (category.toLowerCase()) {
            case "emergency": return 1;
            case "senior": return 2;
            case "child": return 3;
            default: return 4;
        }
    }

    // -------------------- GETTERS & SETTERS --------------------
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getSex() { return sex; }
    public String getCategory() { return category; }

    public int getTokenNumber() { return tokenNumber; }
    public int getQueueNumber() { return queueNumber; }
    public void setQueueNumber(int queueNumber) { this.queueNumber = queueNumber; }

    public long getApproxWaitTime() { return approxWaitTime; }
    public void setApproxWaitTime(long seconds) { this.approxWaitTime = seconds; }

    public long getExpectedServeTime() { return expectedServeTime; }
    public void setExpectedServeTime(long seconds) { this.expectedServeTime = seconds; }

    public boolean isBeingServed() { return beingServed; }
    public void setBeingServed(boolean beingServed) { this.beingServed = beingServed; }

    public boolean isServed() { return served; }

    // -------------------- TIME HANDLING --------------------
    public LocalTime getArrivalTime() { return addedTime; }

    public String getAddedTime() {
        return addedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public void markServed() {
        this.served = true;
        this.servedTime = LocalTime.now();
    }

    public LocalTime getServedTime() { return servedTime; }

    public String getServedTimeString() {
        return servedTime != null
                ? servedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                : "Pending";
    }

    // -------------------- DISPLAY HELPERS --------------------
    public String getFormattedWaitTime() {
        long minutes = approxWaitTime / 60;
        long seconds = approxWaitTime % 60;
        return String.format("%02dm %02ds", minutes, seconds);
    }

    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(name)
                .append("\nAge: ").append(age)
                .append("\nSex: ").append(sex)
                .append("\nCategory: ").append(category)
                .append("\nToken No: ").append(tokenNumber)
                .append("\nQueue No: ").append(queueNumber)
                .append("\nAdded At: ").append(getAddedTime())
                .append("\nApprox Wait Time: ").append(getFormattedWaitTime())
                .append("\nExpected Serve Time: ").append(expectedServeTime).append("s");
        if (isBeingServed()) sb.append("\nStatus: 🟢 Being Served");
        if (isServed()) sb.append("\nServed At: ").append(getServedTimeString());
        return sb.toString();
    }

    @Override
    public String toString() {
        return name + " (" + category + ")";
    }
}
