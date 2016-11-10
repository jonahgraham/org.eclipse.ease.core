#################################################################################
# Copyright (c) 2016 Kichwa Coders and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Jonah Graham (Kichwa Coders) - initial API and implementation
#################################################################################

import code
import os
import py4j
from py4j.clientserver import ClientServer, JavaParameters, PythonParameters
from py4j.java_collections import MapConverter
from py4j.java_gateway import JavaObject
from py4j.protocol import Py4JJavaError, get_command_part
import sys
import threading

# To ease some debugging of the py4j engine itself it is useful to turn logging on,
# uncomment the following lines for one way to do that
# import logging
# logger = logging.getLogger("py4j")
# logger.setLevel(logging.DEBUG)
# logger.addHandler(logging.StreamHandler())

class EaseInteractiveConsole(code.InteractiveConsole):
    '''
    Extension to standard InteractiveConsole so we can handle and capture
    error output better
    '''
    def __init__(self, engine, *args, **kwargs):
        code.InteractiveConsole.__init__(self, *args, **kwargs)
        self.engine = engine
    def write(self, data):
        # Python 3 write the whole error in one write, Python 2 does multiple writes
        if self.engine.except_data is None:
            self.engine.except_data = [data]
        else:
            self.engine.except_data.append(data)
    def runcode(self, code):
        try:
            exec(code, self.locals)
        except SystemExit:
            raise
        except Py4JJavaError as e:
            if isinstance(e.java_exception, JavaObject):
                # Skip self.showtraceback/self.write as we can get
                # a Java exception instance already
                self.engine.except_data = e.java_exception
            else:
                # No java exception here, fallback to normal case
                self.showtraceback()
        except:
            # create information that will end up in a
            # ScriptExecutionException
            self.showtraceback()

class InteractiveReturn(object):
    '''
    Instance of Java's IInteractiveReturn.
    This class encapsulates the return state from the
    ScriptEngineExecute.executeInteractive() method
    '''
    def __init__(self, gateway_client, display_data=None, except_data=None):
        self.gateway_client = gateway_client
        self.display_data = display_data
        self.except_data = except_data
    def getException(self):
        data = self.except_data
        if data is None:
            return None
        if isinstance(data, JavaObject):
            return data;
        return "".join(data)
    def getResult(self):
        data = self.display_data

        try:
            # test if py4j understands this type
            get_command_part(data, dict())
            # py4j knows how to convert this to Java,
            # just return it as is
            return data
        except:
            pass

        # try registered converters
        for converter in self.gateway_client.converters:
            if converter.can_convert(data):
                return converter.convert(data, self.gateway_client)

        # data cannot be represented in Java, return a string representation
        # instead
        return repr(data)

    class Java:
        implements = ['org.eclipse.ease.lang.python.py4j.internal.IInteractiveReturn']

class ScriptEngineExecute(object):
    '''
    Instance of Java's IPythonSideEngine.
    This class is the main class of the Python side.
    '''
    def __init__(self):
        self.shutdown_event = threading.Event()

    def set_gateway(self, gateway):
        self.gateway = gateway
        self.locals = dict()
        self.interp = EaseInteractiveConsole(self, self.locals)
        # Provide most common top level pacakage names in the namespace
        # so that code like "java.lang.String()" can work.
        # for other names, jvm.<package name> should be used
        self.locals['java'] = gateway.jvm.java
        self.locals['javax'] = gateway.jvm.javax
        self.locals['org'] = gateway.jvm.org
        self.locals['com'] = gateway.jvm.com
        self.locals['net'] = gateway.jvm.net
        self.locals['jvm'] = gateway.jvm
        self.locals['gateway'] = gateway
        self.locals['py4j'] = py4j
        sys.displayhook = self.displayhook
        self.display_data = None
        self.except_data = None

    def displayhook(self, data):
        self.display_data = data
        if data is not None:
            self.locals['_'] = data

    def executeCommon(self, code_text, code_exec):
        self.display_data = None
        self.except_data = None
        needMore = code_exec(code_text)
        if needMore:
            # TODO, need to handle this with prompts, this message
            # is a workaround
            return InteractiveReturn(self.gateway._gateway_client, display_data="... - more input required to complete statement")
        else:
            display_data = self.display_data
            except_data = self.except_data
            self.display_data = None
            self.except_data = None
            return InteractiveReturn(self.gateway._gateway_client, display_data=display_data, except_data=except_data)

    def executeScript(self, code_text, filename=None):
        # TODO: Handle filename
        return self.executeCommon(code_text, self.interp.runcode)

    def executeInteractive(self, code_text):
        return self.executeCommon(code_text, self.interp.push)

    def internalGetVariable(self, name):
        return repr(self.locals.get(name))

    def internalGetVariables(self):
        locals_repr = dict()
        for k, v in self.locals.items():
            if not k.startswith("__"):
                locals_repr[k] = repr(v)
        converted = MapConverter().convert(locals_repr, self.gateway._gateway_client)
        return converted

    def internalHasVariable(self, name):
        return name in self.locals

    def internalSetVariable(self, name, content):
        self.locals[name] = content

    def teardownEngine(self):
        self.shutdown_event.set()

    def wait_on_shutdown(self):
        self.shutdown_event.wait()

    class Java:
        implements = ['org.eclipse.ease.lang.python.py4j.internal.IPythonSideEngine']


def watchdog(engine):
    # Read from the parent process until EOF, EOF indicates the
    # parent process has terminated
    try:
        sys.stdin.read()
    except:
        pass

    # shutdown the engine
    engine.teardownEngine()

    # Allow some time for the shutdown to be clean, but
    # fallback to a hard exit if that fails
    timer = threading.Timer(10.0, os._exit, (1,))
    timer.setDaemon(True)
    timer.start()

def main(argv):
    port = int(argv[1])
    engine = ScriptEngineExecute()
    gateway = ClientServer(java_parameters=JavaParameters(auto_convert=True, port=port),
                          python_parameters=PythonParameters(port=0),
                          python_server_entry_point=engine)
    # retrieve the port on which the python callback server was bound to.
    python_port = gateway.get_callback_server().get_listening_port()
    engine.set_gateway(gateway)

    # tell Java that we are up and running and where to direct
    # calls to python to.
    gateway.entry_point.pythonStartupComplete(python_port, engine)

    # start a watchdog on stdin to make sure we terminate
    thread = threading.Thread(target=watchdog, args=(engine,))
    thread.setDaemon(True)
    thread.start()

    # now wait until we have a request for shutdown
    engine.wait_on_shutdown()

    gateway.shutdown_callback_server()
    gateway.shutdown()

if __name__ == '__main__':
    main(sys.argv)
