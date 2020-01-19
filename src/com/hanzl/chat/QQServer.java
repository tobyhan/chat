package com.hanzl.chat;

/*
 * QQServer，服务器端代码
 */
import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class QQServer {
    public static void main(String[] args) {
        try {
            // 声明存放在线用户列表的Socket的集合
            HashMap<String, Socket> hm = new HashMap<String, Socket>();

            // 服务器在8000端口监听
            ServerSocket ss = new ServerSocket(8000);

//			System.out.println("每一个你不满意的现在，都有一个你没有努力的曾经。");
            while (true) {
                System.out.println("服务器正在8000端口监听......");
                Socket s = ss.accept();

                MyService t = new MyService();
                t.setSocket(s);
                // 将HashMap的引用传入服务线程
                t.setHashMap(hm);
                t.start();
            }
        } catch (Exception e) {
        }
    }
}

class MyService extends Thread {
    private Socket s;

    // 接收HashMap的引用
    private HashMap<String, Socket> hm;

    public void setHashMap(HashMap hm) {
        this.hm = hm;
    }

    public void setSocket(Socket s) {
        this.s = s;
    }

    public void run() {
        try {
            // 接受用户名和密码
            InputStream is = s.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String uandp = br.readLine();

            // 拆分用户名和密码
            String u = uandp.split("%")[0];
            String p = uandp.split("%")[1];

            OutputStream os = s.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            PrintWriter pw = new PrintWriter(osw, true);

            // 防止同一用户重复登录
            if(hm.containsKey(u)) {
                pw.println("REPEAT");
                return;
            }

            // 到数据库中验证用户身份
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection cn = DriverManager
                    .getConnection("jdbc:mysql://127.0.0.1:3306/qq?useSSL=false&characterEncoding=utf8", "root", null);
            PreparedStatement ps = cn.prepareStatement("select * from user where username=? and password=?");
            ps.setString(1, u);
            ps.setString(2, p);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // 发送正确信息到客户端
                pw.println("OK");

                // 将本人的名字发送给其他的用户
                for (Socket ts : hm.values()) {
                    OutputStream tos = ts.getOutputStream();
                    OutputStreamWriter tosw = new OutputStreamWriter(tos);
                    PrintWriter tpw = new PrintWriter(tosw, true);

                    tpw.println("add1%" + u);
                }

                // 将其他人的名字发送给自己
                for (String tu : hm.keySet()) {
                    pw.println("add2%" + tu);
                }

                // 将本人的用户名和Socket存入HashMap
                hm.put(u, s);

                // 不断地接收客户端发送过来的信息
                while (true) {
                    String message = br.readLine();

                    // 监听到客户端关闭window事件
                    if (message.equals("{exit}")) {
                        // 将该用户从HashMap中删除掉
                        hm.remove(u);
                        // 通知所有的人，本用户退出
                        for (Socket ts : hm.values()) {
                            OutputStream tos = ts.getOutputStream();
                            OutputStreamWriter tosw = new OutputStreamWriter(tos);
                            PrintWriter tpw = new PrintWriter(tosw, true);

                            tpw.println("exit%" + u);
                        }
                        // 打印最新的在线用户列表
                        System.out.println("-------- 最新在线用户列表 开始 --------");
                        for (String tu : hm.keySet()) {
                            System.out.println(tu);
                        }
                        return;
                    }

                    // 转发信息
                    String to = message.split("%")[0];
                    String mess = message.split("%")[1];
                    Socket ts = hm.get(to);
                    OutputStream tos = ts.getOutputStream();
                    OutputStreamWriter tosw = new OutputStreamWriter(tos);
                    PrintWriter tpw = new PrintWriter(tosw, true);

                    tpw.println("mess%" + u + "%" + mess);
                }
            } else {
                // 发送错误信息到客户端
                pw.println("ERROR");
            }
        } catch (Exception e) {
        }
    }
}
