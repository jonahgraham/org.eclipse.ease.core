package org.eclipse.ease.lang.scriptarchive;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.ease.ICodeParser;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.sign.ScriptSignatureException;
import org.eclipse.ease.sign.SignatureInfo;
import org.eclipse.ease.tools.StringTools;

public class ManifestParser implements ICodeParser {

	@Override
	public String getHeaderComment(InputStream stream) {
		try {
			final InputStream archiveStream = ArchiveEngine.getArchiveStream(stream, "/META-INF/MANIFEST.MF");
			if (archiveStream != null)
				return StringTools.toString(archiveStream);
		} catch (final IOException e) {
			Logger.error(PluginConstants.PLUGIN_ID, "Could not read manifest", e);
		}

		return null;
	}

	@Override
	public boolean isAcceptedBeforeHeader(String line) {
		return false;
	}

	@Override
	public ICompletionContext getContext(IScriptEngine scriptEngine, Object resource, String contents, int position, int selectionRange) {
		return null;
	}

	@Override
	public SignatureInfo getSignatureInfo(InputStream stream) throws ScriptSignatureException {
		return null;
	}
}
