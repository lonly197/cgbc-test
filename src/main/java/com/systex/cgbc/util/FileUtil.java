package com.systex.cgbc.util;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;


/**
 * Date: 14-9-30 Time: 上午9:34 To change 文件工具类
 */
public class FileUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);


    /**
     * 创建目录
     *
     * @param dir
     *            目录
     */
    public static boolean mkdir(String dir) {
        try {
            File dirPath = new File(dir);
            if (!dirPath.exists()) {
                return dirPath.mkdir();
            }
        } catch (Exception e) {
            LOG.error("创建目录操作出错: ", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 创建目录及其上级目录
     *
     * @param dirs
     *            目录路径
     */
    public static boolean mkdirs(String dirs) {
        try {
            File dirPath = new File(dirs);
            if (!dirPath.exists()) {
                return dirPath.mkdirs();
            }
        } catch (Exception e) {
            LOG.error("创建目录操作出错: ", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 判断文件是否存在.
     * 
     * @param path
     * @return
     */
    public static boolean isExists(String path) {
        File file = new File(path);
        return file.exists();
    }


    /**
     * 删除文件夹或清空文件夹
     *
     * @param dir_path
     *            文件夹的path
     * @param isDelRootDir
     *            true 删除，false 清空
     */
    public static void deldir(String dir_path, boolean isDelRootDir) {
        File file = new File(dir_path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] childFiles = file.list();
        File temp = null;
        for (int i = 0; i < childFiles.length; i++) {
            // File.separator与系统有关的默认名称分隔符
            // 在UNIX系统上，此字段的值为'/'；在Microsoft Windows系统上，它为 '\'。
            if (dir_path.endsWith(File.separator)) {
                temp = new File(dir_path + childFiles[i]);
            } else {
                temp = new File(dir_path + File.separator + childFiles[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                deldir(dir_path + "/" + childFiles[i], true);// 先删除文件夹里面的文件
                File temp1 = new File(dir_path + "/" + childFiles[i]);
                temp1.delete(); // 删除空文件夹
            }
        }
        if (isDelRootDir) {
            file.delete();
        }
    }


    /**
     * 　 新建单个文件
     *
     * @param filePath
     */
    public static boolean createNewFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return file.createNewFile();
            }
        } catch (Exception e) {
            LOG.error("新建文件操作出错: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 新建文件并写内容
     *
     * @param filePath
     *            包含路径的文件名 如:E:\phsftp\src\123.txt
     * @param content
     *            文件内容
     *
     */
    public static boolean createNewFile(String filePath, String content) {
        PrintWriter pw = null;
        try {
            createNewFile(filePath);
            pw =
                new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath),
                        "utf-8")), false);
            pw.println(content);
            pw.flush();
            pw.close();
            return true;
        } catch (Exception e) {
            LOG.error("新建文件操作出错: " + e.getMessage());
            e.printStackTrace();
        } finally {
            pw.close();
        }
        return false;
    }


    /**
     * 重命名单个文件
     *
     * @param targetfilePath
     *            包含路径的文件名
     */
    public static boolean renameFile(String srcfilePath, String targetfilePath) {
        File srcFile = new File(srcfilePath);
        return srcFile.renameTo(new File(targetfilePath));
    }


    /**
     * 删除单个文件
     *
     * @param filePath
     *            包含路径的文件名
     */
    public static boolean delFile(String filePath) {
        try {
            File delFile = new File(filePath);
            return delFile.delete();
        } catch (Exception e) {
            LOG.error("删除文件操作出错: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 得到单个文件大小,如果文件不存在，则返回－1
     *
     * @param filePath
     */
    public static long getFileSize(String filePath) {
        File f = new File(filePath);
        if (!f.exists()) {
            return -1;
        }
        return f.length();
    }


    /**
     * 复制单个文件
     *
     * @param srcFilePath
     *            包含路径的源文件 如：E:/abc.txt
     * @param destFilePath
     *            目标文件目录；若文件目录不存在则自动创建 如：E:/dest
     * @throws java.io.IOException
     */
    public static void copyFile(String srcFilePath, String destFilePath) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        InputStream in = new FileInputStream(srcFilePath);
        bis = new BufferedInputStream(in);
        OutputStream out = new FileOutputStream(destFilePath, true);
        bos = new BufferedOutputStream(out);
        byte[] buffer = new byte[4 * 1024 * 1024];
        int len = 0;
        while ((len = bis.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        if (bis != null) {
            bis.close();
        }
        if (bos != null) {
            bos.close();
        }
    }


    /**
     * 复制文件夹
     *
     * @param oldPath
     *            String 源文件夹路径 如：E:/src
     * @param newPath
     *            String 目标文件夹路径 如：E:/dest
     */
    public static void copyFolder(String oldPath, String newPath) {
        try {
            mkdir(newPath);
            File file = new File(oldPath);
            String[] files = file.list();
            String tempPath = null;
            File tempFile = null;
            for (int i = 0; i < files.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    tempPath = oldPath + files[i];
                } else {
                    tempPath = oldPath + File.separator + files[i];
                }
                tempFile = new File(tempPath);
                if (tempFile.isFile()) {
                    copyFile(tempPath, newPath + "/" + files[i]);
                }
                if (tempFile.isDirectory()) {// 如果是子文件夹
                    copyFolder(oldPath + "/" + files[i], newPath + "/" + files[i]);
                }
            }
        } catch (Exception e) {
            LOG.error("复制文件夹操作出错:" + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 读取文件内容
     *
     * @param filePath
     *            文件路径
     */
    public static String readFile(String filePath) {
        StringBuffer sb = new StringBuffer();
        InputStream in = null;
        BufferedInputStream bis = null;
        try {
            in = new FileInputStream(filePath);
            bis = new BufferedInputStream(in);
            // System.out.println("当前字节输入流中的字节数为:" + in.available());
            int byteread = 0;
            byte[] tempbytes = new byte[4 * 1024];
            while ((byteread = bis.read(tempbytes)) != -1) {
                sb.append(new String(tempbytes, 0, byteread));
            }
            if (bis != null) {
                bis.close();
            }
        } catch (Exception e1) {
            LOG.error("读取文件失败:" + e1.getMessage());
            e1.printStackTrace();
        }
        return sb.toString();
    }


    /**
     * 以行为单位读取文件
     * 
     * @param filePath
     * @return
     */
    public static List<String> readFileByLines(String filePath) {
        return readFileByLines(filePath, -1, -1);
    }


    /**
     * 取文件行数
     * 
     * @param filePath
     * @return
     */
    public static int getFileLineCount(String filePath) {
        return readFileByLines(filePath).size();
    }


    /**
     * 按行号为单位按行读取文件，行号以1开始，-1代表无限小或无限大
     *
     * @param filePath
     *            文件路径
     * @param beginIndex
     *            开始行号
     * @param endIndex
     *            结束行号
     * @return
     */
    public static List<String> readFileByLines(String filePath, int beginIndex, int endIndex) {
        List<String> list = new ArrayList<String>();
        BufferedReader br = null;
        try {
            LOG.info("以行为单位读取文件内容，一次读一整行：");
            br = new BufferedReader(new FileReader(filePath));
            String tempString = null;
            int lineindex = 0;
            if (endIndex == -1) {
                while ((tempString = br.readLine()) != null) {
                    lineindex++;
                    if (lineindex >= beginIndex)
                        list.add(tempString);
                }
            } else {
                while ((tempString = br.readLine()) != null) {
                    lineindex++;
                    if ((lineindex >= beginIndex) && (lineindex <= endIndex))
                        list.add(tempString);
                }
            }
            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
            LOG.error("读取文件失败:" + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }


    /**
     * 以行为单位读取文件
     * 
     * @param filePath
     * @return
     */
    public static List<String> readFileByLinesNoDup(String filePath) {
        return readFileByLinesNoDup(filePath, -1, -1);
    }


    /**
     * 按行号为单位按行读取文件，行号以1开始，-1代表无限小或无限大
     *
     * @param filePath
     *            文件路径
     * @param beginIndex
     *            开始行号
     * @param endIndex
     *            结束行号
     * @return
     */
    public static List<String> readFileByLinesNoDup(String filePath, int beginIndex, int endIndex) {
        Set<String> set = new HashSet<String>();
        BufferedReader br = null;
        try {
            LOG.info("以行为单位读取文件内容，一次读一整行：");
            br = new BufferedReader(new FileReader(filePath));
            String tempString = null;

            int lineindex = 0;
            if (endIndex == -1) {
                while ((tempString = br.readLine()) != null) {
                    lineindex++;
                    if (lineindex >= beginIndex)
                        set.add(tempString);
                }
            } else {
                while ((tempString = br.readLine()) != null) {
                    lineindex++;
                    if ((lineindex >= beginIndex) && (lineindex <= endIndex))
                        set.add(tempString);
                }
            }

            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
            LOG.error("读取文件失败:" + e.getMessage());
            e.printStackTrace();
        }
        List<String> list = new ArrayList<String>(set.size());
        list.addAll(set);
        return list;
    }


    /**
     * 写文件
     *
     * @param content
     *            写入内容
     * @param filePath
     *            写入的文件
     * @param append
     *            是否追加到文件
     * @throws Exception
     */
    public static void writeFile(String filePath, String content, boolean append) {
        PrintWriter pw;
        try {
            pw =
                new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath,
                        append), "utf-8")), false);
            pw.write(content);
            pw.flush();
            if (pw != null) {
                pw.close();
            }
        } catch (IOException e) {
            LOG.error("写文件失败:" + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 文件名是否采用日期格式
     * 
     * @param filePath
     * @param fileName
     * @param fileName
     * @param list
     * @param append
     */
    public static void writeFileByLines(String filePath, String fileName, List<String> list,
        boolean append) {
        writeFileByLines(filePath + File.separator + fileName, list, append);
    }


    /**
     * 按行写文件List
     *
     * @param filePath
     *            文件路径
     * @param list
     *            写入内容的String 列表
     * @param append
     *            是否追加到文件
     */
    public static void writeFileByLines(String filePath, List<String> list, boolean append) {
        PrintWriter pw = null;
        try {
            pw =
                new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath,
                        append), "utf-8")), false);
            Iterator<String> it = list.iterator();
            int index = 0;
            while (it.hasNext()) {
                String str = it.next();
                pw.println(str);
                index++;
                if (index % 5000 == 0) {
                    pw.flush();
                }
            }
            pw.flush();
            if (pw != null) {
                pw.close();
            }
        } catch (Exception e) {
            LOG.error("写文件异常:" + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 创建文件,如果指定的路径不存在,则创建
     */
    public static void createFile(String path) {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            LOG.error("创建文件操作出错:" + e.getMessage());
        }
    }


    /**
     * 写对象到文件
     * 
     * @param obj
     * @param filePath
     */
    public static void writeObject(Object obj, String filePath) {
        try {
            File f = new File(filePath);
            if (f.exists()) {
                f.delete();
            }
            ObjectOutputStream out =
                new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)));
            out.writeObject(obj);
            out.close();
        } catch (FileNotFoundException e) {
            LOG.error("写对象到文件出错:" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("写对象到文件出错:" + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 从文件读对象
     * 
     * @param filePath
     * @return
     */
    public static Object readObject(String filePath) {
        File f = new File(filePath);
        if (!f.exists()) {
            return null;
        }
        Object obj = null;
        try {
            ObjectInputStream in =
                new ObjectInputStream(new BufferedInputStream(new FileInputStream(filePath)));
            obj = in.readObject();
            in.close();
        } catch (ClassNotFoundException e) {
            LOG.error("从文件读对象出错:" + e.getMessage());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            LOG.error("从文件读对象出错:" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("从文件读对象出错:" + e.getMessage());
            e.printStackTrace();
        }
        return obj;
    }


    /**
     * 根据当前日期产生文件名
     * 
     * @param prefix
     * @return
     */
    public static String genFileNameWithDate(String prefix) {
        String date = DateUtil.parseToString(new Date(), "yyyy-MM-dd");
        return prefix + date + ".log";
    }


    /**
     * 根据文件路径获取输出流
     * 
     * @param path
     * @return
     */
    public static PrintWriter getPrintWriter(String path) {
        return getPrintWriter(path, "UTF-8");
    }


    /**
     * 根据文件路径获取输出流
     * 
     * @param path
     * @param charset
     * @return
     */
    public static PrintWriter getPrintWriter(String path, String charset) {
        PrintWriter pw = null;
        try {
            pw =
                new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path),
                        charset)));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOG.error("{}创建输出流失败！{}", path, e.getMessage());
        }
        return pw;
    }


    /**
     * 根据文件路径获取输入流
     * 
     * @param path
     * @return
     */
    public static BufferedReader getBufferedReader(String path) {
        return getBufferedReader(path, "UTF-8");
    }


    /**
     * 根据文件路径获取输入流
     * 
     * @param path
     * @param charset
     * @return
     */
    public static BufferedReader getBufferedReader(String path, String charset) {
        BufferedReader br = null;
        try {
            br =
                new BufferedReader(
                    new BufferedReader(new InputStreamReader(new FileInputStream(path),
                        charset)));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOG.error("{}创建输入流失败！{}", path, e.getMessage());
        }
        return br;
    }


    /**
     * 获取文件后缀
     * 
     * @param fileName
     * @return
     */
    public static String getFileType(String fileName) {
        int idx = fileName.indexOf(".");
        return idx > 0 ? fileName.substring(idx) : "";
    }


    /**
     * 更改名字
     * 
     * @param fileName
     * @return
     */
    public static String rename(String fileName, String newName) {
        int idx = fileName.indexOf(".");
        if (idx > 0) {
            String[] namePart = fileName.split("\\.");
            return newName + "." + namePart[1];
        }
        return newName;
    }


    public static void main(String args[]) {
        List<String> list = Lists.newArrayList();
        list.add("aa");
        list.add("bb");
        FileUtil.writeFileByLines("D:", "aop", list, true);
    }
}
