package server.repository;
import common.models.User;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class UserRepository
{
    static final String DATA_DIR =  "data"; //directory for data files
    static final String FILE_PATH = DATA_DIR + "/users.json";

    public static void init()
    {
        try
        {
            Files.createDirectories(Paths.get(DATA_DIR));
            Path p = Paths.get(FILE_PATH);
            if(!Files.exists(p))
            {
                JSONArray seed = new JSONArray();
                //seed for HR user
                seed.put(buildJson("E0100", "Barnacle@mail.com", PasswordUtility.hash("hrrocks123"), "HR"));
                //seed for Employee user
                seed.put(buildJson("E0101", "Mermaid@mail.com", PasswordUtility.hash("emprocks123"), "Employee"));
                writeFile(p, seed.toString(2));
                System.out.println("User data file '" + FILE_PATH + "' created with seed data.");
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to initialize user repository: " + e.getMessage(), e);
        }
    }

    //READ PURPOSE
    //return all users
    public static synchronized List<User> findAll()
    {
        JSONArray arr = readArray();
        List<User> list = new ArrayList<>();
        for(int i = 0; i < arr.length(); i++)
        {
            list.add(fromJson(arr.getJSONObject(i)));
        }
        return list;
    }

    //find by email
    public static synchronized User findByEmail (String email)
    {
        if (email == null) return null;
        JSONArray arr = readArray();
        for(int i = 0; i < arr.length(); i++)
        {
            JSONObject obj = arr.getJSONObject(i);
            if (email.equalsIgnoreCase(obj.optString("email")))
            {
                return fromJson(obj);
            }
        }
        return null;
    }

    //find employee ID
    public static synchronized User findByEmployeeId (String employeeId)
    {
        if (employeeId == null) return null;
        JSONArray arr = readArray();
        for (int i = 0; i < arr.length(); i++)
        {
            JSONObject obj = arr.getJSONObject(i);
            if (employeeId.equalsIgnoreCase(obj.optString("name"))) //using "name" field to store employee ID for simplicity
            {
                return fromJson(obj);
            }
        }
    }

    //write new
    public static synchronized boolean save(User user)
    {
        JSONArray arr = readArray();
        for(int i = 0; i < arr.length(); i++)
        {
            if(user.getEmail().equalsIgnoreCase(arr.getJSONObject(i).optString("email")))
            {
                return false; //email already exists
            }
        }
        arr.put(toJson(user));
        persist(arr);
        return true;
    }

    //update existing
    public static synchronized boolean update (String originalEmail, User updated)
    {
        JSONArray arr = readArray();
        for(int i = 0; i < arr.length(); i++)
        {
            if(originalEmail.equalsIgnoreCase(arr.getJSONObject(i).optString("email")))
            {
                arr.put(i, toJson(updated));
                persist(arr);
                return true; //update successful
            }
        }
        return false;
    }

    //HELPER METHODS
    private static JSONArray readArray()
    {
        try
        {
            byte[] bytes = Files.readAllBytes(Paths.get(FILE_PATH));
            String raw = new String(bytes, StandardCharsets.UTF_8).trim();
            return raw.isEmpty() ? new JSONArray() : new JSONArray(raw);
        }
        catch (IOException e)
        {
            return new JSONArray(); //return empty array if file read fails
        }
    }

    private static void persist(JSONArray arr)
    {
        try
        {
            writeFile(Paths.get(FILE_PATH), arr.toString(2));
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to persist user data: " + e.getMessage(), e);
        }
    }

    private static void writeFile(Path path, String content) throws IOException
    {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static JSONObject toJson (User u)
    {
        return buildJson(u.getEmployeeId(), u.getEmail(), u.getPassword(), u.getRole());
    }

    private static JSONObject buildJson(String employeeId, String email, String password, String role)
    {
        JSONObject obj = new JSONObject();
        obj.put("employeeId", employeeId);
        obj.put("email", email);
        obj.put("password", password);
        obj.put("role", role);
        return obj;
    }

    private static User fromJson (JSONObject obj)
    {
        User u = new User();
        u.setEmployeeId(obj.optString("employeeId"));
        u.setEmail(obj.optString("email"));
        u.setPassword(obj.optString("password"));
        u.setRole(obj.optString("role"));
        return u;
    }
}