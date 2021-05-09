package driver;

import services.FileHash;
import services.FileScan;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Driver {
    public static void main(String[] args) {

        if(args.length!=1){
            System.out.println("Invalid number of arguments!");
            return;
        }

        File file = new File(args[0]);      //Getting the file path from the arguments passed in the terminal
        MessageDigest messageDigest = null;
        String fileChecksum = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");       //Generating a MD5 message digest for finding the MD5 checksum of the file.
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        FileHash fileHash = new FileHash(file, messageDigest);      //Finding the file checksum.
        try {
            fileChecksum = fileHash.getChecksum();
        } catch (IOException e) {
            System.out.println("File not found !");
            e.printStackTrace();
            return;
        }

        FileScan fileScan = new FileScan(fileChecksum);
        if(!fileScan.checkIfFileExists()){          //Checking if file checksum exist in the cache.
            fileScan.uploadFile(file);              //Uploading the file on the server for scanning.
            fileScan.retrieveResults();             //Retrieving the scanned results for the file.
        }
        fileScan.printResults();                    //Displaying the scanned results for each engine on the console.
    }
}
