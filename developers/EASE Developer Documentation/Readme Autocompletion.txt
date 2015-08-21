EASE AutoCompletion Readme
==========================

Table of contents:
------------------
    1. Introduction
    2. Overview
    3. Code Explanation
        3.1. Extension Points
            3.1.1. completionProcessor
            3.1.2. language#completionAnalyzer
        3.2. Interfaces and Classes
            3.2.1. CompletionProviderDispatcher
            3.2.2. ICompletionProvider
            3.2.3. AbstractCompletionProvider
            3.2.4. ICompletionContext
            3.2.5. CompletionContext
            3.2.6. ICompletionSource
            3.2.7. ICompletionAnalyzer
            3.2.8. AbstractCompletionAnalyzer
            3.2.9. JavaScriptEditorCompletionComputer
    4. Completion Flow
    5. List of Completion Providers
        5.1. LoadedModuleCompletionProvider
        5.2. ScriptShellCompletionProvider
        5.3. SourceStackCompletionProvider
        5.4. KeywordCompletionProvider
        5.5. CustomAnnotationCompletionProvider
        5.6. JavaLookupCompletionProvider
    6. Open Issues
        6.1. All code is single-threaded
        6.2. No caching mechanism
        6.3. No mechanism to detect duplicates
        6.4. No scalable recursion mechanism for e.g. included files
        
        
1. Introduction
---------------
The purpose of this document is to give an overview on the current auto-completion features of EASE.
It gives an overview of the involved classes and extension points as well as the flow of auto-completion.


2. Overview
-----------
Eclipse currently does not support a single interface for code completion.
Different text editors and the ScriptShell all have different extension points.
Therefore new extension points and interfaces have been defined to allow central calculation of proposals for EASE components.
These proposals are then wrapped to match the different interfaces.

Currently code completion is available for the ScriptShell and the JavaScript editor.
As this document will explain adding new completion providers for other editors or scripting languages should be easy.

The main class involved is the 'CompletionProviderDispatcher' (see 3.2.1.).
It dispatches calculation of an ICompletionContext(3.2.4.) with relevant information based on the input.
It then dispatches this context to calculate the actual completion proposals.
Developers that are interested in the code should use this class as their entry-point.

The benefits of the current implementation are:
    - Single generic interface for completion providers
        + Reusable for both the ScriptShell and editors.
        + (Possibly) Reusable for different scripting languages.
    - Single entry point 'CompletionProviderDispatcher' to calculate proposals
    - New completion providers can be implemented very fast
    - Completion support for new scripting languages can be added fast
    - Used for type detection in code
        + Necessary for completion calculation
        + Helpful for utility functionality like hover tool-tips
        
      
3. Code Explanation
-------------------
The following sections will be excerpts from the code documentation or short descriptions of code.
They should help explain what the involved parties are, what they are intended to do and most of all they should help understand the other parts of this document.
Note that they are meant as short introductions and that further documentation is available via the JavaDocs.
  
3.1. Extension Points
`````````````````````
This section will give an overview on the involved extension points.

3.1.1. completionProcessor
''''''''''''''''''''''''''
Plugin:         org.eclipse.ease.ui
Schema:         org.eclipse.ease.ui/schema/completionProcessor.exsd
Description:    This extension point is used to register new completion providers.
                These completion providers are used to actually calculate completion proposals.
                completionProcessors must have an attribute 'class' that maps to the corresponding 'ICompletionProvider' (see 3.2.2.).
                An optional engine ID can be given to restrict where this provider is applied. If empty the completionProcessor will be used for all languages.
                
3.1.2. language#completionAnalyzer
''''''''''''''''''''''''''''''''
Plugin:         org.eclipse.ease
Schema:         org.eclipse.ease/schema/language.exsd
Description:    For code-completion only the 'completionAnalyzer' element is used.
                The attribute 'class' maps to the corresponding class of 'ICompletionAnalyzer' type (see 3.2.7.).
                This class is used to statically analyze a given line of code.
                This includes:
                    - Extracting the relevant portion of the code
                    - Parsing the code to a more suitable 'ICompletionContext' (see 3.1.3.)
                The element also includes the attribute 'engineID' to decide for which engine to use this analyzer.
                
                
3.2. Interfaces and Classes
```````````````````````````

