package com.direwolf20.buildinggadgets.building.coreTests;

import com.direwolf20.buildinggadgets.building.Region;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class RegionTest {

    @Test
    public void sizeMethodShouldReturnPositiveInteger() {
        Region region = new Region(-1, -1, -1, -16, -16, -16);
        assertEquals(4096, region.size());

        Region regionRegular = new Region(1, 1, 1, 16, 16, 16);
        assertEquals(4096, regionRegular.size());
    }

    @Test
    public void sizeMethodShouldReturnPositiveIntegerCaseAwayFromOrigin() {
        Region region = new Region(-33, -33, -33, -48, -48, -48);
        assertEquals(4096, region.size());

        Region regionRegular = new Region(33, 33, 33, 48, 48, 48);
        assertEquals(4096, regionRegular.size());
    }

    @Test
    public void translationShouldAddAllVertexesToTranslationCaseAllPositive() {
        Region region = new Region(1, 1, 1, 8, 8, 8);
        Region expected = new Region(9, 9, 9, 16, 16, 16);
        assertEquals(expected, region.translate(8, 8, 8));
    }

    @Test
    public void translationShouldAddAllVertexesToTranslationCaseAllNegative() {
        Region region = new Region(-1, -1, -1, -8, -8, -8);
        Region expected = new Region(-9, -9, -9, -16, -16, -16);
        assertEquals(expected, region.translate(-8, -8, -8));
    }

    @Test
    public void translationShouldAddAllVertexesToTranslationCaseOrigin() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        Region expected = new Region(0, 0, 0, 8, 8, 8);
        assertEquals(expected, region.translate(4, 4, 4));
    }

    @Test
    public void expandShouldDecreaseRegionSizeCaseOrigin() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        Region expected = new Region(-6, -6, -6, 6, 6, 6);
        assertEquals(expected, region.expand(2));
    }

    @Test
    public void collapseShouldDecreaseRegionSizeCaseOrigin() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        Region expected = new Region(-2, -2, -2, 2, 2, 2);
        assertEquals(expected, region.collapse(2));
    }

    @Test
    public void growShouldIncreaseAndOnlyIncreaseMaxCoordinates() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        Region expected = new Region(-4, -4, -4, 6, 6, 6);
        assertEquals(expected, region.grow(2));
    }

    @Test
    public void shrinkShouldDecreaseAndOnlyDecreaseMaxCoordinates() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        Region expected = new Region(-4, -4, -4, 2, 2, 2);
        assertEquals(expected, region.shrink(2));
    }

    @Test
    public void intersectShouldReturnIntersectionBoxThatsSmallerThanSources() {
        Region region1 = new Region(-2, -2, -2, 4, 4, 4);
        Region region2 = new Region(-4, -4, -4, 2, 2, 2);
        Region expected = new Region(-2, -2, -2, 2, 2, 2);
        assertEquals(expected, region1.intersect(region2));
    }

    @Test
    public void intersectShouldReturnSelfWhenParameterIsSelf() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        assertEquals(region, region.intersect(region));
    }

    @Test
    public void unionShouldReturnUnionBoxThatsLargerThanSources() {
        Region region1 = new Region(-2, -2, -2, 4, 4, 4);
        Region region2 = new Region(-4, -4, -4, 2, 2, 2);
        Region expected = new Region(-4, -4, -4, 4, 4, 4);
        assertEquals(expected, region1.union(region2));
    }

    @Test
    public void unionShouldReturnSelfWhenParameterIsSelf() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        assertEquals(region, region.union(region));
    }

    @Test
    public void xSizeShouldReturnDifferenceBetweenInputsPlus1Hardcoded() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        assertEquals(Math.abs(4 - -4) + 1, region.getXSize());
    }

    @Test
    public void ySizeShouldReturnDifferenceBetweenInputsPlus1Hardcoded() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        assertEquals(Math.abs(4 - -4) + 1, region.getYSize());
    }

    @Test
    public void zSizeShouldReturnDifferenceBetweenInputsPlus1Hardcoded() {
        Region region = new Region(-4, -4, -4, 4, 4, 4);
        assertEquals(Math.abs(4 - -4) + 1, region.getZSize());
    }

}
