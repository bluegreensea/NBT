package net.rossquerz.mca.util;


import junit.framework.TestCase;

public class ChunkBoundingRectangleTest extends TestCase {

    public void testRelocate_int() {
        ChunkBoundingRectangle cbr = new ChunkBoundingRectangle(1, -1);  // [16..32), [-16..0)
        assertEquals(16, cbr.getMinBlockX());
        assertEquals(-16, cbr.getMinBlockZ());
        assertEquals(32, cbr.getMaxBlockX());
        assertEquals(0, cbr.getMaxBlockZ());

        assertEquals(1, cbr.getWidthChunkXZ());
        assertEquals(16, cbr.getWidthBlockXZ());

        assertEquals(16, cbr.relocateX(0));
        assertEquals(16 + 3, cbr.relocateX(3));
        assertEquals(16 + 7, cbr.relocateX(16 + 7));
        assertEquals(16 + 15, cbr.relocateX(-1));

        assertEquals(-1, cbr.relocateZ(16 * 53 - 1));
        assertEquals(-15, cbr.relocateZ(16 * -53 - 15));
        assertEquals(-16 + 3, cbr.relocateZ(3));
        assertEquals(-16 + 7, cbr.relocateZ(16 + 7));
        assertEquals(-16 + 15, cbr.relocateZ(-1));

        cbr = new ChunkBoundingRectangle(0, 0, 32);
        assertEquals(0, cbr.getMinBlockX());
        assertEquals(0, cbr.getMinBlockZ());
        assertEquals(512, cbr.getMaxBlockX());
        assertEquals(512, cbr.getMaxBlockZ());
        assertEquals(32, cbr.getWidthChunkXZ());
        assertEquals(512, cbr.getWidthBlockXZ());
    }

    public void testRelocate_double() {
        ChunkBoundingRectangle cbr = new ChunkBoundingRectangle(0, 1);  // [0..16), [16..32)

        assertEquals(16 - 1e-6, cbr.relocateX(-1e-6), 1e-10);
        assertEquals(16 - 0.5, cbr.relocateX(41 * 16 -0.5), 1e-10);
        assertEquals(16 - 0.5, cbr.relocateX(-41 * 16 -0.5), 1e-10);

        assertEquals(16 + 1e-6, cbr.relocateZ(1e-6), 1e-10);
        assertEquals(16 + 1e-6, cbr.relocateZ(41 * 16 + 1e-6), 1e-10);
        assertEquals(16 + 0.5, cbr.relocateZ(41 * 16 + 0.5), 1e-10);
        assertEquals(16 + 6.789, cbr.relocateZ(-41 * 16 + 6.789), 1e-10);
    }

    public void testContainsChunk() {
        ChunkBoundingRectangle cbr = new ChunkBoundingRectangle(0, 0, 5);  // [512..1024), [-512..0) | [32..64), [-32..0)
        assertTrue(cbr.containsChunk(0, 0));
        assertTrue(cbr.containsChunk(4, 4));
        assertFalse(cbr.containsChunk(-1, 2));
        assertFalse(cbr.containsChunk(2, 5));
    }
}
