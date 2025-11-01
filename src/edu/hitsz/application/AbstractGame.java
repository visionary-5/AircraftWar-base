package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.prop.*;
import edu.hitsz.observer.Observer;
import edu.hitsz.dao.ScoreDao;
import edu.hitsz.dao.ScoreDaoImpl;
import edu.hitsz.dto.ScoreRecord;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 游戏抽象类，使用模板模式
 * 定义游戏流程框架，具体难度由子类实现
 *
 * @author hitsz
 */
public abstract class AbstractGame extends JPanel {

    private int backGroundTop = 0;

    /**
     * Scheduled 线程池，用于任务调度
     */
    private final ScheduledExecutorService executorService;

    /**
     * 时间间隔(ms)，控制刷新频率
     */
    private int timeInterval = 40;

    protected final HeroAircraft heroAircraft;
    protected final List<AbstractAircraft> enemyAircrafts;
    protected final List<BaseBullet> heroBullets;
    protected final List<BaseBullet> enemyBullets;
    protected final List<AbstractProp> props;

    /**
     * 敌机工厂
     */
    protected final EnemyFactory mobEnemyFactory;
    protected final EnemyFactory eliteEnemyFactory;
    protected final EnemyFactory elitePlusEnemyFactory;
    protected final EnemyFactory bossEnemyFactory;

    /**
     * 道具工厂
     */
    protected final PropFactory bloodPropFactory;
    protected final PropFactory firePropFactory;
    protected final PropFactory bombPropFactory;
    protected final PropFactory superFirePropFactory;

    /**
     * 得分数据访问对象
     */
    protected final ScoreDao scoreDao;

    /**
     * 游戏难度
     */
    protected String difficulty;

    /**
     * 屏幕中出现的敌机最大数量
     */
    protected int enemyMaxNumber;

    /**
     * 当前得分
     */
    protected int score = 0;

    /**
     * Boss出现的分数阈值
     */
    protected int bossScoreThreshold;

    /**
     * 上一次Boss出现时的分数
     */
    protected int lastBossScore = 0;

    /**
     * 是否有Boss存在
     */
    protected boolean bossExists = false;

    /**
     * 超级精英敌机生成周期计数器
     */
    protected int elitePlusCycleCount = 0;

    /**
     * 超级精英敌机生成周期（每隔N个周期生成一次）
     */
    protected int elitePlusCyclePeriod = 5;

    /**
     * 当前时刻
     */
    protected int time = 0;

    /**
     * 周期（ms)，指示子弹的发射、敌机的产生频率
     */
    protected int cycleDuration;
    protected int cycleTime = 0;

    /**
     * 英雄机射击周期
     */
    protected int heroShootPeriod;
    protected int heroShootTime = 0;

    /**
     * 敌机射击周期
     */
    protected int enemyShootPeriod = 600; // 敌机射击周期，默认600ms
    protected int enemyShootTime = 0;

    /**
     * 精英敌机产生概率
     */
    protected double eliteProbability;

    /**
     * Boss初始血量
     */
    protected int initialBossHp;

    /**
     * 普通敌机血量
     */
    protected int mobEnemyHp;

    /**
     * 精英敌机血量
     */
    protected int eliteEnemyHp;

    /**
     * 普通敌机速度
     */
    protected int mobEnemySpeed;

    /**
     * 精英敌机速度
     */
    protected int eliteEnemySpeed;

    /**
     * 游戏结束标志
     */
    protected boolean gameOverFlag = false;

    /**
     * 音效是否开启
     */
    protected boolean soundEnabled = true;

    /**
     * 背景音乐线程
     */
    protected MusicThread bgmThread;

    /**
     * Boss背景音乐线程
     */
    protected MusicThread bossBgmThread;

