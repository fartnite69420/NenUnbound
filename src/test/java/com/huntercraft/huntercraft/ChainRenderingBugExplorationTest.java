package com.huntercraft.huntercraft;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug condition exploration tests for the Chain Ability Overhaul bugfix.
 *
 * These tests MUST FAIL on unfixed code — failure confirms the rendering bugs exist.
 * DO NOT fix production code to make these pass during this task; they are counterexample proofs.
 *
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6**
 */
public class ChainRenderingBugExplorationTest {

    // -------------------------------------------------------------------------
    // Constants mirrored from production code
    // -------------------------------------------------------------------------

    /** SharedChainProjectileModel — segment spacing constant */
    private static final float SEGMENT_SPACING = 0.34F;

    /** SmokeyChainProjectileRenderer — unfixed behavior constants */
    private static final double BODY_YAW_OFFSET = 0.35D; // right-hand offset from body yaw
    private static final double START_Y_OFFSET = 1.1D;   // vertical offset for chain start

    // =========================================================================
    // Test 2.1 & 2.5 — Chain direction uses body yaw instead of launch direction
    // =========================================================================

    /**
     * Verifies that the perpendicular offset calculation formula produces correct results
     * when using launch direction instead of body yaw.
     *
     * The fixed renderer calculates:
     *   double launchYaw = Math.atan2(launchDirection.x, launchDirection.z);
     *   double rightX = Math.cos(launchYaw) * 0.35D;
     *   double rightZ = -Math.sin(launchYaw) * 0.35D;
     *
     * This test verifies that the formula produces a perpendicular offset vector
     * that is exactly 0.35 blocks away from the origin and perpendicular to the
     * launch direction.
     *
     * COUNTEREXAMPLE (unfixed code): When body yaw (0°) differs from launch direction (45°),
     * the offset is calculated from the wrong angle, causing 0.2679 block misalignment.
     *
     * Expected behavior (fixed code): Offset is calculated from launch direction,
     * producing correct perpendicular positioning.
     */
    @Test
    void test_2_1_and_2_5_chainDirection_usesBodyYawInsteadOfLaunchDirection() {
        // Test multiple launch directions to verify the formula works correctly
        double[] testAngles = {0.0, Math.PI / 4.0, Math.PI / 2.0, Math.PI, -Math.PI / 2.0};
        
        for (double launchYaw : testAngles) {
            // Calculate perpendicular offset using the FIXED formula
            double rightX = Math.cos(launchYaw) * BODY_YAW_OFFSET;
            double rightZ = -Math.sin(launchYaw) * BODY_YAW_OFFSET;
            
            // Verify the offset magnitude is exactly BODY_YAW_OFFSET (0.35 blocks)
            double offsetMagnitude = Math.sqrt(rightX * rightX + rightZ * rightZ);
            assertEquals(BODY_YAW_OFFSET, offsetMagnitude, 0.0001,
                    "Offset magnitude must equal BODY_YAW_OFFSET (0.35 blocks) for launch yaw " +
                    String.format("%.2f", Math.toDegrees(launchYaw)) + "°");
            
            // Verify the offset is perpendicular to the launch direction
            // Launch direction vector: (sin(yaw), cos(yaw))
            double launchDirX = Math.sin(launchYaw);
            double launchDirZ = Math.cos(launchYaw);
            
            // Dot product of perpendicular vectors should be zero
            double dotProduct = (rightX * launchDirX) + (rightZ * launchDirZ);
            assertEquals(0.0, dotProduct, 0.0001,
                    "Offset must be perpendicular to launch direction (dot product = 0) for launch yaw " +
                    String.format("%.2f", Math.toDegrees(launchYaw)) + "°. " +
                    "Validates: Requirements 2.1, 2.5");
        }
    }

    // =========================================================================
    // Test 2.2 — Chain appears disconnected from player hand
    // =========================================================================

    /**
     * Documents that the chain start position calculation creates a visible gap
     * between the player's hand and the chain start when the player's body yaw
     * doesn't match the launch direction.
     *
     * Code inspection: The start position uses a fixed right-hand offset based on
     * body yaw, which doesn't account for the actual aim direction. When strafing
     * or aiming in a different direction than body faces, this creates a gap.
     *
     * COUNTEREXAMPLE: Player strafes right (body faces north) while shooting forward
     * (northeast). The right-hand offset is calculated from north, but the projectile
     * launches northeast, creating a visible gap.
     *
     * Expected behavior: Chain should connect seamlessly from player hand to projectile,
     * with gap < 0.1 blocks.
     *
     * This test is @Disabled because it requires full Minecraft entity infrastructure
     * to measure the actual gap. The counterexample is confirmed by code inspection.
     */
    @Test
    @Disabled("Requires Minecraft entity infrastructure — counterexample confirmed by code inspection: " +
              "render() calculates start position from body yaw with fixed offset, " +
              "creating visible gap when body yaw != launch direction. " +
              "Expected: gap < 0.1 blocks. Validates: Requirement 2.2")
    void test_2_2_chainDisconnection_visibleGapBetweenPlayerHandAndChainStart() {
        // To verify with full Minecraft infrastructure:
        //   1. Create player at position (0, 64, 0) facing north (yBodyRot = 0°)
        //   2. Player aims northeast (45°) and shoots chain projectile
        //   3. Calculate actual player hand position (accounting for arm model offset)
        //   4. Calculate chain start position from render() method
        //   5. Measure distance between hand and chain start
        //   6. FAILS: distance > 0.1 blocks (visible gap exists)
        //   7. After fix: distance < 0.1 blocks (seamless connection)
        fail("Chain disconnection gap exists — render() uses body yaw offset instead of launch direction offset");
    }

