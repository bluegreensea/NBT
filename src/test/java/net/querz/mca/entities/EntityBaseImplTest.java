package net.querz.mca.entities;

import net.querz.mca.DataVersion;
import net.querz.mca.MCATestCase;
import net.querz.nbt.tag.CompoundTag;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.UUID;

public class EntityBaseImplTest extends MCATestCase {

    // <editor-fold desc="NBT manipulation helpers" defaultstate="collapsed">

    protected CompoundTag makeEntityTag(String id) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        return tag;
    }

    protected CompoundTag setBoolean(CompoundTag tag, String name, Boolean value) {
        if (value == null) tag.remove(name);
        else tag.putBoolean(name, value);
        return tag;
    }

    protected CompoundTag setInvulnerable(CompoundTag tag, Boolean value) {
        return setBoolean(tag, "Invulnerable", value);
    }

    protected CompoundTag setSilent(CompoundTag tag, Boolean value) {
        return setBoolean(tag, "Silent", value);
    }

    protected CompoundTag setGlowing(CompoundTag tag, Boolean value) {
        return setBoolean(tag, "Glowing", value);
    }

    protected CompoundTag setPos(CompoundTag tag, double x, double y, double z) {
        tag.putDoubleArrayAsTagList("Pos", x, y, z);
        return tag;
    }

    protected CompoundTag setMotion(CompoundTag tag, double dx, double dy, double dz) {
        tag.putDoubleArrayAsTagList("Motion", dx, dy, dz);
        return tag;
    }

    protected CompoundTag setRotation(CompoundTag tag, float yaw, float pitch) {
        tag.putFloatArrayAsTagList("Rotation", yaw, pitch);
        return tag;
    }

    // </editor-fold>

    // <editor-fold desc="Misc Test Helpers" defaultstate="collapsed">

    protected void assertPositionEquals(EntityBaseImpl entity, double expectX, double expectY, double expectZ) {
        // assertEquals(double...) takes care of non-finite equality checking too!
        assertEquals(expectX, entity.getX(), 1e-4);
        assertEquals(expectY, entity.getY(), 1e-4);
        assertEquals(expectZ, entity.getZ(), 1e-4);
    }

    protected void assertMotionEquals(EntityBaseImpl entity, double expectDX, double expectDY, double expectDZ) {
        // assertEquals(double...) takes care of non-finite equality checking too!
        assertEquals(expectDX, entity.getMotionDX(), 1e-7);
        assertEquals(expectDY, entity.getMotionDY(), 1e-7);
        assertEquals(expectDZ, entity.getMotionDZ(), 1e-7);
    }

    // sets values for everything but passenger and UUID
    protected CompoundTag makeTestEntityTag() {
        CompoundTag tag = makeEntityTag("pig");
        setPos(tag, 1420.276, 71.0, -317.416);
        setRotation(tag, 346.4548f, -40f);
        setMotion(tag, -0.01312, 0.117600, 0.05052);
        tag.putShort("Air", (short) 147);
        tag.putString("CustomName", "{\"text\":\"bob\"}");
        tag.putFloat("FallDistance", 23.787f);
        tag.putShort("Fire", (short) -25);
        tag.putInt("PortalCooldown", 96);
        tag.putStringsAsTagList("Tags", Arrays.asList("T1", "another_one"));
        tag.putInt("TicksFrozen", 291);

        tag.putBoolean("CustomNameVisible", true);
        tag.putBoolean("Glowing", true);
        tag.putBoolean("HasVisualFire", true);
        tag.putBoolean("Invulnerable", true);
        tag.putBoolean("NoGravity", true);
        tag.putBoolean("OnGround", true);
        tag.putBoolean("Silent", true);

        return tag;
    }

    // </editor-fold>

    // <editor-fold desc="Constructor Tests" defaultstate="collapsed">

    public void testConstructor_allTags() {
        // Constructor for tags containing passengers transitively tested by #testUpdateHandle_withPassengers()
        CompoundTag tag = makeTestEntityTag();
        UUID uuid = UUID.randomUUID();
        EntityUtil.setUuid(DataVersion.latest().id(), tag, uuid);
        CompoundTag originalTagCopy = tag.clone();

        EntityBaseImpl entity = new EntityBaseImpl(tag, DataVersion.latest().id());
        assertEquals(DataVersion.latest().id(), entity.getDataVersion());
        assertEquals("pig", entity.getId());
        assertEquals(uuid, entity.getUuid());

        assertEquals(1420.276, entity.getX(), 1e-8);
        assertEquals(71.0, entity.getY(), 1e-8);
        assertEquals(-317.416, entity.getZ(), 1e-8);

        assertEquals(346.4548f, entity.getRotationYaw(), 1e-5f);
        assertEquals(-40f, entity.getRotationPitch(), 1e-5f);

        assertEquals(-0.01312, entity.getMotionDX(), 1e-8);
        assertEquals(0.117600, entity.getMotionDY(), 1e-8);
        assertEquals(0.05052, entity.getMotionDZ(), 1e-8);

        assertEquals((short) 147, entity.getAir());
        assertEquals("{\"text\":\"bob\"}", entity.getCustomName());
        assertEquals(23.787f, entity.getFallDistance(), 1e-5f);
        assertEquals((short) -25, entity.getFire());

        assertEquals(96, entity.getPortalCooldown());
        assertEquals(Arrays.asList("T1", "another_one"), entity.getScoreboardTags());
        assertEquals(291, entity.getTicksFrozen());

        assertTrue(entity.isCustomNameVisible());
        assertTrue(entity.isGlowing());
        assertTrue(entity.hasVisualFire());
        assertTrue(entity.isInvulnerable());
        assertTrue(entity.hasNoGravity());
        assertTrue(entity.isOnGround());
        assertTrue(entity.isSilent());

        assertSame(tag, entity.getHandle());
        assertNotSame(originalTagCopy, entity.getHandle());
        assertEquals(originalTagCopy, entity.updateHandle());

        // now to check for copy-paste errors on booleans
        tag.putBoolean("CustomNameVisible", false);
        entity = new EntityBaseImpl(tag, DataVersion.latest().id());
        assertFalse(entity.isCustomNameVisible());

        tag.putBoolean("Glowing", false);
        entity = new EntityBaseImpl(tag, DataVersion.latest().id());
        assertFalse(entity.isGlowing());

        tag.putBoolean("HasVisualFire", false);
        entity = new EntityBaseImpl(tag, DataVersion.latest().id());
        assertFalse(entity.hasVisualFire());

        tag.putBoolean("Invulnerable", false);
        entity = new EntityBaseImpl(tag, DataVersion.latest().id());
        assertFalse(entity.isInvulnerable());

        tag.putBoolean("NoGravity", false);
        entity = new EntityBaseImpl(tag, DataVersion.latest().id());
        assertFalse(entity.hasNoGravity());

        tag.putBoolean("OnGround", false);
        entity = new EntityBaseImpl(tag, DataVersion.latest().id());
        assertFalse(entity.isOnGround());

        tag.putBoolean("Silent", false);
        entity = new EntityBaseImpl(tag, DataVersion.latest().id());
        assertFalse(entity.isSilent());
    }

    public void testCopyConstructor_withStackedPassengers() {
        EntityBaseImpl pig = new EntityBaseImpl(makeTestEntityTag(), DataVersion.latest().id());
        pig.setPosition(12, 65, -44);
        EntityBaseImpl chicken = new EntityBaseImpl(DataVersion.latest().id(), "chicken");
        EntityBaseImpl zombie = new EntityBaseImpl(DataVersion.latest().id(), "zombie");
        UUID pigUuid = pig.generateNewUuid();
        UUID chickenUuid = chicken.generateNewUuid();
        UUID zombieUuid = zombie.generateNewUuid();
        chicken.addPassenger(zombie);
        pig.addPassenger(chicken);

        EntityBaseImpl pig2 = pig.clone();
        assertNotEquals(pigUuid, pig2.getUuid());
        assertTrue(pig2.hasPassengers());
        assertEquals(1, pig2.getPassengers().size());

        EntityBaseImpl chicken2 = (EntityBaseImpl) pig2.getPassengers().get(0);
        assertNotEquals(chickenUuid, chicken2.getUuid());
        assertTrue(chicken2.hasPassengers());
        assertEquals(1, chicken2.getPassengers().size());

        EntityBaseImpl zombie2 = (EntityBaseImpl) chicken2.getPassengers().get(0);
        assertNotEquals(zombieUuid, zombie2.getUuid());
        assertFalse(zombie2.hasPassengers());
    }

    // </editor-fold>

    // <editor-fold desc="Update Handle Tests" defaultstate="collapsed">

    public void testUpdateHandle_validPositionRequired() {
        EntityBaseImpl entity = new EntityBaseImpl(DataVersion.latest().id(), "zombie");
        assertThrowsException(entity::updateHandle, IllegalStateException.class);
        entity.setPosition(0, 0, Double.NaN);
        assertThrowsException(entity::updateHandle, IllegalStateException.class);
        entity.setPosition(0, Double.NaN, 0);
        assertThrowsException(entity::updateHandle, IllegalStateException.class);
        entity.setPosition(Double.NaN, 0, 0);
        assertThrowsException(entity::updateHandle, IllegalStateException.class);
        entity.setPosition(0, 0, 0);
        assertThrowsNoException(entity::updateHandle);
    }

    public void testUpdateHandle_rotationTagNotOutputUnlessRotationIsValid() {
        EntityBaseImpl entity = new EntityBaseImpl(DataVersion.latest().id(), "zombie", 0, 0, 0);
        entity.setRotation(0, Float.NaN);
        CompoundTag tag = entity.updateHandle();
        assertFalse(tag.containsKey("Rotation"));

        entity.setRotation(Float.NaN, 0);
        tag = entity.updateHandle();
        assertFalse(tag.containsKey("Rotation"));

        entity.setRotation(0, 0);
        tag = entity.updateHandle();
        assertTrue(tag.containsKey("Rotation"));
    }

    public void testUpdateHandle_motionTagNotOutputUnlessMotionIsValid() {
        EntityBaseImpl entity = new EntityBaseImpl(DataVersion.latest().id(), "zombie", 0, 0, 0);
        entity.setMotion(0, 0, Double.NaN);
        CompoundTag tag = entity.updateHandle();
        assertFalse(tag.containsKey("Motion"));

        entity.setMotion(0, Double.NaN, 0);tag = entity.updateHandle();
        assertFalse(tag.containsKey("Motion"));

        entity.setMotion(Double.NaN, 0, 0);
        tag = entity.updateHandle();
        assertFalse(tag.containsKey("Motion"));

        entity.setMotion(0, 0, 0);
        tag = entity.updateHandle();
        assertTrue(tag.containsKey("Motion"));
    }

    public void testUpdateHandle_airTagNotOutputWhenAirUnset() {
        EntityBaseImpl entity = new EntityBaseImpl(DataVersion.latest().id(), "zombie", 0, 0, 0);
        CompoundTag tag = entity.updateHandle();
        assertFalse(tag.containsKey("Air"));

        entity.setAir((short) 500);
        tag = entity.updateHandle();
        assertTrue(tag.containsKey("Air"));

        entity.setAir(EntityBase.AIR_UNSET);
        tag = entity.updateHandle();
        assertFalse(tag.containsKey("Air"));
    }

    public void testUpdateHandle_uuidGeneratedWhenUnset() {
        EntityBaseImpl entity = new EntityBaseImpl(DataVersion.latest().id(), "zombie", 0, 0, 0);
        assertNull(entity.getUuid());
        CompoundTag tag = entity.updateHandle();
        assertTrue(tag.containsKey("UUID"));
        assertNotNull(entity.getUuid());
    }

    public void testUpdateHandle_withPassengers() {
        // transitively tests constructor with passengers
        EntityBaseImpl pig = new EntityBaseImpl(makeTestEntityTag(), DataVersion.latest().id());
        pig.setPosition(12, 65, -44);
        EntityBaseImpl chicken = new EntityBaseImpl(DataVersion.latest().id(), "chicken");
        EntityBaseImpl zombie = new EntityBaseImpl(DataVersion.latest().id(), "zombie");
        EntityBaseImpl skeleton = new EntityBaseImpl(DataVersion.latest().id(), "skeleton");
        pig.addPassenger(chicken);
        assertTrue(chicken.isPositionValid());
        pig.addPassenger(skeleton);
        assertTrue(skeleton.isPositionValid());
        chicken.addPassenger(zombie);
        assertTrue(zombie.isPositionValid());

        CompoundTag pigTag = pig.updateHandle();

        assertTrue(pigTag.containsKey("Passengers"));
        assertEquals(2, pigTag.getListTag("Passengers").asCompoundTagList().size());
        CompoundTag chickenTag = pigTag.getListTag("Passengers").asCompoundTagList().get(0);
        CompoundTag skeletonTag = pigTag.getListTag("Passengers").asCompoundTagList().get(1);

        assertTrue(chickenTag.containsKey("Passengers"));
        assertEquals(1, chickenTag.getListTag("Passengers").asCompoundTagList().size());
        CompoundTag zombieTag = chickenTag.getListTag("Passengers").asCompoundTagList().get(0);

        assertFalse(skeletonTag.containsKey("Passengers"));
        assertFalse(zombieTag.containsKey("Passengers"));

        assertNotNull(pig.getUuid());
        assertEquals(pig.getUuid(), EntityUtil.getUuid(DataVersion.latest().id(), pigTag));
        assertEquals("pig", pigTag.getString("id"));

        assertNotNull(chicken.getUuid());
        assertEquals(chicken.getUuid(), EntityUtil.getUuid(DataVersion.latest().id(), chickenTag));
        assertEquals("chicken", chickenTag.getString("id"));

        assertNotNull(skeleton.getUuid());
        assertEquals(skeleton.getUuid(), EntityUtil.getUuid(DataVersion.latest().id(), skeletonTag));
        assertEquals("skeleton", skeletonTag.getString("id"));

        assertNotNull(zombie.getUuid());
        assertEquals(zombie.getUuid(), EntityUtil.getUuid(DataVersion.latest().id(), zombieTag));
        assertEquals("zombie", zombieTag.getString("id"));
    }

    // </editor-fold>

    // <editor-fold desc="Misc Passenger Tests" defaultstate="collapsed">

    public void testPassengers_cannotSetSelfAsPassenger() {
        EntityBaseImpl entity = new EntityBaseImpl(DataVersion.latest().id(), "zombie", 0, 0, 0);
        assertThrowsIllegalArgumentException(() -> entity.addPassenger(entity));
        assertNull(entity.getPassengers());
    }

    public void testPassengers_cannotAddNullPassenger() {
        EntityBaseImpl entity = new EntityBaseImpl(DataVersion.latest().id(), "zombie", 0, 0, 0);
        assertThrowsIllegalArgumentException(() -> entity.addPassenger(null));
        assertNull(entity.getPassengers());
    }


    public void testPassengers_addPassengerSetsRiderPositionIfUnset() {
        EntityBaseImpl spider = new EntityBaseImpl(DataVersion.latest().id(), "spider", 42.743, 68, -96.23);
        spider.setMotion(0.1, -0.05, 0.008);
        EntityBaseImpl skeleton = new EntityBaseImpl(DataVersion.latest().id(), "skeleton");
        assertFalse(skeleton.isPositionValid());
        spider.addPassenger(skeleton);
        assertTrue(skeleton.isPositionValid());
        assertEquals(42.743, skeleton.getX(), 1e-8);
        assertEquals(68, skeleton.getY(), 1e-8);
        assertEquals(-96.23, skeleton.getZ(), 1e-8);

        // also copies motion
        assertEquals(0.1, skeleton.getMotionDX(), 1e-8);
        assertEquals(-0.05, skeleton.getMotionDY(), 1e-8);
        assertEquals(0.008, skeleton.getMotionDZ(), 1e-8);
    }

    // </editor-fold>

    // <editor-fold desc="Position and Move XYZ Tests" defaultstate="collapsed">

    public void testSetPosition_basic() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        pig.setPosition(19.123, 68.5, -47.89);
        assertPositionEquals(pig, 19.123, 68.5, -47.89);

        pig.setX(17.45);
        assertPositionEquals(pig, 17.45, 68.5, -47.89);

        pig.setY(65.78);
        assertPositionEquals(pig, 17.45, 65.78, -47.89);

        pig.setZ(-42.111);
        assertPositionEquals(pig, 17.45, 65.78, -42.111);
    }

    public void testSetPosition_withPassengersHavingPositions() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        pig.setPosition(19.123, 68.5, -47.89);
        EntityBaseImpl chicken = new EntityBaseImpl(DataVersion.latest().id(), "chicken");
        chicken.setPosition(19.456, 68.85, -47.87);
        EntityBaseImpl zombie = new EntityBaseImpl(DataVersion.latest().id(), "zombie");
        zombie.setPosition(19.789, 69.15, -47.86);
        chicken.addPassenger(zombie);
        pig.addPassenger(chicken);

        assertPositionEquals(pig, 19.123, 68.5, -47.89);
        assertPositionEquals(chicken, 19.456, 68.85, -47.87);
        assertPositionEquals(zombie, 19.789, 69.15, -47.86);

        // passengers should move with their mounts
        pig.setPosition(0, 0, 0);
        assertPositionEquals(pig, 0, 0, 0);
        assertPositionEquals(chicken, 19.456 - 19.123, 68.85 - 68.5, -47.87 + 47.89);
        assertPositionEquals(zombie, 19.789 - 19.123, 69.15 - 68.5, -47.86 + 47.89);
    }

    public void testSetPosition_withPassengersHavingInvalidPositions() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        EntityBaseImpl chicken = new EntityBaseImpl(DataVersion.latest().id(), "chicken");
        EntityBaseImpl zombie = new EntityBaseImpl(DataVersion.latest().id(), "zombie");
        pig.addPassenger(chicken);
        chicken.addPassenger(zombie);
        assertFalse(pig.isPositionValid());
        assertFalse(chicken.isPositionValid());
        assertFalse(zombie.isPositionValid());

        // setting mount position should also set passengers
        pig.setPosition(19.123, 68.5, -47.89);
        assertTrue(pig.isPositionValid());
        assertTrue(chicken.isPositionValid());
        assertTrue(zombie.isPositionValid());
        assertPositionEquals(pig, 19.123, 68.5, -47.89);
        assertPositionEquals(chicken, 19.123, 68.5, -47.89);
        assertPositionEquals(zombie, 19.123, 68.5, -47.89);

        // should not move mount position, but should move passenger to also all NaN
        chicken.setPosition(Double.NaN, Double.NaN, Double.NaN);
        assertTrue(pig.isPositionValid());
        assertFalse(chicken.isPositionValid());
        assertFalse(zombie.isPositionValid());
        assertPositionEquals(pig, 19.123, 68.5, -47.89);
        assertPositionEquals(chicken, Double.NaN, Double.NaN, Double.NaN);
        assertPositionEquals(zombie, Double.NaN, Double.NaN, Double.NaN);

        // test the rest using XYZ setters to avoid a whitebox testing trap in case someday the impl changes

        zombie.setPosition(pig.getX(), pig.getY(), pig.getZ());
        pig.setX(0);
        assertPositionEquals(pig, 0, 68.5, -47.89);
        assertPositionEquals(chicken, 0, Double.NaN, Double.NaN);
        assertPositionEquals(zombie, 0, 68.5, -47.89);

        pig.setY(Double.NaN);
        assertPositionEquals(pig, 0, Double.NaN, -47.89);
        assertPositionEquals(chicken, 0, Double.NaN, Double.NaN);
        assertPositionEquals(zombie, 0, Double.NaN, -47.89);

        pig.setY(42);
        assertPositionEquals(pig, 0, 42, -47.89);
        assertPositionEquals(chicken, 0, 42, Double.NaN);
        assertPositionEquals(zombie, 0, 42, -47.89);

        zombie.setZ(-47.5);
        assertPositionEquals(pig, 0, 42, -47.89);
        assertPositionEquals(chicken, 0, 42, Double.NaN);
        assertPositionEquals(zombie, 0, 42, -47.5);

        pig.setZ(0);
        assertPositionEquals(pig, 0, 42,0);
        assertPositionEquals(chicken, 0, 42, 0);
        assertPositionEquals(zombie, 0, 42, 0);
    }

    public void testMovePosition_basic() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        pig.setPosition(19.123, 68.5, -47.89);
        assertPositionEquals(pig, 19.123, 68.5, -47.89);
        pig.movePosition(-7, -1, 4);
        assertPositionEquals(pig, 19.123 - 7, 68.5 - 1, -47.89 + 4);
    }

    public void testMovePosition_cannotMoveInvalidPosition() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        assertThrowsException(() -> pig.movePosition(-7, -1, 4), IllegalStateException.class);
        pig.setX(0);
        assertThrowsException(() -> pig.movePosition(-7, -1, 4), IllegalStateException.class);
        pig.setY(0);
        assertThrowsException(() -> pig.movePosition(-7, -1, 4), IllegalStateException.class);
        pig.setZ(0);
        assertThrowsNoException(() -> pig.movePosition(-7, -1, 4));
        assertPositionEquals(pig, -7, -1, 4);
    }

    public void testMovePosition_withPassengers() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        EntityBaseImpl chicken = new EntityBaseImpl(DataVersion.latest().id(), "chicken");
        EntityBaseImpl zombie = new EntityBaseImpl(DataVersion.latest().id(), "zombie");
        chicken.addPassenger(zombie);
        pig.addPassenger(chicken);

        pig.setPosition(0, 0, 0);
        // add offsets
        chicken.movePosition(0.1, 0.5, 0);
        zombie.movePosition(0, 0.15, -0.1);
        // check everyone is where they should be
        assertPositionEquals(pig, 0, 0, 0);
        assertPositionEquals(chicken, 0.1, 0.5, 0);
        assertPositionEquals(zombie, 0.1, 0.65, -0.1);

        // move the root pig
        pig.movePosition(-7, 80, 4);
        assertPositionEquals(pig, -7, 80, 4);
        // check that offsets are preserved
        assertPositionEquals(chicken, -6.9, 80.5, 4);
        assertPositionEquals(zombie, -6.9, 80.65, 3.9);

        // now with some invalid positions in the mix
        pig.setPosition(0, 0, 0);
        chicken.setPosition(Double.NaN, Double.NaN, Double.NaN);
        zombie.setPosition(-1, 0.5, 1);

        pig.movePosition(-7, -1, 4);
        assertPositionEquals(pig, -7, -1, 4);
        assertPositionEquals(chicken, -7, -1, 4);
        assertPositionEquals(zombie, -7, -1, 4);
    }

    // </editor-fold>

    // <editor-fold desc="Rotation Yaw Pitch Tests" defaultstate="collapsed">

    public void testSetRotation_basic() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        pig.setRotation(73.23f, -11.55f);
        assertEquals(73.23f, pig.getRotationYaw(), 1e-3);
        assertEquals(-11.55f, pig.getRotationPitch(), 1e-3);

        // also checks yaw normalization
        pig.setRotationYaw(-30.78f - 720);
        assertEquals(360 - 30.78f, pig.getRotationYaw(), 1e-3);

        pig.setRotationPitch(45.976f);
        assertEquals(45.976f, pig.getRotationPitch(), 1e-3);

        // pitch clamping
        pig.setRotationPitch(145.976f);
        assertEquals(90f, pig.getRotationPitch(), 1e-3);
        pig.setRotationPitch(-145.976f);
        assertEquals(-90f, pig.getRotationPitch(), 1e-3);
    }

    public void testSetRotation_doesNotAffectPassengers() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        pig.setRotation(30f, 45f);
        EntityBaseImpl chicken = new EntityBaseImpl(DataVersion.latest().id(), "chicken");
        chicken.setRotation(51f, -17f);
        pig.addPassenger(chicken);
        assertEquals(30f, pig.getRotationYaw(), 1e-3);
        assertEquals(51f, chicken.getRotationYaw(), 1e-3);
        assertEquals(45f, pig.getRotationPitch(), 1e-3);
        assertEquals(-17f, chicken.getRotationPitch(), 1e-3);

        pig.setRotationYaw(32f);
        assertEquals(32f, pig.getRotationYaw(), 1e-3);  // changed
        assertEquals(51f, chicken.getRotationYaw(), 1e-3);  // unchanged

        chicken.setRotationYaw(52f);
        assertEquals(32f, pig.getRotationYaw(), 1e-3);  // unchanged
        assertEquals(52f, chicken.getRotationYaw(), 1e-3);  // changed

        pig.setRotationPitch(60f);
        assertEquals(60f, pig.getRotationPitch(), 1e-3);  // changed
        assertEquals(-17f, chicken.getRotationPitch(), 1e-3);  // unchanged

        chicken.setRotationPitch(20f);
        assertEquals(60f, pig.getRotationPitch(), 1e-3);  // unchanged
        assertEquals(20f, chicken.getRotationPitch(), 1e-3);  // changed
    }

    public void testCardinalAngleHelpers() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        pig.setFacingCardinalAngle(0);
        assertEquals(0f, pig.getFacingCardinalAngle(), 1e-4f);
        assertEquals(180f, pig.getRotationYaw(), 1e-4f);

        pig.setFacingCardinalAngle(90);
        assertEquals(90f, pig.getFacingCardinalAngle(), 1e-4f);
        assertEquals(270f, pig.getRotationYaw(), 1e-4f);

        pig.setFacingCardinalAngle(179);
        assertEquals(179f, pig.getFacingCardinalAngle(), 1e-4f);
        assertEquals(359f, pig.getRotationYaw(), 1e-4f);

        pig.setFacingCardinalAngle(181);
        assertEquals(181f, pig.getFacingCardinalAngle(), 1e-4f);
        assertEquals(1f, pig.getRotationYaw(), 1e-4f);

        pig.setFacingCardinalAngle(361);
        assertEquals(1f, pig.getFacingCardinalAngle(), 1e-4f);
        assertEquals(181f, pig.getRotationYaw(), 1e-4f);

        pig.setFacingCardinalAngle(-90);
        assertEquals(270f, pig.getFacingCardinalAngle(), 1e-4f);
        assertEquals(90f, pig.getRotationYaw(), 1e-4f);
    }

    public void testRotate_givenAngleMustBeFinite() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        assertThrowsIllegalArgumentException(() -> pig.rotate(Float.POSITIVE_INFINITY));
    }

    public void testRotate_passingHighExponentArgDoesNotSquashExistingYaw() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        pig.setRotationYaw(213.87139458f);
        pig.rotate((float)Long.MAX_VALUE);
        float yaw = pig.getRotationYaw();
        float fraction = yaw - ((int) yaw);
        assertEquals(0.87139458f, fraction, 1e-4);

        pig.setRotationYaw(213.87139458f);
        pig.rotate(100000000000.12);
        assertEquals(133.99139458f, pig.getRotationYaw(), 1e-3);  // yes i did the math by hand to get this number
    }

    public void testRotate_noPassengers() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        pig.setRotationYaw(Float.NaN);
        assertThrowsException(() -> pig.rotate(42), IllegalStateException.class);
        pig.setRotationYaw(10);
        pig.rotate(30);
        assertEquals(40f, pig.getRotationYaw(), 1e-4f);
        pig.rotate(-45);
        assertEquals(355f, pig.getRotationYaw(), 1e-4f);
    }

    public void testRotate_rotatesPassengers() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        EntityBaseImpl chicken = new EntityBaseImpl(DataVersion.latest().id(), "chicken");
        EntityBaseImpl zombie = new EntityBaseImpl(DataVersion.latest().id(), "zombie");
        pig.setRotationYaw(90f);
        chicken.setRotationYaw(-15f);
        zombie.setRotationYaw(22.5f);

        // validate that adding passengers doesn't rotate them based on mount yaw
        pig.addPassenger(chicken);
        chicken.addPassenger(zombie);
        pig.setRotationYaw(177f);
        assertEquals(177f, pig.getRotationYaw(), 1e-4f);
        assertEquals(345f, chicken.getRotationYaw(), 1e-4f);
        assertEquals(22.5f, zombie.getRotationYaw(), 1e-4f);

        // rotating mount should rotate passengers
        pig.setRotationYaw(0f);
        pig.rotate(45f);
        assertEquals(45f, pig.getRotationYaw(), 1e-4f);
        assertEquals(45f - 15f, chicken.getRotationYaw(), 1e-4f);
        assertEquals(45f + 22.5f, zombie.getRotationYaw(), 1e-4f);

        // rotating middle mount should rotate passengers, but not entity being ridden
        chicken.rotate(-10f);
        assertEquals(45f, pig.getRotationYaw(), 1e-4f);
        assertEquals(45f - 15f - 10f, chicken.getRotationYaw(), 1e-4f);
        assertEquals(45f + 22.5f - 10f, zombie.getRotationYaw(), 1e-4f);
    }


    // </editor-fold>

    // <editor-fold desc="Motion Tests" defaultstate="collapsed">


    public void testMotion_basic() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        assertMotionEquals(pig, 0, 0, 0);
        pig.setMotion(0.1, -0.12, -0.03);
        assertMotionEquals(pig, 0.1, -0.12, -0.03);

        pig.setMotionDX(1);
        assertMotionEquals(pig, 1, -0.12, -0.03);

        pig.setMotionDY(2);
        assertMotionEquals(pig, 1, 2, -0.03);

        pig.setMotionDZ(-3);
        assertMotionEquals(pig, 1, 2, -3);

        assertTrue(pig.isMotionValid());
        pig.setMotionDX(Double.NaN);
        assertFalse(pig.isMotionValid());
        pig.setMotionDX(0);
        pig.setMotionDY(Double.POSITIVE_INFINITY);
        assertFalse(pig.isMotionValid());
        pig.setMotionDY(0);
        pig.setMotionDZ(Double.NEGATIVE_INFINITY);
        assertFalse(pig.isMotionValid());

        pig.setMotionDZ(0);
        assertTrue(pig.isMotionValid());
    }

    public void testAddPassenger_syncsPassengerMotion() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        EntityBaseImpl chicken = new EntityBaseImpl(DataVersion.latest().id(), "chicken");
        EntityBaseImpl zombie = new EntityBaseImpl(DataVersion.latest().id(), "zombie");

        pig.setMotion(0.1, 0.2, 0.3);

        chicken.addPassenger(zombie);
        assertMotionEquals(chicken, 0, 0, 0);
        assertMotionEquals(zombie, 0, 0, 0);

        pig.addPassenger(chicken);
        assertMotionEquals(pig, 0.1, 0.2, 0.3);
        assertMotionEquals(chicken, 0.1, 0.2, 0.3);
        assertMotionEquals(zombie, 0.1, 0.2, 0.3);
    }
    // </editor-fold>

    public void testGenerateNewUuid() {
        EntityBaseImpl pig = new EntityBaseImpl(DataVersion.latest().id(), "pig");
        EntityBaseImpl chicken = new EntityBaseImpl(DataVersion.latest().id(), "chicken");
        EntityBaseImpl zombie = new EntityBaseImpl(DataVersion.latest().id(), "zombie");
        chicken.addPassenger(zombie);
        pig.addPassenger(chicken);

        UUID pigUuid = UUID.randomUUID();
        UUID chickenUuid = UUID.randomUUID();
        UUID zombieUuid = UUID.randomUUID();

        pig.setUuid(pigUuid);
        chicken.setUuid(chickenUuid);
        zombie.setUuid(zombieUuid);

        pig.generateNewUuid();

        assertNotNull(pig.getUuid());
        assertNotNull(chicken.getUuid());
        assertNotNull(zombie.getUuid());

        assertNotEquals(pigUuid, pig.getUuid());
        assertNotEquals(chickenUuid, chicken.getUuid());
        assertNotEquals(zombieUuid, zombie.getUuid());
    }
}
