package crixec.filehelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by crixec on 17-2-11.
 */

public class Utils {

    public static final int BUFFER_SIZE = 1024;

    public static File[] listFiles(File parentFile) {
        if (parentFile == null)
            parentFile = SettingHelper.getDefaultStartStorage();
        File[] files = parentFile.listFiles();
        if (files == null)
            files = new File[]{};
        return files;
    }

    public static String computeDigest(File file, String type) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance(type);
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    public static String computeFileMD5(File file) {
        return computeDigest(file, "MD5");
    }

    public static String computeFileSHA(File file) {
        return computeDigest(file, "SHA");
    }


    public static String computeUseableFileSize(File file) {
        return computeReadableFileSize(file) + "(" + file.length() + ")";
    }

    public static String computeReadableFileSize(File file) {
        if (file == null || file.isDirectory()) return "";
        long size = file.length();
        DecimalFormat formater = new DecimalFormat("####.00");
        if (size < 1024) {
            return size + " bytes";
        } else if (size < 1024 * 1024) {
            float kbsize = size / 1024f;
            return formater.format(kbsize) + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            float mbsize = size / 1024f / 1024f;
            return formater.format(mbsize) + " MB";
        } else if (size < 1024 * 1024 * 1024 * 1024) {
            float gbsize = size / 1024f / 1024f / 1024f;
            return formater.format(gbsize) + " GB";
        }
        return "";
    }

    public static String computeReadableTime(long time) {
        return computeTime(time, "HH:mm MM-dd-yyyy");
    }

    public static String computeTime(long time, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date((time)));
    }

    public static String computeUseableTime(long time) {
        return computeTime(time, "yyyy-MM-dd HH:mm:ss");
    }

    public static boolean isTextEmpty(CharSequence text) {
        if (text == null || text.length() == 0 || text.toString().equals("") || text.toString().trim().equals("")) {
            return true;
        } else
            return false;
    }

    public static boolean createFile(File file) {
        try {
            return file != null && file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean createFolder(File file) {
        return file != null && file.mkdirs();
    }

    public static boolean delete(File clickedFile) {
        return clickedFile != null && clickedFile.delete();
    }

    public static String realPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isSymlink(File file) {
        boolean result = false;
        try {
            File canon = null;
            if (file == null) return false;
            if (file.getParent() == null) {
                canon = file;
            } else {
                File canonDir = file.getParentFile().getCanonicalFile();
                canon = new File(canonDir, file.getName());
            }
            result = !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isSymlinkFile(File file) {
        if (isSymlink(file)) {
            try {
                return file.getCanonicalFile().isFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file.isFile();
    }

    public static boolean isSymlinkDirectory(File file) {
        if (isSymlink(file)) {
            try {
                return file.getCanonicalFile().isDirectory();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file.isDirectory();
    }

    public static StringBuilder readFile(String filePath) {
        return readFile(filePath, "UTF-8");
    }

    public static StringBuilder readFile(File file) {
        return readFile(file.getPath(), "UTF-8");
    }

    public static StringBuilder readFile(String filePath, String charsetName) {
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (!file.isFile()) {
            return null;
        }

        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(reader);
        }
        return fileContent;
    }

    public static void close(Closeable... closeable) {
        if (closeable != null)
            try {
                for (Closeable c : closeable) {
                    if (c != null)
                        c.close();
                }
            } catch (Exception ignored) {

            }
    }

    public static void writeFile(File file, String content) {
        writeFile(file.getPath(), content, false);
    }

    public static void writeFile(String filePath, String content, boolean append) {
        if (isTextEmpty(content)) {
            return;
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.close(fileWriter);
        }
    }

    public static boolean splitFilesBySize(String src, long[] sizes, String[] outputNames) throws IOException {
        long countSize = 0;
        if (src != null && sizes.length == outputNames.length) {
            File inputFile = new File(src);
            if (!inputFile.canRead()) return false;
            for (long size : sizes) {
                countSize += size;
            }
            if (countSize != inputFile.length()) return false;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
            BufferedOutputStream bos;
            for (int i = 0; i < outputNames.length; i++) {
                int readCount = 0;
                int onceSize;
                String fn = outputNames[i];
                File outputFile = new File(fn);
                byte[] buf = new byte[BUFFER_SIZE];
                bos = new BufferedOutputStream(new FileOutputStream(outputFile));
                while (readCount < sizes[i]) {
                    onceSize = bis.read(buf);
                    if (onceSize < 0) break;
                    bos.write(buf, 0, onceSize);
                    readCount += onceSize;
                }
                close(bos);
            }
            close(bis);
            return true;

        }
        return false;
    }

    public static String[] splitFileName(String srcFile, int num) {
        String prefix = ".part%d";
        String[] names = new String[num];
        if (srcFile != null) {
            for (int i = 0; i < num; i++) {
                String name = String.format(srcFile + prefix, i);
                names[i] = name;
            }
        }
        return names;
    }

    public static long[] splitLength(long length, int num) {
        long[] sizes = new long[num];
        long usedSize = 0;
        if (length >= num) {
            for (int i = 0; i < num; i++) {
                if (i == num - 1) {
                    sizes[i] = length - usedSize;
                } else {
                    sizes[i] = length / num;
                }
                usedSize += sizes[i];
            }
        }
        return sizes;
    }
}
