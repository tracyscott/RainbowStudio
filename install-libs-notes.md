# Installing external libraries

This document contains a description of how to install Maven libraries.

## Chroma (not needed)

Main GitHub URL: [neilpanchal/Chroma](https://github.com/neilpanchal/Chroma)

Files:
* Compiled library extracted from `distribution/Chroma-3/download/Chroma-3.zip`.
* Version information from  `distribution/Chroma-3/download/Chroma-3.txt`.

Maven install, after changing to the `Chroma` directory from the unzipped library:
* Main JAR: `mvn install:install-file -Dfile=library/Chroma.jar -DgroupId=com.chroma -DartifactId=chroma -Dversion=1.3.4 -Dpackaging=jar`
* Create JAR for sources: `jar cvf chroma-sources.jar -C src .`
* Sources: `mvn install:install-file -Dfile=chroma-sources.jar -Dclassifier=sources -DgroupId=com.chroma -DartifactId=chroma -Dversion=1.3.4 -Dpackaging=jar`

## GifAnimation

Main GitHub URL: [01010101/GifAnimation](https://github.com/01010101/GifAnimation)

Maven install:
* Main JAR: `mvn install:install-file -Dfile=library/GifAnimation.jar -DgroupId=gifanimation -DartifactId=gifanimation -Dversion=3.0.0 -Dpackaging=jar`
* Create JAR for sources: `jar cvf gifanimation-sources.jar -C src .`
* Sources: `mvn install:install-file -Dfile=gifanimation-sources.jar -Dclassifier=sources -DgroupId=gifanimation -DartifactId=gifanimation -Dversion=1.3.4 -Dpackaging=jar`

## LXStudio

Main GitHub URL: [heronarts/LXStudio](https://github.com/heronarts/LXStudio)

Maven install:
* Main JAR: `mvn install:install-file -Dfile=LXStudio/code/LXStudio.jar -DgroupId=heronarts.lx -DartifactId=studio -Dversion=1.0.0 -Dpackaging=jar`
* P3 JAR: `mvn install:install-file -Dfile=LXStudio/code/P3LX.jar -DgroupId=heronarts.lx -DartifactId=studio-p3 -Dversion=1.0.0 -Dpackaging=jar`

## PixelFlow

Main GitHub URL: [diwi/PixelFlow](https://github.com/diwi/PixelFlow)

Download the latest release.

Maven install:
* Main JAR: `mvn install:install-file -Dfile=library/PixelFlow.jar -DgroupId=com.thomasdiewald -DartifactId=pixelflow -Dversion=1.3.0 -Dpackaging=jar`
* Create JAR for sources: `jar cvf gifanimation-sources.jar -C src .`
* Sources: `mvn install:install-file -Dfile=pixelflow-sources.jar -Dclassifier=sources -DgroupId=com.thomasdiewald -DartifactId=pixelflow -Dversion=1.3.0 -Dpackaging=jar`

## Minim

Main GitHub URL: [ddf/Minim](https://github.com/ddf/Minim)

Download the latest release.

Maven install:
* Main JAR: `mvn install:install-file -Dfile=library/minim.jar -DgroupId=ddf.minim -DartifactId=minim-core -Dversion=2.2.2 -Dpackaging=jar`
* Create JAR for sources: `jar cvf minim-core-sources.jar -C src .`
* Sources: `mvn install:install-file -Dfile=minim-core-sources.jar -Dclassifier=sources -DgroupId=ddf.minim -DartifactId=minim-core -Dversion=2.2.2 -Dpackaging=jar`
