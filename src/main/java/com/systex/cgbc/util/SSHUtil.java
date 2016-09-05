package com.systex.cgbc.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.*;

public class SSHUtil {

    public static void main(String[] args) {
        String hostName = "10.201.26.114";
        String userName = "spark";
        String password = "systex_spark";

        String commond =
            "cd /home/spark/jiangboding/KMeans && sh run.sh start '1' '6' '1' '/tmp/test' 'http://10.201.26.114:8089/systex/jobService/result' '{\"tbTAppResults\":[{\"appId\":1,\"descInfo\":\"聚类结果表\",\"name\":\"聚类结果表\",\"position\":\"jdbc:mysql://10.201.26.111:3306/ding?user=root&password=root&useUnicode=true&characterEncoding=UTF-8\",\"resultCode\":\"1\",\"resultId\":1,\"storetype\":3},{\"appId\":1,\"descInfo\":\"知乎问题表\",\"name\":\"知乎问题表\",\"position\":\"zhihu#question\",\"resultCode\":\"2\",\"resultId\":2,\"storetype\":2},{\"appId\":1,\"descInfo\":\"关键词及对应问题表\",\"name\":\"关键词及对应问题表\",\"position\":\"zhihu#kwrelques\",\"resultCode\":\"3\",\"resultId\":3,\"storetype\":2}]}' 'k=4;taggers=NN,NR,NT,NP;extractNum=20;topN=20'";
        try {
            String s = exec(hostName, userName, password, commond);
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行命令 ，多个命令用 && 分割
     *
     * @throws Exception
     */
    public static String exec(String hostName, String userName,
        String password, String command) throws Exception {
        StringBuffer sb = new StringBuffer();
        Session session = getSession(hostName, userName, password);
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        channel.setInputStream(null);
        ((ChannelExec) channel).setErrStream(System.err);
        InputStream in = channel.getInputStream();
        channel.connect();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                sb.append(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        channel.disconnect();
        session.disconnect();
        return sb.toString();
    }

    /**
     * 从远程下载文件到本地
     *
     * @throws Exception
     */
    public static void scpFrom(String hostName, String userName,
        String password, String remoteFile, String localFile)
        throws Exception {
        Session session = getSession(hostName, userName, password);

        String prefix = null;
        if (new File(localFile).isDirectory()) {
            prefix = localFile + File.separator;
        }
        FileOutputStream fos = null;
        // exec 'scp -f rfile' remotely
        String command = "scp -f " + remoteFile;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        byte[] buf = new byte[1024];

        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        while (true) {
            int c = checkAck(in);
            if (c != 'C') {
                break;
            }

            // read '0644 '
            in.read(buf, 0, 5);

            long filesize = 0L;
            while (true) {
                if (in.read(buf, 0, 1) < 0) {
                    // error
                    break;
                }
                if (buf[0] == ' ')
                    break;
                filesize = filesize * 10L + (long) (buf[0] - '0');
            }

            String file = null;
            for (int i = 0; ; i++) {
                in.read(buf, i, 1);
                if (buf[i] == (byte) 0x0a) {
                    file = new String(buf, 0, i);
                    break;
                }
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            // read a content of lfile
            fos = new FileOutputStream(prefix == null ? localFile : prefix
                + file);
            int foo;
            while (true) {
                if (buf.length < filesize)
                    foo = buf.length;
                else
                    foo = (int) filesize;
                foo = in.read(buf, 0, foo);
                if (foo < 0) {
                    // error
                    break;
                }
                fos.write(buf, 0, foo);
                filesize -= foo;
                if (filesize == 0L)
                    break;
            }
            fos.close();
            fos = null;

            if (checkAck(in) != 0) {
                System.exit(0);
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
        }

        channel.disconnect();
        session.disconnect();

        System.out.println("==下载成功==");
    }

    /**
     * 上传本地文件到远程
     *
     * @param hostName
     * @param userName
     * @param password
     * @param remoteFile
     * @param localFile
     * @throws Exception
     */
    public static void scpTo(String hostName, String userName, String password,
        String remoteFile, String localFile) throws Exception {
        Session session = getSession(hostName, userName, password);
        FileInputStream fis = null;
        boolean ptimestamp = true;
        String command = "scp " + (ptimestamp ? "-p" : "") + " -t "
            + remoteFile;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        if (checkAck(in) != 0) {
            System.exit(0);
        }

        File _lfile = new File(localFile);

        if (ptimestamp) {
            command = "T " + (_lfile.lastModified() / 1000) + " 0";
            command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                System.exit(0);
            }
        }

        long filesize = _lfile.length();
        command = "C0644 " + filesize + " ";
        if (localFile.lastIndexOf('/') > 0) {
            command += localFile.substring(localFile.lastIndexOf('/') + 1);
        } else {
            command += localFile;
        }
        command += "\n";
        out.write(command.getBytes());
        out.flush();
        if (checkAck(in) != 0) {
            System.exit(0);
        }

        fis = new FileInputStream(localFile);
        byte[] buf = new byte[1024];
        while (true) {
            int len = fis.read(buf, 0, buf.length);
            if (len <= 0)
                break;
            out.write(buf, 0, len); // out.flush();
        }
        fis.close();
        fis = null;
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();
        if (checkAck(in) != 0) {
            System.exit(0);
        }
        out.close();

        channel.disconnect();
        session.disconnect();

    }

    private static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        if (b == 0)
            return b;
        if (b == -1)
            return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }

    public static Session getSession(String hostName, String userName,
        String password) throws Exception {
        JSch jsch = new JSch();
        Session session = null;
        session = jsch.getSession(userName, hostName, 22);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword(password);
        session.connect();
        return session;
    }
}