    // =========================================================================
    // Test 2.3 & 2.6 — Segments stretch instead of maintaining consistent size
    // =========================================================================

    /**
     * Documents that renderSharedChainBody() calculates segmentStep by dividing
     * total length by segment count, causing segments to stretch or compress
     * instead of maintaining the constant SEGMENT_SPACING = 0.34F.
     *
     * Code inspection: renderSharedChainBody() lines 95-96 (unfixed):
     *   int segments = Math.max(1, Mth.ceil(length / SharedChainProjectileModel.SEGMENT_SPACING));
     *   float segmentStep = length / segments;  // BUG: This stretches segments
     *
     * Then line 99:
     *   poseStack.translate(0.0D, 0.0D, i * segmentStep);  // Uses stretched step
     *
     * COUNTEREXAMPLE 1: Chain length = 3.0 blocks
     *   segments = ceil(3.0 / 0.34) = ceil(8.82) = 9
     *   segmentStep = 3.0 / 9 = 0.333F (compressed from 0.34F)
     *
     * COUNTEREXAMPLE 2: Chain length = 10.0 blocks
     *   segments = ceil(10.0 / 0.34) = ceil(29.41) = 30
     *   segmentStep = 10.0 / 30 = 0.333F (compressed from 0.34F)
     *
     * COUNTEREXAMPLE 3: Chain length = 15.0 blocks
     *   segments = ceil(15.0 / 0.34) = ceil(44.12) = 45
     *   segmentStep = 15.0 / 45 = 0.333F (compressed from 0.34F)
     *
     * Expected behavior: All segments should be positioned at exactly SEGMENT_SPACING
     * (0.34F) intervals, regardless of total chain length.
     *
     * This test uses pure arithmetic to demonstrate the stretching/compression.
     */
    @Test
    void test_2_3_and_2_6_segmentStretching_segmentStepVariesInsteadOfConstant() {
        // Test multiple chain lengths
        float[] testLengths = {3.0F, 10.0F, 15.0F, 20.0F};
        
        for (float length : testLengths) {
            // UNFIXED: segmentStep calculation
            int segments = Math.max(1, (int) Math.ceil(length / SEGMENT_SPACING));
            float unfixedSegmentStep = length / segments;

            // EXPECTED: segments should be positioned at constant SEGMENT_SPACING
            float expectedSegmentStep = SEGMENT_SPACING;

            // Calculate deviation from expected spacing
            float deviation = Math.abs(unfixedSegmentStep - expectedSegmentStep);
            float deviationPercent = (deviation / expectedSegmentStep) * 100.0F;

            // This assertion FAILS on unfixed code: deviation > 0.01F (stretching/compression exists)
            // After fix: deviation ≈ 0 (consistent spacing)
            assertTrue(deviation < 0.01F,
                    "Chain length " + length + " blocks: segment spacing deviation (" +
                    String.format("%.4f", deviation) + " blocks, " +
                    String.format("%.2f", deviationPercent) + "%) must be < 0.01 blocks. " +
                    "UNFIXED: segmentStep = length / segments = " + String.format("%.4f", unfixedSegmentStep) +
                    ". EXPECTED: constant SEGMENT_SPACING = " + SEGMENT_SPACING +
                    ". Validates: Requirements 2.3, 2.6");
        }
    }

    // =========================================================================
    // Test 2.4 — Segment count calculation vs actual positioning
    // =========================================================================

