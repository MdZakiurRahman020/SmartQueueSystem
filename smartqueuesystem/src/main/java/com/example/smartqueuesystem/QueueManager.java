package com.example.smartqueuesystem;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class QueueManager implements Runnable {

    private final PriorityBlockingQueue<Patient> queue;
    private final Comparator<Patient> priorityComparator;
    private final ObservableList<Patient> waitingPatients;
    private final ObservableList<Patient> servedPatients;
    private final ListView<Patient> waitingList;
    private final ListView<Patient> servedList;
    private final TextArea detailsArea;
    private final TextArea currentServingArea;
    private final Label statusLabel;

    private boolean running = false;
    private Patient currentServing = null;
    private long currentRemainingTime = 0;

    private long totalServed = 0;
    private long totalServeTime = 0;

    private static final String LOG_PATH = "E:\\downloads\\smartqueuesystem\\data\\queue_log.txt";

    public QueueManager(
            PriorityBlockingQueue<Patient> queue,
            Comparator<Patient> priorityComparator,
            ObservableList<Patient> waitingPatients,
            ObservableList<Patient> servedPatients,
            ListView<Patient> waitingList,
            ListView<Patient> servedList,
            TextArea detailsArea,
            TextArea currentServingArea,
            Label statusLabel
    ) {
        this.queue = queue;
        this.priorityComparator = priorityComparator;
        this.waitingPatients = waitingPatients;
        this.servedPatients = servedPatients;
        this.waitingList = waitingList;
        this.servedList = servedList;
        this.detailsArea = detailsArea;
        this.currentServingArea = currentServingArea;
        this.statusLabel = statusLabel;
        ensureDirectoryExists();
    }

    @Override
    public void run() {
        running = true;
        logHeader("Queue Started");

        while (!queue.isEmpty() && running) {
            List<Patient> sorted = new ArrayList<>(queue);
            sorted.sort(priorityComparator);
            currentServing = sorted.get(0);
            queue.remove(currentServing);

            logPatientEvent("🟠 Serving started", currentServing);

            long expected = currentServing.getExpectedServeTime();
            int deviation = new Random().nextInt(11) + 10; // 10–20s diff
            boolean longer = new Random().nextBoolean();
            long actual = longer ? expected + deviation : Math.max(30, expected - deviation);
            long extraAdded = 0;

            Platform.runLater(() -> {
                currentServing.setBeingServed(true);
                updateWaitTimes(currentServing);
                showCurrentServing(currentServing, 0, expected);
                statusLabel.setText("🩺 Serving " + currentServing.getName() + " (" + currentServing.getCategory() + ")");
                waitingPatients.remove(currentServing);
            });

            long start = System.currentTimeMillis();

            for (long elapsed = 1; elapsed <= actual && running; elapsed++) {
                currentRemainingTime = Math.max(0, actual - elapsed);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                long finalElapsed = elapsed;
                Platform.runLater(() -> {
                    showCurrentServing(currentServing, finalElapsed, expected);
                    statusLabel.setText(String.format("🩺 Serving %s [%ds / %ds]",
                            currentServing.getName(), finalElapsed, expected));
                });

                decrementWaitTimes();

                // Overrun handling
                if (elapsed > expected && elapsed % 10 == 0) {
                    extraAdded += 10;
                    applyExtraDelayToQueue(10);
                    log("⏳ +10s delay added due to overrun by " + currentServing.getName());
                }
            }

            long end = System.currentTimeMillis();
            long servedDuration = (end - start) / 1000;

            currentServing.setBeingServed(false);
            currentServing.markServed();
            totalServed++;
            totalServeTime += servedDuration;

            // Make a safe copy to avoid NPE
            Patient servedCopy = currentServing;

            Platform.runLater(() -> {
                if (servedCopy != null) {
                    servedPatients.add(servedCopy);
                    detailsArea.setText(servedCopy.getDetails() + "\n\n✅ Served Successfully!");
                    servedList.refresh();
                    currentServingArea.clear();
                }
            });

            logPatientEvent("✅ Served completed", currentServing);

            if (actual > expected && extraAdded > 0) {
                long totalOverrun = actual - expected;
                long leftover = extraAdded - totalOverrun;
                if (leftover > 0) {
                    removeExtraDelay(leftover);
                    log("🔹 -" + leftover + "s deducted (early finish correction by " + currentServing.getName() + ")");
                }
            }

            currentServing = null;
            currentRemainingTime = 0;
            updateWaitTimes(null);
        }

        logSummary();
        logHeader("Queue Completed");

        Platform.runLater(() -> statusLabel.setText("🎉 Queue complete!"));
        running = false;
    }

    // --- Display Info ---
    private void showCurrentServing(Patient p, long elapsed, long expected) {
        if (p == null) return;
        currentServingArea.setText(
                "👤 " + p.getName() + " (" + p.getCategory() + ")\n" +
                        "Token: " + p.getTokenNumber() + "\n" +
                        "Expected Duration: " + expected + "s\n" +
                        "Elapsed: " + elapsed + "s\n" +
                        "Remaining: " + Math.max(0, expected - elapsed) + "s\n" +
                        "Status: " + (p.isBeingServed() ? "Serving..." : "Served ✅")
        );
    }

    private void applyExtraDelayToQueue(long seconds) {
        Platform.runLater(() -> {
            for (Patient p : waitingPatients) {
                p.setApproxWaitTime(p.getApproxWaitTime() + seconds);
            }
            waitingList.refresh();
        });
    }

    private void removeExtraDelay(long seconds) {
        Platform.runLater(() -> {
            for (Patient p : waitingPatients) {
                long corrected = Math.max(0, p.getApproxWaitTime() - seconds);
                p.setApproxWaitTime(corrected);
            }
            waitingList.refresh();
        });
    }

    private void decrementWaitTimes() {
        Platform.runLater(() -> {
            for (Patient p : waitingPatients) {
                long remaining = Math.max(0, p.getApproxWaitTime() - 1);
                p.setApproxWaitTime(remaining);
            }
            waitingList.refresh();
        });
    }

    private void updateWaitTimes(Patient current) {
        List<Patient> sorted = new ArrayList<>(queue);
        sorted.sort(priorityComparator);

        long cumulative = (current != null) ? current.getExpectedServeTime() : 0;
        int pos = 1;
        for (Patient p : sorted) {
            p.setQueueNumber(pos++);
            p.setApproxWaitTime(cumulative);
            cumulative += p.getExpectedServeTime();
        }

        Platform.runLater(() -> waitingPatients.setAll(sorted));
    }

    // --- Logging Utilities ---
    private void logHeader(String header) {
        try (FileWriter fw = new FileWriter(LOG_PATH, true)) {
            fw.write("\n==========================\n");
            fw.write("📅 " + LocalDate.now() + " | " + header + "\n");
            fw.write("==========================\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logSummary() {
        try (FileWriter fw = new FileWriter(LOG_PATH, true)) {
            fw.write(String.format("\n📊 Queue Summary:\nPatients Served: %d\nTotal Serve Time: %ds\nAverage Serve Time: %.2fs\n",
                    totalServed, totalServeTime, (totalServed > 0 ? (double) totalServeTime / totalServed : 0)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logPatientEvent(String event, Patient p) {
        try (FileWriter fw = new FileWriter(LOG_PATH, true)) {
            fw.write(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                    " - " + event + ": " + p.getName() + " (" + p.getCategory() + ")\n" +
                    "   Token: " + p.getTokenNumber() + " | Queue: " + p.getQueueNumber() +
                    " | Expected: " + p.getExpectedServeTime() + "s\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String msg) {
        try (FileWriter fw = new FileWriter(LOG_PATH, true)) {
            fw.write(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                    " - " + msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureDirectoryExists() {
        File f = new File(LOG_PATH);
        File dir = f.getParentFile();
        if (!dir.exists()) {
            boolean ok = dir.mkdirs();
            if (!ok) System.err.println("⚠ Could not create log directory: " + dir);
        }
    }

    public long getCurrentRemainingTime() {
        return currentRemainingTime;
    }

    public void stop() {
        running = false;
    }
}
