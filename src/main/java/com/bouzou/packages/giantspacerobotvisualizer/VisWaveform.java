package com.bouzou.packages.giantspacerobotvisualizer;

// ************************************************************************************************
// Waveform visualiser class
//
// Draws a simple osciliscope type wavform
// ************************************************************************************************
class VisWaveform extends Visualiser {
  /**
   *
   */
  private final GiantSpaceRobotVisualizer giantSpaceRobotVisualizer;
  int scale = 500;

  VisWaveform(GiantSpaceRobotVisualizer giantSpaceRobotVisualizer, String n) {
    super(giantSpaceRobotVisualizer, n);
    this.giantSpaceRobotVisualizer = giantSpaceRobotVisualizer;
  }

  void draw() {

    pg.beginDraw();
    pg.clear();
    pg.strokeWeight(2);

    if (this.giantSpaceRobotVisualizer.myBgPalette.getBlackOrWhite()) {
      pg.stroke(10);
    } else {
      pg.stroke(250);
    }

    pg.pushMatrix();
    pg.translate(0, halfHeight);

    float distance = (float) this.giantSpaceRobotVisualizer.width / this.giantSpaceRobotVisualizer.input.bufferSize();
    for (int i = 0; i < this.giantSpaceRobotVisualizer.input.bufferSize() - 1; i++) {
      float x1 = distance * i;
      float x2 = distance * (i + 1);

      pg.line(x1, this.giantSpaceRobotVisualizer.input.left.get(i) * scale, x2, this.giantSpaceRobotVisualizer.input.left.get(i + 1) * scale);
    }

    pg.popMatrix();
    pg.endDraw();
    super.draw();
  }

  void scale(int v) {
    scale = GiantSpaceRobotVisualizer.round(GiantSpaceRobotVisualizer.map(v, 0, 127, 100, 1000));
  }
}