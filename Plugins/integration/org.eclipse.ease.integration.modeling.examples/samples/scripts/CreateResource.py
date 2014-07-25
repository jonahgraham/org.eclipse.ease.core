#
# Menu: Examples > Modeling > Create resources
# Kudos: Arthur Daussy
# Description: { Demonstrate how to create and fill a resource.}
# License: EDL 1.0
#

import  org.eclipse.uml2.uml.Class as Clazz
from java.lang import Runnable

class MyRunnable(Runnable) :
    
    def fillModel(self,resource):
        model = createModel();
        resource.getContents().add(model)
        for i in range(1,50):
            newClass = createClass();
            newClass.setName("class_"+str(i));
            model.getPackagedElements().add(newClass)
        print "Saing the resource..."
        save(resource)
    
    def run(self):
        print "Creating resource dynamically"
        newResource = createResource()
        print "Filling the resource...."
        self.fillModel(newResource)
        print "Creating resource semi dynamically"
        newResource = createResource(name="semeAutomatically.uml")
        self.fillModel(newResource)
        print "Filling the resource...."
                
        
uml = loadModule("EcoreModule")
initEPackage("http://www.eclipse.org/uml2/4.0.0/UML")
print str(uml.getFactory())
# Get the selected EObject
op = MyRunnable()
runOperation(op,"Create resources")


