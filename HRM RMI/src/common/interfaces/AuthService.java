package common.interfaces;
import common.models.User;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthService extends Remote
{
    User login(String email, String password) throws RemoteException;
    boolean logout(String sessionID) throws RemoteException;
    User getProfile(String sessionID) throws RemoteException;
    User updateProfile(String sessionID, String newEmail, String newPassword) throws RemoteException;
    User getUserByEmail(String sessionID, String targetEmail) throws RemoteException;
    boolean isSessionValid(String sessionID) throws RemoteException;
    String getEmailBySessionID(String sessionID) throws RemoteException;
    boolean isHR(String sessionID) throws RemoteException;
}