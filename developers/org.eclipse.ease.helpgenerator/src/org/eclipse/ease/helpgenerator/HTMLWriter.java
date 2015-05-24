package org.eclipse.ease.helpgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Tag;

public class HTMLWriter {

	private class Overview implements Comparable<Overview> {
		private final String fTitle;
		private final String fLinkID;
		private final String fDescription;
		private final boolean fDeprecated;
		private FieldDoc fField;

		public Overview(final String title, final String linkID, final String description, final boolean deprecated) {
			fTitle = title;
			fLinkID = linkID;
			fDescription = description;
			fDeprecated = deprecated;
		}

		public Overview(final String title, final String linkID, final String description, final boolean deprecated, final FieldDoc field) {
			this(title, linkID, description, deprecated);
			fField = field;
		}

		@Override
		public int compareTo(final Overview arg0) {
			return fTitle.compareTo(arg0.fTitle);
		}
	};

	private static final String WRAP_TO_SCRIPT = "WrapToScript";
	private static final String QUALIFIED_WRAP_TO_SCRIPT = "org.eclipse.ease.modules." + WRAP_TO_SCRIPT;
	private static final Object SCRIPT_PARAMETER = "ScriptParameter";
	private static final Object QUALIFIED_SCRIPT_PARAMETER = "org.eclipse.ease.modules." + SCRIPT_PARAMETER;

	private static final String LINE_DELIMITER = "\n";

	private final LinkProvider fLinkProvider;
	private final ClassDoc fClazz;
	private final IMemento[] fDependencies;

	public HTMLWriter(final ClassDoc clazz, final LinkProvider linkProvider, final IMemento[] dependencies) {
		fClazz = clazz;
		fLinkProvider = linkProvider;
		fDependencies = dependencies;
	}

	public String createContents(final String name) {
		final StringBuffer buffer = new StringBuffer();

		addLine(buffer, "<html>");
		addLine(buffer, "<head>");
		addLine(buffer, "	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>");
		addLine(buffer, "	<link rel=\"stylesheet\" type=\"text/css\" href=\"../../org.eclipse.ease.help/help/css/modules_reference.css\" />");
		addLine(buffer, "</head>");
		addLine(buffer, "<body>");
		addText(buffer, "	<div class=\"module\" title=\"");
		addText(buffer, name);
		addLine(buffer, " Module\">");

		// header
		addText(buffer, "		<h1>");
		addText(buffer, name);
		addLine(buffer, " Module</h1>");

		// class description
		addText(buffer, "		<p>");
		final String classComment = fClazz.commentText();
		if (classComment != null)
			addText(buffer, fLinkProvider.insertLinks(fClazz, fClazz.commentText()));

		addLine(buffer, "</p>");

		// dependencies
		addLine(buffer, createDependenciesSection());

		addLine(buffer, "	</div>");

		// constants
		addLine(buffer, createConstantsSection());

		// function overview
		addLine(buffer, createOverviewSection());

		// function details
		addLine(buffer, createDetailSection());

		addLine(buffer, "</body>");
		addLine(buffer, "</html>");

		return buffer.toString();
	}

	private String createDependenciesSection() {

		if (fDependencies.length > 0) {

			final StringBuffer buffer = new StringBuffer();
			addLine(buffer, "\t<h3>Dependencies</h3>");
			addLine(buffer, "\t<p>This module depends on following other modules which will automatically be loaded.</p>");
			addLine(buffer, "\t<ul class=\"dependency\">");

			for (final IMemento dependency : fDependencies)
				addLine(buffer, "\t\t<li>{@module " + dependency.getString("module") + "}</li>");

			addLine(buffer, "\t</ul>");

			return fLinkProvider.insertLinks(fClazz, buffer.toString());
		}

		return "";
	}

