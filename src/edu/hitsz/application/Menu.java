package edu.hitsz.application;

import javax.swing.*;

public class Menu extends JFrame {
    private JPanel ButtonPanel;
    private JPanel ComboBoxPanel;
    private JButton EasyButton;
    private JButton NormalButton;
    private JButton HardButton;
    private JComboBox<String> comboBox1;
    private JLabel music;
    private JPanel MainPanel;

    private boolean soundEnabled = true;

    public Menu() {
        // 设置窗口
        setTitle("Aircraft War");
        setContentPane(MainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // 添加按钮监听器
        EasyButton.addActionListener(e -> startGame("EASY"));
        NormalButton.addActionListener(e -> startGame("NORMAL"));
        HardButton.addActionListener(e -> startGame("HARD"));

        // 添加音效开关监听器
        comboBox1.addActionListener(e -> {
            String selected = (String) comboBox1.getSelectedItem();
            soundEnabled = "开".equals(selected);
            System.out.println("音效设置：" + (soundEnabled ? "开启" : "关闭"));
        });
    }

    private void startGame(String difficulty) {
        System.out.println("开始游戏，难度：" + difficulty + "，音效：" + (soundEnabled ? "开" : "关"));

        // 关闭菜单窗口
        this.dispose();

        // 创建游戏窗口
        JFrame gameFrame = new JFrame("Aircraft War - " + difficulty);
        gameFrame.setSize(512, 768);
        gameFrame.setResizable(false);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 创建游戏实例
        Game game = new Game(difficulty, soundEnabled);
        gameFrame.add(game);
        gameFrame.setVisible(true);

        // 启动游戏
        game.action();
    }

    private void createUIComponents() {
        // 手动创建按钮组件
        EasyButton = new JButton("简单模式");
        NormalButton = new JButton("正常模式");
        HardButton = new JButton("困难模式");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Menu menu = new Menu();
            menu.setVisible(true);
        });
    }
}
