EASE Demo Product for EclipseCon France 2016
============================================

This repository contains Eclipse EASE as used for EclipseCon France 2016. To ease use of EASE we packaged it up as an Eclipse product. The sessions ECF sessions are:

 - [EASE-ily Make the Most of Eclipse with Python](https://www.eclipsecon.org/france2016/session/ease-ily-make-most-eclipse-python)
 - [Elevate your IDE with scripts](https://www.eclipsecon.org/france2016/session/elevate-your-ide-scripts)

Details on using the product can be found in the [EASE Python Examples](https://github.com/jonahkichwacoders/EASE-Python-Examples) repo. To build the product:

 - clone [org.eclipse.ease.core](https://github.com/jonahkichwacoders/org.eclipse.ease.core) and [org.eclipse.ease.modules](https://github.com/jonahkichwacoders/org.eclipse.ease.modules) with a common parent directory.
 - checkout the ECF2016 branch on both repos
 - run `org.eclipse.ease.core/product/build.sh <parent>` where `<parent>` is the common parent directory name.
 - a P2 repo containing all of EASE core and modules will be in `org.eclipse.ease.core/product/combined_repo.zip`
 - zips/tars for all the platforms will be in `org.eclipse.ease.core/product/org.eclipse.ease.releng.product.repository/target/products`

