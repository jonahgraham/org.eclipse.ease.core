package org.eclipse.ease.lang.javascript.nashorn;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;

public class NashornScriptEngine extends AbstractScriptEngine implements IScriptEngine {

	private ScriptEngine fEngine;

	public NashornScriptEngine() {
		super("Nashorn");
	}

	@Override
	public void terminateCurrent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVariable(final String name, final Object content) {
		fEngine.put(name, content);
	}

	@Override
	public Object getVariable(final String name) {
		return fEngine.get(name);
	}

	@Override
	public boolean hasVariable(final String name) {
		return fEngine.getBindings(ScriptContext.ENGINE_SCOPE).containsKey(name);
	}

	@Override
	public String getSaveVariableName(final String name) {
		return getSaveName(name);
	}

	@Override
	public Object removeVariable(final String name) {
		return fEngine.getBindings(ScriptContext.ENGINE_SCOPE).remove(name);
	}

	@Override
	public Map<String, Object> getVariables() {
		Map<String, Object> variables = new HashMap<String, Object>();
		Bindings bindings = fEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		for (Entry<String, Object> entry : bindings.entrySet())
			variables.put(entry.getKey(), entry.getValue());

		return variables;
	}

	@Override
	public void registerJar(final URL url) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean setupEngine() {
		ScriptEngineManager engineManager = new ScriptEngineManager();
		fEngine = engineManager.getEngineByName("nashorn");

		return fEngine != null;
	}

	@Override
	protected boolean teardownEngine() {
		return true;
	}

	@Override
	protected Object execute(final Script script, final Object reference, final String fileName, final boolean uiThread) throws Exception {
		return fEngine.eval(script.getCode());
	}

	private static String getSaveName(final String identifier) {
		// check if name is already valid
		if (isSaveName(identifier))
			return identifier;

		// not valid, convert string to valid format
		final StringBuilder buffer = new StringBuilder(identifier.replaceAll("[^a-zA-Z0-9]", "_"));

		// remove '_' at the beginning
		while ((buffer.length() > 0) && (buffer.charAt(0) == '_'))
			buffer.deleteCharAt(0);

		// remove trailing '_'
		while ((buffer.length() > 0) && (buffer.charAt(buffer.length() - 1) == '_'))
			buffer.deleteCharAt(buffer.length() - 1);

		// check for valid first character
		if (buffer.length() > 0) {
			final char start = buffer.charAt(0);
			if ((start < 65) || ((start > 90) && (start < 97)) || (start > 122))
				buffer.insert(0, '_');
		} else
			// buffer is empty
			buffer.insert(0, '_');

		return buffer.toString();
	}

	private static boolean isSaveName(final String identifier) {
		return Pattern.matches("[a-zA-Z_$][a-zA-Z0-9_$]*", identifier);
	}
}
