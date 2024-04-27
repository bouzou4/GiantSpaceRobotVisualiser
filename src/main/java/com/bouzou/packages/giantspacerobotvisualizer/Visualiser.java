/**
 * This class represents a generic visualizer for a Giant Space Robot.
 * Specific visualizers can extend this class and implement their own behavior.
 * The visualizer has controls mapped onto the Maschine Jam and Traktor transport,
 * including a browse knob, two buttons, two faders, and a knob.
 * Each visualizer can interpret music using an FFT for frequency analysis.
 * The class provides a method to pick out the frequency range used by Traktor's Z-ISO EQ.
 */
package com.bouzou.packages.giantspacerobotvisualizer;

import ddf.minim.analysis.FFT;
import processing.core.PGraphics;

abstract class Visualiser {
  private final GiantSpaceRobotVisualizer giantSpaceRobotVisualizer;
  String name;
  PGraphics pg;
  int halfWidth;
  int halfHeight;
  boolean button1;
  boolean button2;
  int fader1;
  int fader2;
  int knob1;
  float scaling = 1;
  FFT fft;
  float smoothing = (float) 0.60;
  float[] fftSmooth;
  int avgSize;

  /**
   * Constructs a Visualiser object with the specified GiantSpaceRobotVisualizer and name.
   * @param giantSpaceRobotVisualizer the GiantSpaceRobotVisualizer object
   * @param n the name of the visualizer
   */
  Visualiser(GiantSpaceRobotVisualizer giantSpaceRobotVisualizer, String n) {
    this.giantSpaceRobotVisualizer = giantSpaceRobotVisualizer;
    name = n;
    pg = this.giantSpaceRobotVisualizer.createGraphics(this.giantSpaceRobotVisualizer.width, this.giantSpaceRobotVisualizer.height, GiantSpaceRobotVisualizer.P3D);
    halfWidth = this.giantSpaceRobotVisualizer.width / 2;
    halfHeight = this.giantSpaceRobotVisualizer.height / 2;
    button1 = false;
    button2 = false;
    fader1 = 0;
    fader2 = 0;
    knob1 = 0;
  }

  /**
   * Draws the visualizer on the screen.
   */
  void draw() {
    this.giantSpaceRobotVisualizer.image(pg, 0, 0);
  }

  /**
   * Toggles the state of button 1.
   */
  void toggleButton1() {
    button1 = !button1;
  }

  /**
   * Toggles the state of button 2.
   */
  void toggleButton2() {
    button2 = !button2;
  }

  /**
   * Sets the value of fader 1.
   * @param v the value of fader 1
   */
  void setFader1(int v) {
    fader1 = v;
  }

  /**
   * Sets the value of fader 2.
   * @param v the value of fader 2
   */
  void setFader2(int v) {
    fader2 = v;
  }

  /**
   * Sets the value of knob 1.
   * @param v the value of knob 1
   */
  void setKnob1(int v) {
    knob1 = v;
  }

  /**
   * Sets the scaling factor for the visualizer.
   * @param s the scaling factor
   */
  void setScaling(float s) {
    scaling = GiantSpaceRobotVisualizer.map(s, 0, 127, 0, 20);
  }

  /**
   * Calculates the decibel value of a given value.
   * @param x the value to calculate the decibel value for
   * @return the decibel value
   */
  float dB(float x) {
    if (x == 0) {
      return 0;
    } else {
      return 10 * (float) Math.log10(x);
    }
  }
}