package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileHash {
    private File file;
    private MessageDigest messageDigest;

    public FileHash(File file, MessageDigest messageDigest) {
        this.file = file;
        this.messageDigest = messageDigest;
    }

    public String getChecksum() throws IOException {
        MessageDigest messageDigest = null;

        FileInputStream fis = new FileInputStream(this.file);
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        while((bytesCount = fis.read(byteArray)) != -1){
            this.messageDigest.update(byteArray, 0 , bytesCount);
        }

        fis.close();

        byte[] bytes = this.messageDigest.digest();

        StringBuilder sb = new StringBuilder();
        for(int i=0; i<bytes.length; i++){
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
