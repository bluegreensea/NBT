package net.rossquerz.mca;

import java.util.Iterator;

public interface SectionIterator<T extends SectionBase<?>> extends Iterator<T> {
    /** Current section y within chunk */
    int sectionY();
    /** Current block world level y of the bottom most block in the current section. Inclusive. */
    int sectionBlockMinY();
    /** Current block world level y of the top most block in the current section. Inclusive. */
    int sectionBlockMaxY();
}