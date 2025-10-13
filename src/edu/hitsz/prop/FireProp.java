package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.shoot.ScatterShootStrategy;

/**
 * 火力道具类
 * 提升英雄机火力，弹道由直射切换为散射
 *
 * @author hitsz
 */
public class FireProp extends AbstractProp {

    public FireProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        System.out.println("FireSupply active! 英雄机切换为散射弹道");
        // 切换为散射策略：3颗子弹，威力30，向上发射（direction=-1），散射角度30度
        heroAircraft.setShootStrategy(new ScatterShootStrategy(3, 30, -1, 30));
    }
}
