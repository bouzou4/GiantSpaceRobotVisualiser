package com.bouzou.packages.giantspacerobotvisualizer;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * A class to store a set of shaders used as "post processing" effects they may
 * have different parameters, these can be set using two faders on the Maschine
 * Jam
 */
class PostProcessingShaders {
  PApplet applet;
  ArrayList<VisShader> VisShaders;
  VisShader currentVisShader;
  boolean VisShadersOn;
  int x;
  int y;

  /**
   * Create a new PostProcessingShaders object
   * 
   * @param applet the PApplet
   */
  PostProcessingShaders(PApplet applet) {
    this.applet = applet;
    VisShaders = new ArrayList<VisShader>();
    VisShaders.add(new ShaderBrcosa(applet));
    VisShaders.add(new ShaderHue(applet));
    VisShaders.add(new ShaderPixelate(applet));
    VisShaders.add(new ShaderChannels(applet));
    VisShaders.add(new ShaderThreshold(applet));
    VisShaders.add(new ShaderNeon(applet));
    VisShaders.add(new ShaderDeform(applet));
    VisShaders.add(new ShaderPixelRolls(applet));
    VisShaders.add(new ShaderModcolor(applet));
    VisShaders.add(new ShaderHalftone(applet));
    VisShaders.add(new ShaderInvert(applet));

    currentVisShader = VisShaders.get(0);
    x = this.applet.width / 2;
    y = this.applet.height / 2;
    VisShadersOn = false;
  }

  void draw() {
    if (VisShadersOn) {
      currentVisShader.draw();
    }
  }

  void setShader(int value) {
    if (value >= VisShaders.size()) {
      value = 0;
    }
    currentVisShader = VisShaders.get(value);
  }

  String getCurrentShaderInfo() {
    return currentVisShader.name + " X = " + x + " Y = " + y;
  }

  void setX(int value) {
    x = PApplet.round(PApplet.map(value, 0, 127, 0, applet.width));
    currentVisShader.setX(x);
  }

  void setY(int value) {
    y = PApplet.round(PApplet.map(value, 0, 127, 0, applet.height));
    currentVisShader.setY(y);
  }

  void toggleVisShaders() {
    VisShadersOn = !VisShadersOn;
  }

  class ShaderBrcosa extends VisShader {
    ShaderBrcosa(PApplet applet) {

      super("brcosa", "brcosa.glsl", applet);
      x = applet.width / 3;
      y = 10;// height/3;
      shade.set("brightness", (float) 1.0);
    }

    void draw() {
      shade.set("contrast", PApplet.map(x, 0, applet.width, -5, 5));
      shade.set("saturation", PApplet.map(y, 0, applet.height, -5, 5));
      super.draw();
    }
  }

  class ShaderHue extends VisShader {
    ShaderHue(PApplet applet) {
      super("hue", "hue.glsl", applet);
    }

    void draw() {
      shade.set("hue", PApplet.map(x, 0, applet.width, 0, PConstants.TWO_PI));
      super.draw();
    }
  }

  class ShaderPixelate extends VisShader {
    ShaderPixelate(PApplet applet) {
      super("pixelate", "pixelate.glsl", applet);
    }

    void draw() {
      shade.set("pixels", (float) 0.1 * x, (float) 0.1 * x);
      super.draw();
    }
  }

  class ShaderChannels extends VisShader {
    ShaderChannels(PApplet applet) {
      super("channels", "channels.glsl", applet);
    }

    void draw() {
      shade.set("rbias", (float) 0.0, (float) 0.0);
      shade.set("gbias", PApplet.map((float) y, (float) 0, (float) applet.height, (float) -0.2, (float) 0.2),
          (float) 0.0);
      shade.set("bbias", (float) 0.0, (float) 0.0);
      shade.set("rmult", PApplet.map((float) x, (float) 0, (float) applet.width, (float) 0.8, (float) 1.5),
          (float) 1.0);
      shade.set("gmult", (float) 1.0, (float) 1.0);
      shade.set("bmult", (float) 1.0, (float) 1.0);
      super.draw();
    }
  }

