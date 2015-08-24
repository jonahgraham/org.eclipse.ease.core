package org.eclipse.ease;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class AbstractHeaderParserTest {

	// classic implementation of a HeaderParser
	private class HeaderParser extends AbstractCodeParser {

		@Override
		protected boolean hasBlockComment() {
			return true;
		}

		@Override
		protected String getBlockCommentEndToken() {
			return "*/";
		}

		@Override
		protected String getBlockCommentStartToken() {
			return "/*";
		}

		@Override
		protected String getLineCommentToken() {
			return "//";
		}
	}

	private static final String TEMPLATE_BLOCK_HEADER = "/**\n" + " * This is a block comment with a keyword.\n" + " * \n" + " * key: word\n"
			+ " * menu: no menu selected\n" + " * multi: this is a multi\n" + " * line keyword\n" + " **/\n";
	private static final String TEMPLATE_LINE_HEADER = "//\n" + "// This is a block comment with a keyword.\n" + "// \n" + " // key: word\n"
			+ "// menu: no menu selected\n" + "// multi: this is a multi\n" + "// line keyword\n" + "//\n";
	private static final String TEMPLATE_NO_COMMENT = "var a = 13;\n" + TEMPLATE_BLOCK_HEADER;
	private static final String TEMPLATE_WHITESPACE_BEFORE_BLOCK_HEADER = "\n" + "\t\t\n" + "        \n" + "     " + TEMPLATE_BLOCK_HEADER;

	private HeaderParser fParser;

	@Before
	public void setUp() throws Exception {
		fParser = new HeaderParser();
	}

	@Test
	public void parseEmpty() {
		assertTrue(fParser.parse(toStream("")).isEmpty());
	}

	@Test
	public void parseCode() {
		assertTrue(fParser.parse(toStream(TEMPLATE_NO_COMMENT)).isEmpty());
	}

	@Test
	public void parseLineHeader() {
		final Map<String, String> keywords = fParser.parse(toStream(TEMPLATE_LINE_HEADER));
		assertEquals(3, keywords.size());
		assertEquals("word", keywords.get("key"));
		assertEquals("no menu selected", keywords.get("menu"));
		assertEquals("this is a multi line keyword", keywords.get("multi"));
	}

	@Test
	public void parseBlockHeader() {
		final Map<String, String> keywords = fParser.parse(toStream(TEMPLATE_BLOCK_HEADER));
		assertEquals(3, keywords.size());
		assertEquals("word", keywords.get("key"));
		assertEquals("no menu selected", keywords.get("menu"));
		assertEquals("this is a multi line keyword", keywords.get("multi"));
	}

	@Test
	public void parseBlockAfterWhitespace() {
		final Map<String, String> keywords = fParser.parse(toStream(TEMPLATE_WHITESPACE_BEFORE_BLOCK_HEADER));
		assertEquals(3, keywords.size());
		assertEquals("word", keywords.get("key"));
		assertEquals("no menu selected", keywords.get("menu"));
		assertEquals("this is a multi line keyword", keywords.get("multi"));
	}

	@Test
	public void createHeader() {
		final HashMap<String, String> keywords = new HashMap<String, String>();
		keywords.put("first", "value");
		keywords.put("menu", "this is a menu entry");
		final String header = fParser.createHeader(keywords);

		assertEquals(keywords, fParser.parse(toStream(header)));
	}

	private static final InputStream toStream(String data) {
		return new ByteArrayInputStream(data.getBytes());
	}
}
