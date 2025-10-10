package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 超级精英敌机
 * 继承自精英敌机，是精英敌机的加强版
 * 实现散射弹道：同时发射3颗子弹，呈扇形分布
 *
 * @author hitsz
 */
public class ElitePlusEnemy extends EliteEnemy {

    /**
     * 子弹一次发射数量（散射弹道：3颗）
     */
    private int shootNum = 3;

    /**
     * 子弹伤害
     */
    private int power = 30;

    /**
     * 子弹射击方向 (向上发射：-1，向下发射：1)
     */
    private int direction = 1;

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + direction * 2;
        int speedY = this.getSpeedY() + direction * 5;

        // 散射弹道：发射3颗子弹，呈扇形分布
        // 中间子弹：垂直向下
        BaseBullet centerBullet = new EnemyBullet(x, y, 0, speedY, power);
        res.add(centerBullet);

        // 左侧子弹：向左下方散射
        BaseBullet leftBullet = new EnemyBullet(x - 10, y, -3, speedY, power);
        res.add(leftBullet);

        // 右侧子弹：向右下方散射
        BaseBullet rightBullet = new EnemyBullet(x + 10, y, 3, speedY, power);
        res.add(rightBullet);

        return res;
    }
}

