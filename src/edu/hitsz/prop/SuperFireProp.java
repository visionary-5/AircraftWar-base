package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.shoot.CircleShootStrategy;

/**
 * 超级火力道具类
 * 大幅提升英雄机火力，弹道由直射切换为环射
 *
 * @author hitsz
 */
public class SuperFireProp extends AbstractProp {

    public SuperFireProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        System.out.println("SuperFireSupply active! 英雄机切换为环射弹道");
        // 切换为环射策略：20颗子弹，威力30
        heroAircraft.setShootStrategy(new CircleShootStrategy(20, 30));
    }
}

