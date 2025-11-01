package edu.hitsz.application;

import edu.hitsz.aircraft.*;

/**
 * 简单模式游戏
 * 特点：
 * - 无Boss敌机
 * - 敌机数量少（3个）
 * - 敌机血量和速度较低
 * - 英雄机射击周期短（400ms）
 * - 敌机产生周期长（800ms）
 * - 精英敌机概率低（20%）
 * - 难度不随时间增加
 */
public class EasyGame extends AbstractGame {

    public EasyGame(String difficulty, boolean soundEnabled) {
        super(difficulty, soundEnabled);
    }

    @Override
    protected void initGameParameters() {
        // 简单模式参数设置
        this.enemyMaxNumber = 3;              // 敌机最大数量：3个
        this.cycleDuration = 800;              // 敌机产生周期：800ms（较慢）
        this.heroShootPeriod = 400;            // 英雄射击周期：400ms（较快）
        this.eliteProbability = 0.2;           // 精英敌机概率：20%
        this.mobEnemyHp = 20;                  // 普通敌机血量：20
        this.eliteEnemyHp = 60;                // 精英敌机血量：60
        this.mobEnemySpeed = 8;                // 普通敌机速度：8
        this.eliteEnemySpeed = 6;              // 精英敌机速度：6
        this.bossScoreThreshold = 0;           // 无Boss（阈值设为0表示不产生Boss）
        this.initialBossHp = 0;                // 无Boss
    }

    @Override
    protected void generateEnemy() {
        // 随机产生普通敌机或精英敌机
        if (Math.random() < (1 - eliteProbability)) {
            // 80%概率产生普通敌机
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
            // 20%概率产生精英敌机
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
        // 简单模式无Boss敌机
        System.out.println("简单模式无Boss敌机");
    }

    @Override
    protected boolean shouldIncreaseDifficulty() {
        // 简单模式难度不增加
        return false;
    }

    @Override
    protected void increaseDifficulty() {
        // 简单模式难度不增加，此方法为空实现
    }

    @Override
    protected int getBossHp() {
        // 简单模式无Boss
        return 0;
    }
}
