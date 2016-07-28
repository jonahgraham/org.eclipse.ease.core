/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.sign.ScriptSignatureException;
import org.eclipse.ease.sign.SignatureInfo;

public abstract class AbstractCodeParser implements ICodeParser {

	/** Default line break character. */
	public static final String LINE_DELIMITER = System.getProperty(Platform.PREF_LINE_SEPARATOR);

	private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\s*([\\p{Alnum}-_]*)\\s*:(.*)");

	/** Begin and End strings for signature block. */
	private static final String BEGIN_STRING = "-----BEGIN SIGNATURE-----", END_STRING = "-----END SIGNATURE-----";

	@Override
	public Map<String, String> parse(final InputStream stream) {
		final Map<String, String> parameters = new HashMap<String, String>();

		final String comment = getComment(stream);

		String key = null;
		for (String line : comment.split("\\r?\\n")) {
			final Matcher matcher = PARAMETER_PATTERN.matcher(line);
			if (matcher.matches()) {
				// key value pair found
				key = matcher.group(1);
				parameters.put(key, matcher.group(2).trim());

			} else if (key != null) {
				if (!line.trim().isEmpty()) {
					// check that we do not have a delimiter line (all same chars)
					line = line.trim();
					if (!Pattern.matches("[" + line.charAt(0) + "]+", line))
						// line belongs to previous key value pair
						parameters.put(key, parameters.get(key) + " " + line.trim());
					else
						// line does not belong to previous key anymore
						key = null;
				} else
					// remove cached key as we hit an empty line
					key = null;
			}

			// any other line will be ignored
		}

		return parameters;
	}

	@Override
	public String createHeader(final Map<String, String> headerContent) {
		final StringBuilder builder = new StringBuilder();

		builder.append(getLineCommentToken());
		builder.append(' ');
		builder.append("********************************************************************************");
		builder.append(LINE_DELIMITER);

		for (final Entry<String, String> entry : headerContent.entrySet()) {
			final StringBuilder lineBuilder = new StringBuilder();

			lineBuilder.append(getLineCommentToken()).append(" ").append(entry.getKey());
			while (lineBuilder.length() < 24)
				lineBuilder.append(' ');

			lineBuilder.append(": ").append(entry.getValue()).append(LINE_DELIMITER);
			builder.append(lineBuilder);
		}

		builder.append(getLineCommentToken());
		builder.append(' ');
		builder.append("********************************************************************************");
		builder.append(LINE_DELIMITER);

		return builder.toString();
	}

	/**
	 * Default implementation to extract the first comment area from a stream. Looks for block and line comments. Might be replaced by more specific
	 * implementations for dedicated languages.
	 *
	 * @param stream
	 *            stream to parse
	 * @return String containing the detected comment or an empty string. Never returns <code>null</code>
	 */
	protected String getComment(final InputStream stream) {
		final StringBuilder comment = new StringBuilder();

		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		boolean isComment = true;
		boolean isBlock = false;
		try {
			do {
				String line = reader.readLine();
				if (line == null)
					break;

				line = line.trim();

				if (line.isEmpty())
					continue;

				if (line.startsWith(getLineCommentToken())) {
					comment.append(line.substring(getLineCommentToken().length()).trim());
					comment.append("\n");
					continue;
				}

				if (hasBlockComment()) {
					if (line.startsWith(getBlockCommentStartToken())) {
						isBlock = true;
						line = line.substring(getBlockCommentStartToken().length()).trim();
					}

					if (isBlock) {
						if (line.contains(getBlockCommentEndToken())) {
							isBlock = false;
							line = line.substring(0, line.indexOf(getBlockCommentEndToken()));
						}

						// remove leading '*' characters
						line = line.trim();
						while (line.startsWith("*"))
							line = line.substring(1);

						comment.append(line.trim());
						comment.append("\n");
						continue;
					}
				}

				// not a comment line, not empty
				isComment = false;

			} while (isComment);

		} catch (final IOException e) {
			Logger.error(Activator.PLUGIN_ID, "Could not parse input stream header", e);
			return "";
		}

		return comment.toString();
	}

	@Override
	public ICompletionContext getContext(final IScriptEngine scriptEngine, final Object resource, final String contents, final int position,
			final int selectionRange) {
		return null;
	}

