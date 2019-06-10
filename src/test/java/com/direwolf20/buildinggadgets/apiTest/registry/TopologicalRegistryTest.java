package com.direwolf20.buildinggadgets.apiTest.registry;

import com.direwolf20.buildinggadgets.api.registry.IOrderedRegistry;
import com.direwolf20.buildinggadgets.api.registry.TopologicalRegistryBuilder;
import com.direwolf20.buildinggadgets.apiTest.util.annotations.SingleTest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TopologicalRegistryTest {
    private void verifyOrderIsExposedConsistently(IOrderedRegistry<?> reg) {
        ImmutableList<?> val = reg.getValuesInOrder();
        assertIterableEquals(val, reg);
        assertIterableEquals(val, reg.values()
                .collect(Collectors.toList()));
    }

    private <T> void verifyContainsAll(IOrderedRegistry<T> reg, ImmutableMap<ResourceLocation, T> values) {
        ImmutableList<T> regValuesInOrder = reg.getValuesInOrder();
        for (Map.Entry<ResourceLocation, T> obj : values.entrySet()) {
            assertTrue(reg.contains(obj.getKey()), () -> "Registry was expected to contain " + obj.getKey());
            final T regVal = obj.getValue();
            assertEquals(reg.get(obj.getKey()), regVal, () -> "Registry was expected to contain " + regVal + " for key " + obj.getKey() + " instead of " + regVal);
            assertTrue(regValuesInOrder.contains(obj.getValue()), () -> "Ordered Values were expected to contain " + obj.getValue());
        }
    }

    private <T> void verifyContainsNone(IOrderedRegistry<T> reg, ImmutableList<ResourceLocation> values) {
        for (ResourceLocation key : values) {
            assertFalse(reg.contains(key), () -> "Registry was expected not to contain " + key);
        }
    }

    @SingleTest
    public void regContainsValuesAndDoesNotContainMarkers() {
        ResourceLocation start = new ResourceLocation("start");
        ResourceLocation mid = new ResourceLocation("mid");
        ResourceLocation obj1 = new ResourceLocation("obj1");
        ResourceLocation obj2 = new ResourceLocation("obj2");
        ResourceLocation obj3 = new ResourceLocation("obj3");
        ResourceLocation obj4 = new ResourceLocation("obj4");
        ResourceLocation obj5 = new ResourceLocation("obj5");
        ResourceLocation obj6 = new ResourceLocation("obj6");
        ResourceLocation obj7 = new ResourceLocation("obj7");
        ImmutableMap<ResourceLocation, String> objects = ImmutableMap.<ResourceLocation, String>builder()
                .put(obj1, "firstAfterStart")
                .put(obj2, "After1_1")
                .put(obj3, "After1_2")
                .put(obj4, "After2Before3AndMid")
                .put(obj5, "Before4")
                .put(obj6, "AfterMid")
                .put(obj7, "After6")
                .build();
        ImmutableList<ResourceLocation> markers = ImmutableList.of(start, mid);
        IOrderedRegistry<String> reg = TopologicalRegistryBuilder.<String>create()
                .addAllMarkers(markers)
                .addAllValues(objects)
                .addDependency(start, obj1)
                .addDependency(obj1, obj2)
                .addDependency(obj1, obj3)
                .addDependency(obj2, obj4)
                .addDependency(obj4, obj3)
                .addDependency(obj4, mid)
                .addDependency(obj5, obj4)
                .addDependency(mid, obj6)
                .addDependency(obj6, obj7)
                .build();
        verifyOrderIsExposedConsistently(reg);
        verifyContainsAll(reg, objects);
        verifyContainsNone(reg, markers);
        //dependencies don't need to be checked, as Forge's sorter is used and works...
    }

    @SingleTest
    public void throwsOnDuplicateValueTest() {
        TopologicalRegistryBuilder<String> builder = TopologicalRegistryBuilder.create();
        builder.addValue(new ResourceLocation("a"), "a");
        assertThrows(IllegalArgumentException.class, () -> builder.addValue(new ResourceLocation("b"), "a"));
    }

    @SingleTest
    public void throwsOnNullKey() {
        TopologicalRegistryBuilder<String> builder = TopologicalRegistryBuilder.create();
        assertThrows(NullPointerException.class, () -> builder.addValue(null, ""));
        assertThrows(NullPointerException.class, () -> builder.addMarker(null));
        assertThrows(NullPointerException.class, () -> builder.addDependency(null, null));
    }

    @SingleTest
    public void throwsWhenAlreadyBuild() {
        TopologicalRegistryBuilder<String> builder = TopologicalRegistryBuilder.create();
        builder.build();
        assertThrows(IllegalStateException.class, () -> builder.addValue(new ResourceLocation("a"), "c"));
        assertThrows(IllegalStateException.class, () -> builder.addMarker(new ResourceLocation("a")));
        assertThrows(IllegalStateException.class, () -> builder.addAllValues(ImmutableMap.of(new ResourceLocation("a"), "a")));
        assertThrows(IllegalStateException.class, () -> builder.addAllMarkers(ImmutableList.of(new ResourceLocation("a"))));
        assertThrows(IllegalStateException.class, () -> builder.addDependency(new ResourceLocation("a"), new ResourceLocation("b")));
        assertThrows(IllegalStateException.class, builder::build);
    }
}
