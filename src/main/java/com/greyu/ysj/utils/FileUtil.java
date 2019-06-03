package com.greyu.ysj.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @Description: File upload and delete class
 * @Author: gre_yu@163.com
 * @Date: Created in 16:48 2018/3/12.
 */
public class FileUtil {
    /**
      * File Upload
      * @param file The file to be uploaded
      * @param path The path to upload
      * @return The new name of the file after processing
      * @throws IOException exception
      */
    public static String upload(MultipartFile file, String path) throws IOException {
        String fileName = file.getOriginalFilename();
        String extensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        String newFileName = String.valueOf(System.currentTimeMillis()) + "." + extensionName;
        File targetFile = new File(path);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        Files.copy(file.getInputStream(), Paths.get(path, newFileName));
        System.out.println(path);
        Runtime.getRuntime().exec("chmod 755 /var/www/html/cloudimg/goods/" + newFileName);
        return newFileName;
    }

    /**
      * Delete Files
      * @param path path
      * @param fileName name
      * @throws IOException exception
      */
    public static void delete(String path, String fileName) throws IOException {
        File delete = new File(path + "\\" + fileName);
        boolean b = delete.delete();
        if (b) {
            System.out.println("Delete Files");
        }
    }
}
