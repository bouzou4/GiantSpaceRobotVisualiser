package com.bouzou.packages.giantspacerobotvisualizer;

import java.io.File;
import java.util.ArrayList;

import ddf.minim.analysis.FFT;
import processing.core.PImage;
import processing.core.PVector;

// ***************************************************************************
// Oblivion visualiser class
//
// Browse Knob1 - Change pallette
// Button1 - Background clear toggle
// Button2 - Ellipse on/off
// Fader1 - Rotation
// Fader2 - Opacity
// ***************************************************************************
class VisOblivion extends Visualiser {

  /**
   *
   */
  private final GiantSpaceRobotVisualizer giantSpaceRobotVisualizer;
  int[] activeGradient;
  int gradientIndex;
  ArrayList<int[]> gradients;
  int opacity = 100;

  int count = 20;
  float positionRadius = (pg.height * (float) 0.3) * (float) 1.35;

  float[] previousValues;
  PVector[][] prevPos;

  float rotationAngle = (float) 0.0;
  float rotationSpeed = (float) 0.0;

  int previousKnobValue = 0; // for this visualiser the knob is used to choose the next or previous gradient
  // so we simply check the direction the knob is turned
  // and increment or decrement accordingly

  VisOblivion(GiantSpaceRobotVisualizer giantSpaceRobotVisualizer, String n) {
    super(giantSpaceRobotVisualizer, n);
    this.giantSpaceRobotVisualizer = giantSpaceRobotVisualizer;

    scaling = 5;

    loadGradients();

    // Initialise the fft analysis
    fft = new FFT(this.giantSpaceRobotVisualizer.input.bufferSize(), this.giantSpaceRobotVisualizer.input.sampleRate());

    // set up the arrays to hold the previous values of the fft analysis spectrum
    previousValues = new float[fft.specSize() / 10];
    prevPos = new PVector[previousValues.length][20];
  }

  void draw() {
    calculateFFTValues();

    pg.beginDraw();
    if (!button1) {
      pg.clear();
    }

    pg.noStroke();

    // Rotate display, rate set by Fader1
    pg.translate(this.giantSpaceRobotVisualizer.width / 2, this.giantSpaceRobotVisualizer.height / 2);
    rotationAngle += rotationSpeed;
    pg.rotate(rotationAngle);
    pg.translate(-this.giantSpaceRobotVisualizer.width / 2, -this.giantSpaceRobotVisualizer.height / 2);

    for (int i = 0; i < previousValues.length; i++) {

      float startAngle = (i * GiantSpaceRobotVisualizer.PI / 100);
      float deltaAngle = GiantSpaceRobotVisualizer.PI * 2 / count;
      float value = previousValues[i];
      float percent = (float) i / previousValues.length;

      int col = activeGradient[GiantSpaceRobotVisualizer.min((int) (activeGradient.length * percent), activeGradient.length)];
      pg.fill(col, opacity);

      float s = GiantSpaceRobotVisualizer.max(2, value * (float) 0.5f * positionRadius / 360f);

      float distance = positionRadius - (percent * positionRadius * value / 40);
      distance = GiantSpaceRobotVisualizer.max(-positionRadius, distance);

      for (int j = 0; j < count; j++) {
        float a = startAngle + deltaAngle * j;
        if (j % 2 == 0) {
          a -= startAngle * 2;
        }
        PVector prev = prevPos[i][j];
        PVector curr = new PVector(this.giantSpaceRobotVisualizer.width / 2 + GiantSpaceRobotVisualizer.cos(a) * distance, this.giantSpaceRobotVisualizer.height / 2 + GiantSpaceRobotVisualizer.sin(a) * distance);

        // Draw an ellipse, makes the visualisation more dramatic
        if (button2) {
          pg.ellipse(pg.width / 2 + GiantSpaceRobotVisualizer.cos(a) * distance, pg.height / 2 + GiantSpaceRobotVisualizer.sin(a) * distance, s, s);
        }

        if (prev != null) {

          float dx = prev.x - curr.x;
          float dy = prev.y - curr.y;
          float d = GiantSpaceRobotVisualizer.sqrt(dx * dx + dy * dy);

          pg.pushMatrix();
          pg.translate(curr.x, curr.y);
          pg.rotate(GiantSpaceRobotVisualizer.atan2(dy, dx));

          pg.rect(0, -s / 2, d, s);

          pg.popMatrix();
        }
        prevPos[i][j] = curr;
      }
    }
    pg.endDraw();

    super.draw();
  }

  void calculateFFTValues() {
    fft.forward(this.giantSpaceRobotVisualizer.input.mix);

    int size = 10;

    for (int n = 0; n < fft.specSize() - size; n += size) {
      float percent = n / (fft.specSize() - size);
      float avg = 0;
      for (int i = n; i < n + size; i++) {
        avg += fft.getBand(n);
      }
      avg = avg * GiantSpaceRobotVisualizer.lerp(4, 8, percent) * scaling / size;

      float previous = previousValues[n / size];
      previous *= (float) 0.9;
      previous = GiantSpaceRobotVisualizer.max(avg, previous);

      previousValues[n / size] = previous;
    }
  }

  void loadGradients() {
    int[] gradient;

    // Load the colour gradients
    gradientIndex = 0;
    gradients = new ArrayList<int[]>();

    // Read in a list of image files used to define a set of gradients used in the
    // visualisation
    File dir = new File(GiantSpaceRobotVisualizer.resourcesDir("") + "//gradients//");

    File[] files = dir.listFiles();
    boolean gradientReverse = false;
    for (int i = 0; i < files.length; i++) {
      String path = files[i].getAbsolutePath();

      // check the file type and work with jpg/png files
      if (path.toLowerCase().endsWith(".png")) {
        PImage image = this.giantSpaceRobotVisualizer.loadImage(path);

        gradient = new int[image.width];
        for (int j = 0; j < image.width; j++) {
          gradient[j] = image.get(gradientReverse ? (image.width - j - 1) : j, 0);
        }
        gradients.add(gradient);
      }
    }
    activeGradient = gradients.get(gradientIndex);
  }

  // If the browser knob is moved clockwise (value increases) then select next
  // gradient,
  // if anti-clockwise the select previous
  void setKnob1(int v) {
    int inc = 0;
    int gradSize = gradients.size() - 1;

    if (v > previousKnobValue) {
      inc = 1;
    } else {
      inc = -1;
    }

    gradientIndex += inc;

    // Wrap the index back to the start (or end) of the array accordingly
    if (gradientIndex > gradSize) {
      gradientIndex = 0;
    }
    if (gradientIndex < 0) {
      gradientIndex = gradSize;
    }
    activeGradient = gradients.get(gradientIndex);

    // and store the previoub knob value so that we can tell if it is going up or
    // down
    previousKnobValue = v;
  }

  void setFader1(int v) {
    rotationSpeed = GiantSpaceRobotVisualizer.map(v, 0, 127, (float) -0.1, (float) 0.1);
    if ((rotationSpeed > (float) -0.01) && (rotationSpeed < (float) 0.01)) {
      rotationSpeed = (float) 0.0;
    }
  }

  void setFader2(int v) {
    opacity = GiantSpaceRobotVisualizer.round(GiantSpaceRobotVisualizer.map(v, 0, 127, 0, 255));
  }

  void setScaling(float s) {
    scaling = GiantSpaceRobotVisualizer.map(s, 0, 127, 0, 20);
  }
}