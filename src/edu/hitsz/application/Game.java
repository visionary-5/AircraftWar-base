package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.prop.*;
import edu.hitsz.dao.ScoreDao;
import edu.hitsz.dao.ScoreDaoImpl;
import edu.hitsz.dto.ScoreRecord;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * 游戏主面板，游戏启动
 *
 * @author hitsz
 */
public class Game extends JPanel {

    private int backGroundTop = 0;

    /**
     * Scheduled 线程池，用于任务调度
     */
    private final ScheduledExecutorService executorService;

    /**
     * 时间间隔(ms)，控制刷新频率
     */
    private int timeInterval = 40;

    private final HeroAircraft heroAircraft;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<AbstractProp> props;

    /**
     * 敌机工厂
     */
    private final EnemyFactory mobEnemyFactory;
    private final EnemyFactory eliteEnemyFactory;
    private final EnemyFactory elitePlusEnemyFactory;
    private final EnemyFactory bossEnemyFactory;

    /**
     * 道具工厂
     */
    private final PropFactory bloodPropFactory;
    private final PropFactory firePropFactory;
    private final PropFactory bombPropFactory;
    private final PropFactory superFirePropFactory;

    /**
     * 得分数据访问对象
     */
    private final ScoreDao scoreDao;

    /**
     * 游戏难度
     */
    private String difficulty = "EASY";

    /**
     * 屏幕中出现的敌机最大数量
     */
    private int enemyMaxNumber = 5;

    /**
     * 当前得分
     */
    private int score = 0;

    /**
     * Boss出现的分数阈值
     */
    private int bossScoreThreshold = 600;

    /**
     * 上一次Boss出现时的分数
     */
    private int lastBossScore = 0;

    /**
     * 是否有Boss存在
     */
    private boolean bossExists = false;

    /**
     * 超级精英敌机生成周期计数器
     */
    private int elitePlusCycleCount = 0;

    /**
     * 超级精英敌机生成周期（每隔N个周期生成一次）
     */
    private int elitePlusCyclePeriod = 5;

    /**
     * 当前时刻
     */
    private int time = 0;

    /**
     * 周期（ms)，指示子弹的发射、敌机的产生频率
     */
    private int cycleDuration = 600;
    private int cycleTime = 0;

    /**
     * 游戏结束标志
     */
    private boolean gameOverFlag = false;
    /**
     * 音效是否开启
     */
    private boolean soundEnabled = true;
    /**
     * 背景音乐线程
     */
    private MusicThread bgmThread;
    /**
     * Boss背景音乐线程
     */
    private MusicThread bossBgmThread;

    public Game(String difficulty, boolean soundEnabled) {
        this.difficulty = difficulty;
        this.soundEnabled = soundEnabled;
        // 根据难度设置背景图
        ImageManager.setBackgroundImage(difficulty);
        
        heroAircraft = HeroAircraft.getInstance(
                Main.WINDOW_WIDTH / 2,
                Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight() ,
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

        /**
         * Scheduled 线程池，用于定时任务调度
         * 关于alibaba code guide：可命名的 ThreadFactory 一般需要第三方包
         * apache 第三方库： org.apache.commons.lang3.concurrent.BasicThreadFactory
         */
        this.executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("game-action-%d").daemon(true).build());

        //启动英雄机鼠标监听
        new HeroController(this, heroAircraft);
        // 如果音效开启，播放背景音乐
        if (soundEnabled) {
            bgmThread = new MusicThread("src/videos/bgm.wav", true);
            bgmThread.start();
        }

    }