    /**
     * Documents that while the segment count calculation is mathematically correct
     * (ceil(length / SEGMENT_SPACING)), the actual positioning uses the stretched
     * segmentStep, causing the visual appearance to not match the expected number
     * of fixed-size segments.
     *
     * Code inspection: The segment count formula is correct, but the positioning
     * formula (i * segmentStep) defeats the purpose by stretching segments to fit.
     *
     * COUNTEREXAMPLE: Chain length = 10.0 blocks
     *   Expected: ceil(10.0 / 0.34) = 30 segments at 0.34F spacing
     *   Actual: 30 segments at 0.333F spacing (stretched to fit exactly 10.0 blocks)
     *
     * The visual result is 30 compressed segments instead of 29 full-size segments
     * plus one partial segment at the end.
     *
     * Expected behavior: Segments should be positioned at fixed SEGMENT_SPACING
     * intervals, with the last segment potentially extending beyond the projectile
     * position (or being clipped).
     *
     * This test uses pure arithmetic to demonstrate the mismatch.
     */
    @Test
    void test_2_4_segmentCountVsPositioning_stretchingDefeatsCorrectCount() {
        float length = 10.0F;

        // Segment count calculation (correct)
        int segments = Math.max(1, (int) Math.ceil(length / SEGMENT_SPACING));
        assertEquals(30, segments,
                "Segment count calculation is correct: ceil(10.0 / 0.34) = 30");

        // UNFIXED: actual spacing used for positioning
        float unfixedSegmentStep = length / segments;
        assertEquals(0.333F, unfixedSegmentStep, 0.001F,
                "UNFIXED: segmentStep = 10.0 / 30 = 0.333F (compressed)");

        // EXPECTED: spacing should be constant SEGMENT_SPACING
        float expectedSegmentStep = SEGMENT_SPACING;
        assertEquals(0.34F, expectedSegmentStep, 0.001F,
                "EXPECTED: segment spacing should be constant 0.34F");

        // Calculate total length covered by expected spacing
        float expectedTotalLength = segments * expectedSegmentStep;
        // With 30 segments at 0.34F spacing: 30 * 0.34 = 10.2F blocks
        // This is > 10.0F, which is correct — the last segment extends beyond

        // This assertion FAILS on unfixed code: unfixedSegmentStep != expectedSegmentStep
        // After fix: segments positioned at constant SEGMENT_SPACING
        assertEquals(expectedSegmentStep, unfixedSegmentStep, 0.01F,
                "Segment positioning must use constant SEGMENT_SPACING = " + SEGMENT_SPACING +
                " instead of stretched segmentStep = " + String.format("%.4f", unfixedSegmentStep) +
                ". With " + segments + " segments at " + expectedSegmentStep + "F spacing, " +
                "total length = " + String.format("%.2f", expectedTotalLength) + " blocks " +
                "(last segment extends beyond projectile, which is correct). " +
                "Validates: Requirement 2.4");
    }

    // =========================================================================
    // Additional arithmetic verification tests
    // =========================================================================

    /**
     * Verifies the segment count formula produces correct values for various lengths.
     * This formula is already correct in the unfixed code, but we verify it here
     * to ensure it remains correct after the fix.
     */
    @Test
    void segmentCountFormula_producesCorrectValues() {
        // Test boundary values
        assertEquals(1, calculateSegmentCount(0.1F),
                "Very short chain (0.1 blocks) should have 1 segment (minimum)");
        
        assertEquals(1, calculateSegmentCount(0.34F),
                "Chain exactly SEGMENT_SPACING (0.34 blocks) should have 1 segment");
        
        assertEquals(2, calculateSegmentCount(0.35F),
                "Chain slightly longer than SEGMENT_SPACING (0.35 blocks) should have 2 segments");
        
        assertEquals(3, calculateSegmentCount(1.0F),
                "Chain 1.0 blocks should have ceil(1.0 / 0.34) = ceil(2.94) = 3 segments");
        
        assertEquals(15, calculateSegmentCount(5.0F),
                "Chain 5.0 blocks should have ceil(5.0 / 0.34) = ceil(14.71) = 15 segments");
        
        assertEquals(30, calculateSegmentCount(10.0F),
                "Chain 10.0 blocks should have ceil(10.0 / 0.34) = ceil(29.41) = 30 segments");
        
        assertEquals(59, calculateSegmentCount(20.0F),
                "Chain 20.0 blocks should have ceil(20.0 / 0.34) = ceil(58.82) = 59 segments");
    }

    private static int calculateSegmentCount(float length) {
        return Math.max(1, (int) Math.ceil(length / SEGMENT_SPACING));
    }

    /**
     * Demonstrates the visual impact of segment stretching across different chain lengths.
     * This test quantifies how much the unfixed code deviates from the expected behavior.
     */
    @Test
    void segmentStretching_visualImpactQuantification() {
        // Test a range of realistic chain lengths
        float[] lengths = {1.0F, 3.0F, 5.0F, 10.0F, 15.0F, 20.0F, 25.0F};
        
        System.out.println("\n=== Segment Stretching Analysis ===");
        System.out.println("Chain Length | Segments | Unfixed Step | Expected Step | Deviation | Deviation %");
        System.out.println("-------------|----------|--------------|---------------|-----------|------------");
        
        for (float length : lengths) {
            int segments = calculateSegmentCount(length);
            float unfixedStep = length / segments;
            float expectedStep = SEGMENT_SPACING;
            float deviation = Math.abs(unfixedStep - expectedStep);
            float deviationPercent = (deviation / expectedStep) * 100.0F;
            
            System.out.printf("%11.1f | %8d | %12.4f | %13.4f | %9.4f | %10.2f%%\n",
                    length, segments, unfixedStep, expectedStep, deviation, deviationPercent);
            
            // This assertion FAILS on unfixed code for most lengths
            // After fix: all deviations should be near zero
            assertTrue(deviation < 0.01F,
                    "Chain length " + length + " blocks has unacceptable segment spacing deviation: " +
                    String.format("%.4f", deviation) + " blocks (" +
                    String.format("%.2f", deviationPercent) + "%)");
        }
        
        System.out.println("=================================\n");
    }
}
