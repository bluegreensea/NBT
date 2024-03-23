package net.querz;

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import junit.framework.TestCase;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NBTSerializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.Tag;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class NBTTestCase extends TestCase {

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		cleanupTmpDir();
	}

	protected byte[] serialize(Tag<?> tag) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (DataOutputStream dos = new DataOutputStream(baos)) {
			new NBTSerializer(false).toStream(new NamedTag(null, tag), dos);
		} catch (IOException ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
		return baos.toByteArray();
	}

	protected Tag<?> deserialize(byte[] data) {
		try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
			return new NBTDeserializer(false).fromStream(dis).getTag();
		} catch (IOException ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
			return null;
		}
	}

	protected File getResourceFile(String name) {
		URL resource = getClass().getClassLoader().getResource(name);
		assertNotNull(resource);
		return new File(resource.getFile());
	}

	protected Tag<?> deserializeFromFile(String f) {
		try (DataInputStream dis = new DataInputStream(new FileInputStream(getResourceFile(f)))) {
			return new NBTDeserializer(false).fromStream(dis).getTag();
		} catch (IOException ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
			return null;
		}
	}

	protected <T> void invokeSetValue(Tag<T> tag, T value) {
		try {
			Class<?> c = tag.getClass();
			Method m;
			while (c != Object.class) {
				try {
					m = c.getDeclaredMethod("setValue", Object.class);
					m.setAccessible(true);
					m.invoke(tag, value);
					return;
				} catch (NoSuchMethodException ex) {
					c = c.getSuperclass();
				}
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			fail("unable to invoke setValue() on " + tag.getClass().getSimpleName());
		}
		fail("could not find setValue()");
	}

	@SuppressWarnings("unchecked")
	protected <T> T invokeGetValue(Tag<T> tag) {
		try {
			Class<?> c = tag.getClass();
			Method m;
			while (c != Object.class) {
				try {
					m = c.getDeclaredMethod("getValue");
					m.setAccessible(true);
					return (T) m.invoke(tag);
				} catch (NoSuchMethodException ex) {
					c = c.getSuperclass();
				}
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			fail("unable to invoke getValue() on " + tag.getClass().getSimpleName());
		}
		fail("could not find getValue()");
		return null;
	}

	protected void assertThrowsIllegalArgumentException(ExceptionRunnable<IllegalArgumentException> r) {
		assertThrowsException(r, IllegalArgumentException.class);
	}

	protected void assertThrowsUnsupportedOperationException(ExceptionRunnable<IllegalArgumentException> r) {
		assertThrowsException(r, UnsupportedOperationException.class);
	}

	protected <E extends Exception> void assertThrowsException(ExceptionRunnable<E> r, Class<? extends Exception> e) {
		try {
			r.run();
			TestCase.fail("Did not throw expected: " + e.getSimpleName());
		} catch (Exception ex) {
			if (!e.equals(ex.getClass())) {
				throw new WrongExceptionThrownException(e, ex);
			}
		}
	}

	/**
	 * @deprecated replaced by improved {@link #assertThrowsException(ExceptionRunnable, Class)}
	 */
	@Deprecated
	protected <E extends Exception> void assertThrowsException(ExceptionRunnable<E> r, Class<? extends Exception> e, boolean printStackTrace) {
		assertThrowsException(r, e);
	}

	protected <E extends Exception> void assertThrowsNoException(ExceptionRunnable<E> r) {
		try {
			r.run();
		} catch (Exception ex) {
			ex.printStackTrace();
			TestCase.fail("Threw exception " + ex.getClass().getName() + " with message \"" + ex.getMessage() + "\"");
		}
	}

	protected <T, E extends Exception> void assertThrowsException(ExceptionSupplier<T, E> r, Class<? extends Exception> e) {
		try {
			r.run();
			TestCase.fail();
		} catch (Exception ex) {
			if (!e.equals(ex.getClass())) {
				throw new WrongExceptionThrownException(e, ex);
			}
		}
	}

	/**
	 * @deprecated replaced by improved {@link #assertThrowsException(ExceptionSupplier, Class)}
	 */
	@Deprecated
	protected <T, E extends Exception> void assertThrowsException(ExceptionSupplier<T, E> r, Class<? extends Exception> e, boolean printStackTrace) {
		assertThrowsException(r, e);
	}

	private static class WrongExceptionThrownException extends ComparisonFailure {
		public WrongExceptionThrownException(Class<? extends Exception> expectedType, Exception actual) {
			super("", expectedType.getTypeName(), actual.getClass().getTypeName());
			this.setStackTrace(actual.getStackTrace());
		}
	}

	private static class UnexpectedExceptionThrownException extends AssertionFailedError {
		public UnexpectedExceptionThrownException(Exception ex) {
			super("Threw exception " + ex.getClass().getName() + " with message \"" + ex.getMessage() + "\"");
			this.setStackTrace(ex.getStackTrace());
		}
	}

	protected <T, E extends Exception> T assertThrowsNoException(ExceptionSupplier<T, E> r) {
		try {
			return r.run();
		} catch (Exception ex) {
			throw new UnexpectedExceptionThrownException(ex);
		}
	}

	protected void assertThrowsRuntimeException(Runnable r, Class<? extends Exception> e) {
		try {
			r.run();
			TestCase.fail();
		} catch (Exception ex) {
			if (!e.equals(ex.getClass())) {
				throw new WrongExceptionThrownException(e, ex);
			}
		}
	}

	/**
	 * @deprecated replaced by improved {@link #assertThrowsRuntimeException(Runnable, Class)}
	 */
	@Deprecated
	protected void assertThrowsRuntimeException(Runnable r, Class<? extends Exception> e, boolean printStackTrace) {
		assertThrowsRuntimeException(r, e);
	}

	protected void assertThrowsRuntimeException(Runnable r, boolean printStackTrace) {
		try {
			r.run();
			TestCase.fail();
		} catch (Exception ex) {
			if (printStackTrace) {
				ex.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	protected void assertThrowsNoRuntimeException(Runnable r) {
		Void v = assertThrowsNoRuntimeException(() -> {
			r.run();
			return null;
		});
	}

	protected <T> T assertThrowsNoRuntimeException(Supplier<T> r) {
		try {
			return r.get();
		} catch (Exception ex) {
			ex.printStackTrace();
			TestCase.fail("Threw exception " + ex.getClass().getName() + " with message \"" + ex.getMessage() + "\"");
		}
		return null;
	}

	protected File getNewTmpFile(String name) {
		final String workingDir = System.getProperty("user.dir");
		Path tmpPath = Paths.get(
				workingDir,
				"tmp",
				this.getClass().getSimpleName(),
				getName(),
				UUID.randomUUID().toString(),
				name);
		File dir = tmpPath.getParent().toFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return tmpPath.toFile();
	}

	protected File copyResourceToTmp(String resource) {
		URL resFileURL = getClass().getClassLoader().getResource(resource);
		TestCase.assertNotNull(resFileURL);
		String resPath = null;
		try {
			resPath = java.net.URLDecoder.decode(resFileURL.getFile(), StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		File resFile = new File(resPath);
		File tmpFile = getNewTmpFile(resource);
		assertThrowsNoException(() -> Files.copy(resFile.toPath(), tmpFile.toPath()));
		return tmpFile;
	}

	private void deleteRecursive(File deleteMe) {
		File[] contents = deleteMe.listFiles();
		if (contents != null) {
			for (File file : contents) {
				deleteRecursive(file);
			}
		}
		deleteMe.delete();
	}

	protected void cleanupTmpDir() {
		deleteRecursive(new File(System.getProperty("user.dir"), "tmp"));
	}

	protected String calculateFileMD5(File file) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ex) {
			TestCase.fail(ex.getMessage());
		}
		try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
			byte[] buffer = new byte[8192];
			int numRead;
			do {
				numRead = bis.read(buffer);
				if (numRead > 0) {
					md.update(buffer, 0, numRead);
				}
			} while (numRead != -1);
		} catch (IOException ex) {
			TestCase.fail(ex.getMessage());
		}
		return byteArrayToHexString(md.digest());
	}

	protected String byteArrayToHexString(byte[] bytes) {
		BigInteger bi = new BigInteger(1, bytes);
		return String.format("%0" + (bytes.length << 1) + "X", bi);
	}
}
