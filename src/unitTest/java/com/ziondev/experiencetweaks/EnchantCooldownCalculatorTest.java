package com.ziondev.experiencetweaks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link EnchantCooldownCalculator}.
 * No Minecraft context required — pure math only.
 *
 * Formula: increment = max(1, ceil(buttonLevel × bias × 50 / √currentLevel))
 *
 * Run with: ./gradlew unitTest --rerun
 */
@DisplayName("EnchantCooldownCalculator")
class EnchantCooldownCalculatorTest {

    // -------------------------------------------------------------------------
    // computeNextLevels — contracts that must hold for ALL inputs
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("All buttons always require at least currentLevel + 1")
    void allButtonsAtLeastPlusOne() {
        double[] biasValues = {0.0, 0.1, 0.5, 1.0};
        for (double bias : biasValues) {
            for (int level = 1; level <= 2000; level++) {
                int[] next = EnchantCooldownCalculator.computeNextLevels(level, bias);
                for (int b = 0; b < 3; b++) {
                    assertTrue(next[b] >= level + 1,
                            "bias=" + bias + " level=" + level + " btn" + b + "=" + next[b]);
                }
            }
        }
    }

    @Test
    @DisplayName("Button 2 is always at least Button 1 + 1")
    void button2AlwaysAboveButton1() {
        double[] biasValues = {0.0, 0.1, 0.5, 1.0};
        for (double bias : biasValues) {
            for (int level = 1; level <= 2000; level++) {
                int[] next = EnchantCooldownCalculator.computeNextLevels(level, bias);
                assertTrue(next[1] >= next[0] + 1,
                        "bias=" + bias + " level=" + level + ": btn2=" + next[1] + " btn1=" + next[0]);
            }
        }
    }

    @Test
    @DisplayName("Button 3 is always at least Button 2 + 1")
    void button3AlwaysAboveButton2() {
        double[] biasValues = {0.0, 0.1, 0.5, 1.0};
        for (double bias : biasValues) {
            for (int level = 1; level <= 2000; level++) {
                int[] next = EnchantCooldownCalculator.computeNextLevels(level, bias);
                assertTrue(next[2] >= next[1] + 1,
                        "bias=" + bias + " level=" + level + ": btn3=" + next[2] + " btn2=" + next[1]);
            }
        }
    }

    // -------------------------------------------------------------------------
    // computeNextLevels — concrete values (formula: ceil(b × bias × 50 / √L))
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Level 0 or negative — increment defaults to 1, gap enforced")
    void levelZeroOrNegative() {
        // currentLevel <= 0: increment = 1 for all → [1,1,1] → gap → [1,2,3]
        int[] next0 = EnchantCooldownCalculator.computeNextLevels(0, 0.5);
        assertArrayEquals(new int[]{1, 2, 3}, next0,
                "Level 0: expected [1,2,3] but got " + arrayToString(next0));

        // level -5 + 1 = -4, gap → [-4, -3, -2]
        int[] nextNeg = EnchantCooldownCalculator.computeNextLevels(-5, 0.5);
        assertArrayEquals(new int[]{-4, -3, -2}, nextNeg,
                "Level -5: expected [-4,-3,-2] but got " + arrayToString(nextNeg));
    }

    @Test
    @DisplayName("bias 0.0 — always collapses to minimum gap [L+1, L+2, L+3]")
    void biasZeroAlwaysMinimum() {
        // With bias=0.0, ceil(b × 0 × 50 / √L) = 0 → increment = max(1,0) = 1 for all
        // → gap enforcement → [L+1, L+2, L+3]
        int[] levels = {10, 100, 500, 1000};
        for (int L : levels) {
            int[] next = EnchantCooldownCalculator.computeNextLevels(L, 0.0);
            assertArrayEquals(new int[]{L + 1, L + 2, L + 3}, next,
                    "bias=0 level=" + L + ": expected [" + (L+1) + "," + (L+2) + "," + (L+3) + "] got " + arrayToString(next));
        }
    }

    // -------------------------------------------------------------------------
    // Parameterized scenarios — formula reference table
    // -------------------------------------------------------------------------
    //
    // Formula per button: ceil(buttonLevel × bias × 50 / sqrt(level))
    //
    // Calculation guide:
    //   √10  ≈ 3.162   √50  ≈ 7.071   √100 = 10.0
    //   √200 ≈ 14.14   √500 ≈ 22.36   √1000 ≈ 31.62
    //
    // bias=0.5, C=50: effective multiplier per button = [25, 50, 75]
    //   Level  10: [ceil(25/3.162), ceil(50/3.162), ceil(75/3.162)] = [8,16,24]
    //   Level  50: [ceil(25/7.071), ceil(50/7.071), ceil(75/7.071)] = [4,8,11]
    //   Level 100: [ceil(25/10.0),  ceil(50/10.0),  ceil(75/10.0) ] = [3,5,8]
    //   Level 200: [ceil(25/14.14), ceil(50/14.14), ceil(75/14.14)] = [2,4,6]  → gap: [2,4,6] ok
    //   Level 500: [ceil(25/22.36), ceil(50/22.36), ceil(75/22.36)] = [2,3,4]  → gap ok
    //   Level 1000:[ceil(25/31.62), ceil(50/31.62), ceil(75/31.62)] = [1,2,3]  → gap ok