	private Object createDetailSection() {
		final StringBuffer buffer = new StringBuffer();

		addLine(buffer, "\t<h2>Methods</h2>");

		final List<MethodDoc> methods = new ArrayList<MethodDoc>(Arrays.asList(fClazz.methods()));
		Collections.sort(methods, new Comparator<MethodDoc>() {

			@Override
			public int compare(final MethodDoc o1, final MethodDoc o2) {
				return o1.name().compareTo(o2.name());
			}
		});

		for (final MethodDoc method : methods) {
			if (isExported(method)) {
				// heading
				addText(buffer, "\t<div class=\"command");
				if (isDeprecated(method))
					addText(buffer, " deprecated");
				addText(buffer, "\" title=\"");
				addText(buffer, method.name());
				addLine(buffer, "\">");

				addLine(buffer, "\t\t<h3" + (isDeprecated(method) ? " class=\"deprecatedText\"" : "") + "><a id=\"" + method.name() + "\">" + method.name()
						+ "</a></h3>");

				// synopsis
				addLine(buffer, createSynopsis(method));

				// main description
				addLine(buffer, "\t\t<p class=\"description\">" + fLinkProvider.insertLinks(fClazz, method.commentText()) + "</p>");

				if (isDeprecated(method)) {
					String deprecationText = method.tags("deprecated")[0].text();
					if (deprecationText.isEmpty())
						deprecationText = "This method is deprecated and might be removed in future versions.";

					addLine(buffer, "\t\t<p class=\"warning\"><b>Deprecation warning:</b> " + fLinkProvider.insertLinks(fClazz, deprecationText) + "</p>");
				}

				// aliases
				addLine(buffer, createAliases(method));

				// parameters
				addLine(buffer, createParametersTable(method));

				// return value
				addLine(buffer, createReturnValueArea(method));

				// examples
				addLine(buffer, createExampleArea(method));

				addLine(buffer, "\t</div>");
			}
		}

		return buffer;
	}

	private StringBuffer createExampleArea(final MethodDoc method) {
		final StringBuffer buffer = new StringBuffer();

		final Tag[] tags = method.tags("scriptExample");
		for (final Tag tag : tags) {
			final String fullText = tag.text();

			// extract end position of example code
			int pos = fullText.indexOf('(');
			if (pos > 0) {
				int open = 1;
				for (int index = pos + 1; index < fullText.length(); index++) {
					if (fullText.charAt(index) == ')')
						open--;
					else if (fullText.charAt(index) == '(')
						open++;

					if (open == 0) {
						pos = index + 1;
						break;
					}
				}
			}
			final String codeText = (pos > 0) ? fullText.substring(0, pos) : fullText;
			final String description = "<br />... " + ((pos > 0) ? fullText.substring(pos) : "");

			addLine(buffer, "		<p class=\"example\"><em>Example:</em><code>" + codeText + "</code>" + description + "</p>");
		}

		return buffer;
	}

	private StringBuffer createReturnValueArea(final MethodDoc method) {
		final StringBuffer buffer = new StringBuffer();

		if (!"void".equals(method.returnType().qualifiedTypeName())) {
			addText(buffer, "		<p class=\"return\"><em>Returns:</em>");
			addText(buffer, fLinkProvider.createClassText(LinkProvider.resolveClassName(method.returnType().qualifiedTypeName(), fClazz)));

			final Tag[] tags = method.tags("return");
			if (tags.length > 0) {
				addText(buffer, " ... ");
				addText(buffer, fLinkProvider.insertLinks(fClazz, tags[0].text()));
			}

			addLine(buffer, "</p>");
		}

		return buffer;
	}

	private StringBuffer createParametersTable(final MethodDoc method) {
		final StringBuffer buffer = new StringBuffer();

		if (method.parameters().length > 0) {

			addLine(buffer, "		<table class=\"parameters\">");
			addLine(buffer, "			<tr>");
			addLine(buffer, "				<th>Parameter</th>");
			addLine(buffer, "				<th>Type</th>");
			addLine(buffer, "				<th>Description</th>");
			addLine(buffer, "			</tr>");

			for (final Parameter parameter : method.parameters()) {
				addLine(buffer, "			<tr>");
				addLine(buffer, "				<td>" + parameter.name() + "</td>");
				addLine(buffer, "				<td>" + fLinkProvider.createClassText(LinkProvider.resolveClassName(parameter.type().qualifiedTypeName(), fClazz))
						+ "</td>");
				addText(buffer, "				<td>" + fLinkProvider.insertLinks(fClazz, findComment(method, parameter.name())));

				final AnnotationDesc parameterAnnotation = getScriptParameterAnnotation(parameter);
				if (parameterAnnotation != null) {
					addText(buffer, "<br /><b>Optional:</b> defaults to &lt;<i>");
					for (final ElementValuePair pair : parameterAnnotation.elementValues()) {
						if ("org.eclipse.ease.modules.ScriptParameter.defaultValue()".equals(pair.element().toString())) {
							String defaultValue = pair.value().toString();

							if ((!String.class.getName().equals(parameter.type().qualifiedTypeName())) && (defaultValue.length() > 2))
								// remove quotes from default
								// value
								defaultValue = defaultValue.substring(1, defaultValue.length() - 1);

							if (defaultValue.contains("org.eclipse.ease.modules.ScriptParameter.null"))
								addText(buffer, "null");

							else
								addText(buffer, defaultValue);
						}
					}
					addText(buffer, "</i>&gt;.");
				}
				addLine(buffer, "</td>");
				addLine(buffer, "			</tr>");
			}
			addLine(buffer, "		</table>");
		}

		return buffer;
	}

