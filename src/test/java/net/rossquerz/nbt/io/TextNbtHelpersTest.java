package net.rossquerz.nbt.io;

import net.rossquerz.NbtTestCase;
import net.rossquerz.nbt.tag.CompoundTag;
import net.rossquerz.nbt.tag.StringTag;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static net.rossquerz.nbt.io.TextNbtHelpers.*;

public class TextNbtHelpersTest extends NbtTestCase {

    private void verify(CompoundTag tag) {
        assertEquals(3, tag.size());
        assertTrue(tag.containsKey("question"));
        assertEquals("¿why?", tag.getString("question"));
        assertTrue(tag.containsKey("answer"));
        assertEquals(42, tag.getInt("answer"));
        assertTrue(tag.containsKey("when"));
        Assert.assertArrayEquals(new int[] {1978, 1981, 1984, 2005}, tag.getIntArray("when"));
    }

    public void testReadTextNbt_nakedTag() throws IOException {
        NamedTag uncompressedResult = readTextNbtFile(getResourceFile("text_nbt_samples/unnamed_tag_sample.snbt"));
        assertNull(uncompressedResult.getName());
        verify(uncompressedResult.getTagAutoCast());

        NamedTag compressedResult = readTextNbtFile(getResourceFile("text_nbt_samples/unnamed_tag_sample.snbt.gz"));
        assertEquals(uncompressedResult, compressedResult);
    }

    public void testReadTextNbt_namedTag() throws IOException {
//        System.out.println("-----------------------------");
//        System.out.println(new String(Files.readAllBytes(getResourceFile("text_nbt_samples/named_tag_sample-with_bom.snbt").toPath())));
//        System.out.println("-----------------------------");
        NamedTag uncompressedResult = readTextNbtFile(getResourceFile("text_nbt_samples/named_tag_sample-with_bom.snbt"));
        assertEquals("HitchhikerGuide", uncompressedResult.getName());
        verify(uncompressedResult.getTagAutoCast());

        NamedTag compressedResult = readTextNbtFile(getResourceFile("text_nbt_samples/named_tag_sample-with_bom.snbt.gz"));
        assertEquals(uncompressedResult, compressedResult);
    }

    public void testReadTextNbt_emptyFile() throws IOException {
        NamedTag uncompressedResult = readTextNbtFile(getResourceFile("text_nbt_samples/empty_file.snbt"));
        assertNull(uncompressedResult);
        NamedTag compressedResult = readTextNbtFile(getResourceFile("text_nbt_samples/empty_file.snbt.gz"));
        assertNull(compressedResult);
    }

    public void testWriteTextNbt_nakedTag() throws IOException {
        File tempFile = getNewTmpFile("text_nbt_helpers_test/write_naked_tag.snbt");
        StringTag tag = new StringTag("¿why?");
        assertEquals(tempFile.toPath(), writeTextNbtFile(tempFile, tag));
        StringTag readTag = readTextNbtFile(tempFile).getTagAutoCast();
        assertEquals(tag, readTag);
    }

    public void testWriteTextNbt_writeGzFile() throws IOException {
        NamedTag namedTagGolden = readTextNbtFile(getResourceFile("text_nbt_samples/named_tag_sample-with_bom.snbt"));
        File tempFile = getNewTmpFile("text_nbt_helpers_test/write_gz_file.snbt.gz");
        assertEquals(tempFile.toPath(), writeTextNbtFile(tempFile, namedTagGolden));
        NamedTag readTag = readTextNbtFile(tempFile);
        assertEquals(namedTagGolden, readTag);
        verify(readTag.getTagAutoCast());
    }
}
