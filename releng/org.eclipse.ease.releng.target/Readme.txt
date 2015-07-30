tpd file handling
=================

	For target platforms use following eclipse plugin:
		https://github.com/mbarbero/fr.obeo.releng.targetplatform

	It resolves and sets *.tpd files.


Tycho naming
============
	
	The file named 'org.eclipse.ease.releng.target.target' will be used for tycho builds, so make sure to manually override this file after creating a new target file.


Available targets
=================

	* Indigo.tpd - contains requirements for builds based on Eclipse indigo. Used to check dependencies before a release
	
	* Mars.tpd - target platform used to build org.eclipse.ease.core repository
	
	* Developers.tpd - target platform to be set by developers. Contains additional dependencies for modules, pydev, ...