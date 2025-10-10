package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 英雄飞机，游戏玩家操控
 * 采用单例模式，确保游戏中只有一个英雄机实例
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {

    /**
     * 单例模式 - 静态实例变量
     */
    private static HeroAircraft instance = null;

    /**攻击方式 */

    /**
     * 子弹一次发射数量
     */
    private int shootNum = 1;

    /**
     * 子弹伤害
     */
    private int power = 30;

    /**
     * 子弹射击方向 (向上发射：1，向下发射：-1)
     */
    private int direction = -1;

    /**
     * 私有构造函数，防止外部实例化
     * @param locationX 英雄机位置x坐标
     * @param locationY 英雄机位置y坐标
     * @param speedX 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param speedY 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param hp    初始生命值
     */
    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    /**
     * 懒汉式单例模式 - 获取唯一实例（无参版本）
     * @return HeroAircraft唯一实例
     */
    public static synchronized HeroAircraft getInstance() {
        if (instance == null) {
            // 使用默认参数创建实例
            instance = new HeroAircraft(400, 550, 0, 0, 1000);
        }
        return instance;
    }

    /**
     * 懒汉式单例模式 - 获取唯一实例（带参版本）
     * @param locationX 英雄机位置x坐标
     * @param locationY 英雄机位置y坐标
     * @param speedX 英雄机射出的子弹的基准速度
     * @param speedY 英雄机射出的子弹的基准速度
     * @param hp 初始生命值
     * @return HeroAircraft唯一实例
     */
    public static synchronized HeroAircraft getInstance(int locationX, int locationY, int speedX, int speedY, int hp) {
        if (instance == null) {
            instance = new HeroAircraft(locationX, locationY, speedX, speedY, hp);
        }
        return instance;
    }

    @Override
    public void forward() {
        // 英雄机由鼠标控制，不通过forward函数移动
    }

    @Override
    /**
     * 通过射击产生子弹
     * @return 射击出的子弹List
     */
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + direction*2;
        int speedX = 0;
        int speedY = this.getSpeedY() + direction*5;
        BaseBullet bullet;
        for(int i=0; i<shootNum; i++){
            // 子弹发射位置相对飞机位置向前偏移
            // 多个子弹横向分散
            bullet = new HeroBullet(x + (i*2 - shootNum + 1)*10, y, speedX, speedY, power);
            res.add(bullet);
        }
        return res;
    }

    /**
     * 获取最大血量
     * @return 最大血量
     */
    public int getMaxHp() {
        return maxHp;
    }

    /**
     * 设置当前血量
     * @param hp 血量值
     */
    public void setHp(int hp) {
        this.hp = hp;
    }

    /**
     * 重置单例实例（仅用于测试）
     * 注意：此方法仅应在单元测试中使用
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

}
