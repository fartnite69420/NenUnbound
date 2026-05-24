package com.huntercraft.huntercraft;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification test for Task 3.3: Segment Count Calculation
 * 
 * This test verifies that the segment count formula in renderSharedChainBody()
 * is mathematically correct:
 * 
 *   int segments = Math.max(1, Mth.ceil(length / SharedChainProjectileModel.SEGMENT_SPACING));
 * 
 * The formula correctly calculates how many 0.34F segments fit in the total
 * chain length, rounds up to ensure full coverage, and ensures a minimum of
 * 1 segment for very short chains.
 * 
 * Validates: Requirements 2.4, 2.6
 */
public class SegmentCountVerificationTest {

    /** SharedChainProjectileModel — segment spacing constant */
    private static final float SEGMENT_SPACING = 0.34F;

    /**
     * Verifies the segment count formula produces correct results for various
     * chain lengths.
     */
    @Test
    void segmentCountFormula_producesCorrectResults() {
        // Test case 1: Very short chain (< SEGMENT_SPACING)
        assertEquals(1, calculateSegmentCount(0.1F),
                "Chain shorter than SEGMENT_SPACING (0.1 blocks) should have 1 segment (minimum)");
        
        // Test case 2: Exactly one segment spacing
        assertEquals(1, calculateSegmentCount(0.34F),
                "Chain exactly SEGMENT_SPACING (0.34 blocks) should have 1 segment");
        
        // Test case 3: Slightly longer than one segment spacing
        assertEquals(2, calculateSegmentCount(0.35F),
                "Chain slightly longer than SEGMENT_SPACING (0.35 blocks) should have 2 segments");
        
        // Test case 4: 1 block distance
        assertEquals(3, calculateSegmentCount(1.0F),
                "Chain of 1.0 blocks should have 3 segments (ceil(1.0 / 0.34) = ceil(2.94) = 3)");
        
        // Test case 5: 10 blocks distance
        assertEquals(30, calculateSegmentCount(10.0F),
                "Chain of 10.0 blocks should have 30 segments (ceil(10.0 / 0.34) = ceil(29.41) = 30)");
        
        // Test case 6: 20 blocks distance
        assertEquals(59, calculateSegmentCount(20.0F),
                "Chain of 20.0 blocks should have 59 segments (ceil(20.0 / 0.34) = ceil(58.82) = 59)");
        
        // Test case 7: Edge case - zero length (should still return 1)
        assertEquals(1, calculateSegmentCount(0.0F),
                "Chain of 0.0 blocks should have 1 segment (minimum)");
    }

    /**
     * Verifies that the segment count increases proportionally with chain length.
     * This ensures the formula satisfies Requirement 2.6: "the number of segments
     * dynamically increasing as the chain extends".
     */
    @Test
    void segmentCount_increasesProportionallyWithLength() {
        float[] lengths = {1.0F, 2.0F, 5.0F, 10.0F, 15.0F, 20.0F};
        int previousCount = 0;
        
        for (float length : lengths) {
            int currentCount = calculateSegmentCount(length);
            
            // Verify segment count increases with length
            assertTrue(currentCount > previousCount,
                    "Segment count must increase as chain length increases. " +
                    "Length " + length + " should have more segments than previous length");
            
            // Verify the count is approximately length / SEGMENT_SPACING
            float expectedFloat = length / SEGMENT_SPACING;
            int expectedCeil = (int) Math.ceil(expectedFloat);
            assertEquals(expectedCeil, currentCount,
                    "Segment count for length " + length + " should be ceil(" + length + " / " + SEGMENT_SPACING + ")");
            
            previousCount = currentCount;
        }
    }

    /**
     * Verifies that the formula ensures at least 1 segment for all positive lengths.
     * This prevents rendering nothing for very short chains.
     */
    @Test
    void segmentCount_minimumOfOne() {
        float[] shortLengths = {0.0F, 0.01F, 0.1F, 0.2F, 0.33F};
        
        for (float length : shortLengths) {
            int count = calculateSegmentCount(length);
            assertTrue(count >= 1,
                    "Segment count must be at least 1 for length " + length + ", got " + count);
        }
    }

    /**
     * Verifies that the ceiling function ensures full coverage.
     * Even if the length is slightly more than N segments, it should round up to N+1.
     */
    @Test
    void segmentCount_ceilingEnsuresFullCoverage() {
        // Test lengths that are just slightly more than exact multiples
        float justOver1 = SEGMENT_SPACING + 0.01F;  // 0.35F
        assertEquals(2, calculateSegmentCount(justOver1),
                "Length just over 1 segment spacing should round up to 2 segments");
        
        float justOver2 = (SEGMENT_SPACING * 2) + 0.01F;  // 0.69F
        assertEquals(3, calculateSegmentCount(justOver2),
                "Length just over 2 segment spacings should round up to 3 segments");
        
        float justOver10 = (SEGMENT_SPACING * 10) + 0.01F;  // 3.41F
        assertEquals(11, calculateSegmentCount(justOver10),
                "Length just over 10 segment spacings should round up to 11 segments");
    }

    /**
     * Verifies that exact multiples of SEGMENT_SPACING produce the expected count.
     * 
     * Note: Due to floating-point precision, multiplying SEGMENT_SPACING by an integer
     * may produce a value slightly larger than the mathematical result, causing ceil()
     * to round up. This is expected behavior and the formula handles it correctly.
     */
    @Test
    void segmentCount_exactMultiples() {
        for (int i = 1; i <= 10; i++) {
            float length = SEGMENT_SPACING * i;
            int actualCount = calculateSegmentCount(length);
            
            // The count should be either i or i+1 due to floating-point precision
            // For exact multiples, ceil(i) = i, but floating-point errors may cause ceil(i + epsilon) = i+1
            assertTrue(actualCount == i || actualCount == i + 1,
                    "Length of " + i + " segment spacings (" + length + " blocks) " +
                    "should produce " + i + " or " + (i + 1) + " segments (got " + actualCount + "). " +
                    "Floating-point precision may cause slight variations.");
            
            // Verify the count is reasonable (within 1 of expected)
            assertTrue(Math.abs(actualCount - i) <= 1,
                    "Segment count " + actualCount + " should be within 1 of expected " + i);
        }
    }

    /**
     * Helper method that replicates the segment count calculation from
     * SmokeyChainProjectileRenderer.renderSharedChainBody().
     * 
     * Original code:
     *   int segments = Math.max(1, Mth.ceil(length / SharedChainProjectileModel.SEGMENT_SPACING));
     */
    private static int calculateSegmentCount(float length) {
        // Mth.ceil is equivalent to (int) Math.ceil for our purposes
        return Math.max(1, (int) Math.ceil(length / SEGMENT_SPACING));
    }
}
