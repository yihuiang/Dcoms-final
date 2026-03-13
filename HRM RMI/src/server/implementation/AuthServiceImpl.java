package server.implementation;
import common.interfaces.AuthService;
import common.models.User;
import server.repository.PasswordUtility;
import server.repository.UserRepository;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java,util.*;

public class AuthServiceImpl extends UnicastRemoteObject implements AuthService
{
    private static final long serialVersionUID =  1L;
    private final Map<String, User> activeSessions = new ConcurrentHashMap<>();

    public AuthServiceImpl() throws RemoteException 
    {
        super();
    }

    @Override
    public User login(String email, String password) throws RemoteException
    {
        System.out.println("Authenticating user --> " + email);
        //input validation
        if (isBlank(email) || isBlank(password))
        {
            throw new RemoteException("Email and password cannot be blank.");
        }
        //lookup user
        User user = UserRepository.findByEmail(email.trim());
        if (user == null)
        {
            throw new RemoteException("There is no account with email: " + email);
        }
        //verify password
        if (!PasswordUtility.verify(password, user.getPassword()))
        {
            throw new RemoteException("Incorrect password or email.");
        }
        //session issue
        String sessionID = UUID.randomUUID().toString();
        User.setSessionID(sessionID);

        //new copy in session map to mask password
        User sessionUser = copyUser(user);
        sessionUser.setPassword("[PROTECTED]");
        activeSessions.put(sessionID, sessionUser);

        System.out.println("Login Authenticated for user: " + email + " , Role = " + user.getRole() + " , HR = " + user.isHR() + " , session = " + sessionID);
        //return masked password
        user.setPassword("[PROTECTED]");
        return user;
    }

    //LOG OUT
    @Override
    public boolean logout(String sessionID) throws RemoteException
    {
        User removed = activeSessions.remove(sessionID);
        if (removes != null)
        {
            System.out.println("User logged out, session removed: " + sessionID);
            return true;
        }
        return false;
    }

    //GET THE PROFILE
    @Override
    public User getProfile(String sessionID) throws RemoteException
    {
        User session = requireSession(sessionID);
        //reread if there is update
        User fresh = UserRepository.findByEmail(session.getEmail());
        if(fresh == null)
        {
            throw new RemoteException("User is no longer exist.");
        }

        fresh.setSessionID(sessionID);
        fresh.setPassword("[PROTECTED]");
        return fresh;
    }

    //UPDATE PROFILE
    @Override
    public User updateProfile(String sessionID, String newName, String newEmail, String newPassword) throws RemoteException
    {
        User session = requireSession(sessionID);
        String originalEmail = session.getEmail();
        //load current record
        User user = UserRepository.findByEmail(originalEmail);
        if (user == null)
        {
            throw new RemoteException("User account not found.");
        }

        boolean changed = false;

        //name update
        if(!isBlank(newName))
        {
            user.setName(newName.trim());
            changed = true;
        }

        //email update
        if(!isBlank(newEmail))
        {
            String trimmed = newEmail.trim().toLowerCase();
            if(!trimmed.equals(originalEmail.toLowerCase()))
            {
                if(UserRepository.findByEmail(trimmed) != null)
                {
                    throw new RemoteException("Email '" + trimmed + "' is already in use by another account.");
                    user.setEmail(trimmed);
                    changed = true;
                }
            }
        }

        //password update
        if(!isBlank(newPassword))
        {
            if(newPassword.length() < 8)
            {
                throw new RemoteException("Password must be at least 8 characters long.");
            }
            user.setPassword(PasswordUtility.hash(newPassword));
            changed = true;
        }

        if(!changed)
        {
            throw new RemoteException("No changes provided for update.");
        }

        //persist
        boolean saved = UserRepository.update(originalEmail, user);
        if(!saved)
        {
            throw new RemoteException("Failed to update user profile. Please try again.");
        }

        //update session
        if(!user.getEmail().equals(originalEmail))
        {
            activeSessions.remove(sessionId);
            String newSessionID = UUID.randomUUID().toString();
            User refreshed = copyUser(user);
            refreshed.setPassword("[PROTECTED]");
            refreshed.setSessionId(newSessionID);
            activeSessions.put(newSessionID, refreshed);
            user.setSessionID(newSessionID);
        }
        else
        {
            User refreshed = copyUser(user);
            refreshed.setPassword("[PROTECTED]");
            refreshed.setSessionId(sessionID);
            activeSessions.put(sessionID, refreshed);
            user.setSessionID(sessionID);
        }

        System.out.println("User profile updated for: " + user.getEmail());
        user.setPassword("[PROTECTED]");
        return user;
    }

    //FOR HR ACCESS
    @Override
    public User getUserByEmail(String sessionID, string targetEmail) throws RemoteException
    {
        User session = requireSession(sessionID);
        if(!session.isHR())
        {
            throw new RemoteException("Access denied. HR role required.");
        }
        User target = UserRepository.findByEmail(targetEmail.trim());
        if(target == null)
        {
            throw new RemoteException("No user found with email: " + targetEmail);
        }
        return target;
    }

    //SESSION VALIDITY
    @Override
    public boolean isSessionValid(String sessionID) throws RemoteException
    {
        return !isBlank(sessionID) && activeSessions.containsKey(sessionID);
    }

    //EMAIL BY SESSION
    @Override
    public String getEmailBySession(String sessionID) throws RemoteException
    {
        User user = activeSessions.get(sessionID);
        return user != null ? user.getEmail() : null;
    }

    //HR CHECK
    @Override
    public boolean isHR(String sessionID) throws RemoteException
    {
        User user = activeSessions.get(sessionID);
        return user != null && user.isHR();
    }

    //HELPER METHODS
    private User requireSession(String sessionID) throws RemoteException
    {
        if(isBlank(sessionID))
        {
            throw new RemoteException("Session ID cannot be blank.");
        }
        User user = activeSessions.get(sessionID);
        if(user == null)
        {
            throw new RemoteException("Invalid or expired session. Please log in again.");
        }
        return user;
    }

    //USER COPY
    private User copyUser(User src)
    {
        User copy = new User();
        copy.setName(src.getName());
        copy.setEmail(src.getEmail());
        copy.setPassword(src.getPassword());
        copy.setRole(src.getRole());
        return copy;
    }
    private static boolean isBlank(String str)
    {
        return str == null || str.trim().isEmpty();
    }
}