  class ShaderThreshold extends VisShader {
    ShaderThreshold(PApplet applet) {
      super("threshold", "threshold.glsl", applet);
    }

    void draw() {
      shade.set("threshold", PApplet.map(x, 0, applet.width, 0, 1));
      super.draw();
    }
  }

  class ShaderNeon extends VisShader {
    ShaderNeon(PApplet applet) {
      super("neon", "neon.glsl", applet);
    }

    void draw() {
      shade.set("brt", PApplet.map((float) x, (float) 0, (float) applet.width, (float) 0, (float) 0.5));
      shade.set("rad", (int) PApplet.map(y, 0, applet.height, 0, 3));
      super.draw();
    }
  }

  class ShaderDeform extends VisShader {
    ShaderDeform(PApplet applet) {
      super("deform", "deform.glsl", applet);
    }

    void draw() {
      shade.set("time", (float) applet.millis() / (float) 1000.0);
      shade.set("mouse", (float) x / applet.width, (float) y / applet.height);
      shade.set("turns",
          PApplet.map(PApplet.sin((float) 0.01 * applet.frameCount), (float) -1, (float) 1, (float) 2.0, (float) 10.0));
      super.draw();
    }
  }

  class ShaderPixelRolls extends VisShader {
    ShaderPixelRolls(PApplet applet) {
      super("pixelRolls", "pixelrolls.glsl", applet);
    }

    void draw() {
      shade.set("time", (float) applet.millis() / (float) 1000.0);
      shade.set("pixels", x / 5, (float) 150.0);
      shade.set("rollRate", PApplet.map(y, 0, applet.height, (float) -0.5, (float) 0.5));
      shade.set("rollAmount", (float) 0.25);
      super.draw();
    }
  }

  class ShaderModcolor extends VisShader {
    ShaderModcolor(PApplet applet) {
      super("modcolor", "modcolor.glsl", applet);
    }

    void draw() {
      shade.set("modr", PApplet.map(x, 0, applet.width, 0, (float) 0.5));
      shade.set("modg", (float) 0.3);
      shade.set("modb", PApplet.map(y, 0, applet.height, 0, (float) 0.5));
      super.draw();
    }
  }

  class ShaderHalftone extends VisShader {
    ShaderHalftone(PApplet applet) {
      super("halftone", "halftone.glsl", applet);
    }

    void draw() {
      shade.set("pixelsPerRow", (int) PApplet.map(x, 0, applet.width, 2, 100));
      super.draw();
    }
  }

  class ShaderInvert extends VisShader {
    ShaderInvert(PApplet applet) {
      super("inversion", "invert.glsl", applet);
    }

    void draw() {
      super.draw();
    }
  }

  class ShaderVHSGlitch extends VisShader {
    ShaderVHSGlitch(PApplet applet) {
      super("VHS Glitch", "vhs_glitch.glsl", applet);
      shade.set("iResolution", (float) applet.width, (float) applet.height);
    }

    void draw() {
      shade.set("iGlobalTime", applet.millis() / (float) 1000.0);
      super.draw();
    }
  }

  class ShaderSobel extends VisShader {
    ShaderSobel(PApplet applet) {
      super("Sobel", "sobel.glsl", applet);
      shade.set("iResolution", (float) applet.width, (float) applet.height);
    }

    void draw() {
      super.draw();
    }
  }

  class ShaderKaleidoscope extends VisShader {
    int viewAngleMod;
    float rot;

    ShaderKaleidoscope(PApplet applet) {
      super("Kaleidoscope", "kaleidoscope.glsl", applet);
      shade.set("rotation", 0);
      shade.set("viewAngle", PApplet.TWO_PI / 10);
    }

    void draw() {
      applet.shader(shade);
    }
  }
}
