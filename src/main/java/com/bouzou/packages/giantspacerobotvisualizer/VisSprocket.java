package com.bouzou.packages.giantspacerobotvisualizer;

import ddf.minim.analysis.FFT;

// ************************************************************************************************
// visSprocket Visualiser class
// Browse Knob1 -
// Button1 - Background retention on/off
// Button2 - Color mode (random or linear)
// Fader1 - Rotation speed
// Fader2 - Stroke colour
// ************************************************************************************************
class VisSprocket extends Visualiser {

  /**
   *
   */
  private final GiantSpaceRobotVisualizer giantSpaceRobotVisualizer;
  int specSize;
  float[] angle, x, y;
  float f, b, density;
  int outlineColour = 10;

  VisSprocket(GiantSpaceRobotVisualizer giantSpaceRobotVisualizer, String n) {
    super(giantSpaceRobotVisualizer, n);
    this.giantSpaceRobotVisualizer = giantSpaceRobotVisualizer;
    fft = new FFT(this.giantSpaceRobotVisualizer.input.bufferSize(), this.giantSpaceRobotVisualizer.input.sampleRate());
    specSize = fft.specSize();

    y = new float[specSize];
    x = new float[specSize];
    angle = new float[specSize];
    density = 1;
    fader1 = 800;
  }

  void draw() {

    fft.forward(this.giantSpaceRobotVisualizer.input.mix);

    pg.beginDraw();

    if (!button1) {
      pg.clear();
    }

    pg.push();
    if (button2) {
      pg.colorMode(GiantSpaceRobotVisualizer.RGB);
    } else {
      pg.colorMode(GiantSpaceRobotVisualizer.HSB);
    }

    pg.translate(pg.width / 2, pg.height / 2);

    for (int i = 0; i < specSize; i++) {
      if (button2) {
        pg.fill(this.giantSpaceRobotVisualizer.random(255), this.giantSpaceRobotVisualizer.random(255), this.giantSpaceRobotVisualizer.random(255), 255);
      } else {
        pg.fill(i, 150, 150, 150);
      }

      f = fft.getFreq(i);
      b = fft.getBand(i);

      pg.stroke(outlineColour);
      y[i] = y[i] + b / 10;
      x[i] = x[i] + f / 10;
      angle[i] = angle[i] + f / (fader1 + 1);

      pg.rotateX(GiantSpaceRobotVisualizer.sin(angle[i] / 2) / density);
      pg.rotateY(GiantSpaceRobotVisualizer.cos(angle[i] / 2) / density);

      pg.pushMatrix();
      pg.translate((x[i] + 5) % pg.width / 5, (y[i] + 5) % pg.height / 5);
      pg.box(f * scaling);
      pg.popMatrix();
    }
    pg.pop();

    pg.endDraw();
    super.draw();
  }

  void initAnalysis() {
  }

  void setFader1(int v) {
    fader1 = GiantSpaceRobotVisualizer.round(GiantSpaceRobotVisualizer.map(v, 0, 127, 80, 800));
  }

  void setFader2(int v) {
    outlineColour = GiantSpaceRobotVisualizer.round(GiantSpaceRobotVisualizer.map(v, 0, 127, 0, 255));
  }

  void setScaling(float s) {
    scaling = GiantSpaceRobotVisualizer.map(s, 0, 127, (float) 0.5, 10);
  }
}