    /**
     * Add rows here to test specific level/bias combinations.
     * Format: currentLevel, bias, expectedBtn1, expectedBtn2, expectedBtn3
     *
     * Reference: increment[b] = max(1, ceil((b+1) × bias × 50 / sqrt(level)))
     */
    @ParameterizedTest(name = "level={0} bias={1} → [{2},{3},{4}]")
    @CsvSource({
            //  level, bias,  b1,  b2,  b3
            // --- bias 0.5 (default) ---
            //   √10≈3.162: ceil(25/3.162)=8, ceil(50/3.162)=16, ceil(75/3.162)=24
            "   10,  0.5,   18,  26,  34",
            //   √50≈7.071: ceil(25/7.071)=4, ceil(50/7.071)=8,  ceil(75/7.071)=11
            "   50,  0.5,   54,  58,  61",
            //   √100=10.0: ceil(25/10)=3,    ceil(50/10)=5,     ceil(75/10)=8
            "  100,  0.5,  103, 105, 108",
            //   √200≈14.14: ceil(25/14.14)=2, ceil(50/14.14)=4, ceil(75/14.14)=6
            "  200,  0.5,  202, 204, 206",
            //   √500≈22.36: ceil(25/22.36)=2, ceil(50/22.36)=3, ceil(75/22.36)=4
            "  500,  0.5,  502, 503, 504",
            //   √1000≈31.62: ceil(25/31.62)=1, ceil(50/31.62)=2, ceil(75/31.62)=3
            " 1000,  0.5, 1001, 1002, 1003",

            // --- bias 0.1 ---
            //   √100=10.0: ceil(5/10)=1, ceil(10/10)=1, ceil(15/10)=2 → gap: [1,2,3] → [101,102,103]
            "  100,  0.1,  101, 102, 103",
            //   √500≈22.36: ceil(5/22.36)=1, ceil(10/22.36)=1, ceil(15/22.36)=1 → [501,502,503]
            "  500,  0.1,  501, 502, 503",

            // --- bias 1.0 (maximum) ---
            //   √100=10.0: ceil(50/10)=5, ceil(100/10)=10, ceil(150/10)=15
            "  100,  1.0,  105, 110, 115",
            //   √500≈22.36: ceil(50/22.36)=3, ceil(100/22.36)=5, ceil(150/22.36)=7
            "  500,  1.0,  503, 505, 507",
            //   √1000≈31.62: ceil(50/31.62)=2, ceil(100/31.62)=4, ceil(150/31.62)=5
            " 1000,  1.0, 1002, 1004, 1005",
    })
    @DisplayName("Concrete level/bias → expected next levels")
    void parametrized(int level, double bias, int b1, int b2, int b3) {
        int[] next = EnchantCooldownCalculator.computeNextLevels(level, bias);
        assertArrayEquals(new int[]{b1, b2, b3}, next,
                "level=" + level + " bias=" + bias + " → got " + arrayToString(next));
    }

    // -------------------------------------------------------------------------
    // computeFirstUseLevels
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("First use: config minimums dominate for low-level player")
    void firstUseConfigMinDominates() {
        // level 9, config [10, 15, 20]
        // player-based: ceil(9*1/3)=3, ceil(9*2/3)=6, ceil(9*3/3)=9
        // max: 10, 15, 20 — gap already > 1, no correction needed
        int[] result = EnchantCooldownCalculator.computeFirstUseLevels(9, new int[]{10, 15, 20});
        assertArrayEquals(new int[]{10, 15, 20}, result,
                "Expected [10,15,20] but got " + arrayToString(result));
    }

    @Test
    @DisplayName("First use: player level dominates for high-level player")
    void firstUsePlayerLevelDominates() {
        // level 50, config [10, 15, 20]
        // player-based: ceil(50*1/3)=17, ceil(50*2/3)=34, ceil(50*3/3)=50
        // max: 17, 34, 50 — gap 34-17=17 >= 1, 50-34=16 >= 1, no correction
        int[] result = EnchantCooldownCalculator.computeFirstUseLevels(50, new int[]{10, 15, 20});
        assertArrayEquals(new int[]{17, 34, 50}, result,
                "Expected [17,34,50] but got " + arrayToString(result));
    }

    @Test
    @DisplayName("First use: gap enforced when config values are too close")
    void firstUseGapEnforced() {
        // level 1, config [5, 5, 5] — equal values, gap must be enforced
        // player-based: ceil(1/3)=1, ceil(2/3)=1, ceil(3/3)=1
        // max: 5, 5, 5 → after gap: [5, 6, 7]
        int[] result = EnchantCooldownCalculator.computeFirstUseLevels(1, new int[]{5, 5, 5});
        assertArrayEquals(new int[]{5, 6, 7}, result,
                "Expected [5,6,7] but got " + arrayToString(result));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String arrayToString(int[] arr) {
        if (arr == null) return "null";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(arr[i]);
        }
        return sb.append("]").toString();
    }
}
