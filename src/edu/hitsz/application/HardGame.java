package edu.hitsz.application;

import edu.hitsz.aircraft.*;

/**
 * 困难模式游戏
 * 特点：
 * - 有Boss敌机（每次血量递增）
 * - 敌机数量多（7个，可提升至10个）
 * - 敌机血量和速度较高
 * - 英雄机射击周期长（200ms）
 * - 敌机产生周期短（400ms）
 * - 精英敌机概率高（40%，可提升至70%）
 * - Boss出现阈值：500分
 * - Boss初始血量：600
 * - Boss血量递增：每次+150
 * - 难度随时间增加（每10秒提升一次）
 */
public class HardGame extends AbstractGame {

    private static final int DIFFICULTY_INCREASE_INTERVAL = 10000;  // 10秒
    private int lastDifficultyIncreaseTime = 0;
    private int bossAppearCount = 0;
    private static final int BOSS_HP_INCREMENT = 150;

    public HardGame(String difficulty, boolean soundEnabled) {
        super(difficulty, soundEnabled);
    }

    @Override
    protected void initGameParameters() {
        // 困难模式参数设置
        this.enemyMaxNumber = 7;               // 敌机最大数量：7个
        this.cycleDuration = 400;              // 敌机产生周期：400ms（很快）
        this.heroShootPeriod = 200;            // 英雄射击周期：200ms（较慢）
        this.eliteProbability = 0.4;           // 精英敌机概率：40%
        this.mobEnemyHp = 40;                  // 普通敌机血量：40
        this.eliteEnemyHp = 100;               // 精英敌机血量：100
        this.mobEnemySpeed = 12;               // 普通敌机速度：12
        this.eliteEnemySpeed = 10;             // 精英敌机速度：10
        this.bossScoreThreshold = 500;         // Boss出现阈值：500分
        this.initialBossHp = 600;              // Boss初始血量：600
    }

    @Override
    protected void generateEnemy() {
        // 随机产生普通敌机或精英敌机
        if (Math.random() < (1 - eliteProbability)) {
            // 60%概率产生普通敌机
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
            // 40%概率产生精英敌机
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

        int hp = getBossHp();
        double hpMultiplier = (double) hp / initialBossHp;
        System.out.println("Boss血量倍率: " + String.format("%.2f", hpMultiplier));

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

        enemyAircrafts.add(bossEnemyFactory.createEnemy(locationX, locationY, speedX, speedY, hp));
        bossExists = true;
        lastBossScore = score;

        System.out.println("Boss血量: " + hp);
        System.out.println("========================");
    }

    @Override
    protected boolean shouldIncreaseDifficulty() {
        // 每10秒提升一次难度
        return (time - lastDifficultyIncreaseTime) >= DIFFICULTY_INCREASE_INTERVAL;
    }

    @Override
    protected void increaseDifficulty() {
        lastDifficultyIncreaseTime = time;

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  🔥🔥🔥  难度大幅提升！准备迎接挑战！  🔥🔥🔥  ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("【游戏时长】 " + (time / 1000) + "秒");
        System.out.println("------------------------------------------------------------");

        // 提升敌机血量（+15%）
        double oldMobHp = mobEnemyHp;
        double oldEliteHp = eliteEnemyHp;
        mobEnemyHp = (int) (mobEnemyHp * 1.15);
        eliteEnemyHp = (int) (eliteEnemyHp * 1.15);
        System.out.println("【血量大幅提升】");
        System.out.println("  ▶ 普通敌机血量: " + String.format("%.0f", oldMobHp) + " → " + mobEnemyHp +
                         " (×" + String.format("%.2f", mobEnemyHp / oldMobHp) + ")");
        System.out.println("  ▶ 精英敌机血量: " + String.format("%.0f", oldEliteHp) + " → " + eliteEnemyHp +
                         " (×" + String.format("%.2f", eliteEnemyHp / oldEliteHp) + ")");

        // 提升敌机速度（+10%）
        double oldMobSpeed = mobEnemySpeed;
        double oldEliteSpeed = eliteEnemySpeed;
        mobEnemySpeed = (int) (mobEnemySpeed * 1.10);
        eliteEnemySpeed = (int) (eliteEnemySpeed * 1.10);
        System.out.println("【速度大幅提升】");
        System.out.println("  ▶ 普通敌机速度: " + String.format("%.0f", oldMobSpeed) + " → " + mobEnemySpeed +
                         " (×" + String.format("%.2f", mobEnemySpeed / oldMobSpeed) + ")");
        System.out.println("  ▶ 精英敌机速度: " + String.format("%.0f", oldEliteSpeed) + " → " + eliteEnemySpeed +
                         " (×" + String.format("%.2f", eliteEnemySpeed / oldEliteSpeed) + ")");

        // 缩短敌机产生周期（-10%，最小250ms）
        if (cycleDuration > 250) {
            double oldCycle = cycleDuration;
            cycleDuration = Math.max(250, (int) (cycleDuration * 0.90));
            System.out.println("【刷新大幅加速】");
            System.out.println("  ▶ 敌机产生周期: " + String.format("%.0f", oldCycle) + "ms → " + cycleDuration + "ms" +
                             " (×" + String.format("%.2f", cycleDuration / oldCycle) + ")");
        }

        // 增加精英敌机概率（+4%，最高70%）
        if (eliteProbability < 0.7) {
            double oldProb = eliteProbability;
            eliteProbability = Math.min(0.7, eliteProbability + 0.04);
            System.out.println("【精英大幅增多】");
            System.out.println("  ▶ 精英敌机概率: " + String.format("%.0f%%", oldProb * 100) +
                             " → " + String.format("%.0f%%", eliteProbability * 100));
        }

        // 增加敌机最大数量（最多10个）
        if (enemyMaxNumber < 10) {
            int oldMaxNumber = enemyMaxNumber;
            enemyMaxNumber = Math.min(10, enemyMaxNumber + 1);
            System.out.println("【数量大幅增加】");
            System.out.println("  ▶ 敌机最大数量: " + oldMaxNumber + " → " + enemyMaxNumber);
        }

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           挑战加剧！保持警惕！继续战斗！           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    @Override
    protected int getBossHp() {
        // 困难模式Boss血量递增：600 + 150 * (出现次数 - 1)
        return initialBossHp + BOSS_HP_INCREMENT * (bossAppearCount - 1);
    }
}