The following list will give short introductions to the involved interfaces classes.
It will quickly describe the most relevant methods that will hopefully help clarify the completion flow later.
Again, for further information please refer to the JavaDocs.

3.2.1. CompletionProviderDispatcher
'''''''''''''''''''''''''''''''''''
Package:
    org.eclipse.ease.ui.completion
Description:
    This is the main entry point for completion calculation.
    It handles the creation of an ICompletionContext for given input and the calculation of actual proposals.
    It implements the IContentProposalProvider interface to be directly usable with ScriptShell.
Methods of interest:
    - void setAnalyzer(ICompletionAnalyzer)
        Sets the 'ICompletionAnalyzer' (see 3.2.3) used to calculate static completion context for given input.
    - void (un)registerCompletionProvider(ICompletionProvider)
        Adds or removes a new ICompletionProvider to/from the internal list.
    - void setScriptEngine(IScriptEngine)
        Sets the script engine in use (also used to indicate ScriptShell use)
    - void addCode(String code):
        Dispatches code to addCode(String) for all registered ICompletionProviders .
    - ICompletionContext getContext(String)
        Calculates ICompletionContext for the given input.
        First uses ICompletionAnalyzer to get initial static context.
        Then uses ICompletionProvider#refineContext(ICompletionContext) to get a refined context.
    - Collection<ICompletionSource> calculateProposals(ICompletionContext)
        Calculates all proposals for the given ICompletionContext using ICompletionProvider#getProposals(ICompletionContext).
    - Collection<ICompletionProvider> getProviders(String)
        Gets all completion providers registered via extension point that match given engine description.
        See: 3.1.1.
    
3.2.2. ICompletionProvider
''''''''''''''''''''''''''
Package:
    org.eclipse.ease.ui.completion
Description:
    Interface used to refine ICompletionContext and create completion proposals.
    Each ICompletionProvider should focus on a single completion suggestion type.
    Examples: KeywordCompletionProvder, CustomAnnotationCompletionProvider
Methods of interest:
    - void addCode(String)
        Adds a given piece of code. Providers can try to extract relevant information.
    - ICompletionContext refineContext(ICompletionContext)
        Tries to refine given context. 
        This refinement is focused on updating the sourceStack and get matching ICompletionSources for each object in chain.
        Most of the times it is only necessary to correctly identify first item in chain.
    - Collection<ICompletionSource> getProposals(ICompletionContext)
        Calculates proposals for given context.
        Checks sourceStack and filter to only get real matches.

3.2.3. AbstractCompletionProvider
'''''''''''''''''''''''''''''''''
Package:
    org.eclipse.ease.ui.completion
Description:
    Base class for all completion providers.
    It is highly recommended that you subclass this for each new ICompletionProvider.

3.2.4. ICompletionContext
'''''''''''''''''''''''''
Package:
    org.eclipse.ease.completion
Description:
    Interface to store information about completion context.
    Most of the time the simple data-storage implementation CompletionContext (3.2.5.) should suffice.
Methods of interest:
    - String getInput()
        Returns the initial input the context is based on.
    - String getFilter()
        Returns the filter to be applied to all proposals.
        Examples:   foo -> foo
                    foo.bar.baz -> baz
    - List<ICompletionSource> getSourceStack()
        Returns the source stack of the context.
        The source stack is a list of all items in a call chain.
        This source stack can be used to get to the class of interest for completion calculation.
        Examples:   foo -> []
                    foo.bar.baz -> [foo, bar]

3.2.5. CompletionContext
''''''''''''''''''''''''
Package:
    org.eclipse.ease.completion
Description:
    Simple data-storage implementation of ICompletionContext.
    Offers static method to "follow" source stack and refine it.
Methods of interest:
    - static List<ICompletionSource> refineSourceStack(List<ICompletionSource)
        Should be called when first item in source stack has been refined / identified.
        Iterates over all following items and gets information based on parent class.

3.2.6. ICompletionSource
''''''''''''''''''''''''
Package:
    org.eclipse.ease.completion
