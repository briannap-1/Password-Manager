import java.io.*;
import java.util.Scanner;

/**
 * @author Brianna Penkala
 * This class represents a file manager
 */
public class FileManager {

    /** A field that initializes a placeholder file */
    static File file = new File("Passwords");
    /** A field that initializes a file directory */
    static File directory = new File("Password Files");
    /** A field that initializes an array for the directory */
    static File[] directoryArray = directory.listFiles();

    /** A method that gets the directory array
     * @return an array of files from the directory
     * */
    public static File[] getDirectoryList() {
        return directoryArray;
    }

    /** A method that sets the working file
     * @param verifiedFile the new working file
     * */
    public static void setFile(File verifiedFile) {
        file = verifiedFile;
    }

    /** A method that checks if a file exists
     * @return true if the file exists
     * */
    public boolean fileExists() {
        return file.isFile();
    }

    /** A method that creates an initial file
     * @param fileName the name of the new file
     * @param salt the salt to be stored in the file
     * @param masterPassword the master password to be stored in the file
     * */
    public static void createInitialFile(String fileName, String salt, String masterPassword) {
        file = new File(directory, fileName);
        try {
            FileWriter writer = new FileWriter(file, true);
            writer.write(salt + " : " + masterPassword);
            writer.close();
        } catch (IOException e) {
            System.out.println("I/O issue.");
        }
    }

    /** A method that appends a password to a file
     * @param website the website associated with the new password
     * @param encryptedPassword the encrypted password to be added
     * */
    public static void appendPassword(String website, String encryptedPassword) {
        if(contains(website)) {
            overwriteFile(website); //just removes the old website password line, appends as usual with the rest of this method
            System.out.println("Website already has a password. Replacing old password.");
        }
        try {
            FileWriter writer = new FileWriter(file, true);
            writer.write(System.lineSeparator());
            writer.write(website + " : " + encryptedPassword);
            writer.close();
        } catch (IOException e) {
            System.out.println("IO issue.");
        }
    }

    /** A method that gets a password in a file
     * @param website the website associated with the password
     * @return the password
     * */
    public static String getPassword(String website) {
        Scanner myReader = null;
        try {
            myReader = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
        int lengthWeb = website.length(); //gets length of username to determine correct line in file
        String password;
        while (myReader.hasNextLine()) {
            String line = myReader.nextLine();
            if (line.substring(0, lengthWeb).equals(website)) {
                int index = line.indexOf(": ");
                password = line.substring(index + 2);
                return password;
            }
        }
        return "fail";
    }

    /** A method that gets the salt from a file
     * @param file the file that contains the salt
     * @return the salt
     * */
    public static String getSalt(File file) {
        Scanner myReader = null;
        try {
            myReader = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
            String line = myReader.nextLine();
            int index = line.indexOf(" :");
        return line.substring(0, index);
    }

    /** A method that checks if a file contains a specified website
     * @param website the website being checked
     * @return true if the website is found in the file
     * */
    public static boolean contains(String website) {
        Scanner myReader;
        boolean containsWebsite = false;
        try {
            myReader = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while(myReader.hasNextLine()) {
            String line = myReader.nextLine();
            if(line.contains(website)) {
                containsWebsite = true;
            }
        }
        return containsWebsite;
    }

    /** A method that creates a duplicate file without the line containing the password to be rewritten
     * @param website the website associated with the password to be replaced
     * */
    public static void overwriteFile(String website) { //will only run when it is confirmed the file contains the website
        File oldFile = file;
        File newFile = new File(directory, file.getName() + " "); //temp fix
        Scanner myReader = null;
        try {
            myReader = new Scanner(oldFile);
             //really need to write this once
            FileWriter writer = new FileWriter(newFile, true);
            String line = myReader.nextLine();
            writer.write(line);
            while (myReader.hasNextLine()) {
                line = myReader.nextLine();
                if (!line.contains(website)) {
                    writer.write(System.lineSeparator());
                    writer.write(line);
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("I/O issue.");
        }
        myReader.close();
        oldFile.delete();
        file = newFile;
    }
}