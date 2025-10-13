package edu.hitsz.aircraft;

import edu.hitsz.shoot.ScatterShootStrategy;

/**
 * 超级精英敌机
 * 继承自精英敌机，是精英敌机的加强版
 * 实现散射弹道：同时发射3颗子弹，呈扇形分布
 *
 * @author hitsz
 */
public class ElitePlusEnemy extends EliteEnemy {

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 初始化为散射策略：3颗子弹，威力20，向下发射（direction=1），散射角度30度
        this.shootStrategy = new ScatterShootStrategy(3, 20, 1, 30);
    }
}
