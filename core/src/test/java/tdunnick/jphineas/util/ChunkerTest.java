package tdunnick.jphineas.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for Chunker
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class ChunkerTest {
	@Test
	public void testChunking() {
		String message = "this is a message";
		byte[] data = message.getBytes(StandardCharsets.UTF_8);
		System.out.println(data.length);
		ByteBuffer buf = ByteBuffer.wrap(data);
		
		int chunkSize = 0;
		int needed = Chunker.needed(buf, chunkSize);
		assertEquals(0, needed);
		byte[] part = Chunker.getBytes(buf, 0, chunkSize);
		assertArrayEquals(data, part);
		
		buf.flip();
		chunkSize = data.length;
		needed = Chunker.needed(buf, chunkSize);
		assertEquals(1, needed);
		part = Chunker.getBytes(buf, 0, chunkSize);
		assertArrayEquals(data, part);
		
		buf.flip();
		chunkSize = data.length/2;
		needed = Chunker.needed(buf, chunkSize);
		assertEquals(3, needed);
		
		// check the chunks based on the above chunkSize
		part = Chunker.getBytes(buf, 0, chunkSize);
		byte[] chunk = new byte[chunkSize];
		System.arraycopy(data, 0, chunk, 0, chunkSize);
		assertArrayEquals(chunk, part);
		
		part = Chunker.getBytes(buf, 1, chunkSize);
		chunk = new byte[chunkSize];
		System.arraycopy(data, 8, chunk, 0, chunkSize);
		assertArrayEquals(chunk, part);
		
		part = Chunker.getBytes(buf, 2, chunkSize);
		assertEquals(data[data.length - 1], part[0]);
	}
}