    public AbstractGame(String difficulty, boolean soundEnabled) {
        this.difficulty = difficulty;
        this.soundEnabled = soundEnabled;

        // 根据难度设置背景图
        ImageManager.setBackgroundImage(difficulty);

        heroAircraft = HeroAircraft.getInstance(
                Main.WINDOW_WIDTH / 2,
                Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight(),
                0, 0, 1000);

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props = new LinkedList<>();

        // 初始化敌机工厂
        mobEnemyFactory = new MobEnemyFactory();
        eliteEnemyFactory = new EliteEnemyFactory();
        elitePlusEnemyFactory = new ElitePlusEnemyFactory();
        bossEnemyFactory = new BossEnemyFactory();

        // 初始化道具工厂
        bloodPropFactory = new BloodPropFactory();
        firePropFactory = new FirePropFactory();
        bombPropFactory = new BombPropFactory();
        superFirePropFactory = new SuperFirePropFactory();

        // 初始化得分数据访问对象
        scoreDao = new ScoreDaoImpl();

        // 初始化游戏参数（由子类实现）
        initGameParameters();

        this.executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("game-action-%d").daemon(true).build());

        // 启动英雄机鼠标监听
        new HeroController(this, heroAircraft);

        // 如果音效开启，播放背景音乐
        if (soundEnabled) {
            bgmThread = new MusicThread("src/videos/bgm.wav", true);
            bgmThread.start();
        }

