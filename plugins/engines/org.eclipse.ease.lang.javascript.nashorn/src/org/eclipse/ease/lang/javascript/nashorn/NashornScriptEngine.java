package org.eclipse.ease.lang.javascript.nashorn;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.lang.javascript.JavaScriptHelper;

public class NashornScriptEngine extends AbstractScriptEngine implements IScriptEngine {

	private ScriptEngine fEngine;

	private final Map<String, Object> fBufferedVariables = new HashMap<String, Object>();

	public NashornScriptEngine() {
		super("Nashorn");
	}

	@Override
	public void terminateCurrent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVariable(final String name, final Object content) {
		if (!JavaScriptHelper.isSaveName(name))
			throw new RuntimeException("\"" + name + "\" is not a valid JavaScript variable name");

		if (fEngine != null)
			fEngine.put(name, content);

		else
			fBufferedVariables.put(name, content);
	}

	@Override
	public Object getVariable(final String name) {
		if (fEngine != null)
			return fEngine.get(name);

		throw new RuntimeException("Cannot retrieve variable, engine not initialized");
	}

	@Override
	public boolean hasVariable(final String name) {
		if (fEngine != null)
			return fEngine.getBindings(ScriptContext.ENGINE_SCOPE).containsKey(name);

		throw new RuntimeException("Cannot query variable, engine not initialized");
	}

	@Override
	public String getSaveVariableName(final String name) {
		return JavaScriptHelper.getSaveName(name);
	}

	@Override
	public Object removeVariable(final String name) {
		if (fEngine != null)
			return fEngine.getBindings(ScriptContext.ENGINE_SCOPE).remove(name);

		throw new RuntimeException("Cannot remove variable, engine not initialized");
	}

	@Override
	public Map<String, Object> getVariables() {
		if (fEngine != null) {
			Map<String, Object> variables = new HashMap<String, Object>();
			Bindings bindings = fEngine.getBindings(ScriptContext.ENGINE_SCOPE);
			for (Entry<String, Object> entry : bindings.entrySet())
				variables.put(entry.getKey(), entry.getValue());

			return variables;
		}

		throw new RuntimeException("Cannot retrieve variables, engine not initialized");
	}

	@Override
	public void registerJar(final URL url) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean setupEngine() {
		ScriptEngineManager engineManager = new ScriptEngineManager();
		fEngine = engineManager.getEngineByName("nashorn");

		if (fEngine != null) {
			// engine is initialized, set buffered variables
			for (final Entry<String, Object> entry : fBufferedVariables.entrySet())
				setVariable(entry.getKey(), entry.getValue());

			fBufferedVariables.clear();
		}

		return fEngine != null;
	}

	@Override
	protected boolean teardownEngine() {
		fEngine = null;

		return true;
	}

	@Override
	protected Object execute(final Script script, final Object reference, final String fileName, final boolean uiThread) throws Exception {
		return fEngine.eval(script.getCode());
	}
}
