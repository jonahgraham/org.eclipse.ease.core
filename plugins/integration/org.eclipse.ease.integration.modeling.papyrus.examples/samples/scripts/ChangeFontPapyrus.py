# Menu: Examples > Modeling > Papyrus > Change Class font to Tahoma (Papyrus)
# License: EDL 1.0
# VisibleWhen:[And {
#   With activeEditor {
#        Equal "org.eclipse.papyrus.infra.core.papyrusEditor"
#    },
#    With selection {
#        Iterable {
#            AdaptTo "org.eclipse.gmf.runtime.notation.View"{
#                InstanceOf "org.eclipse.gmf.runtime.notation.Node"
#            } 
#        }
#    } 
#}]
# Description: {Demonstrate how to modify the Notation model from a GMF Editor using NotationModule. In this script will we change the font of a selected node (From GMF editor) to Tahoma.}
#

from java.lang import Runnable
from org.eclipse.gmf.runtime.diagram.core.util import ViewUtil
from org.eclipse.gmf.runtime.notation import NotationPackage
from org.eclipse.swt.graphics import FontData
from org.eclipse.jface.resource import StringConverter
from org.eclipse.swt.SWT import NORMAL

from org.eclipse.uml2.uml import Class


def main():
    selection = getSelectionView()
    if selection == None:
        print "[ERROR] Please select a graphical node."
        return
    GMFresource = selection.eResource()
    print GMFresource
            
    class MyRunnable(Runnable) :
        def run(self):
            font = FontData("Tahoma", 8, NORMAL) 
            for elt in GMFresource.getAllContents():
                if eInstanceOf(elt, "Shape"):
                    object = elt.getElement()
                    print Class
                    if eInstanceOf(object, "Class"):
                        print "[INFO] Update Fonts"
                        print "Currently in "+str(elt)
                        fontNameFeature = NotationPackage.eINSTANCE.getFontStyle_FontName()
                        fontAsString = StringConverter.asString(font)
                        ViewUtil.setStructuralFeatureValue(elt,fontNameFeature ,fontAsString)
    
    op = MyRunnable()
    runOperation(op, "Change Class font to Tahoma")
    save()
    print "[INFO] file " + GMFresource.getURI().toString() + " has been saved."


loadModule("PapyrusModule")
main()
