package com.bouzou.packages.giantspacerobotvisualizer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;

public class AudioSetup {
    public static void listMixers() {
        Info[] mixers = AudioSystem.getMixerInfo();
        for (Info mixerInfo : mixers) {
            System.out.println(mixerInfo.getName() + " - " + mixerInfo.getDescription());
        }
    }

    public static Mixer getMixerByName(String name) {
        Info[] mixers = AudioSystem.getMixerInfo();
        for (Info mixerInfo : mixers) {
            if (mixerInfo.getName().equals(name)) {
                return AudioSystem.getMixer(mixerInfo);
            }
        }
        return null;
    }

    public static void printMixerDetails(Mixer mixer) {
      System.out.println("Lines for mixer: " + mixer.getMixerInfo().getName());
      javax.sound.sampled.Line.Info[] sourceLines = mixer.getSourceLineInfo();
      for (javax.sound.sampled.Line.Info line : sourceLines) {
          System.out.println("Source Line: " + line.toString());
      }
      javax.sound.sampled.Line.Info[] targetLines = mixer.getTargetLineInfo();
      for (javax.sound.sampled.Line.Info line : targetLines) {
          System.out.println("Target Line: " + line.toString());
      }
  }
}
