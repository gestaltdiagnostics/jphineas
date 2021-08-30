package tdunnick.jphineas.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class ByteArrayTest {
	private String s = "The quick brown fox jumped over the lazy dogs";
	private byte[] b1 = s.substring(0, 20).getBytes();
	private byte[] b2 = s.substring (20).getBytes();
	
	@Test
	public void testInsert()
	{
		byte[] b = ByteArray.insert (b1, b2, 20);
		assertTrue("insert failed", s.equals(new String(b)));
	}

	@Test
	public void testAppend()
	{
		byte[] b = ByteArray.append (b1, b2);
		assertTrue("append failed", s.equals(new String(b)));
		
	}

	@Test
	public void testCopyByteArray()
	{
		byte[] b = ByteArray.copy (s.getBytes(), 0, 20);
		assertTrue("copy from beginning failed", new String(b).equals(new String(b1)));
		b = ByteArray.copy (s.getBytes(), 20);
		assertTrue("copy from middle failed", new String(b).equals (new String (b2)));
	}
}
