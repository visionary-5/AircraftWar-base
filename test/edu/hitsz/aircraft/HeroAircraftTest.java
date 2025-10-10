package edu.hitsz.aircraft;

import org.junit.jupiter.api.*;
import edu.hitsz.bullet.BaseBullet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HeroAircraft Unit Test Class
 * Testing methods from HeroAircraft, AbstractAircraft, and AbstractFlyingObject
 * Using both black-box testing (boundary value analysis, equivalence partitioning)
 * and white-box testing (statement coverage, branch coverage)
 */
class HeroAircraftTest {

    private HeroAircraft heroAircraft;

    @BeforeEach
    void setUp() {
        // Reset singleton instance before each test to ensure independence
        HeroAircraft.resetInstance();
        heroAircraft = HeroAircraft.getInstance(400, 550, 0, 0, 1000);
    }

    @AfterEach
    void tearDown() {
        // Reset singleton instance after each test
        HeroAircraft.resetInstance();
    }

    // ==================== Test Case 1: HeroAircraft.shoot() ====================

    /**
     * Test Case ID: HA001
     * Method Under Test: HeroAircraft.shoot()
     * Test Method: testShoot()
     * Description: Verify that the hero aircraft can shoot bullets correctly
     * Black-box Testing: Functional testing - validate bullet generation
     * White-box Testing: Statement coverage - cover loop logic in shoot()
     */
    @Test
    @DisplayName("Test shoot() method - Verify bullet generation")
    void testShoot() {
        // Test Step: Call shoot() method
        List<BaseBullet> bullets = heroAircraft.shoot();

        // Expected Result 1: Bullet list should not be null
        assertNotNull(bullets, "shoot() should return a non-null bullet list");

        // Expected Result 2: At least one bullet should be generated
        assertFalse(bullets.isEmpty(), "shoot() should generate at least one bullet");

        // Expected Result 3: Bullets should be positioned in front of the aircraft
        for (BaseBullet bullet : bullets) {
            assertNotNull(bullet, "Bullet object should not be null");
            assertTrue(bullet.getLocationY() < heroAircraft.getLocationY(),
                "Bullets should be generated in front of the aircraft");
        }
    }

    // ==================== Test Case 2: AbstractAircraft.decreaseHp() ====================

    /**
     * Test Case ID: HA002
     * Method Under Test: AbstractAircraft.decreaseHp()
     * Test Method: testDecreaseHp()
     * Description: Verify HP decrease and aircraft state change when HP reaches 0
     * Black-box Testing: Boundary value analysis (HP = 0, HP > 0, HP < 0)
     * White-box Testing: Branch coverage (hp > 0 and hp <= 0 branches)
     */
    @Test
    @DisplayName("Test decreaseHp() method - Verify HP decrease and boundary cases")
    void testDecreaseHp() {
        int initialHp = heroAircraft.getHp();

        // Test Step 1: Decrease HP by a normal amount (within bounds)
        int decrease = 100;
        heroAircraft.decreaseHp(decrease);

        // Expected Result 1: HP should decrease correctly
        assertEquals(initialHp - decrease, heroAircraft.getHp(),
            "HP should decrease by the specified amount");

        // Test Step 2: Decrease HP to exactly 0 (boundary value)
        heroAircraft.setHp(50);
        heroAircraft.decreaseHp(50);

        // Expected Result 2: HP should be 0 and aircraft should be invalid
        assertEquals(0, heroAircraft.getHp(),
            "HP should be 0 when decreased to boundary");
        assertTrue(heroAircraft.notValid(),
            "Aircraft should be marked as invalid when HP reaches 0");

        // Test Step 3: Decrease HP below 0 (boundary value)
        HeroAircraft.resetInstance();
        heroAircraft = HeroAircraft.getInstance(400, 550, 0, 0, 1000);
        heroAircraft.decreaseHp(heroAircraft.getHp() + 100);

        // Expected Result 3: HP should not go below 0
        assertEquals(0, heroAircraft.getHp(),
            "HP should be clamped to 0, not negative");
        assertTrue(heroAircraft.notValid(),
            "Aircraft should be invalid when HP goes below 0");
    }

    // ==================== Test Case 3: AbstractFlyingObject.setLocation() ====================

    /**
     * Test Case ID: HA003
     * Method Under Test: AbstractFlyingObject.setLocation()
     * Test Method: testSetLocation()
     * Description: Verify position setting with various coordinate values
     * Black-box Testing: Boundary value analysis (0, positive values, large values)
     * White-box Testing: Statement coverage - verify coordinate assignment logic
     */
    @Test
    @DisplayName("Test setLocation() method - Verify position setting")
    void testSetLocation() {
        // Test Step 1: Set normal position coordinates
        double newX = 200.5;
        double newY = 300.7;
        heroAircraft.setLocation(newX, newY);

        // Expected Result 1: Coordinates should be set correctly (cast to int)
        assertEquals((int) newX, heroAircraft.getLocationX(),
            "X coordinate should be set correctly (cast to int)");
        assertEquals((int) newY, heroAircraft.getLocationY(),
            "Y coordinate should be set correctly (cast to int)");

        // Test Step 2: Set boundary value - origin (0, 0)
        heroAircraft.setLocation(0, 0);

        // Expected Result 2: Should handle origin coordinates
        assertEquals(0, heroAircraft.getLocationX(),
            "Should be able to set X coordinate to 0");
        assertEquals(0, heroAircraft.getLocationY(),
            "Should be able to set Y coordinate to 0");

        // Test Step 3: Set large coordinate values
        heroAircraft.setLocation(1000, 1000);

        // Expected Result 3: Should handle large coordinates
        assertEquals(1000, heroAircraft.getLocationX(),
            "Should be able to set large X coordinate");
        assertEquals(1000, heroAircraft.getLocationY(),
            "Should be able to set large Y coordinate");
    }

    // ==================== Test Case 4: AbstractFlyingObject.vanish() ====================

    /**
     * Test Case ID: HA004
     * Method Under Test: AbstractFlyingObject.vanish()
     * Test Method: testVanish()
     * Description: Verify aircraft state changes from valid to invalid
     * Black-box Testing: State transition testing (valid -> invalid)
     * White-box Testing: Branch coverage - verify isValid flag modification
     */
    @Test
    @DisplayName("Test vanish() method - Verify state transition")
    void testVanish() {
        // Precondition Verification: Aircraft should be valid initially
        assertFalse(heroAircraft.notValid(),
            "Aircraft should be in valid state initially");

        // Test Step: Call vanish() to mark aircraft as invalid
        heroAircraft.vanish();

        // Expected Result: Aircraft should be marked as invalid
        assertTrue(heroAircraft.notValid(),
            "Aircraft should be marked as invalid after calling vanish()");

        // Additional Verification: Calling vanish() multiple times should be safe
        heroAircraft.vanish();
        assertTrue(heroAircraft.notValid(),
            "Aircraft should remain invalid after multiple vanish() calls");
    }
}
