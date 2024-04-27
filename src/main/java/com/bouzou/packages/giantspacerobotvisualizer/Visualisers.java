package com.bouzou.packages.giantspacerobotvisualizer;

import java.util.ArrayList;

class Visualisers {
  ArrayList<Visualiser> visualisers;
  Visualiser currentVisualiser;
  int visIndex;
  int visCount;

  Visualisers() {
    visualisers = new ArrayList<Visualiser>();
    visCount = 0;
  }

  void addVisualiser(Visualiser v) {
    visualisers.add(v);
    visCount = visualisers.size();
    visIndex = 0;
    currentVisualiser = visualisers.get(visIndex);
  }

  void setVisualiser(int v) {
    if (v == 1) {
      visIndex -= 1;
      if (visIndex < 0) {
        visIndex = visCount - 1;
      }
    } else if (v == 2) {
      visIndex += 1;
      if (visIndex >= visCount) {
        visIndex = 0;
      }
    }

    currentVisualiser = visualisers.get(visIndex);
  }

  void toggleButton1() {
    currentVisualiser.toggleButton1();
  }

  void toggleButton2() {
    currentVisualiser.toggleButton2();
  }

  void setFader1(int v) {
    currentVisualiser.setFader1(v);
  }

  void setFader2(int v) {
    currentVisualiser.setFader2(v);
  }

  void setKnob1(int v) {
    currentVisualiser.setKnob1(v);
  }

  void setScaling(int v) {
    currentVisualiser.setScaling(v);
  }

  String getName() {
    return currentVisualiser.name;
  }
}