	@Override
	public SignatureInfo getSignatureInfo(final InputStream stream) throws ScriptSignatureException {
		BufferedReader bReader = new BufferedReader(new InputStreamReader(stream));
		try {
			String prev, cur;

			// contentBuffer is used to get content excluding signature block. It is updated continuously because in rare cases, we may come to know that
			// what we have read yet is not an actual signature block but part of original script.
			StringBuffer contentBuffer = new StringBuffer();

			cur = bReader.readLine();
			if (cur == null)
				return null;

			// A line before BEGIN_STRING will be comment and to not include that in contentOnly, prev is used. Using prev, contentOnly will be appended with
			// previous line only if next line is not BEGIN_STRING.
			prev = cur;
			while ((cur = bReader.readLine()) != null) {

				while (!cur.equals(BEGIN_STRING)) {
					contentBuffer.append(prev + "\n");
					prev = cur;
					cur = bReader.readLine();
					if (cur == null)
						return null;
				}

				StringBuffer contentOnlyBuffer = new StringBuffer(contentBuffer);
				// remove an extra \n character at end. Since this content is used for verification. Same script is required in contentOnly as it was before
				// signing
				if (contentOnlyBuffer.length() > 0)
					contentOnlyBuffer.deleteCharAt(contentOnlyBuffer.length() - 1);

				contentBuffer.append(prev + "\n");
				if (!prev.equals(getBlockCommentStartToken())) {
					// if start block comment is not present, it is not a signature block
					prev = cur;
					continue;
				}

				contentBuffer.append(cur + "\n");

				// else{ continue; } denote that signature in proper format is not yet found and can be found later. So, continue with finding BEGIN_STRING.
				// else{ break; } denote that end of script is reached and so, return null.

				String provider, messageDigestAlgo;
				cur = bReader.readLine();
				if (cur != null) {
					String params[] = cur.split(" ");
					if (params.length == 2) {
						String temp[] = params[0].split(":");
						if (temp.length == 2)
							messageDigestAlgo = temp[1];
						else {
							prev = cur;
							continue;
						}
						temp = params[1].split(":");
						if (temp.length == 2)
							provider = temp[1];
						else {
							prev = cur;
							continue;
						}

					} else {
						prev = cur;
						continue;
					}
					contentBuffer.append(cur + "\n");
				} else
					break;

				// following block checks for empty line. If one is not found, then restart checking BEGIN_STRING
				cur = bReader.readLine();
				if (cur != null) {
					if (!cur.isEmpty()) {
						prev = cur;
						continue;
					}
					contentBuffer.append(cur + "\n");
				} else
					break;

				// following block fetches signature
				String signature;
				cur = bReader.readLine();
				if (cur != null) {
					if (cur.length() == 64)
						signature = cur;
					else {
						prev = cur;
						continue;
					}
					contentBuffer.append(cur + "\n");
				} else
					break;

				// following block checks for empty line.
				cur = bReader.readLine();
				if (cur != null) {
					if (!cur.isEmpty()) {
						prev = cur;
						continue;
					}
					contentBuffer.append(cur + "\n");
				} else
					break;

				// following block fetches certificates separated by colon(:)
				StringBuffer certBuf = new StringBuffer();
				while ((cur = bReader.readLine()) != null && !cur.isEmpty()) {
					certBuf.append(cur);
					contentBuffer.append(cur + "\n");
				}

				if (cur == null)
					break;

				contentBuffer.append(cur + "\n");

				cur = bReader.readLine();
				if (cur != null) {
					if (!cur.equals(END_STRING)) {
						prev = cur;
						continue;
					}
					contentBuffer.append(cur + "\n");
				} else
					break;

				cur = bReader.readLine();
				if (cur != null) {
					if (!cur.equals(getBlockCommentEndToken())) {
						// if end block comment is not present, it is not a signature block
						prev = cur;
						continue;
					}

					contentBuffer.append(cur + "\n");
				} else
					break;

				// checks end of script
				cur = bReader.readLine();
				if (cur != null)
					throw new ScriptSignatureException("Text after signature is not allowed");
				else {
					String certificates[] = certBuf.toString().split(":");
					return new SignatureInfo(signature, provider, messageDigestAlgo, certificates, contentOnlyBuffer.toString());
				}
			}
			return null;

		} catch (IOException e) {
			Logger.error(Activator.PLUGIN_ID, "An IO error occurred while reading file.", e);
			throw new ScriptSignatureException("An IO error occurred while reading file.", e);

		} finally {
			try {
				if (bReader != null)
					bReader.close();
			} catch (IOException e) {
				Logger.error(Activator.PLUGIN_ID, "File already closed.", e);
			}
		}

	}

	protected abstract boolean hasBlockComment();

	protected abstract String getBlockCommentEndToken();

	protected abstract String getBlockCommentStartToken();

	protected abstract String getLineCommentToken();
}
