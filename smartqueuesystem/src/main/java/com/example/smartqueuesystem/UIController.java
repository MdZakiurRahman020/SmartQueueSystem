package com.example.smartqueuesystem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class UIController {

    @FXML private ListView<Patient> waitingList;
    @FXML private ListView<Patient> servedList;
    @FXML private TextArea detailsArea;
    @FXML private TextArea currentServingArea;
    @FXML private TextField nameField, ageField;
    @FXML private ComboBox<String> sexChoice, categoryChoice;
    @FXML private Button addButton, startButton;
    @FXML private Label statusLabel;

    private final ObservableList<Patient> waitingPatients = FXCollections.observableArrayList();
    private final ObservableList<Patient> servedPatients = FXCollections.observableArrayList();

    private boolean queueRunning = false;
    private Thread queueThread;
    private QueueManager activeManager; // reference to running queue

    // Priority rule (Emergency → Senior → Child → General)
    private final Comparator<Patient> priorityComparator = Comparator
            .comparingInt(Patient::getPriority)
            .thenComparing((p1, p2) -> {
                if (p1.getCategory().equalsIgnoreCase("senior"))
                    return Integer.compare(p2.getAge(), p1.getAge()); // older seniors first
                if (p1.getCategory().equalsIgnoreCase("child"))
                    return Integer.compare(p1.getAge(), p2.getAge()); // younger children first
                return 0;
            })
            .thenComparing(Patient::getArrivalTime);

    private final PriorityBlockingQueue<Patient> queue =
            new PriorityBlockingQueue<>(10, priorityComparator);

    @FXML
    public void initialize() {
        sexChoice.getItems().addAll("Male", "Female", "Other");
        categoryChoice.getItems().addAll("General", "Emergency");

        waitingList.setItems(waitingPatients);
        servedList.setItems(servedPatients);

        waitingList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Patient p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                } else {
                    setText("Q" + p.getQueueNumber() +
                            " | Token " + p.getTokenNumber() +
                            " | " + p.getName() +
                            " | " + p.getCategory() +
                            " | ⏱ " + p.getFormattedWaitTime());
                }
            }
        });

        servedList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Patient p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                } else {
                    setText("Token " + p.getTokenNumber() +
                            " | " + p.getName() +
                            " | Served ✅ at " + p.getServedTimeString());
                }
            }
        });

        waitingList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) showPatientDetails(n);
        });

        statusLabel.setText("Status: Queue not started");
    }

    // Add new patient (before or after queue start)
    @FXML
    public void addPatient() {
        String name = nameField.getText();
        String ageText = ageField.getText();
        String sex = sexChoice.getValue();
        String category = categoryChoice.getValue();

        if (name.isEmpty() || ageText.isEmpty() || sex == null || category == null) {
            statusLabel.setText("⚠ Please fill all fields");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            Patient patient = new Patient(name, age, sex, category);
            patient.setExpectedServeTime(40 + new Random().nextInt(61)); // 40–100s realistic

            queue.add(patient);

            // ✅ Log event (works before and after start)
            QueueManager.logPatientEvent("🟢 Patient Added", patient);

            // ✅ If queue is running, resync dynamically
            if (queueRunning && activeManager != null) {
                long baseOffset = activeManager.getCurrentRemainingTime();
                syncWaitTimesDuringRuntime(baseOffset);
            } else {
                updateWaitTimes();
            }

            nameField.clear();
            ageField.clear();
            sexChoice.setValue(null);
            categoryChoice.setValue(null);
            statusLabel.setText("✅ Added " + name + " to queue");
        } catch (NumberFormatException e) {
            statusLabel.setText("⚠ Invalid age input");
        }
    }

    // Start serving queue
    @FXML
    public void startQueue() {
        if (queueRunning || queue.isEmpty()) {
            statusLabel.setText("⚠ Queue already running or empty");
            return;
        }

        queueRunning = true;
        statusLabel.setText("✅ Queue started...");

        activeManager = new QueueManager(
                queue,
                priorityComparator,
                waitingPatients,
                servedPatients,
                waitingList,
                servedList,
                detailsArea,
                currentServingArea,
                statusLabel
        );

        queueThread = new Thread(activeManager);
        queueThread.setDaemon(true);
        queueThread.start();
    }

    // Display detailed info for selected patient
    private void showPatientDetails(Patient p) {
        if (p == null) {
            detailsArea.clear();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(p.getName()).append("\n");
        sb.append("Age: ").append(p.getAge()).append("\n");
        sb.append("Sex: ").append(p.getSex()).append("\n");
        sb.append("Category: ").append(p.getCategory()).append("\n");
        sb.append("Token No: ").append(p.getTokenNumber()).append("\n");
        sb.append("Queue No: ").append(p.getQueueNumber()).append("\n");
        sb.append("Added At: ").append(p.getAddedTime()).append("\n");
        sb.append("Approx Wait Time: ").append(p.getFormattedWaitTime()).append("\n");
        sb.append("Expected Serve Time: ").append(p.getExpectedServeTime()).append("s\n");
        if (p.isBeingServed()) sb.append("\n🟢 Currently Being Served\n");
        detailsArea.setText(sb.toString());
    }

    // Update queue wait times dynamically (normal pre-start calculation)
    private void updateWaitTimes() {
        List<Patient> sorted = new ArrayList<>(queue);
        sorted.sort(priorityComparator);

        long cumulative = 0;
        int position = 1;
        for (Patient p : sorted) {
            p.setQueueNumber(position++);
            p.setApproxWaitTime(cumulative);
            cumulative += p.getExpectedServeTime();
        }

        Platform.runLater(() -> waitingPatients.setAll(sorted));
    }

    // 🔁 Update queue during runtime sync (after queue start)
    private void syncWaitTimesDuringRuntime(long baseOffset) {
        List<Patient> sorted = new ArrayList<>(queue);
        sorted.sort(priorityComparator);

        long cumulative = baseOffset; // start from remaining time of currently serving
        int position = 1;
        for (Patient p : sorted) {
            p.setQueueNumber(position++);
            p.setApproxWaitTime(cumulative);
            cumulative += p.getExpectedServeTime();
        }

        Platform.runLater(() -> waitingPatients.setAll(sorted));
    }
}