Description:
    Interface to get information about single item in call chain.
    Also used to store information about completion proposal.
    Stores all relevant information to sufficiently describe a single item in the chain.
    Most of the time the simple data-storage implementation org.eclipse.ease.completion.CompletionSource should suffice.
Methods of interest:
    - SourceType getSourceType()
        Returns the source type of the item.
    - String getName()
        Returns the name of the item.
    - Class<?> getClazz()
        Returns the class the item belongs to.
    - Object getObject()
        Returns the actual object (might be null if other information is enough)
    - String getDescription()
        Gets a description of the item.
        Can be used to display e.g. help tips.
Example:
    java.lang.StringBuilder.toString()
    Source type:    JAVA_METHOD
    Name:           toString
    Class:          java.lang.StringBuilder
    Object:         Method toString
    Description:    javadoc of toString method


3.2.7. ICompletionAnalyzer
''''''''''''''''''''''''''
Package:
    org.eclipse.ease.completion
Description:
    Interface to get static information about given piece of code.
    Helps parse the input to simplify context creation for ICompletionProviders.
Methods of interest:
    - ICompletionContext getContext(String, int)
        Returns static context information for given piece of code.
        This includes removing everything that is not necessary for completion calculation.
        Distinguishing between constructors, calls and fields for items in call chain.
        Actually retrieving the matching call-chain.
        The returned context is then provided to all completion providers for refinements.
    - String getIncludedCode(String, Object)
        Returns the given code with all included code added in the position it belongs.
        Optional parent object given to have a root for relative imports

3.2.8. AbstractCompletionAnalyzer
'''''''''''''''''''''''''''''''''
Package:
    org.eclipse.ease.completion
Description:
    Abstract base class for ICompletionAnalyzers.
    All ICompletionAnalyzers should subclass this to simplify their work.
    Actually implements all the interface methods but internally calls simpler abstract methods.
Methods of interest:
    - abstract String removeUnnecessaryCode(String)
        Abstract method to remove all unnecessary code from given input.
        Since most of the actual parsing is generic only unnecessary code is language specific.
    - List<String> splitChain(String)
        Splits a given piece of code to call chain (of Strings).
        Used because different scripting languages might have different object accessor.
    - abstract ICompletionSource toCompletionSource(String)
        Parses a given piece of code to ICompletionSource.
        The given string will only contain information about single item in call chain.
        Used because different scripting languages might have different syntax.
    - abstract Patter getIncludePattern()
        Returns regular expression to query for includes.
        The pattern must have a single group with the string that needs to be included.
        Used in getIncludedCode(String, Object) to have generic base for most languages.
        
3.2.9. JavaScriptEditorCompletionComputer
'''''''''''''''''''''''''''''''''''''''''
Package:
    org.eclipse.ease.lang.javascript.ui.completion
Description:
    Completion computer for JavaScript editor.
    Internally uses only CompletionProviderDispatcher to calculate the proposals.
    Since (as mentioned) eclipse does not have a single interface for code completion wrapper classes need to be implemented for all different editors.
Methods of interest:
    - List<?> computeCompletionProposals(ContentAssistInvocationContext, IProgressMonitor)
        Sample implementation of how CompletionProviderDispatcher can be used to create proposals for different editors.
        Implements IJavaCompletionProposalComputer interface to be usable with .js files.
        

4. Completion Flow
------------------
This section is intended to explain what is actually happening when auto-completion is triggered.
The flow is explained in a generic way but differences between the ScriptShell and the JavaScript editor are still noted.

First off the CompletionProviderDispatcher needs to be instantiated and set up.
For the ScriptShell this happens in the addAutoCompletion() method whereas for the JavaScript editor this happens in the constructor the first time auto-completion is triggered.
Based on the currently selected script type/engine the corresponding ICompletionProviders are retrieved via the getProviders(String) method.
These providers are then added and (if available) the ScriptEngine is set.
Further a suiting ICompletionAnalyzer needs to be set. This is either done via the setAnalyzer(ICompletionAnalyzer) method or implicitely via setScriptEngine(IScriptEngine).
With these steps the CompletionProviderDispatcher is ready to calculate proposals.

