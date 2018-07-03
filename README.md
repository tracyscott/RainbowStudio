Rainbow Studio
==
Based on LX Studio, Prototype for [Rainbow Bridge](http://giantrainbow.com/)
==

**BY DOWNLOADING OR USING THE LX STUDIO SOFTWARE OR ANY PART THEREOF, YOU AGREE TO THE TERMS AND CONDITIONS OF THE [LX STUDIO SOFTWARE LICENSE AND DISTRIBUTION AGREEMENT](http://lx.studio/license).**

Please note that LX Studio is not open-source software. The license grants permission to use this software freely in non-commercial applications. Commercial use is subject to a total annual revenue limit of $25K on any and all projects associated with the software. If this licensing is obstructive to your needs or you are unclear as to whether your desired use case is compliant, contact me to discuss proprietary licensing: mark@heronarts.com

---

![Rainbow Studio](https://raw.github.com/tracyscott/RainbowStudio/master/assets/rainbowstudio.jpg)

[LX Studio](http://lx.studio/) is a digital lighting workstation, bringing concepts from digital audio workstations and modular synthesis into the realm of LED lighting control. Generative patterns, interactive inputs, and flexible parameter-driven modulation — a rich environment for lighting composition and performance.

### Getting Started ###

To set up RainbowStudio, perform the following steps:

- [Download and install Processing](https://processing.org/download/)
    - Rainbow Studio runs on top of Processing
- Open Processing
- Open its "Preference" and find out the "Sketchbook location"
    - Example: `~/sketchbook`
- [Download and install GifAnimation](https://github.com/01010101/GifAnimation) to `libraries/` in Sketchbook location
    - Example: `cd ~/sketchbook/libraries && git clone https://github.com/01010101/GifAnimation.git`
- [Download and install PixelFlow](https://github.com/diwi/PixelFlow/releases) to `libraries/` in Sketchbook location
- Clone this project and open the `RainbowStudio.pde` project in Processing:
    - Example:
    - ```bash
      # Clone our repo
      cd ~/sketchbook
      git clone https://github.com/tracyscott/RainbowStudio.git

      # Open RainbowStudio in Processing (`xdg-open` is a Linux-specific command)
      xdg-open RainbowStudio/RainbowStudio.pde
      # For OS X: open RainbowStudio/RainbowStudio.pde
      # A GUI double click should work as well
      ```
- Press "Play" button to launch RainbowStudio
- RainbowStudio should launch with a side-scrolling white dot on a black grid
- Click "Folder" icon in top-center and select a pattern (e.g. "RainbowGifLife.lxp")
- RainbowStudio may show a blank screen. If it does, close it and reopen it
- RainbowStudio should now show the desired pattern

### Documentation ###

[LX Studio User Guide](https://github.com/tracyscott/RainbowStudio/blob/master/LXStudioUserGuide.md)

There is some minimal documentation on the [LX Studio Wiki](https://github.com/heronarts/LXStudio/wiki)

The RainbowGIF pattern uses an 'out-b' animated gif from [beeple](https://vimeo.com/129881600)

For Processing based drawing check out the PGDraw2, AnimatedSprite, or AnimatedSpritePP patterns.

Pattern names ending in 'PP' are pixel-perfect patterns.  They render to a straight 420x30 image which is effectively bent along the rainbow.  For pixel-art type patterns, these work best.  It is also possible to sample from an image but these will have aliasing effects because the Rainbow LEDs are in polar coordinate space while images use cartesian coordinates.  Most image based patterns have an antialias button to enable and disable antialiasing.  Aliasing is especially bad on high frequency images with movement (lots of animated, overlapping geometric shapes).  For sampled-image based patterns, the image is scaled to match the dimensions of the entire top half-circle of the circle defined by the Rainbow.  This makes it easier to directly use the Radius in your image drawing code (see AnimatedSprite).  It is also useful for using existing renderings as a texture, such as the RainbowGIF pattern.

Consult the [LX Studio API reference &rarr;](http://lx.studio/api/)

More and better documentation is coming soon!

### Troubleshooting ###

If you encounter an issue like the following on Linux:

```
com.jogamp.opengl.GLException: Caught ThreadDeath: null on thread main-FPSAWTAnimator#00-Timer0
    at com.jogamp.opengl.GLException.newGLException(GLException.java:76)
```

Then we recommend wiping your `.lxproject` file, selecting a `.lxp` file, and rebooting RainbowStudio twice. It was causing us issues, potentially due to odd-sized windows

### LX Studio Contact and Collaboration ###

Building a big cool project? I'm probably interested in hearing about it! Want to solicit some help, request new framework features, or just ask a random question? Open an issue on the project or drop me a line: mark@heronarts.com

---

HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR PURPOSE, WITH RESPECT TO THE SOFTWARE.
