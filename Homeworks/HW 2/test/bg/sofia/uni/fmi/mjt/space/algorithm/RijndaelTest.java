package bg.sofia.uni.fmi.mjt.space.algorithm;

import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;

public class RijndaelTest
{

	private static final int KEY_SIZE_IN_BITS = 128;

	private static final String ENCRYPTION_ALGORITHM = "AES";

	@Test
	public void testEncryptShouldEncryptCorrectly() {
		String messageStr = "Hello World!";
		byte[] message = messageStr.getBytes();
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		SecretKey secretKey = createSecretKey();

		Rijndael cipher = new Rijndael(secretKey);

		try
		{
			cipher.encrypt(stream, outputStream);
			Cipher newCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
			newCipher.init(Cipher.ENCRYPT_MODE, secretKey);

			byte[] encryptedData = newCipher.doFinal(message);

			assertEquals(new String(encryptedData), outputStream.toString());
		}
		catch (CipherException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
			   BadPaddingException | IllegalBlockSizeException e)
		{
			fail("Unexpected exception");
		}
	}

	@Test
	public void testEncryptWhenNullKeyShouldThrowCipherException()
	{
		String messageStr = "Hello World!";
		byte[] message = messageStr.getBytes();
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Rijndael cipher = new Rijndael(null);

		assertThrowsExactly(CipherException.class, () -> cipher.encrypt(stream, outputStream));
	}

	@Test
	public void testDecryptWhenNullKeyShouldThrowCipherException()
	{
		String messageStr = "Hello World!";
		byte[] message = messageStr.getBytes();
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Rijndael cipher = new Rijndael(null);

		assertThrowsExactly(CipherException.class, () -> cipher.decrypt(stream, outputStream));
	}

	@Test
	public void testEncryptThenDecryptShouldBeSame()
	{
		String messageStr = "Hello World!";
		byte[] message = messageStr.getBytes();
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Rijndael cipher = new Rijndael(createSecretKey());

		try
		{
			cipher.encrypt(stream, outputStream);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream();
			cipher.decrypt(inputStream, decryptedStream);
			assertEquals(messageStr, decryptedStream.toString());

		}
		catch (CipherException e)
		{
			fail("Unexpected exception");
		}
	}


	private static SecretKey createSecretKey()
	{
		try
		{
			KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
			keyGenerator.init(KEY_SIZE_IN_BITS);
			return   keyGenerator.generateKey();
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}
}