    /**
     * 游戏启动入口，执行游戏逻辑
     */
    public void action() {

        // 定时任务：绘制、对象产生、碰撞判定、击毁及结束判定
        Runnable task = () -> {

            time += timeInterval;

            // 周期性执行（控制频率）
            if (timeCountAndNewCycleJudge()) {
                System.out.println(time);

                // 检查是否需要生成Boss敌机（分数达到阈值且当前没有Boss）
                if (score - lastBossScore >= bossScoreThreshold && !bossExists) {
                    generateBoss();
                }

                // 超级精英敌机周期生成
                elitePlusCycleCount++;
                if (elitePlusCycleCount >= elitePlusCyclePeriod && Math.random() < 0.5) {
                    // 每隔一定周期，50%概率生成超级精英敌机
                    generateElitePlusEnemy();
                    elitePlusCycleCount = 0;
                }

                // 新敌机产生
                if (enemyAircrafts.size() < enemyMaxNumber) {
                    // 随机产生普通敌机或精英敌机
                    if (Math.random() < 0.7) {
                        // 70%概率产生普通敌机
                        int mobEnemyWidth = ImageManager.MOB_ENEMY_IMAGE.getWidth();
                        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - mobEnemyWidth)) + mobEnemyWidth / 2;
                        enemyAircrafts.add(mobEnemyFactory.createEnemy(
                                locationX,
                                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                                0,
                                10,
                                30
                        ));
                    } else {
                        // 30%概率产生精英敌机
                        int eliteEnemyWidth = ImageManager.ELITE_ENEMY_IMAGE.getWidth();
                        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - eliteEnemyWidth));
                        enemyAircrafts.add(eliteEnemyFactory.createEnemy(
                                locationX,
                                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                                (int) (Math.random() * 4 - 2), // 随机左右移动速度 -2 到 2
                                8,
                                80
                        ));
                    }
                }
                // 飞机射出子弹
                shootAction();
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

            //每个时刻重绘界面
            repaint();

            // 游戏结束检查
            if (gameOverFlag || heroAircraft.getHp() <= 0) {
                // 游戏结束
                executorService.shutdown();
                gameOverFlag = true;
                System.out.println("Game Over!");

                // 处理游戏结束后的得分记录
                gameOver();
            }
        };

        /**
         * 以固定延迟时间进行执行
         * 本次任务执行完成后，需要延迟设定的延迟时间，才会执行新的任务
         */
        executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);

    }

    /**
     * 游戏结束处理
     */
    private void gameOver() {
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
            // 添加当前得分
            scoreboard.addScore(score);
        });
    }

    /**
     * 打印得分排行榜
     */
    private void printScoreBoard() {
        System.out.println(score);
        System.out.println("************************************");
        System.out.println("           得分排行榜");
        System.out.println("************************************");

        // 获取所有得分记录
        List<ScoreRecord> allScores = scoreDao.getAllScores(difficulty);

        // 打印每条记录
        for (ScoreRecord record : allScores) {
            System.out.println("第" + record.getRank() + "名: " +
                             record.getPlayerName() + "," +
                             record.getScore() + "," +
                             record.getRecordTime());
        }
    }

    //***********************
    //      Action 各部分
    //***********************

    private boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            // 跨越到新的周期
            cycleTime %= cycleDuration;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 生成Boss敌机
     */
    private void generateBoss() {
        System.out.println("Boss出现！当前分数：" + score);
        // Boss 出场，停止普通背景音乐，播放 Boss 背景音乐
        if (soundEnabled) {
            if (bgmThread != null) {
                bgmThread.stopMusic();
            }
            bossBgmThread = new MusicThread("src/videos/bgm_boss.wav", true);
            bossBgmThread.start();
        }
        // Boss机悬浮于界面上方左右移动
        int locationX = Main.WINDOW_WIDTH / 2;
        int locationY = ImageManager.BOSS_ENEMY_IMAGE.getHeight();
        int speedX = 5;  // 左右移动速度
        int speedY = 0;  // 不向下移动，悬浮在上方
        int hp = 500;   // Boss血量很高

        enemyAircrafts.add(bossEnemyFactory.createEnemy(locationX, locationY, speedX, speedY, hp));
        bossExists = true;
        lastBossScore = score;
    }

    /**
     * 生成超级精英敌机
     */
    private void generateElitePlusEnemy() {
        System.out.println("超级精英敌机出现！");
        int elitePlusWidth = ImageManager.ELITE_PLUS_ENEMY_IMAGE.getWidth();
        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - elitePlusWidth));
        int locationY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
        int speedX = (int) (Math.random() * 6 - 3); // 随机左右移动速度 -3 到 3
        int speedY = 10;
        int hp = 120;  // 超级精英敌机血量较高

        enemyAircrafts.add(elitePlusEnemyFactory.createEnemy(locationX, locationY, speedX, speedY, hp));
    }

    private void shootAction() {
        // 敌机射击
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft instanceof BossEnemy) {
                // Boss敌机每周期射击（100%概率）
                enemyBullets.addAll(enemyAircraft.shoot());
            } else if (enemyAircraft instanceof ElitePlusEnemy) {
                // 超级精英敌机每周期射击（100%概率）
                enemyBullets.addAll(enemyAircraft.shoot());
            } else if (enemyAircraft instanceof EliteEnemy) {
                // 精英敌机每周期射击（100%概率）
                enemyBullets.addAll(enemyAircraft.shoot());
            }
            // 普通敌机不射击
        }

        // 英雄射击
        heroBullets.addAll(heroAircraft.shoot());
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }

    private void propsMoveAction() {
        for (AbstractProp prop : props) {
            prop.forward();
        }
    }

    /**
     * 碰撞检测：
     * 1. 敌机攻击英雄
     * 2. 英雄攻击/撞击敌机
     * 3. 英雄获得补给
     */
    private void crashCheckAction() {
        // 敌机子弹攻击英雄
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            if (heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
                // 检查英雄机是否死亡
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
                        // 播放子弹击中音效
                        if (soundEnabled) {
                            new MusicThread("src/videos/bullet_hit.wav").start();
                        }
                        // 敌机被击毁，获得分数，产生道具补给
                        if (enemyAircraft instanceof BossEnemy) {
                            // Boss被击毁，获得大量分数
                            score += 300;
                            System.out.println("击毁Boss！获得300分");
                            bossExists = false;
                            // Boss被击毁，停止Boss音乐，恢复普通背景音乐
                            if (soundEnabled) {
                                if (bossBgmThread != null) {
                                    bossBgmThread.stopMusic();
                                }
                                bgmThread = new MusicThread("src/videos/bgm.wav", true);
                                bgmThread.start();
                            }
                            // Boss必定掉落3个道具
                            generateBossProp(enemyAircraft.getLocationX(), enemyAircraft.getLocationY());
                        } else if (enemyAircraft instanceof ElitePlusEnemy) {
                            // 超级精英敌机被击毁
                            score += 100;
                            System.out.println("击毁超级精英敌机！获得100分");
                            // 超级精英敌机必定掉落1个道具
                            generateElitePlusProp(enemyAircraft.getLocationX(), enemyAircraft.getLocationY());
                        } else if (enemyAircraft instanceof EliteEnemy) {
                            score += 50;
                            // 精英敌机坠毁后有概率产生道具
                            generateEliteProp(enemyAircraft.getLocationX(), enemyAircraft.getLocationY());
                        } else {
                            score += 10;
                        }
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
                // 道具生效音效
                if (soundEnabled) {
                    new MusicThread("src/videos/get_supply.wav").start();
                }
                // 道具生效
                prop.activate(heroAircraft);
                prop.vanish();
            }
        }

        // 英雄机与敌机碰撞检测 - 均损毁
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft.notValid()) {
                continue;
            }
            if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                // 英雄机与敌机相撞，均损毁，游戏结束
                System.out.println("英雄机与敌机相撞！游戏结束！");
                enemyAircraft.vanish();
                heroAircraft.decreaseHp(Integer.MAX_VALUE);
                gameOverFlag = true;
                return;
            }
        }
    }

    /**
     * 生成道具（精英敌机掉落）
     */
    private void generateEliteProp(int x, int y) {
        double random = Math.random();
        if (random < 0.3) {
            // 30%概率产生加血道具
            props.add(bloodPropFactory.createProp(x, y, 0, 5));
        } else if (random < 0.6) {
            // 30%概率产生火力道具
            props.add(firePropFactory.createProp(x, y, 0, 5));
        } else if (random < 0.85) {
            // 25%概率产生炸弹道具
            props.add(bombPropFactory.createProp(x, y, 0, 5));
        } else {
            // 15%概率产生超级火力道具
            props.add(superFirePropFactory.createProp(x, y, 0, 5));
        }
    }

    /**
     * 生成道具（超级精英敌机掉落，必定掉落1个）
     */
    private void generateElitePlusProp(int x, int y) {
        double random = Math.random();
        if (random < 0.25) {
            // 25%概率产生加血道具
            props.add(bloodPropFactory.createProp(x, y, 0, 5));
        } else if (random < 0.5) {
            // 25%概率产生火力道具
            props.add(firePropFactory.createProp(x, y, 0, 5));
        } else if (random < 0.75) {
            // 25%概率产生炸弹道具
            props.add(bombPropFactory.createProp(x, y, 0, 5));
        } else {
            // 25%概率产生超级火力道具
            props.add(superFirePropFactory.createProp(x, y, 0, 5));
        }
    }

    /**
     * 生成道具（Boss掉落，必定掉落3个道具，包含超级火力道具）
     */
    private void generateBossProp(int x, int y) {
        // Boss掉落3个道具：加血、炸弹、超级火力
        props.add(bloodPropFactory.createProp(x - 50, y, 0, 5));
        props.add(bombPropFactory.createProp(x, y, 0, 5));
        props.add(superFirePropFactory.createProp(x + 50, y, 0, 5));
    }

    /**
     * 后处理：
     * 1. 删除无效的子弹
     * 2. 删除无效的敌机
     * 3. 删除无效的道具
     * <p>
     * 无效的原因可能是撞击或者飞出边界
     */
    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);

        // 检查Boss是否还存在
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


    //***********************
    //      Paint 各部分
    //***********************

    /**
     * 重写paint方法
     * 通过重复调用paint方法，实现游戏动画
     *
     * @param  g 绘图对象
     */
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

        // 先绘制子弹，后绘制飞机
        // 这样子弹显示在飞机的下层
        paintImageWithPositionRevised(g, enemyBullets);
        paintImageWithPositionRevised(g, heroBullets);

        paintImageWithPositionRevised(g, enemyAircrafts);

        // 绘制道具
        paintImageWithPositionRevised(g, props);

        g.drawImage(ImageManager.HERO_IMAGE, heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
                heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2, null);

        //绘制得分和生命值
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


