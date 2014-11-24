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
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;

 public abstract class AbstractHeaderParser implements IHeaderParser {

	 private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\s*([\\p{Alnum}-_]*)\\s*:(.*)");

	 @Override
	 public Map<String, String> parser(final InputStream stream) {
		 Map<String, String> parameters = new HashMap<String, String>();

		 String comment = getComment(stream);

		 String key = null;
		 for (String line : comment.split("\\r?\\n")) {
			 Matcher matcher = PARAMETER_PATTERN.matcher(line);
			 if (matcher.matches()) {
				 // key value pair found
				 key = matcher.group(1);
				 parameters.put(key, matcher.group(2).trim());

			 } else if ((key != null) && (!line.trim().isEmpty())) {
				 // check that we do not have a delimiter line (all same chars)
				 line = line.trim();
				 if (!Pattern.matches("[" + line.charAt(0) + "]+", line))
					 // line belongs to previous key value pair
					 parameters.put(key, parameters.get(key) + " " + line.trim());
			 }

			 // any other line will be ignored
		 }

		 return parameters;
	 }

	 @Override
	 public String createHeader(final Map<String, String> headerContent) {
		 StringBuilder builder = new StringBuilder();

		 builder.append(getLineCommentToken());
		 builder.append(' ');
		 builder.append("********************************************************************************\n");

		 for (String key : headerContent.keySet()) {
			 StringBuilder lineBuilder = new StringBuilder();

			 lineBuilder.append(getLineCommentToken()).append(" ").append(key);
			 while (lineBuilder.length() < 24)
				 lineBuilder.append(' ');

			 lineBuilder.append(": ").append(headerContent.get(key)).append("\n");
			 builder.append(lineBuilder);
		 }

		 builder.append(getLineCommentToken());
		 builder.append(' ');
		 builder.append("********************************************************************************\n");

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
		 StringBuilder comment = new StringBuilder();

		 BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

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

		 } catch (IOException e) {
			 Logger.logError("Could not parse input stream header", e);
			 return "";
		 }

		 return comment.toString();
	 }

	 protected abstract boolean hasBlockComment();

	 protected abstract String getBlockCommentEndToken();

	 protected abstract String getBlockCommentStartToken();

	 protected abstract String getLineCommentToken();
 }