	private StringBuffer createAliases(final MethodDoc method) {
		final StringBuffer buffer = new StringBuffer();

		final Collection<String> aliases = getFunctionAliases(method);
		if (!aliases.isEmpty()) {
			addLine(buffer, "		<p class=\"synonyms\">");

			for (final String alias : aliases)
				addText(buffer, alias + " ");

			addLine(buffer, "</p>");
		}

		return buffer;
	}

	private StringBuffer createSynopsis(final MethodDoc method) {
		final StringBuffer buffer = new StringBuffer();

		addText(buffer, "		<p class=\"synopsis\">");
		addText(buffer, fLinkProvider.createClassText(LinkProvider.resolveClassName(method.returnType().qualifiedTypeName(), fClazz)));
		addText(buffer, " ");
		addText(buffer, method.name());
		addText(buffer, "(");
		for (final Parameter parameter : method.parameters()) {
			final AnnotationDesc parameterAnnotation = getScriptParameterAnnotation(parameter);
			if (parameterAnnotation != null)
				addText(buffer, "[");

			addText(buffer, fLinkProvider.createClassText(LinkProvider.resolveClassName(parameter.type().qualifiedTypeName(), fClazz)));
			addText(buffer, " ");
			addText(buffer, parameter.name());
			if (parameterAnnotation != null)
				addText(buffer, "]");

			addText(buffer, ", ");
		}
		if (method.parameters().length > 0)
			buffer.delete(buffer.length() - 2, buffer.length());

		addText(buffer, ")");
		addLine(buffer, "</p>");

		return buffer;
	}

	private StringBuffer createOverviewSection() {
		final StringBuffer buffer = new StringBuffer();

		addLine(buffer, "	<h2>Method Overview</h2>");
		addLine(buffer, "	<table class=\"functions\">");
		addLine(buffer, "		<tr>");
		addLine(buffer, "			<th>Method</th>");
		addLine(buffer, "			<th>Description</th>");
		addLine(buffer, "		</tr>");

		final List<Overview> overview = new ArrayList<Overview>();
		for (final MethodDoc method : fClazz.methods()) {
			if (isExported(method)) {
				overview.add(new Overview(method.name(), method.name(), method.commentText(), isDeprecated(method)));
				for (final String alias : getFunctionAliases(method))
					overview.add(new Overview(alias, method.name(), "Alias for <a href=\"#" + method.name() + "\">" + method.name() + "</a>.",
							isDeprecated(method)));
			}
		}
		Collections.sort(overview);

		for (final Overview entry : overview) {
			addLine(buffer, "		<tr>");
			if (!entry.fDeprecated) {
				addLine(buffer, "			<td><a href=\"#" + entry.fLinkID + "\">" + entry.fTitle + "</a>()</td>");
				addLine(buffer, "			<td>" + fLinkProvider.insertLinks(fClazz, getFirstSentence(entry.fDescription)) + "</td>");

			} else {
				addLine(buffer, "			<td class=\"deprecatedText\"><a href=\"#" + entry.fLinkID + "\">" + entry.fTitle + "</a>()</td>");
				addLine(buffer,
						"			<td class=\"deprecatedDescription\"><b>Deprecated:</b> " + fLinkProvider.insertLinks(fClazz, getFirstSentence(entry.fDescription))
								+ "</td>");
			}
			addLine(buffer, "		</tr>");
		}

		addLine(buffer, "	</table>");
		addLine(buffer, "");

		return buffer;
	}

