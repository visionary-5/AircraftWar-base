package edu.hitsz.application;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import edu.hitsz.dao.ScoreDao;
import edu.hitsz.dao.ScoreDaoImpl;
import edu.hitsz.dto.ScoreRecord;
import java.util.List;

public class Scoreboard extends JFrame {
    private JPanel MainPanel;
    private JLabel label_difficulty;
    private JLabel lebel_list;
    private JPanel TablePanel;
    private JTable table1;
    private JScrollPane ScorllPane;
    private JButton delete_Button;

    private DefaultTableModel tableModel;
    private String difficulty;
    private ScoreDao scoreDao;

    public Scoreboard(String difficulty) {
        this.difficulty = difficulty;
        this.scoreDao = new ScoreDaoImpl();

        // 初始化表格
        initTable();

        // 设置窗口
        setTitle("Aircraft War - 排行榜");
        setContentPane(MainPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 600);
        setLocationRelativeTo(null);

        // 更新难度标签
        label_difficulty.setText("难度：" + difficulty);

        // 添加删除按钮监听器
        delete_Button.addActionListener(e -> deleteSelectedRecord());

        // 加载排行榜数据
        loadScoreboard();
    }

    private void initTable() {
        // 创建表格模型
        String[] columnNames = {"名次", "玩家名", "得分", "记录时间"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 不可编辑
            }
        };

        table1.setModel(tableModel);

        // 设置列宽
        table1.getColumnModel().getColumn(0).setPreferredWidth(40);
        table1.getColumnModel().getColumn(1).setPreferredWidth(80);
        table1.getColumnModel().getColumn(2).setPreferredWidth(60);
        table1.getColumnModel().getColumn(3).setPreferredWidth(120);

        // 设置表格样式
        table1.setRowHeight(25);
        table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void loadScoreboard() {
        // 清空表格
        tableModel.setRowCount(0);

        // 从数据库加载数据
        List<ScoreRecord> scores = scoreDao.getAllScores(difficulty);

        // 添加到表格
        for (ScoreRecord record : scores) {
            tableModel.addRow(new Object[]{
                record.getRank(),
                record.getPlayerName(),
                record.getScore(),
                record.getRecordTime()
            });
        }
    }

    public void addScore(int score) {
        // 显示输入对话框
        String playerName = JOptionPane.showInputDialog(
            this,
            "恭喜你，你的得分为 " + score + "。\n请输入名字以记录分：",
            "输入",
            JOptionPane.QUESTION_MESSAGE
        );

        if (playerName == null || playerName.trim().isEmpty()) {
            return; // 用户取消或输入为空
        }

        // 添加到数据库
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        String time = sdf.format(new Date());
        ScoreRecord newRecord = new ScoreRecord(playerName, score, time);
        scoreDao.insertScore(newRecord, difficulty);

        // 重新加载排行榜
        loadScoreboard();
    }

    private void deleteSelectedRecord() {
        int selectedRow = table1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "请选择一个要删除的记录",
                "提示",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String playerName = (String) tableModel.getValueAt(selectedRow, 1);
        int score = (Integer) tableModel.getValueAt(selectedRow, 2);
        String time = (String) tableModel.getValueAt(selectedRow, 3);

        // 显示确认对话框
        String[] options = {"是(Y)", "否(N)", "取消"};
        int result = JOptionPane.showOptionDialog(
            this,
            "是否确定删除选中的记录？",
            "选择一项操作",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        if (result == JOptionPane.YES_OPTION) {
            // 从数据库删除 - 使用精确匹配的方法，只删除选中的这一条记录
            if (scoreDao instanceof ScoreDaoImpl) {
                ((ScoreDaoImpl) scoreDao).deleteScoreByDetails(playerName, score, time, difficulty);
            }

            // 重新加载排行榜
            loadScoreboard();

            JOptionPane.showMessageDialog(
                this,
                "删除成功！",
                "提示",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void updateRanks() {
        // 更新名次显示
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(i + 1, i, 0);
        }
    }
}
