/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

/**
 * @author Decebal Suiu
 */
public class FileUtil {

	/**
	 * Returns the <code>byte</code>[] rawValue of the given <code>File</code>.
	 */
	public static byte[] getBytes(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		int fileSize = (int) file.length();

		if (fileSize > Integer.MAX_VALUE) {
			in.close();
			throw new IOException("File size to large: " + fileSize + " > " + Integer.MAX_VALUE);
		}

		byte[] bytes = new byte[fileSize];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = in.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			in.close();
			throw new IOException("Could not completely read file " + file.getName());
		}

		in.close();

		return bytes;
	}

	public static byte[] getBytes(InputStream in) throws Exception {
		int size = in.available();
		byte[] bytes = new byte[size];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = in.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read ");
		}

		in.close();

		return bytes;
	}

    /** Zip files
     *
     * @param fileNames list of file names to zip
     * @param fileContents list of file contents
     * @return zip byte content
     * @throws java.io.IOException IOException
     */
    public static byte[] zip(List<String> fileNames, List<byte[]> fileContents) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Create the ZIP file
        ZipOutputStream out = new ZipOutputStream(baos);

        for (int i=0, size=fileNames.size(); i<size; i++) {
            String name = fileNames.get(i);
            byte[] content = fileContents.get(i);

            // Add ZIP entry to output stream.
            ZipEntry entry = new ZipEntry(name);
            entry.setSize(content.length);            
            out.putNextEntry(entry);
            out.write(content);
            // Complete the entry
            out.closeEntry();
        }
        out.close();
        return baos.toByteArray();
    }

    /** Zip files
     *
     * @param namesMap map of files to zip , each entry is the name of the folder where these files are added
     * @param contentsMap map of file contents
     * @return zip byte content
     * @throws java.io.IOException IOException
     */
    public static byte[] zip(Map<String, List<String>> namesMap, Map<String, List<byte[]>> contentsMap) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Create the ZIP file
        ZipOutputStream out = new ZipOutputStream(baos);

        for (String folder : namesMap.keySet()) {

            // create the folder
            out.putNextEntry(new ZipEntry(folder + "/"));

            List<String> fileNames = namesMap.get(folder);
            List<byte[]> fileContents = contentsMap.get(folder);

            for (int i = 0, size = fileNames.size(); i < size; i++) {
                String name = fileNames.get(i);
                byte[] content = fileContents.get(i);

                // Add ZIP entry to output stream.
                ZipEntry entry = new ZipEntry(folder + "/" + name);
                entry.setSize(content.length);
                out.putNextEntry(entry);
                out.write(content);
                // Complete the entry
                out.closeEntry();
            }
        }
        out.close();
        return baos.toByteArray();
    }
    
    public static void zip(List<File> files, String outFileName, String withoutBase) {
        // Create a buffer for reading the files
        byte[] buf = new byte[1024];

        try {
            // Create the ZIP file
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFileName));

            // Compress the files
            for (File file : files) {            	            	
            	
            	if (file.isDirectory()) {
                	continue;
                }
            	
                FileInputStream in = new FileInputStream(file);

                // Add ZIP entry to output stream.
                String fileName = file.getAbsolutePath();
                if (withoutBase != null) {
                	fileName = fileName.substring(withoutBase.length() + 1);
                }
                out.putNextEntry(new ZipEntry(fileName));
                

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                // Complete the entry
                out.closeEntry();
                in.close();
            }

            // Complete the ZIP file
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    public static void unzip(ZipInputStream zipinputstream, String destination) {
        try {
            byte[] buf = new byte[1024];
            ZipEntry zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                String entryName = zipentry.getName();                
                File newFile = new File(entryName);

                String path = destination + File.separator + entryName;

                // take care to create the directories
                String dirs = path.substring(0, path.lastIndexOf(File.separator));
                new File(dirs).mkdirs();

				if (!zipentry.isDirectory()) {
					FileOutputStream fileoutputstream = new FileOutputStream(path);

					int n;
					while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
						fileoutputstream.write(buf, 0, n);
					}

					fileoutputstream.close();
					zipinputstream.closeEntry();
				} else {
					new File(path).mkdirs();
				}
                zipentry = zipinputstream.getNextEntry();
            }
            zipinputstream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // copy all files recursively from directory source to directory dest
    // dest directory must be already created!
    public static void copyDirToDir(File source, File dest, FilenameFilter filter) throws IOException {
        if (!source.isDirectory() || !dest.isDirectory()) {
            return;
        }

        List<File> files = listFiles(source, filter, true);
        String sourcePath = source.getAbsolutePath();
        String destPath = dest.getAbsolutePath();
        for (File file : files) {
            String filePath = file.getAbsolutePath();
            if (sourcePath.equals(filePath)) {
                continue;
            }
            String newPath = destPath + File.separator + filePath.substring(sourcePath.length());
            File destFile = new File(newPath);
            if (file.isDirectory()) {
                destFile.mkdirs();
            } else {
                copy(file, destFile);
            }
        }
    }
    
    public static void backupRepository(File source, File dest, FilenameFilter filter) throws IOException {
    	SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_hh_mm");
    	dest.mkdirs();
    	copyDirToDir(source, dest, filter);
    	String outputZip = dest.getParent() + File.separator + sdf.format(new Date()) + ".zip";
    	List<File> files = listFiles(dest, null, true);
    	String base = dest.getAbsolutePath().substring(0, dest.getAbsolutePath().lastIndexOf(File.separator));
    	zip(files, outputZip, base);
    	deleteDir(dest);
    }

    public static void copy(File source, File dest) throws IOException {
        InputStream in = null;
        OutputStream out = null;        
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
    
    public static List<File> listFiles(File directory, FilenameFilter filter, boolean recurse) {
        // List of files / directories
        List<File> files = new ArrayList<File>();
        // Get files / directories in the directory
        File[] entries = directory.listFiles();
        if (entries == null) {
            return files;
        }
        // Go over entries
        for (File entry : entries) {
            // If there is no filter or the filter accepts the
            // file / directory, add it to the list
            if (filter == null || filter.accept(directory, entry.getName())) {
                files.add(entry);
            }
            // If the file is a directory and the recurse flag
            // is set, recurse into the directory
            if (recurse && entry.isDirectory()) {
                files.addAll(listFiles(entry, filter, recurse));
            }
        }

        // Return collection of files
        return files;
    }
    
    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static boolean deleteDir(File dir) {        
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String file : children) {
                boolean success = deleteDir(new File(dir, file));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }


}
