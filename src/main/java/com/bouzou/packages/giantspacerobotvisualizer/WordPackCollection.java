package com.bouzou.packages.giantspacerobotvisualizer;

import java.util.ArrayList;

import com.bouzou.packages.giantspacerobotvisualizer.GiantSpaceRobotVisualizer.BgPalette;

import processing.core.PApplet;
import processing.core.PFont;

class WordPacks {
  PApplet applet;
  BgPalette bgPalette;
  PFont font;
  ArrayList<WordPack> words;
  int currentWordPack;
  int wordCount;
  boolean wordsOn;
  int wordColor;
  int alpha;

  WordPacks(PApplet applet, BgPalette bgPalette, PFont font) {
    this.applet = applet;
    this.bgPalette = bgPalette;
    this.font = font;
    words = new ArrayList<WordPack>();
    currentWordPack = 0;
    wordsOn = false;
    wordCount = 0;
    alpha = 150;
    wordColor = applet.color(0);
  }

  void addWords(String[] wp) {
    words.add(new WordPack(wp, bgPalette, applet, font));
    wordCount = words.size();
  }

  void setCurrentPack(int midiValue) {
    if (midiValue == 127 | midiValue == 0) {
      unCueWord();
    } else {
      if (midiValue <= wordCount) {
        currentWordPack = midiValue - 1;
        words.get(currentWordPack).reset();
        wordsOn = true;
      }
    }
  }

  void display() {
    if (wordsOn) {
      words.get(currentWordPack).display(alpha);
    }
  }

  void nextWord() {
    words.get(currentWordPack).cueWord();
  }

  void unCueWord() {
    wordsOn = false;
  }

  void setAlpha(int a) {
    alpha = PApplet.round(PApplet.map(a, 0, 127, 0, 255));
  }
}
