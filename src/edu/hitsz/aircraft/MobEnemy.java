package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.shoot.NoShootStrategy;
import edu.hitsz.observer.Observer;

/**
 * 普通敌机
 * 不可射击
 *
 * @author hitsz
 */
public class MobEnemy extends AbstractAircraft implements Observer {

    private int score = 10;

    public MobEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 初始化为不射击策略
        this.shootStrategy = new NoShootStrategy();
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
        // 炸弹爆炸时，普通敌机直接消失
        vanish();
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
