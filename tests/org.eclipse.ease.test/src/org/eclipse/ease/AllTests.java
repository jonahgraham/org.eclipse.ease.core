package org.eclipse.ease;

import org.eclipse.ease.tools.ResourceToolsTest;
import org.eclipse.ease.tools.RunnableWithResultTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ResourceToolsTest.class, RunnableWithResultTest.class, AbstractHeaderParserTest.class, ScriptResultTest.class, ScriptTest.class })
public class AllTests {

}
