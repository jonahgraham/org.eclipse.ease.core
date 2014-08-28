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

	public NashornScriptEngine() {
		super("Nashorn");
	}

	@Override
	public void terminateCurrent() {
		// TODO Auto-generated method stub
	}

	@Override
	protected Object internalGetVariable(final String name) {
		return fEngine.get(name);
	}

	@Override
	protected Map<String, Object> internalGetVariables() {
		Map<String, Object> variables = new HashMap<String, Object>();
		Bindings bindings = fEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		for (Entry<String, Object> entry : bindings.entrySet())
			variables.put(entry.getKey(), entry.getValue());

		return variables;
	}

	@Override
	protected boolean internalHasVariable(final String name) {
		return fEngine.getBindings(ScriptContext.ENGINE_SCOPE).containsKey(name);
	}

	@Override
	protected void internalSetVariable(final String name, final Object content) {
		if (!JavaScriptHelper.isSaveName(name))
			throw new RuntimeException("\"" + name + "\" is not a valid JavaScript variable name");

		fEngine.put(name, content);
	}

	@Override
	protected Object internalRemoveVariable(final String name) {
		return fEngine.getBindings(ScriptContext.ENGINE_SCOPE).remove(name);
	}

	@Override
	public String getSaveVariableName(final String name) {
		return JavaScriptHelper.getSaveName(name);
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
		fEngine = null;

		return true;
	}

	@Override
	protected Object execute(final Script script, final Object reference, final String fileName, final boolean uiThread) throws Exception {
		return fEngine.eval(script.getCode());
	}
}
