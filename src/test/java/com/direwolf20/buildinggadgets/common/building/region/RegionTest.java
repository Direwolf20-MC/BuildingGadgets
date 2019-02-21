package com.direwolf20.buildinggadgets.common.building.region;

import com.direwolf20.buildinggadgets.common.building.Region;
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

    //TODO grow and shrink test
    //TODO intersect and union test

}
