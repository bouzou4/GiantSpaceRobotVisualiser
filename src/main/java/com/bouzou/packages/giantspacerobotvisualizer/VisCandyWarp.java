package com.bouzou.packages.giantspacerobotvisualizer;

import ddf.minim.analysis.FFT;
import processing.opengl.PShader;

// ************************************************************************************************
// CandyWarp Visualiser class
// Button1 - no action
// Button2 - no action
// Fader1 - Changes the shader's cycle parameter
// Fader2 - Changes the shader's warp parameter
// Knob1 - Picks the frequency range to react to, all the way to the left for
// bass,
// to the right for treble
// ************************************************************************************************
class VisCandyWarp extends Visualiser {

  /**
   *
   */
  private final GiantSpaceRobotVisualizer giantSpaceRobotVisualizer;
  PShader shade;
  float cycle = (float) 0.2;
  float warp = (float) 2.5;
  float scale = (float) 84.0;

  int fftIndex = 1;
  int prevKnobValue = 0;

  VisCandyWarp(GiantSpaceRobotVisualizer giantSpaceRobotVisualizer, String n) {
    super(giantSpaceRobotVisualizer, n);
    this.giantSpaceRobotVisualizer = giantSpaceRobotVisualizer;

    shade = this.giantSpaceRobotVisualizer.loadShader("Candywarp.glsl");

    // settings that are fixed in this visualisation
    shade.set("iResolution", (float) this.giantSpaceRobotVisualizer.width, (float) this.giantSpaceRobotVisualizer.height);
    shade.set("thickness", (float) 0.1); // Default : 0.1 Min : 0.5 Max : 1.0
    shade.set("loops", (float) 61.0); // Default : 61.0 Min : 10.0 Max : 100.0
    shade.set("tint", (float) 0.1); // Default : 0.1 Min : -0.5 Max : 0.5
    shade.set("rate", (float) 1.3); // Default : 1.3 Min : -3.0 Max : 3.0
    shade.set("hue", (float) 0.33); // Default : 0.33 Min : -0.5 Max : 0.5

    // settings that vary in this visualisation
    shade.set("time", this.giantSpaceRobotVisualizer.millis() / (float) 1000.0);
    shade.set("cycle", cycle); // Default : 0.4 Min : 0.01 Max : 0.99
    shade.set("warp", warp); // Default : 2.5 Min : -5.0 Max : 5.0
    shade.set("scale", scale); // Default : 84.0 Min : 10.0 Max : 100.0

    // set up fft analysis
    initAnalysis();
  }

  void draw() {

    analyse();

    pg.beginDraw();

    shade.set("time", this.giantSpaceRobotVisualizer.millis() / (float) 1000.0);
    shade.set("warp", warp);
    shade.set("cycle", cycle);

    scale = GiantSpaceRobotVisualizer.map(fftSmooth[fftIndex], 0, 18, (float) 20.0, (float) 100.0); // use a specific frequency band to modulate
                                                                          // the shader's scale attribute
    shade.set("scale", scale);

    pg.filter(shade);

    pg.endDraw();
    super.draw();
  }

  void initAnalysis() {

    fft = new FFT(this.giantSpaceRobotVisualizer.input.bufferSize(), this.giantSpaceRobotVisualizer.input.sampleRate());
    fft.logAverages(11, 1);

    avgSize = fft.avgSize();
    fftSmooth = new float[avgSize];
  }

  void analyse() {
    final float noiseFloor = 0; // -10; // Minimum sound level that we respond to

    fft.forward(this.giantSpaceRobotVisualizer.input.mix);

    for (int i = 0; i < avgSize; i++) {
      // Get spectrum value (using dB conversion or not, as desired)
      float fftCurr;
      fftCurr = dB(fft.getAvg(i));
      if (fftCurr < noiseFloor) {
        fftCurr = noiseFloor;
      }

      // Smooth using exponential moving average
      fftSmooth[i] = (smoothing) * fftSmooth[i] + ((1 - smoothing) * fftCurr);
    }
  }

  void setFader1(int v) {
    cycle = GiantSpaceRobotVisualizer.map(v, 0, 127, (float) 0.01, (float) 0.4);
  }

  void setFader2(int v) {
    warp = GiantSpaceRobotVisualizer.map(v, 0, 127, (float) -5.0, (float) 5.0);
  }

  void setKnob1(int v) {
    int inc = 0;

    if (v > prevKnobValue) {
      inc = 1;
    } else {
      inc = -1;
    }

    fftIndex += inc;

    // Constrain the index to the range of the array
    if (fftIndex > avgSize - 1) {
      fftIndex = avgSize - 1;
    }
    if (fftIndex < 0) {
      fftIndex = 0;
    }
    // and store the previoub knob value so that we can tell if it is going up or
    // down
    prevKnobValue = v;
  }
}