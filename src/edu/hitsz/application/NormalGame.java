package edu.hitsz.application;

import edu.hitsz.aircraft.*;

/**
 * 普通模式游戏
 * 特点：
 * - 有Boss敌机（每次血量不变500）
 * - 敌机数量中等（5个，可提升至7个）
 * - 敌机血量和速度中等
 * - 英雄机射击周期中等（300ms）
 * - 敌机产生周期中等（600ms）
 * - 精英敌机概率中等（30%，可提升至50%）
 * - Boss出现阈值：600分
 * - 难度随时间增加（每15秒提升一次）
 */
public class NormalGame extends AbstractGame {

    private static final int DIFFICULTY_INCREASE_INTERVAL = 15000;  // 15秒
    private int lastDifficultyIncreaseTime = 0;
    private int bossAppearCount = 0;

    public NormalGame(String difficulty, boolean soundEnabled) {
        super(difficulty, soundEnabled);
    }

    @Override
    protected void initGameParameters() {
        // 普通模式参数设置
        this.enemyMaxNumber = 5;               // 敌机最大数量：5个
        this.cycleDuration = 600;              // 敌机产生周期：600ms
        this.heroShootPeriod = 300;            // 英雄射击周期：300ms
        this.eliteProbability = 0.3;           // 精英敌机概率：30%
        this.mobEnemyHp = 30;                  // 普通敌机血量：30
        this.eliteEnemyHp = 80;                // 精英敌机血量：80
        this.mobEnemySpeed = 10;               // 普通敌机速度：10
        this.eliteEnemySpeed = 8;              // 精英敌机速度：8
        this.bossScoreThreshold = 600;         // Boss出现阈值：600分
        this.initialBossHp = 500;              // Boss初始血量：500
    }

    @Override
    protected void generateEnemy() {
        // 随机产生普通敌机或精英敌机
        if (Math.random() < (1 - eliteProbability)) {
            // 70%概率产生普通敌机
            int mobEnemyWidth = ImageManager.MOB_ENEMY_IMAGE.getWidth();
            int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - mobEnemyWidth)) + mobEnemyWidth / 2;
            enemyAircrafts.add(mobEnemyFactory.createEnemy(
                    locationX,
                    (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                    0,
                    mobEnemySpeed,
                    mobEnemyHp
            ));
        } else {
            // 30%概率产生精英敌机
            int eliteEnemyWidth = ImageManager.ELITE_ENEMY_IMAGE.getWidth();
            int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - eliteEnemyWidth));
            enemyAircrafts.add(eliteEnemyFactory.createEnemy(
                    locationX,
                    (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                    (int) (Math.random() * 4 - 2),
                    eliteEnemySpeed,
                    eliteEnemyHp
            ));
        }
    }

    @Override
    protected void generateBoss() {
        System.out.println("===== Boss敌机出现！=====");
        bossAppearCount++;
        System.out.println("这是第 " + bossAppearCount + " 次Boss出现");
        System.out.println("Boss血量倍率: 1.00");

        if (soundEnabled) {
            if (bgmThread != null) {
                bgmThread.stopMusic();
            }
            bossBgmThread = new MusicThread("src/videos/bgm_boss.wav", true);
            bossBgmThread.start();
        }

        int locationX = Main.WINDOW_WIDTH / 2;
        int locationY = ImageManager.BOSS_ENEMY_IMAGE.getHeight();
        int speedX = 5;
        int speedY = 0;
        int hp = getBossHp();

        enemyAircrafts.add(bossEnemyFactory.createEnemy(locationX, locationY, speedX, speedY, hp));
        bossExists = true;
        lastBossScore = score;

        System.out.println("Boss血量: " + hp);
        System.out.println("========================");
    }

    @Override
    protected boolean shouldIncreaseDifficulty() {
        // 每15秒提升一次难度
        return (time - lastDifficultyIncreaseTime) >= DIFFICULTY_INCREASE_INTERVAL;
    }

    @Override
    protected void increaseDifficulty() {
        lastDifficultyIncreaseTime = time;

        System.out.println();
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("⚠️⚠️⚠️  难度提升！难度提升！难度提升！  ⚠️⚠️⚠️");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("【游戏时长】 " + (time / 1000) + "秒");
        System.out.println("------------------------------------------------------------");

        // 提升敌机血量（+10%）
        double oldMobHp = mobEnemyHp;
        double oldEliteHp = eliteEnemyHp;
        mobEnemyHp = (int) (mobEnemyHp * 1.10);
        eliteEnemyHp = (int) (eliteEnemyHp * 1.10);
        System.out.println("【血量提升】");
        System.out.println("  ▶ 普通敌机血量: " + String.format("%.0f", oldMobHp) + " → " + mobEnemyHp +
                         " (×" + String.format("%.2f", mobEnemyHp / oldMobHp) + ")");
        System.out.println("  ▶ 精英敌机血量: " + String.format("%.0f", oldEliteHp) + " → " + eliteEnemyHp +
                         " (×" + String.format("%.2f", eliteEnemyHp / oldEliteHp) + ")");

        // 提升敌机速度（+8%）
        double oldMobSpeed = mobEnemySpeed;
        double oldEliteSpeed = eliteEnemySpeed;
        mobEnemySpeed = (int) (mobEnemySpeed * 1.08);
        eliteEnemySpeed = (int) (eliteEnemySpeed * 1.08);
        System.out.println("【速度提升】");
        System.out.println("  ▶ 普通敌机速度: " + String.format("%.0f", oldMobSpeed) + " → " + mobEnemySpeed +
                         " (×" + String.format("%.2f", mobEnemySpeed / oldMobSpeed) + ")");
        System.out.println("  ▶ 精英敌机速度: " + String.format("%.0f", oldEliteSpeed) + " → " + eliteEnemySpeed +
                         " (×" + String.format("%.2f", eliteEnemySpeed / oldEliteSpeed) + ")");

        // 缩短敌机产生周期（-8%，最小400ms）
        if (cycleDuration > 400) {
            double oldCycle = cycleDuration;
            cycleDuration = Math.max(400, (int) (cycleDuration * 0.92));
            System.out.println("【刷新加速】");
            System.out.println("  ▶ 敌机产生周期: " + String.format("%.0f", oldCycle) + "ms → " + cycleDuration + "ms" +
                             " (×" + String.format("%.2f", cycleDuration / oldCycle) + ")");
        }

        // 增加精英敌机概率（+3%，最高50%）
        if (eliteProbability < 0.5) {
            double oldProb = eliteProbability;
            eliteProbability = Math.min(0.5, eliteProbability + 0.03);
            System.out.println("【精英增多】");
            System.out.println("  ▶ 精英敌机概率: " + String.format("%.0f%%", oldProb * 100) +
                             " → " + String.format("%.0f%%", eliteProbability * 100));
        }

        // 增加敌机最大数量（最多7个）
        if (enemyMaxNumber < 7) {
            int oldMaxNumber = enemyMaxNumber;
            enemyMaxNumber = Math.min(7, enemyMaxNumber + 1);
            System.out.println("【数量增加】");
            System.out.println("  ▶ 敌机最大数量: " + oldMaxNumber + " → " + enemyMaxNumber);
        }

        System.out.println("════════════════════════════════════════════════════════");
        System.out.println();
    }

    @Override
    protected int getBossHp() {
        // 普通模式Boss血量不变
        return initialBossHp;
    }
}

