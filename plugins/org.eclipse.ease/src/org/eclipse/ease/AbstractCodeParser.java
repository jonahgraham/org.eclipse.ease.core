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
/*******************************************************************************
 * Copyright (c) 2016 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ease.sign.ISignatureConstants;
import org.eclipse.ease.sign.ScriptSignatureException;
import org.eclipse.ease.sign.SignatureInfo;

public abstract class AbstractCodeParser implements ICodeParser {

	public static final Pattern PARAMETER_PATTERN = Pattern.compile("[^\\p{Alnum}-_]*?\\s*([\\p{Alnum}-_]+)\\s*:(.*)");

	public static Map<String, String> extractKeywords(String comment) {
		final Map<String, String> keywords = new HashMap<>();

		String key = null;
		for (String line : comment.split("\\r?\\n")) {
			final Matcher matcher = PARAMETER_PATTERN.matcher(line);
			if (matcher.matches()) {
				// key value pair found
				key = matcher.group(1);
				keywords.put(key, matcher.group(2).trim());

			} else if (key != null) {
				if (!line.trim().isEmpty()) {
					// check that we do not have a delimiter line (all same chars)
					line = line.trim();
					if (!Pattern.matches("[" + line.charAt(0) + "]+", line))
						// line belongs to previous key value pair
						keywords.put(key, keywords.get(key) + " " + line.trim());
					else
						// line does not belong to previous key anymore
						key = null;
				} else
					// remove cached key as we hit an empty line
					key = null;
			}

			// any other line will be ignored
		}

		return keywords;
	}

	/**
	 * Default implementation to extract the first comment area from a stream. Looks for block and line comments. Might be replaced by more specific
	 * implementations for dedicated languages.
	 *
	 * @param stream
	 *            code content stream
	 * @return comment data without decoration characters (eg: '*' at beginning of each line)
	 */
	@Override
	public String getHeaderComment(final InputStream stream) {
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

				if (isBlock) {
					if (line.contains(getBlockCommentEndToken())) {
						isBlock = false;
						line = line.substring(0, line.indexOf(getBlockCommentEndToken()));
					}

					comment.append(stripCommentLine(line.trim())).append('\n');
					continue;

				} else if ((hasBlockComment()) && (line.startsWith(getBlockCommentStartToken()))) {
					isBlock = true;
					line = line.substring(getBlockCommentStartToken().length()).trim();
					comment.append(stripCommentLine(line)).append('\n');
					continue;

				} else if (line.startsWith(getLineCommentToken())) {
					comment.append(stripCommentLine(line.substring(getLineCommentToken().length()).trim())).append('\n');
					continue;
				}

				if (line.isEmpty())
					continue;

				// not a comment line, not empty
				if (!isAcceptedBeforeHeader(line))
					isComment = false;

			} while (isComment);

		} catch (final IOException e) {
			Logger.error(Activator.PLUGIN_ID, "Could not parse input stream header", e);
			return "";
		}

		return comment.toString();
	}

	/**
	 * Allows to remove special delimiter characters from a comment line. Typically comments might start with a character like '*' or '#' depending on the
	 * script language.
	 *
	 * @param commentLine
	 *            single comment line
	 * @return modified comment line
	 */
	protected String stripCommentLine(String commentLine) {
		return commentLine;
	}

	@Override
	public boolean isAcceptedBeforeHeader(String line) {
		return false;
	}

	@Override
	public ICompletionContext getContext(final IScriptEngine scriptEngine, final Object resource, final String contents, final int position,
			final int selectionRange) {
		return null;
	}

	@Override
	public SignatureInfo getSignatureInfo(final InputStream stream) throws ScriptSignatureException {
		final BufferedReader bReader = new BufferedReader(new InputStreamReader(stream));
		try {
			String prev, cur;

			// contentBuffer is used to get content excluding signature block. It is updated continuously because in rare cases, we may come to know that
			// what we have read yet is not an actual signature block but part of original script.
			final StringBuffer contentBuffer = new StringBuffer();

			cur = bReader.readLine();
			if (cur == null)
				return null;

			// A line before BEGIN_STRING will be comment and to not include that in contentOnly, prev is used. Using prev, contentOnly will be appended with
			// previous line only if next line is not BEGIN_STRING.
			prev = cur;
			while ((cur = bReader.readLine()) != null) {

				while (!cur.equals(ISignatureConstants.BEGIN_STRING)) {
					contentBuffer.append(prev + "\n");
					prev = cur;
					cur = bReader.readLine();
					if (cur == null)
						return null;
				}

				final StringBuffer contentOnlyBuffer = new StringBuffer(contentBuffer);
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

				// hash param tag
				cur = bReader.readLine();
				if (cur != null) {
					if (!cur.equals(ISignatureConstants.HASH_PARAM_TAG)) {
						prev = cur;
						continue;
					}
					contentBuffer.append(cur + "\n");
				} else
					break;

				// hash value
				String messageDigestAlgo;
				cur = bReader.readLine();
				if (cur != null) {
					messageDigestAlgo = cur;
					contentBuffer.append(cur + "\n");
				} else
					break;

				// new line
				cur = bReader.readLine();
				if (cur != null) {
					if (!cur.isEmpty()) {
						prev = cur;
						continue;
					}
					contentBuffer.append(cur + "\n");
				} else
					break;

				// provider param tag
				cur = bReader.readLine();
				if (cur != null) {
					if (!cur.equals(ISignatureConstants.PROVIDER_PARAM_TAG)) {
						prev = cur;
						continue;
					}
					contentBuffer.append(cur + "\n");
				} else
					break;

				// provider value
				String provider;
				cur = bReader.readLine();
				if (cur != null) {
					provider = cur;
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

				// signature tag
				cur = bReader.readLine();
				if (cur != null) {
					if (!cur.equals(ISignatureConstants.SIGNATURE_TAG)) {
						prev = cur;
						continue;
					}
					contentBuffer.append(cur + "\n");
				} else
					break;

				// following block fetches signature
				final StringBuffer signBuf = new StringBuffer();
				while (((cur = bReader.readLine()) != null) && !cur.isEmpty()) {
					signBuf.append(cur);
					contentBuffer.append(cur + "\n");
				}

				if (cur == null)
					break;

				contentBuffer.append(cur + "\n");

				cur = bReader.readLine();
				if (cur != null) {
					if (!cur.equals(ISignatureConstants.CERTIFICATE_TAG)) {
						prev = cur;
						continue;
					}
					contentBuffer.append(cur + "\n");
				} else
					break;

				// following block fetches certificates separated by colon(:)
				final StringBuffer certBuf = new StringBuffer();
				while (((cur = bReader.readLine()) != null) && !cur.isEmpty()) {
					certBuf.append(cur);
					contentBuffer.append(cur + "\n");
				}

				if (cur == null)
					break;

				contentBuffer.append(cur + "\n");

				// end string
				cur = bReader.readLine();
				if (cur != null) {
					if (!cur.equals(ISignatureConstants.END_STRING)) {
						prev = cur;
						continue;
					}
					contentBuffer.append(cur + "\n");
				} else
					break;

				// end comment token
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
					final String signature = signBuf.toString();
					final String certificates[] = certBuf.toString().split(":");
					return new SignatureInfo(signature, provider, messageDigestAlgo, certificates, contentOnlyBuffer.toString());
				}
			}
			return null;

		} catch (final IOException e) {
			Logger.error(Activator.PLUGIN_ID, "An IO error occurred while reading file.", e);
			throw new ScriptSignatureException("An IO error occurred while reading file.", e);

		} finally {
			try {
				if (bReader != null)
					bReader.close();
			} catch (final IOException e) {
				Logger.error(Activator.PLUGIN_ID, "File already closed.", e);
			}
		}

	}

	protected abstract boolean hasBlockComment();

	protected abstract String getBlockCommentEndToken();

	protected abstract String getBlockCommentStartToken();

	protected abstract String getLineCommentToken();
}
