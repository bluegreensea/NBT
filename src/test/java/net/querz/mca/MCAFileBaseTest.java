package net.querz.mca;

import net.querz.nbt.tag.CompoundTag;
import net.querz.mca.util.IntPointXZ;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.querz.util.JsonPrettyPrinter.prettyPrintJson;

// TODO: implement abstract test pattern for MCAFileBase & refactor MCAFileTest like mad
public class MCAFileBaseTest extends MCATestCase {

    // TODO: scan resources folder and auto-run this test for each mca file and type so we don't need to update tests every time we add a new test region file.
    /**
     * Reads an mca file, writes it, reads it back in, verifies that the NBT of the first non-empty chunk is identical
     * between the reads and that the data versions are correct and that MCAUtil.readAuto returned the correct type.
     */
    protected <CT extends ChunkBase, FT extends MCAFileBase<CT>> void validateReadWriteParity(DataVersion expectedDataVersion, String mcaResourcePath, Class<FT> clazz) {
        FT mcaA = assertThrowsNoException(() -> MCAUtil.readAuto(copyResourceToTmp(mcaResourcePath)));
        assertTrue(clazz.isInstance(mcaA));
        List<CT> chunksIn = mcaA.stream().filter(Objects::nonNull).collect(Collectors.toList());
        assertFalse(chunksIn.isEmpty());
        assertEquals(expectedDataVersion.id(), mcaA.getDefaultChunkDataVersion());

        // ensure that we are reading and writing every tag - at least at the chunk/section level
        final List<CompoundTag> originalChunkData = new ArrayList<>();
        for (CT chunk : chunksIn) {
            assertEquals(expectedDataVersion, chunk.getDataVersionEnum());
            originalChunkData.add(chunk.data.clone());
            if (chunk instanceof SectionedChunkBase) {
                for (SectionBase<?> section : (SectionedChunkBase<?>) chunk) {
                    section.data.clear();
                }
            }
            chunk.data.clear();
        }

        File tmpFile = super.getNewTmpFile(Paths.get("out", mcaResourcePath).toString());
        assertThrowsNoException(() -> MCAUtil.write(mcaA, tmpFile));
        // writing should have rebuilt the data tag fully and correctly
        for (int i = 0; i < chunksIn.size(); i++) {

            // check the strings match first - and format them so any diffs give a helpful inspection
//            assertEquals(
//                    prettyPrintJson(originalChunkData.get(i).toString()),
//                    prettyPrintJson(chunksIn.get(i).data.toString()));

            try {
                if (!originalChunkData.get(i).equals(chunksIn.get(i).data)) {
                    Path p = Paths.get("F:\\Archive\\Programming\\Java\\MCPlugins 1.20.4\\QuerzNbt\\TESTDBG", mcaResourcePath + "." + i + ".original.json");
                    p.getParent().toFile().mkdirs();
                    Files.write(p,
                            prettyPrintJson(originalChunkData.get(i).toString()).getBytes());
                    Files.write(
                            Paths.get("F:\\Archive\\Programming\\Java\\MCPlugins 1.20.4\\QuerzNbt\\TESTDBG", mcaResourcePath + "." + i + ".out.json"),
                            prettyPrintJson(chunksIn.get(i).data.toString()).getBytes());
                } else {
                    Path p = Paths.get("F:\\Archive\\Programming\\Java\\MCPlugins 1.20.4\\QuerzNbt\\TESTDBG", mcaResourcePath + ".OK." + i + ".original.json");
                    p.getParent().toFile().mkdirs();
                    Files.write(p,
                            prettyPrintJson(originalChunkData.get(i).toString()).getBytes());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                fail(ex.getMessage());
            }

            assertEquals(originalChunkData.get(i), chunksIn.get(i).data);
        }

        FT mcaB = assertThrowsNoException(() -> MCAUtil.readAuto(tmpFile));
        List<CT> chunksAsReread = mcaB.stream().filter(Objects::nonNull).collect(Collectors.toList());
        assertTrue(clazz.isInstance(mcaB));
        assertFalse(chunksAsReread.isEmpty());
        assertEquals(chunksIn.size(), chunksAsReread.size());
        assertEquals(expectedDataVersion.id(), mcaB.getDefaultChunkDataVersion());

        for (int i = 0; i < chunksAsReread.size(); i++) {
            CT chunkA = chunksIn.get(i);
            CT chunkB = chunksAsReread.get(i);
            assertEquals(chunkA.getDataVersionEnum(), chunkB.getDataVersionEnum());
            // check the strings match first - and format them so any diffs give a helpful inspection
//            assertEquals(
//                    prettyPrintJson(originalChunkData.get(i).toString()),
//                    prettyPrintJson(chunkB.data.toString()));
            assertEquals(originalChunkData.get(i), chunkB.data);
        }
    }

    public void testGetRelativeChunkXZ() {
        // few enough iterations to just be lazy and do an exhaustive test
        for (int i = 0; i < 1024; i++) {
            IntPointXZ xz = MCAFileBase.getRelativeChunkXZ(i);
            assertEquals(i, MCAFileBase.getChunkIndex(xz.getX(), xz.getZ()));
        }
        assertThrowsException(() -> MCAFileBase.getRelativeChunkXZ(-1), IndexOutOfBoundsException.class);
        assertThrowsException(() -> MCAFileBase.getRelativeChunkXZ(1024), IndexOutOfBoundsException.class);
    }
}
