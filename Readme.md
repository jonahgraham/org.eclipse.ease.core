Build Instructions
=================

 - cd /scratch/ease/org.eclipse.ease.core
 - mvn clean package -Dtycho.localArtifacts=ignore -P api-docs -P module-docs -P source 
 - cd /scratch/ease/org.eclipse.ease.modules
 - mvn clean package -Dtycho.localArtifacts=ignore -Ddoclet.path=/scratch/ease/org.eclipse.ease.core/developers/org.eclipse.ease.helpgenerator/bin -P api-docs -P module-docs -P source
 - cd /scratch/ease/org.eclipse.ease.core/releng/org.eclipse.ease.releng.product
 - mvn clean package -Dtycho.localArtifacts=ignore 

