package server.repository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtility
{
    private PasswordUtility() {}
    public static String hash(String plainTxt)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plainTxt.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest)
            {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static boolean verify (String plainTxt, String hashed)
    {
        return hash(plainTxt).equals(hashed);
    }
}