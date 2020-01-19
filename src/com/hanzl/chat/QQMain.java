package com.hanzl.chat;

/*
 * QQMain，主界面
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

public class QQMain extends JFrame implements ActionListener, Runnable, WindowListener {
    private Socket s;

    public void setSocket(Socket value) {
        s = value;
        // 启动接收线程
        Thread t = new Thread(this);
        t.start();
    }

    JTextField txtMess = new JTextField();
    JComboBox cmbUser = new JComboBox();
    JTextArea txtContent = new JTextArea();

    QQMain() {
        this.setSize(300, 400);

        // new组件
        JButton btnSend = new JButton("发送");

        txtContent.setEditable(false);// 设置消息历史不可编辑
        JScrollPane spContent = new JScrollPane(txtContent);

        // 注册事件监听
        btnSend.addActionListener(this);
        this.addWindowListener(this);

        // 布置小面板
        JPanel panSmall = new JPanel();
        panSmall.setLayout(new GridLayout(1, 2));

        panSmall.add(cmbUser);
        panSmall.add(btnSend);

        // 布置大面板
        JPanel panBig = new JPanel();
        panBig.setLayout(new GridLayout(2, 1));

        panBig.add(txtMess);
        panBig.add(panSmall);

        // 布置窗体
        this.setLayout(new BorderLayout());

        this.add(panBig, BorderLayout.NORTH);
        this.add(spContent, BorderLayout.CENTER);

//		// 读聊天记录
//		try {
//			File f = new File("D:/SourceCode/JavaDemo/j2se/src/chat/History.log");
//
//			FileReader fr = new FileReader(f);
//			BufferedReader br = new BufferedReader(fr);
//
//			while (br.ready()) {
//				txtContent.append(br.readLine() + "\n");
//			}
//		} catch (Exception e) {
//		}

    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        // 非法输入内容拦截
        if(cmbUser.getSelectedItem() == null) {
            return;
        }
        if(txtMess.getText().equals("")) {
            return;
        }

        // txtMess -------> txtContent
        txtContent.append("[我 -> " + cmbUser.getSelectedItem() + "]:" + txtMess.getText() + "\n");

        // 将txtMess的内容存入聊天记录文件
        try {
            File f = new File("D:/SourceCode/IdeaProjects/chat/src/com/hanzl/chat/History.log");

            FileWriter fw = new FileWriter(f, true);// 第二个参数设置追加写文件
            PrintWriter pw = new PrintWriter(fw);

            pw.println("[我 -> " + cmbUser.getSelectedItem() + "]:" + txtMess.getText());

            pw.close();// close会调用flush方法
        } catch (Exception e) {
        }

        // 发送信息到服务器
        try {
            OutputStream os = s.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            PrintWriter pw = new PrintWriter(osw, true);

            pw.println(cmbUser.getSelectedItem() + "%" + txtMess.getText());
        } catch (Exception e) {
        }

        // 清除txtMess中的内容
        txtMess.setText("");
    }

    // 接收线程
    public void run() {
        try {
            InputStream is = s.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            while (true) {
                String message = br.readLine();
                String type = message.split("%")[0];
                String user = message.split("%")[1];
                String mess = "";

                if (type.equals("add1")) {
                    cmbUser.addItem(user);
                    txtContent.append("[系统提示]:" + user + "上线啦。\n");
                }
                if (type.equals("add2")) {
                    cmbUser.addItem(user);
                }
                if (type.equals("exit")) {
                    cmbUser.removeItem(user);
                    txtContent.append("[系统提示]:" + user + "已下线。\n");
                }
                if (type.equals("mess")) {
                    mess = message.split("%")[2];
                    txtContent.append("[" + user + " -> 我]:" + mess + "\n");
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void windowActivated(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowClosed(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        try {
            OutputStream os = s.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            PrintWriter pw = new PrintWriter(osw, true);

            pw.println("{exit}");

            // 正常退出
            System.exit(0);
        } catch (Exception e) {
        }
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowDeiconified(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowIconified(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowOpened(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }
}