	private StringBuffer createConstantsSection() {
		final StringBuffer buffer = new StringBuffer();
		final List<Overview> constants = new ArrayList<Overview>();
		for (final FieldDoc field : fClazz.fields()) {
			if (isExported(field))
				constants.add(new Overview(field.name(), null, field.commentText(), isDeprecated(field), field));
		}

		if (!constants.isEmpty()) {
			Collections.sort(constants);

			addLine(buffer, "");
			addLine(buffer, "	<h2>Constants</h2>");
			addLine(buffer, "	<table class=\"constants\">");
			addLine(buffer, "		<tr>");
			addLine(buffer, "			<th>Constant</th>");
			addLine(buffer, "			<th>Description</th>");
			addLine(buffer, "		</tr>");

			for (final Overview constant : constants) {
				addLine(buffer, "\t\t<tr>");

				if (!constant.fDeprecated) {
					addLine(buffer, "			<td><a id=\"" + constant.fTitle + "\">" + constant.fTitle + "</a></td>");
					addLine(buffer, "			<td>" + fLinkProvider.insertLinks(fClazz, constant.fDescription) + "</td>");

				} else {
					addLine(buffer, "			<td><a id=\"" + constant.fTitle + "\" class=\"deprecatedText\">" + constant.fTitle + "</a></td>");
					addText(buffer, "			<td>" + fLinkProvider.insertLinks(fClazz, constant.fDescription));
					String deprecationText = constant.fField.tags("deprecated")[0].text();
					if (deprecationText.isEmpty())
						deprecationText = "This constant is deprecated and might be removed in future versions.";

					addText(buffer, "				<div class=\"warning\"><b>Deprecation warning:</b> " + fLinkProvider.insertLinks(fClazz, deprecationText) + "</div>");
					addLine(buffer, "</td>");
				}

				addLine(buffer, "		</tr>");
			}

			addLine(buffer, "	</table>");
			addLine(buffer, "");
		}

		return buffer;
	}

	private Collection<String> getFunctionAliases(final MethodDoc method) {
		final Collection<String> aliases = new HashSet<String>();
		final AnnotationDesc annotation = getWrapAnnotation(method);
		if (annotation != null) {
			for (final ElementValuePair pair : annotation.elementValues()) {
				if ("alias".equals(pair.element().name())) {
					String candidates = pair.value().toString();
					candidates = candidates.substring(1, candidates.length() - 1);
					for (final String token : candidates.split("[,;]")) {
						if (!token.trim().isEmpty())
							aliases.add(token.trim());
					}
				}
			}
		}

		return aliases;
	}

	private static String getFirstSentence(final String description) {
		final int pos = description.indexOf('.');

		return (pos > 0) ? description.substring(0, pos + 1) : description;
	}

	private static void addText(final StringBuffer buffer, final Object text) {
		buffer.append(text);
	}

	private static void addLine(final StringBuffer buffer, final Object text) {
		buffer.append(text).append(LINE_DELIMITER);
	}

	private static boolean isExported(final FieldDoc field) {
		for (final AnnotationDesc annotation : field.annotations()) {
			if (isWrapToScriptAnnotation(annotation))
				return true;
		}

		return false;
	}

	private static boolean isExported(final MethodDoc method) {
		return getWrapAnnotation(method) != null;
	}

	private static boolean isDeprecated(final MethodDoc method) {
		final Tag[] tags = method.tags("deprecated");
		return (tags != null) && (tags.length > 0);
	}

	private static boolean isDeprecated(final FieldDoc field) {
		final Tag[] tags = field.tags("deprecated");
		return (tags != null) && (tags.length > 0);
	}

	private static AnnotationDesc getWrapAnnotation(final MethodDoc method) {
		for (final AnnotationDesc annotation : method.annotations()) {
			if (isWrapToScriptAnnotation(annotation))
				return annotation;
		}

		return null;
	}

	private static AnnotationDesc getScriptParameterAnnotation(final Parameter parameter) {
		for (final AnnotationDesc annotation : parameter.annotations()) {
			if (isScriptParameterAnnotation(annotation))
				return annotation;
		}

		return null;
	}

	private static boolean isScriptParameterAnnotation(final AnnotationDesc annotation) {
		return (QUALIFIED_SCRIPT_PARAMETER.equals(annotation.annotationType().qualifiedName()))
				|| (SCRIPT_PARAMETER.equals(annotation.annotationType().qualifiedName()));
	}

	private static boolean isWrapToScriptAnnotation(final AnnotationDesc annotation) {
		return (QUALIFIED_WRAP_TO_SCRIPT.equals(annotation.annotationType().qualifiedName()))
				|| (WRAP_TO_SCRIPT.equals(annotation.annotationType().qualifiedName()));
	}

	private static String findComment(final MethodDoc method, final String name) {

		for (final ParamTag paramTags : method.paramTags()) {
			if (name.equals(paramTags.parameterName()))
				return paramTags.parameterComment();
		}

		return "";
	}

}