        System.out.println("===== 游戏开始 =====");
        System.out.println("难度: " + difficulty);
        System.out.println("敌机最大数量: " + enemyMaxNumber);
        System.out.println("敌机产生周期: " + cycleDuration + "ms");
        System.out.println("英雄机射击周期: " + heroShootPeriod + "ms");
        System.out.println("精英敌机概率: " + (eliteProbability * 100) + "%");
        System.out.println("普通敌机血量: " + mobEnemyHp + ", 速度: " + mobEnemySpeed);
        System.out.println("精英敌机血量: " + eliteEnemyHp + ", 速度: " + eliteEnemySpeed);
        if (bossScoreThreshold > 0) {
            System.out.println("Boss出现阈值: " + bossScoreThreshold + "分");
            System.out.println("Boss初始血量: " + initialBossHp);
        } else {
            System.out.println("本难度无Boss敌机");
        }
        System.out.println("==================");
    }

    /**
     * 初始化游戏参数（抽象方法，由子类实现）
     */
    protected abstract void initGameParameters();

    /**
     * 生成敌机（抽象方法，由子类实现）
     */
    protected abstract void generateEnemy();

    /**
     * 生成Boss（抽象方法，由子类实现）
     */
    protected abstract void generateBoss();

    /**
     * 判断是否应该提升难度（抽象方法，由子类实现）
     */
    protected abstract boolean shouldIncreaseDifficulty();

    /**
     * 提升难度（抽象方法，由子类实现）
     */
    protected abstract void increaseDifficulty();

    /**
     * 获取Boss血量（抽象方法，由子类实现）
     */
    protected abstract int getBossHp();

    /**
     * 游戏启动入口，执行游戏逻辑（模板方法）
     */
    public final void action() {
        // 定时任务：绘制、对象产生、碰撞判定、击毁及结束判定
        Runnable task = () -> {
            time += timeInterval;

            // 检查是否应该提升难度
            if (shouldIncreaseDifficulty()) {
                increaseDifficulty();
            }

            // 周期性执行（控制频率）
            if (timeCountAndNewCycleJudge()) {
                // 检查是否需要生成Boss敌机（分数达到阈值且当前没有Boss）
                if (bossScoreThreshold > 0 && score - lastBossScore >= bossScoreThreshold && !bossExists) {
                    generateBoss();
                }

                // 超级精英敌机周期生成
                elitePlusCycleCount++;
                if (elitePlusCycleCount >= elitePlusCyclePeriod && Math.random() < 0.5) {
                    generateElitePlusEnemy();
                    elitePlusCycleCount = 0;
                }

                // 新敌机产生
                if (enemyAircrafts.size() < enemyMaxNumber) {
                    generateEnemy();
                }
            }

            // 英雄机射击周期判断
            heroShootTime += timeInterval;
            if (heroShootTime >= heroShootPeriod) {
                heroBullets.addAll(heroAircraft.shoot());
                heroShootTime = 0;
            }

            // 敌机射击周期判断
            enemyShootTime += timeInterval;
            if (enemyShootTime >= enemyShootPeriod) {
                shootAction();
                enemyShootTime = 0;
            }

            // 子弹移动
            bulletsMoveAction();

            // 飞机移动
            aircraftsMoveAction();

            // 道具移动
            propsMoveAction();

            // 撞击检测
            crashCheckAction();

            // 后处理
            postProcessAction();

            // 每个时刻重绘界面
            repaint();

            // 游戏结束检查
            if (gameOverFlag || heroAircraft.getHp() <= 0) {
                executorService.shutdown();
                gameOverFlag = true;
                System.out.println("===== 游戏结束 =====");
                System.out.println("最终得分: " + score);
                gameOver();
            }
        };

        executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * 游戏结束处理
     */
    protected void gameOver() {
        // 停止背景音乐和Boss音乐
        if (bgmThread != null) {
            bgmThread.stopMusic();
        }
        if (bossBgmThread != null) {
            bossBgmThread.stopMusic();
        }
        // 如果音效开启，播放游戏结束音效
        if (soundEnabled) {
            new MusicThread("src/videos/game_over.wav").start();
        }
        // 显示排行榜界面
        SwingUtilities.invokeLater(() -> {
            Scoreboard scoreboard = new Scoreboard(difficulty);
            scoreboard.setVisible(true);
            scoreboard.addScore(score);
        });
    }

    protected boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            cycleTime %= cycleDuration;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 生成超级精英敌机
     */
    protected void generateElitePlusEnemy() {
        System.out.println("超级精英敌机出现！");
        int elitePlusWidth = ImageManager.ELITE_PLUS_ENEMY_IMAGE.getWidth();
        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - elitePlusWidth));
        int locationY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
        int speedX = (int) (Math.random() * 6 - 3);
        int speedY = 10;
        int hp = 120;

        enemyAircrafts.add(elitePlusEnemyFactory.createEnemy(locationX, locationY, speedX, speedY, hp));
    }

    protected void shootAction() {
        // 敌机射击
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft instanceof BossEnemy ||
                enemyAircraft instanceof ElitePlusEnemy ||
                enemyAircraft instanceof EliteEnemy) {
                enemyBullets.addAll(enemyAircraft.shoot());
            }
        }
    }

    protected void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    protected void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }

    protected void propsMoveAction() {
        for (AbstractProp prop : props) {
            prop.forward();
        }
    }

    protected void crashCheckAction() {
        // 敌机子弹攻击英雄
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            if (heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
                if (heroAircraft.getHp() <= 0) {
                    gameOverFlag = true;
                    return;
                }
            }
        }

        // 英雄子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        if (soundEnabled) {
                            new MusicThread("src/videos/bullet_hit.wav").start();
                        }
                        handleEnemyDestroyed(enemyAircraft);
                    }
                }
            }
        }

        // 英雄机获得道具补给
        for (AbstractProp prop : props) {
            if (prop.notValid()) {
                continue;
            }
            if (heroAircraft.crash(prop)) {
                if (soundEnabled) {
                    new MusicThread("src/videos/get_supply.wav").start();
                }
                if (prop instanceof BombProp) {
                    BombProp bombProp = (BombProp) prop;
                    for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                        if (!enemyAircraft.notValid() && enemyAircraft instanceof Observer) {
                            bombProp.registerObserver((Observer) enemyAircraft);
                        }
                    }
                    for (BaseBullet enemyBullet : enemyBullets) {
                        if (!enemyBullet.notValid() && enemyBullet instanceof Observer) {
                            bombProp.registerObserver((Observer) enemyBullet);
                        }
                    }
                    if (soundEnabled) {
                        new MusicThread("src/videos/bomb_explosion.wav").start();
                    }
                    int bombScore = bombProp.notifyObservers();
                    score += bombScore;
                    System.out.println("炸弹爆炸！获得 " + bombScore + " 分");
                } else {
                    prop.activate(heroAircraft);
                }
                prop.vanish();
            }
        }

        // 英雄机与敌机碰撞检测
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft.notValid()) {
                continue;
            }
            if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                System.out.println("英雄机与敌机相撞！游戏结束！");
                enemyAircraft.vanish();
                heroAircraft.decreaseHp(Integer.MAX_VALUE);
                gameOverFlag = true;
                return;
            }
        }
    }

    /**
     * 处理敌机被击毁
     */
    protected void handleEnemyDestroyed(AbstractAircraft enemyAircraft) {
        if (enemyAircraft instanceof BossEnemy) {
            score += 300;
            System.out.println("击毁Boss！获得300分，当前得分: " + score);
            bossExists = false;
            if (soundEnabled) {
                if (bossBgmThread != null) {
                    bossBgmThread.stopMusic();
                }
                bgmThread = new MusicThread("src/videos/bgm.wav", true);
                bgmThread.start();
            }
            generateBossProp(enemyAircraft.getLocationX(), enemyAircraft.getLocationY());
        } else if (enemyAircraft instanceof ElitePlusEnemy) {
            score += 100;
            System.out.println("击毁超级精英敌机！获得100分，当前得分: " + score);
            generateElitePlusProp(enemyAircraft.getLocationX(), enemyAircraft.getLocationY());
        } else if (enemyAircraft instanceof EliteEnemy) {
            score += 50;
            System.out.println("击毁精英敌机！获得50分，当前得分: " + score);
            generateEliteProp(enemyAircraft.getLocationX(), enemyAircraft.getLocationY());
        } else {
            score += 10;
        }
    }

    protected void generateEliteProp(int x, int y) {
        double random = Math.random();
        if (random < 0.3) {
            props.add(bloodPropFactory.createProp(x, y, 0, 5));
        } else if (random < 0.6) {
            props.add(firePropFactory.createProp(x, y, 0, 5));
        } else if (random < 0.85) {
            props.add(bombPropFactory.createProp(x, y, 0, 5));
        } else {
            props.add(superFirePropFactory.createProp(x, y, 0, 5));
        }
    }

    protected void generateElitePlusProp(int x, int y) {
        double random = Math.random();
        if (random < 0.25) {
            props.add(bloodPropFactory.createProp(x, y, 0, 5));
        } else if (random < 0.5) {
            props.add(firePropFactory.createProp(x, y, 0, 5));
        } else if (random < 0.75) {
            props.add(bombPropFactory.createProp(x, y, 0, 5));
        } else {
            props.add(superFirePropFactory.createProp(x, y, 0, 5));
        }
    }

    protected void generateBossProp(int x, int y) {
        props.add(bloodPropFactory.createProp(x - 50, y, 0, 5));
        props.add(bombPropFactory.createProp(x, y, 0, 5));
        props.add(superFirePropFactory.createProp(x + 50, y, 0, 5));
    }

    protected void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);

        if (bossExists) {
            boolean bossStillExists = false;
            for (AbstractAircraft aircraft : enemyAircrafts) {
                if (aircraft instanceof BossEnemy) {
                    bossStillExists = true;
                    break;
                }
            }
            bossExists = bossStillExists;
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // 绘制背景,图片滚动
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop - Main.WINDOW_HEIGHT, null);
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop, null);
        this.backGroundTop += 1;
        if (this.backGroundTop == Main.WINDOW_HEIGHT) {
            this.backGroundTop = 0;
        }

        paintImageWithPositionRevised(g, enemyBullets);
        paintImageWithPositionRevised(g, heroBullets);
        paintImageWithPositionRevised(g, enemyAircrafts);
        paintImageWithPositionRevised(g, props);

        g.drawImage(ImageManager.HERO_IMAGE, heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
                heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2, null);

        paintScoreAndLife(g);
    }

    private void paintImageWithPositionRevised(Graphics g, List<? extends AbstractFlyingObject> objects) {
        if (objects.isEmpty()) {
            return;
        }
        for (AbstractFlyingObject object : objects) {
            BufferedImage image = object.getImage();
            assert image != null : objects.getClass().getName() + " has no image! ";
            g.drawImage(image, object.getLocationX() - image.getWidth() / 2,
                    object.getLocationY() - image.getHeight() / 2, null);
        }
    }

    private void paintScoreAndLife(Graphics g) {
        int x = 10;
        int y = 25;
        g.setColor(new Color(16711680));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SCORE:" + this.score, x, y);
        y = y + 20;
        g.drawString("LIFE:" + this.heroAircraft.getHp(), x, y);
    }
}

