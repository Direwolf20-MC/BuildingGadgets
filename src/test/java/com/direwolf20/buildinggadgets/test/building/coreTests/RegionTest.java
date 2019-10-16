package com.direwolf20.buildinggadgets.test.building.coreTests;

import com.direwolf20.buildinggadgets.common.building.Region;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegionTest {

    @Test
    void sizeMethodShouldReturnPositiveInteger() {
        Region region = new Region(-1, -1, -1, -16, -16, -16);
        assertEquals(4096, region.size());

        Region regionRegular = new Region(1, 1, 1, 16, 16, 16);
        assertEquals(4096, regionRegular.size());
    }

    @Test
    void sizeMethodShouldReturnPositiveIntegerCaseAwayFromOrigin() {
        Region region = new Region(-33, -33, -33, -48, -48, -48);
        assertEquals(4096, region.size());

        Region regionRegular = new Region(33, 33, 33, 48, 48, 48);
        assertEquals(4096, regionRegular.size());
    }

    @Test
    void translationShouldAddAllVertexesToTranslationCaseAllPositive() {
        Region region = new Region(1, 1, 1, 8, 8, 8);
        Region expected = new Region(9, 9, 9, 16, 16, 16);
        assertEquals(expected, region.translate(8, 8, 8));
    }

    @Test
    void translationShouldAddAllVertexesToTranslationCaseAllNegative() {
        Region region = new Region(-1, -1, -1, -8, -8, -8);
        Region expected = new Region(-9, -9, -9, -16, -16, -16);
        assertEquals(expected, region.translate(-8, -8, -8));
    }

    @Test
    void translationShouldAddAllVertexesToTranslationCaseOrigin() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        Region expected = new Region(0, 0, 0, 8, 8, 8);
        assertEquals(expected, region.translate(4, 4, 4));
    }

    @Test
    void expandShouldDecreaseRegionSizeCaseOrigin() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        Region expected = new Region(-6, -6, -6, 6, 6, 6);
        assertEquals(expected, region.expand(2));
    }

    @Test
    void collapseShouldDecreaseRegionSizeCaseOrigin() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        Region expected = new Region(-2, -2, -2, 2, 2, 2);
        assertEquals(expected, region.collapse(2));
    }

    @Test
    void growShouldIncreaseAndOnlyIncreaseMaxCoordinates() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        Region expected = new Region(-4, -4, -4, 6, 6, 6);
        assertEquals(expected, region.grow(2));
    }

    @Test
    void shrinkShouldDecreaseAndOnlyDecreaseMaxCoordinates() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        Region expected = new Region(-4, -4, -4, 2, 2, 2);
        assertEquals(expected, region.shrink(2));
    }

    @Test
    void intersectShouldReturnIntersectionBoxThatsSmallerThanSources() {
        Region region1 = new Region(-2, -2, -2, 4, 4, 4);
        Region region2 = new Region(-4, -4, -4, 2, 2, 2);
        Region expected = new Region(-2, -2, -2, 2, 2, 2);
        assertEquals(expected, region1.intersect(region2));
    }

    @Test
    void intersectShouldReturnSelfWhenParameterIsSelf() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        assertEquals(region, region.intersect(region));
    }

    @Test
    void unionShouldReturnUnionBoxThatsLargerThanSources() {
        Region region1 = new Region(-2, -2, -2, 4, 4, 4);
        Region region2 = new Region(-4, -4, -4, 2, 2, 2);
        Region expected = new Region(-4, -4, -4, 4, 4, 4);
        assertEquals(expected, region1.union(region2));
    }

    @Test
    void unionShouldReturnSelfWhenParameterIsSelf() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        assertEquals(region, region.union(region));
    }

    @Test
    void xSizeShouldReturnDifferenceBetweenInputsPlus1Hardcoded() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        assertEquals(Math.abs(4 - -4) + 1, region.getXSize());
    }

    @Test
    void ySizeShouldReturnDifferenceBetweenInputsPlus1Hardcoded() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        assertEquals(Math.abs(4 - -4) + 1, region.getYSize());
    }

    @Test
    void zSizeShouldReturnDifferenceBetweenInputsPlus1Hardcoded() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        assertEquals(Math.abs(4 - -4) + 1, region.getZSize());
    }

}
