package com.bouzou.packages.giantspacerobotvisualizer;

import processing.core.PApplet;
import processing.opengl.PShader;

abstract class VisShader {
  PApplet applet;
  String name;
  String filename;
  boolean on;
  PShader shade;
  int x;
  int y;

  VisShader(String n, String f, PApplet applet) {
    this.applet = applet;
    name = n;
    filename = f;
    shade = this.applet.loadShader(filename);
    x = this.applet.width / 2;
    y = this.applet.height / 2;
  }

  void draw() {
    this.applet.filter(shade);
  }

  void setX(int i) {
    x = i;
  }

  void setY(int i) {
    y = i;
  }
}
