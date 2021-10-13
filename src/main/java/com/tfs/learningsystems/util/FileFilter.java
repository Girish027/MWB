package com.tfs.learningsystems.util;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

@Slf4j
public class FileFilter implements FilenameFilter {

    private String[] fileType;
    private int hourBeforeDelete;

    public FileFilter(String[] fileType, int hourBeforeDelete){
        this.fileType = fileType;
        this.hourBeforeDelete = hourBeforeDelete;
    }

    public static FileTime getCreationTime(File file) throws IOException {
        Path p = Paths.get(file.getAbsolutePath());
        BasicFileAttributes view
                = Files.getFileAttributeView(p, BasicFileAttributeView.class)
                .readAttributes();
        return view.creationTime();
    }

    @Override
    public boolean accept(File directory, String fileName) {
        for(String file : fileType){
            if(fileName.startsWith(file)){
                LocalDateTime dtNow = LocalDateTime.now().minusHours(this.hourBeforeDelete);
                LocalDateTime dt = null;
                try {
                    dt = new LocalDateTime(FileFilter.getCreationTime(new File(directory + Constants.FORWARD_SLASH + fileName)).toMillis());
                    return dt.isBefore(dtNow);
                } catch (IOException e) {
                    log.error("Error while filtering files to be deleted from tmp folder:", e);
                }
            }
        }
        return false;
    }
}