During normal usage each line entered in the ScriptShell will be passed to all registered CompletionProviders.
With the JavaScript editor the code is only added when auto-completion is triggered.
Each completion provider needs to store the matching information for later use.
Completion providers may also choose to ignore all given code (e.g.: Keyword providers do not user input).

Once the user presses the completion keybinding (default: CTRL+SPACE) the actual completion calculation is started.

As a first step all necessary code must be gathered.
For the ScriptShell this is only the current input but for the JavaScript editor this also can also be code from included files.
Therefore the ICompletionAnalyzer#getIncludedCode(String) is used to retrieve all code including includes as a single String.

The gathered code will then be used to calculate the corresponding completion context.
First a static completion context for the given language will be calculated using ICompletionAnalyzer#getContext(String, int).
This step consists of removing all unnecessary code, splitting the remaining code into a call chain and extracting the filter.

The calculated static context will then be refined by all registered completion providers using their refineContext(ICompletionContext) method.
In almost all cases this method will only try to identify the first item in the call chain and refine the rest using CompletionContext#refineSourceStack(List<ICompletionSource).
If successful this context will contain all information about the items prior in the call-chain.

This context is then used to calculate the actual proposals using the CompletionProviderDispatcher#calculateProposals(ICompletionContext) method.
Internally all registered completion providers get the context and add matching proposals based on the filter and the call-chain.

Once the proposals are calculated they need to be parsed to the correct type. For the ScriptShell this would be an IContentProposal[] whereas for JavaScript it would be a List<?>.

The "casted" proposals are then used by all different completion mechanisms to get include the desired input.


5. List of Completion Providers
-------------------------------
The following list will explain what completion providers are currently available and what they are used for.

5.1. LoadedModuleCompletionProvider
```````````````````````````````````
Interface:  ScriptShell, Editors
Languages:  All
Description:
    Detects when a module has been loaded.
    Offers information about methods and fields of said module.

5.2. ScriptShellCompletionProvider
``````````````````````````````````
Interface:  ScriptShell
Language:   All
Description:
    Simple completion provider to query given script engine if available.
    Can be used for local variables, etc.

5.3. SourceStackCompletionProvider
``````````````````````````````````
Interface:  ScriptShell, Editors
Languages:  All
Description:
    Used for non-empty call chain.
    Calculates all matches based on class of last element and filter.
    Needs a refined context to work.

5.4. KeywordCompletionProvider
``````````````````````````````
Interface:  ScriptShell, Editors
Languages:  All (needs different keywords for each langauge)
Description:
    Simply provides a list of keywords for selected language.

5.5. CustomAnnotationCompletionProvider
```````````````````````````````````````
Interface:  Editors
Languages:  All
Description:
    Parses code for EASE custom type annotation.
    Annotation constists of identifier, variable name and variable type.
    For this to work annotation must be put in commend (otherwise language would throw syntax error)
    Syntax: @type varName varClass
    Example: @type foo java.lang.String
    
5.6. JavaLookupCompletionProvider
`````````````````````````````````
Interface:  ScriptShell, Editors
Languages:  All
Description:
    Used to try to evaluate constructor based on java class name.
    Based on this information call chain can be evaluated easily.
    Relies on Class.forName(String) Java method to work.


6. Open Issues
--------------
The following list will give an overview what is currently not supported or might need refactoring.

6.1. All code is single-threaded
````````````````````````````````
Calculations might take too long and UI responsiveness could decrease.
All registered completion providers are currently called sequentially, would be faster if called in parallel.
At the moment not a problem but might be once more providers are registered.

6.2. No caching mechanism
`````````````````````````
Code might be unchanged but will still be parsed.
This is especially bad for editors because lots of unnecessary code might be parsed for each suggestion.

6.3. No mechanism to detect duplicates
``````````````````````````````````````
The same suggestion might be proposed several times (e.g. from loaded module and from available module)

6.4. No scalable recursion mechanism for e.g. included files
````````````````````````````````````````````````````````````
Each language can implement ICompletionAnalyzer#getIncludedCode(String) but it is currently not possible to dynamically add completion providers that also add code during proposal calculation.
