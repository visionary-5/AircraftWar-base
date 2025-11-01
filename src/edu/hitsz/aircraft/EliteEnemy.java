package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.shoot.StraightShootStrategy;
import edu.hitsz.observer.Observer;

/**
 * 精英敌机
 * 可射击，可左右移动
 *
 * @author hitsz
 */
public class EliteEnemy extends AbstractAircraft implements Observer {

    private int score = 30;

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 初始化为直线射击策略：1颗子弹，威力20，向下发射（direction=1）
        this.shootStrategy = new StraightShootStrategy(1, 20, 1);
    }

    @Override
    public void forward() {
        super.forward();

        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }

    @Override
    public void update() {
        // 炸弹爆炸时，精英敌机直接消失
        vanish();
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
