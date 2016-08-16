#!/bin/bash
set -x
set -e

if [ "$#" -ne 1 ]; then
    echo "Script should be run with parent directory of org.eclipse.ease.core and org.eclipse.ease.modules as command line argument"
fi

ROOT=${1%/} # arg with trailing slash removed
P2_COMBINED=$ROOT/org.eclipse.ease.core/product/combined_repo
DOCLET=$ROOT/org.eclipse.ease.core/developers/org.eclipse.ease.helpgenerator/target/ease.module.doclet.jar

echo "Building EASE for e2 in $ROOT"

PROFILES="-P module-docs -P source"

# The target plaform for modules needs to point at the build output from the local build of core instead of the 
# EASE nightly build.

sed -i -e "s,REPLACE_WITH_PATH_TO_CORE_REPO,file://$ROOT/org.eclipse.ease.core/releng/org.eclipse.ease.releng.p2/target/repository,g" \
  $ROOT/org.eclipse.ease.modules/releng/org.eclipse.ease.modules.releng.target/org.eclipse.ease.modules.releng.target.target

# First need to build to get the ease.module.doclet.jar
cd $ROOT/org.eclipse.ease.core && mvn clean package -Dtycho.localArtifacts=ignore -f developers/org.eclipse.ease.helpgenerator/pom.xml
# then build EASE core
cd $ROOT/org.eclipse.ease.core && mvn clean package -Dtycho.localArtifacts=ignore -Ddoclet.path=$DOCLET $PROFILES
# then build EASE modules
cd $ROOT/org.eclipse.ease.modules && mvn clean package -Dtycho.localArtifacts=ignore -Ddoclet.path=$DOCLET $PROFILES


# Copy without append the first repo
mvn -f $ROOT/org.eclipse.ease.core/releng/org.eclipse.ease.releng/hudson/publish-p2-pom.xml install -Dp2.source=$ROOT/org.eclipse.ease.core/releng/org.eclipse.ease.releng.p2/target/repository -Dp2.destination=$P2_COMBINED -Dp2.keepLatestOnly=true -Dp2.append=false
for p2_input in org.eclipse.ease.core/releng/org.eclipse.ease.releng.p2.source org.eclipse.ease.modules/releng/org.eclipse.ease.modules.releng.p2.source org.eclipse.ease.modules/releng/org.eclipse.ease.modules.releng.p2
do
    # and append in all the others
    mvn -f $ROOT/org.eclipse.ease.core/releng/org.eclipse.ease.releng/hudson/publish-p2-pom.xml install -Dp2.source=$ROOT/$p2_input/target/repository -Dp2.destination=$P2_COMBINED -Dp2.keepLatestOnly=true -Dp2.append=true
done
rm -f $P2_COMBINED.zip
cd $P2_COMBINED && zip -r $P2_COMBINED.zip *

echo Build done, the output zip is: $P2_COMBINED.zip
