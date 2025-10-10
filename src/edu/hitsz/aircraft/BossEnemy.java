package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * Boss敌机
 * 实现环射弹道：同时发射20颗子弹，呈环形
 * 悬浮于界面上方左右移动
 *
 * @author hitsz
 */
public class BossEnemy extends AbstractAircraft {

    /**
     * 子弹一次发射数量（环射弹道：20颗）
     */
    private int shootNum = 20;

    /**
     * 子弹伤害
     */
    private int power = 30;

    /**
     * 出现次数计数器
     */
    private static int bossCount = 0;

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        bossCount++;
    }

    @Override
    public void forward() {
        super.forward();

        // Boss机悬浮于界面上方，只做左右移动
        // 当触碰边界时会自动反向（在AbstractFlyingObject中已实现）
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY();

        // 环射弹道：发射20颗子弹，均匀分布在360度圆周上
        double angleStep = 2 * Math.PI / shootNum; // 每颗子弹之间的角度差
        int bulletSpeed = 6; // 子弹速度

        for (int i = 0; i < shootNum; i++) {
            double angle = i * angleStep;
            // 计算子弹的x和y方向速度分量
            int speedX = (int) (bulletSpeed * Math.cos(angle));
            int speedY = (int) (bulletSpeed * Math.sin(angle));

            BaseBullet bullet = new EnemyBullet(x, y, speedX, speedY, power);
            res.add(bullet);
        }

        return res;
    }

    /**
     * 获取Boss出现次数
     * @return Boss出现次数
     */
    public static int getBossCount() {
        return bossCount;
    }
}

