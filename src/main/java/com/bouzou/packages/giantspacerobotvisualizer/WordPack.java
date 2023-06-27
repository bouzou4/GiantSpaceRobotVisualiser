package com.bouzou.packages.giantspacerobotvisualizer;

import com.bouzou.packages.giantspacerobotvisualizer.GiantSpaceRobotVisualizer.BgPalette;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

class WordPack {
  BgPalette bgPalette;
  PApplet applet;
  PFont font;
  String[] words;
  int currentWord;
  int wordCount;

  WordPack(String[] wp, BgPalette bgPalette, PApplet applet, PFont font) {
    this.bgPalette = bgPalette;
    this.applet = applet;
    this.font = font;
    words = wp;
    wordCount = words.length - 1;
    currentWord = 0;
  }

  void display(int a) {
    this.applet.textAlign(PConstants.CENTER, PConstants.CENTER);
    if (this.bgPalette.getBlackOrWhite()) {
      this.applet.fill(0, a);
    } else {
      this.applet.fill(255, a);
    }
    this.applet.textFont(font, 200);

    this.applet.text(words[currentWord], this.applet.width / 2, this.applet.height / 2);
  }

  void cueWord() {
    currentWord++;
    if (currentWord > wordCount) {
      currentWord = 0;
    }
  }

  void reset() {
    currentWord = 0;
  }
}
