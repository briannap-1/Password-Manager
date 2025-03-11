import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Scanner;

/**
 * @author Brianna Penkala
 * This class represents a password manager
 */
public class PasswordManager {

    /** A field that initializes a byte array for salt */
     static byte[] salt = new byte[16];
    /** A field that initializes the key specification */
     static SecretKeySpec key;
    /** A field that initializes the FileManager class */
     static FileManager fileManager = new FileManager();
    /** A field that initializes a cipher */
     static Cipher cipher;
    /** A field that indicates if the program should continue */
     static boolean proceed = true;
    /** A field that initializes a directory list for the password files */
     static File[] directoryList = FileManager.getDirectoryList();

    /** The main method
     * @param args arguments for the main method
     * */
    public static void main (String[] args) {
        //Initial prompt for master password
        System.out.print("Enter the master password: ");
        Scanner scanner = new Scanner(System.in);
        String masterString = scanner.nextLine();

        //Checks if the master password is already stored, creates a new file if it isn't
        if (!correctMaster(masterString) || !fileManager.fileExists()) { //when the master password is not found and there is not a file already made
                System.out.println("No password files detected. Enter a name for the new password file: ");
                String fileName = scanner.nextLine();
                setMasterPassword(fileName, masterString);
                System.out.println("New master password created.");
        }
        //While the program is running, goes through basic actions
        while(proceed) {
            System.out.println("a: Add new password" + "\n" + "b: Read password" + "\n" + "c: Quit" + "\n" + "Enter choice: ");
            String choice = scanner.nextLine();
            if (choice.equals("a")) {
                System.out.print("Enter website for password: ");
                String website = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();
                encrypt(website, password);
                System.out.print("Password created for website " + website + "." + "\n");
            } else if (choice.equals("b")) {
                System.out.print("Enter website for password: ");
                String website = scanner.nextLine();
                String password = decrypt(website);
                System.out.print("The password for " + website + " is " + password + ".");
            } else {
                System.out.print("Quitting...");
                System.exit(0);
            }
            System.out.print("\n" + "Would you like to proceed?" + "\n" + "y: yes" + "\n" + "n: no" + "\n" + "Enter choice: ");
            choice = scanner.nextLine();
            if (choice.equals("n")) {
                proceed = false;
            }
            System.out.print("\n");
        }
    }

    /** A method that creates a key
     * @param salt the salt for the key
     * @param password the master password to use for the key
     * */
    private static void createKey(byte[] salt, String password) {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1024, 128);
        SecretKeyFactory factory = null;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("This algorithm is not available.");
        }
        SecretKey privateKey = null;
        try {
            privateKey = factory.generateSecret(spec);
        } catch (InvalidKeySpecException e) {
            System.out.println("Invalid key.");
        }
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            System.out.println("Invalid padding.");
        }
        key = new SecretKeySpec(privateKey.getEncoded(), "AES");
    }

    /** A method that checks if an inputted master password is already stored in a file
     * @param password the master password to check
     * @return boolean true if the master password is already stored
     * */
    private static boolean correctMaster(String password) {
        boolean verifiedPassword = false;
        for (File file : directoryList) {
            if (!verifiedPassword) {
                //Retrieving the correct salt
                FileManager.setFile(file);
                String retrievedSalt = FileManager.getSalt(file);
                byte[] retrievedSaltByte = Base64.getDecoder().decode(retrievedSalt);

                //Checking the password
                createKey(retrievedSaltByte, password);
                if (FileManager.contains(masterEncrypt(password))) {
                    if (decrypt(FileManager.getSalt(file)).equals(password)) //decryption will get password based on the website, or salt in this case
                        verifiedPassword = true;
                }
            }
        }
        return verifiedPassword;
    }

    /** A method that sets the master password
     * @param fileName the name of the master password file
     * @param password the master password
     * */
    private static void setMasterPassword(String fileName, String password) {
        //Creating the salt
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        String saltString = Base64.getEncoder().encodeToString(salt);

        //Creating the key and the new file
        createKey(salt, password);
        String encryptedPassword = masterEncrypt(password);
        FileManager.createInitialFile(fileName, saltString, encryptedPassword);
    }

    /** A method that only returns the master password instead of creating a file for it and requiring a website
     * @param password the password to encrypt
     * @return String the encrypted password string
     * */
    private static String masterEncrypt(String password) {
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            System.out.println("Invalid padding.");
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            System.out.println("Invalid key.");
        }
        byte[] encryptedData = new byte[0];
        try {
            encryptedData = cipher.doFinal(password.getBytes());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            System.out.println("Invalid block size.");
        }
        return new String(Base64.getEncoder().encode(encryptedData));
    }

    /** A method that encrypts a password
     * @param website the website associated with the password
     * @param password the password to be encrypted
     * */
    private static void encrypt(String website, String password) {
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            System.out.println("Invalid padding.");
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            System.out.println("Invalid key.");
        }
        byte[] encryptedData = new byte[0];
        try {
            encryptedData = cipher.doFinal(password.getBytes());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            System.out.println("Invalid block.");
        }
        String passwordString = new String(Base64.getEncoder().encode(encryptedData));
            FileManager.appendPassword(website, passwordString);
    }

    /** A method that decrypts a password
     * @param website the website associated with a password
     * @return the decrypted password
     * */
    private static String decrypt(String website) {
        String encryptedPassword;
        encryptedPassword = FileManager.getPassword(website);
        if (!encryptedPassword.equals("fail")) {
            try {
                cipher.init(Cipher.DECRYPT_MODE, key);
            } catch (InvalidKeyException e) {
                System.out.println("Invalid key.");
            }
            byte[] encryptedData = Base64.getDecoder().decode(encryptedPassword);
            byte[] decryptedData = new byte[0];
            try {
                decryptedData = cipher.doFinal(encryptedData);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                System.out.println("Invalid block.");
            }
            return new String(decryptedData);
        } else
            return "not found";
    }
}