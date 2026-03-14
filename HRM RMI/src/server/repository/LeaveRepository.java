package server.repository;

import common.models.LeaveApplication;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LeaveRepository {

    private static final String DATA_FILE = "data/leave_requests.json";

    @SuppressWarnings("unchecked")
    public List<LeaveApplication> loadAll() {
        List<LeaveApplication> list = new ArrayList<>();
        File file = new File(DATA_FILE);
        if (!file.exists()) return list;

        try (FileReader reader = new FileReader(file)) {
            JSONParser parser = new JSONParser();
            Object parsed = parser.parse(reader);
            if (parsed == null) return list;

            JSONArray arr = (JSONArray) parsed;
            for (Object obj : arr) {
                list.add(fromJson((JSONObject) obj));
            }
        } catch (Exception e) {
            System.err.println("[LeaveRepository] Error loading: " + e.getMessage());
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public void saveAll(List<LeaveApplication> applications) {
        JSONArray arr = new JSONArray();
        for (LeaveApplication la : applications) {
            arr.add(toJson(la));
        }
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            writer.write(arr.toJSONString());
        } catch (IOException e) {
            System.err.println("[LeaveRepository] Error saving: " + e.getMessage());
        }
    }

    public void add(LeaveApplication la) {
        List<LeaveApplication> all = loadAll();
        if (la.getApplicationId() == null || la.getApplicationId().isEmpty()) {
            la.setApplicationId("LA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        all.add(la);
        saveAll(all);
    }

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

    public LeaveApplication findById(String applicationId) {
        for (LeaveApplication la : loadAll()) {
            if (la.getApplicationId().equals(applicationId)) return la;
        }
        return null;
    }

    public List<LeaveApplication> findByEmployeeId(String employeeId) {
        List<LeaveApplication> result = new ArrayList<>();
        for (LeaveApplication la : loadAll()) {
            if (la.getEmployeeId().equalsIgnoreCase(employeeId)) result.add(la);
        }
        return result;
    }

    public List<LeaveApplication> findByStatus(String status) {
        List<LeaveApplication> result = new ArrayList<>();
        for (LeaveApplication la : loadAll()) {
            if (la.getStatus().equalsIgnoreCase(status)) result.add(la);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private JSONObject toJson(LeaveApplication la) {
        JSONObject jo = new JSONObject();
        jo.put("applicationId", la.getApplicationId());
        jo.put("employeeId",    la.getEmployeeId());
        jo.put("fromDate",      la.getFromDate().toString());
        jo.put("toDate",        la.getToDate().toString());
        jo.put("amountOfDays",  (long) la.getAmountOfDays());
        jo.put("initialDate",   la.getInitialDate().toString());
        jo.put("status",        la.getStatus());
        return jo;
    }

    private LeaveApplication fromJson(JSONObject jo) {
        LeaveApplication la = new LeaveApplication();
        la.setApplicationId((String) jo.get("applicationId"));
        la.setEmployeeId(   (String) jo.get("employeeId"));
        la.setFromDate(LocalDate.parse((String) jo.get("fromDate")));
        la.setToDate(  LocalDate.parse((String) jo.get("toDate")));
        la.setAmountOfDays(((Long) jo.get("amountOfDays")).intValue());
        la.setInitialDate( LocalDate.parse((String) jo.get("initialDate")));
        la.setStatus(      (String) jo.get("status"));
        return la;
    }
}