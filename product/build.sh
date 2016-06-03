#!/bin/bash
set -x

if [ "$#" -ne 1 ]; then
    echo "Script should be run with parent directory of org.eclipse.ease.core and org.eclipse.ease.modules as command line argument"
fi

ROOT=${1%/} # arg with trailing slash removed
P2_COMBINED=$ROOT/org.eclipse.ease.core/product/combined_repo

echo "Building EASE Demo Product in $ROOT"

echo "Sanity check that target files are pointing at local repos"
# XXX: This could be done by updating tpd file and regenerating target file
# Make sure Modules target platform refers to the correct root by grepping for the repo listed in the target file
if grep -Fq "$ROOT/org.eclipse.ease.core/releng/org.eclipse.ease.releng.p2/target/repository" $ROOT/org.eclipse.ease.modules/releng/org.eclipse.ease.modules.releng.target/org.eclipse.ease.modules.releng.target.target 
then
    echo "modules is using local p2 core repo"
else
    echo "modules is NOT using local p2 core repo"
    exit 1
fi

# Make sure Product target platform refers to the correct root by grepping for the repo listed in the target file
if grep -Fq "$ROOT/org.eclipse.ease.core/product/combined_repo" $ROOT/org.eclipse.ease.core/product/org.eclipse.ease.releng.product.target/org.eclipse.ease.releng.product.target.target
then
    echo "product is using local p2 combined repo"
else
    echo "product is NOT using local p2 combined repo"
#    exit 1
fi

PROFILES="-P api-docs -P module-docs -P source"
PROFILES="-P module-docs -P source"

cd $ROOT/org.eclipse.ease.core && mvn clean package -Dtycho.localArtifacts=ignore $PROFILES
cd $ROOT/org.eclipse.ease.modules && mvn clean package -Dtycho.localArtifacts=ignore -Ddoclet.path=$ROOT/org.eclipse.ease.core/developers/org.eclipse.ease.helpgenerator/bin $PROFILES


# Copy without append the first repo
mvn -f $ROOT/org.eclipse.ease.core/releng/org.eclipse.ease.releng/hudson/publish-p2-pom.xml install -Dp2.source=$ROOT/org.eclipse.ease.core/releng/org.eclipse.ease.releng.p2/target/repository -Dp2.destination=$P2_COMBINED -Dp2.keepLatestOnly=true -Dp2.append=false
for p2_input in org.eclipse.ease.core/releng/org.eclipse.ease.releng.p2.source org.eclipse.ease.modules/releng/org.eclipse.ease.modules.releng.p2.source org.eclipse.ease.modules/releng/org.eclipse.ease.modules.releng.p2
do
    # and append in all the others
    mvn -f $ROOT/org.eclipse.ease.core/releng/org.eclipse.ease.releng/hudson/publish-p2-pom.xml install -Dp2.source=$ROOT/$p2_input/target/repository -Dp2.destination=$P2_COMBINED -Dp2.keepLatestOnly=true -Dp2.append=true
done
rm -f $P2_COMBINED.zip
cd $P2_COMBINED && zip -r $P2_COMBINED.zip $P2_COMBINED

cd $ROOT/org.eclipse.ease.core/product && mvn clean package -Dtycho.localArtifacts=ignore
