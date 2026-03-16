package server.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import common.models.LeaveApplication;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LeaveRepository {

    // Matches EmployeeRepository path pattern
    private static final String DATA_FILE = "HRM RMI/data/leave_requests.json";
    private final Gson gson;

    public LeaveRepository() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        initializeFile();
    }

    private void initializeFile() {
        try {
            File file = new File(DATA_FILE);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (!file.exists()) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("[]");
                }
            }
        } catch (Exception e) {
            System.err.println("[LeaveRepository] Error initializing file: " + e.getMessage());
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    public List<LeaveApplication> loadAll() {
        try (FileReader reader = new FileReader(DATA_FILE)) {
            Type listType = new TypeToken<List<LeaveApplicationJson>>() {}.getType();
            List<LeaveApplicationJson> raw = gson.fromJson(reader, listType);
            if (raw == null) return new ArrayList<>();

            List<LeaveApplication> list = new ArrayList<>();
            for (LeaveApplicationJson r : raw) {
                list.add(r.toLeaveApplication());
            }
            return list;
        } catch (Exception e) {
            System.err.println("[LeaveRepository] Error loading: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    public void saveAll(List<LeaveApplication> applications) {
        List<LeaveApplicationJson> raw = new ArrayList<>();
        for (LeaveApplication la : applications) {
            raw.add(new LeaveApplicationJson(la));
        }
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            gson.toJson(raw, writer);
        } catch (IOException e) {
            System.err.println("[LeaveRepository] Error saving: " + e.getMessage());
        }
    }

    // ── Add ───────────────────────────────────────────────────────────────────

    public void add(LeaveApplication la) {
        List<LeaveApplication> all = loadAll();
        if (la.getApplicationId() == null || la.getApplicationId().isEmpty()) {
            la.setApplicationId("LA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        all.add(la);
        saveAll(all);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public boolean update(LeaveApplication updated) {
        List<LeaveApplication> all = loadAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getApplicationId().equals(updated.getApplicationId())) {
                all.set(i, updated);
                saveAll(all);
                return true;
            }
        }
        return false;
    }

    // ── Find by applicationId ─────────────────────────────────────────────────

    public LeaveApplication findById(String applicationId) {
        for (LeaveApplication la : loadAll()) {
            if (la.getApplicationId().equals(applicationId)) return la;
        }
        return null;
    }

    // ── Find by employeeId ────────────────────────────────────────────────────

    public List<LeaveApplication> findByEmployeeId(String employeeId) {
        List<LeaveApplication> result = new ArrayList<>();
        for (LeaveApplication la : loadAll()) {
            if (la.getEmployeeId().equalsIgnoreCase(employeeId)) result.add(la);
        }
        return result;
    }

    // ── Find by status ────────────────────────────────────────────────────────

    public List<LeaveApplication> findByStatus(String status) {
        List<LeaveApplication> result = new ArrayList<>();
        for (LeaveApplication la : loadAll()) {
            if (la.getStatus().equalsIgnoreCase(status)) result.add(la);
        }
        return result;
    }

    // ── Inner class for Gson serialization ────────────────────────────────────
    // Gson cannot handle LocalDate natively — we store dates as Strings in JSON
    // and convert on read/write. Field names match leave_requests.json exactly.

    private static class LeaveApplicationJson {
        String applicationId;
        String employeeId;
        String fromDate;
        String toDate;
        int    amountOfDays;
        String initialDate;
        String status;

        // Convert LeaveApplication → JSON-friendly object
        LeaveApplicationJson(LeaveApplication la) {
            this.applicationId = la.getApplicationId();
            this.employeeId    = la.getEmployeeId();
            this.fromDate      = la.getFromDate().toString();
            this.toDate        = la.getToDate().toString();
            this.amountOfDays  = la.getAmountOfDays();
            this.initialDate   = la.getInitialDate().toString();
            this.status        = la.getStatus();
        }

        // Convert JSON-friendly object → LeaveApplication
        LeaveApplication toLeaveApplication() {
            LeaveApplication la = new LeaveApplication();
            la.setApplicationId(applicationId);
            la.setEmployeeId(employeeId);
            la.setFromDate(LocalDate.parse(fromDate));
            la.setToDate(LocalDate.parse(toDate));
            la.setAmountOfDays(amountOfDays);
            la.setInitialDate(LocalDate.parse(initialDate));
            la.setStatus(status);
            return la;
        }
    }
}