package com.bouzou.packages.giantspacerobotvisualizer;
// ************************************************************************************* //<>// //<>//

// * giantspacerobot visualiser for Traktor                                            *
// * Heavily based on awesome visualisers from Tobias Wehrum (Oblivion), Ben Farahmand's
// * (Sprocket) and mojovideotech (Candywarp shader)
// * (attributed below)                                                                *
// * This extends giantspacerobot's https://maps.djtechtools.com/mappings/6883         *
// * to use a page on the Maschine Jam to control various parameters in a visualiser.  *
// * It responds to the audio from Traktor, as well as various midi messages from a    *
// * Traktor controller                                                                *
// *************************************************************************************

/* Acknowledgments
 
 ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 // Oblivion
 // Copyright (c) 2016 Tobias Wehrum <Tobias.Wehrum@dragonlab.de>
 // Distributed under the MIT License. (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
 // This notice shall be included in all copies or substantial portions of the Software.
 ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
 ////////////////////////////////////////////////////////////////////////////////////////////
 // Atomic Sprocket Visualiser
 // Benjamin Farahmand
 // https://gist.github.com/benfarahmand/6902359#file-audio-visualizer-atomic-sprocket
 //////////////////////////////////////////////////////////////////////////////////////////// 
 
 ////////////////////////////////////////////////////////////
 // CandyWarp  by mojovideotech
 //
 // based on :  
 // glslsandbox.com/e#38710.0
 // Posted by Trisomie21
 // modified by @hintz
 //
 // Creative Commons Attribution-NonCommercial-ShareAlike 3.0
 ////////////////////////////////////////////////////////////
 
 */

import themidibus.*;
import ddf.minim.*;
import ddf.minim.analysis.FFT;
import processing.core.*;
import processing.data.*;
import processing.opengl.PShader;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;

// import spout.*;

public class GiantSpaceRobotVisualizer extends PApplet {

  // ***********************
  // Initialise audio stuff
  // ***********************

  Minim minim;
  AudioInput input;

  // ***********************
  // Initialise Midi stuff
  // ***********************

  MidiBus myBus;
  // ArrayList midiDevices = new ArrayList();
  // the midi channel defaults to 11, but can be overridden via the config file
  // (Midi channel 11 is numbered 10 in MidiBus (!)
  int midiChannel = 10;

  // ************************************
  // Set up JSON handler for config file
  // ************************************

  // A JSON object
  JSONObject json;

  // ***********************
  // Init text display
  // ***********************

  PFont font;

  // ****************************************
  // Init the shaders & visualiser options
  // ****************************************

  // Shaders used in post processing
  ShaderBrcosa shaderBrcosa;
  ShaderHue shaderHue;
  ShaderPixelate shaderPixelate;
  ShaderChannels shaderChannels;
  ShaderThreshold shaderThreshold;
  ShaderNeon shaderNeon;
  ShaderDeform shaderDeform;
  ShaderPixelRolls shaderPixelrolls;
  ShaderModcolor shaderModcolor;
  ShaderHalftone shaderHalftone;
  ShaderInvert shaderInvert;

  PostProcessingShaders VisShaders;

  // Shaders used to represent effects in traktor
  ShaderVHSGlitch shaderVhs_glitch; // Represent the Gate/Slice or Beatmasher effects in Traktor
  ShaderSobel shaderSobel; // Represent an echo effect in Traktor
  BlurShader shaderBlur; // Represent a filter effect in Traktor

  // Options used in the main draw routine
  boolean kaleidoscopeOn = false;
  boolean waveformOn = false;
  boolean delayOn = false;
  boolean sliceOn = false;

  // Init the main visualisers
  // One of these can be in action at a time
  VisOblivion visOblivion;
  VisSprocket visSprocket;
  VisCandyWarp visCandyWarp;

  // Init the collection of main visualiers
  Visualisers visualisers;

  // Init the waveform visualiser
  // this visualiser can be overlayed on the others
  VisWaveform visWaveform;

  // This shader can be toggled on as part of the main draw routine
  // and affects any object onscreen
  ShaderKaleidoscope shaderKaleidoscope;

  // **************************
  // Set up decks and hotcues
  // **************************

  // Objects that store the state of each deck
  DeckSettings deckA;
  DeckSettings deckB;
  DeckSettings deckC;
  DeckSettings deckD;

  // Object to store the overall mixer settings
  MixerState mixerState;

  // List of images used when a hotkey is pressed
  HotcuePacks hotcuePacks;

  // Init the word packs, these are lists of words that can be displayed on each
  // beat
  WordPacks beatWords;

  // Init the palette used to switch the background colour on each beat
  BgPalette myBgPalette;

  // precalc some constants for performance reasons
  final float PI2 = PI * 2;
  final float PI100 = PI / 100;

  // used to toggle fps and info display
  // press "i" to toggle
  boolean infoOn = false;
  boolean helpOn = false;

  // PImage img; // image to use for the rotating cube demo
  PGraphics pgr; // Graphics for demo

  // Declare a SPOUT object, used to direct video output to another application,
  // such as a video projection app
  // Spout spout;
  boolean spoutOn;

  public void settings() {
    // load the screen definition from the config file
    JSONObject j = loadJSONObject("config.json");

    // load MIDI channel info from config.json
    JSONArray d = j.getJSONArray("screensize");
    if (d.size() == 0) {
      println("Can,t find sceeensize definition in config file, please check this");
      exit();
    }

    JSONObject m = d.getJSONObject(0);

    // is this fullscreen or not?
    boolean full = m.getBoolean("fullscreen");

    // set the display to draw on (only applies to fullscreen)
    int display = m.getInt("displaynumber");

    // if not fullscreen, then what are the window dimensions?
    int w = m.getInt("width");
    int h = m.getInt("height");

    if (full) {
      fullScreen(P3D, display);
    } else {
      size(w, h, P3D);
    }
  }

  // **********************************************************************************
  // * Setup
  // **********************************************************************************
  public void setup() {

    surface.setTitle("GiantSpaceRobot Visualiser");

    // Create a new SPOUT object
    // spout = new Spout(this);
    // spout.createSender("Spout Processing");
    spoutOn = false;

    // *********************
    // Setup Sound stuff
    // **********************
    minim = new Minim(this);
    input = minim.getLineIn(Minim.STEREO, 1024, 48000, 16);

    // *********************
    // Set up text display
    // **********************
    font = loadFont("FranklinGothic-Heavy-200.vlw");

    // ******************
    // * Set up shaders *
    // ******************

    // Add a kaleidoscope effect to the entire display
    shaderKaleidoscope = new ShaderKaleidoscope();

    // Shaders used to represet effects used in Traktor
    shaderVhs_glitch = new ShaderVHSGlitch(); // represents slicer, masher and gater
    shaderSobel = new ShaderSobel(); // reresents echo
    shaderBlur = new BlurShader(); // represents filter (or deck FX)

    // set up a list of shaders used for post processing effects
    VisShaders = new PostProcessingShaders();

    // Set up visualisers
    visWaveform = new VisWaveform("Waveform");

    visOblivion = new VisOblivion("Oblivion");
    visSprocket = new VisSprocket("Sprocket");
    visCandyWarp = new VisCandyWarp("CandyWarp");

    // set up the list of main visualisers
    visualisers = new Visualisers();

    visualisers.addVisualiser(visOblivion);
    visualisers.addVisualiser(visSprocket);
    visualisers.addVisualiser(visCandyWarp);

    // Set up decks
    deckA = new DeckSettings("A");
    deckB = new DeckSettings("B");
    deckC = new DeckSettings("C");
    deckD = new DeckSettings("D");

    mixerState = new MixerState();

    hotcuePacks = new HotcuePacks();

    // Read and process the config file, this includes setting up the midi inputs
    // and the word packs
    loadConfig();
  }

  // **********************************************************************************
  // * Draw
  // **********************************************************************************
  public void draw() {
    myBgPalette.drawBg();

    if (kaleidoscopeOn) {
      shaderKaleidoscope.draw();
    } else {
      resetShader();
    }

    // draw the hotcue image if one has been triggered
    hotcuePacks.draw();

    // main visualiser
    visualisers.currentVisualiser.draw();

    // waveform display
    if (waveformOn) {
      visWaveform.draw();
    }

    // draw words on the screeen (if any are selected)
    beatWords.display();

    // toggle shaders if certain effects are on
    if (delayOn) {
      shaderSobel.draw();
    }

    if (sliceOn) {
      shaderVhs_glitch.draw();
    }

    // post processing shaders
    VisShaders.draw();

    // apply the blur filter, the degree is defined by the position of the filter
    // knob in traktor
    shaderBlur.draw();

    if (infoOn) {
      resetShader();
      displayFPS();
    }
    if (helpOn) {
      resetShader();
      displayHelp();
    }

    // SPOUT is used to export the display to an external program to support things
    // like
    // post processing and projection mapping
    if (spoutOn) {
      // spout.sendTexture();
    }
  }

  // Keyboard controls, used for convenience if midi device is not available
  public void keyPressed() {
    println("key: " + key + " keyCode: " + keyCode);

    if (key == 'i') {
      infoOn = !infoOn;
    }
    if (key == 'd') {
      visualisers.setVisualiser(2);
    }
    if (key == 'a') {
      visualisers.setVisualiser(1);
    }
    if (key == 'w') {
      waveformOn = !waveformOn;
    }
    if (key == 'p') {
      // spoutOn = !spoutOn;
    }
    if ((key == 'h') || (key == '?')) {
      helpOn = !helpOn;
    }
  }

  public void displayFPS() {
    push();
    fill(150);
    textSize(12);
    textAlign(BASELINE);
    text("Info\n"
        + "------------\n"
        + "FPS = " + round(frameRate) + "\n"
        + "SPOUT on = " + spoutOn + "\n"
        + "Visualiser = " + visualisers.getName() + "\n"
        + "\n" + "Post Shader = " + VisShaders.getCurrentShaderInfo() + "\n"
        + deckA.getStatus() + "\n" + deckB.getStatus() + "\n" + deckC.getStatus() + "\n" + deckD.getStatus(), 10, 30);
    // + "FFT Scaling = " + visualisers.getScaling() + "\n"

    pop();
  }

  public void displayHelp() {
    push();
    fill(150);
    textSize(12);
    textAlign(BASELINE);
    text("Help\n"
        + "------------\n"
        + "i - Info toggle\n"
        + "w - Waveform toggle\n"
        + "d - Next Visualiser\n"
        + "a - Prev Visualiser\n"
        + "p - SPOUT toggle", width - 200, 30);
    pop();
  }

  void loadConfig() {
    json = loadJSONObject("config.json");

    loadMidiDevice();
    loadMidiChannel();
    loadWordPacks();
    loadBackgroundPalettes();
  }

  // ****************************************************************************
  // Set up Midi interfaces
  // We need a connection from the Maschine Jam and the Traktor controller,
  // use the config file to specify the correct names for the machine you are on
  // ****************************************************************************
  MidiBus[] buses;

  // MidiBus busA; //The first MidiBus
  // MidiBus busB; //The second MidiBus
  void loadMidiDevice() {
    // display the available midi devices
    MidiBus.list();
    println("\n");
    String deviceName;

    // load MIDI device info from config.json
    JSONArray d = json.getJSONArray("MIDIdevice");
    if (d.size() == 0) {
      println("No Midi device name found in config.json file");
      println("Failed to assign any input devices.\nUse in non-Midi mode.");
    }
    buses = new MidiBus[d.size()];

    for (int i = 0; i < d.size(); i++) {
      JSONObject m = d.getJSONObject(i);
      deviceName = m.getString("device");

      String[] available_inputs = MidiBus.availableInputs();

      for (int j = 0; j < available_inputs.length; j++) {
        if (available_inputs[j].indexOf(deviceName) > -1) {

          buses[i] = new MidiBus(this, deviceName, deviceName, deviceName);
          println(i + " Added Midi device - " + buses[i].getBusName());
        }
      }
    }
  }

  void loadMidiChannel() {
    // String midiChannel;

    // load MIDI channel info from config.json
    JSONArray d = json.getJSONArray("MIDIchannel");
    if (d.size() == 0) {
      println("No Midi channel definition found in config.json file");
      println("in this case we will assume 11, but that may not be correct for you!");
    }
    JSONObject m = d.getJSONObject(0);
    midiChannel = m.getInt("channel") - 1;
  }

  // *******************************************
  // Set up word packs
  // *******************************************

  void loadWordPacks() {
    beatWords = new WordPacks();

    JSONArray wordData = json.getJSONArray("wordpacks");

    for (int i = 0; i < wordData.size(); i++) {
      JSONObject d2 = wordData.getJSONObject(i);
      JSONArray d3 = d2.getJSONArray("words");

      // Convert JSON array to String array
      String[] s = toStringArray(d3);

      // Set up a word pack
      beatWords.addWords(s);
    }
  }

  // *******************************************
  // Set up backgound palettes
  // *******************************************
  void loadBackgroundPalettes() {
    ArrayList<int[]> palettes = new ArrayList<int[]>();

    JSONArray bgData = json.getJSONArray("palettes");

    for (int i = 0; i < bgData.size(); i++) {
      JSONObject d2 = bgData.getJSONObject(i);
      JSONArray d3 = d2.getJSONArray("colours");

      // step through the array, get the hex colour value (in web colour format).
      // To use it as a Processing colour we need to convert it to a hex integer
      // so we strip of the leading "#" character and add "FF" to the start. The "FF"
      // is the
      // alpha value.
      int[] palette;
      palette = new int[d3.size()];

      for (int j = 0; j < d3.size(); j++) {
        String s = d3.getString(j);
        // strip off the # character
        s = s.substring(1, s.length());

        // make the colour, we need to prefix it with the alpha value
        int c = unhex("FF" + s);
        palette[j] = c;
      }
      palettes.add(palette);
    }

    // use the list of color palettes to make the background colour handling object
    myBgPalette = new BgPalette(palettes);
  }

  public static String[] toStringArray(JSONArray array) {
    String[] arr = new String[array.size()];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = array.getString(i);
    }
    return arr;
  }

  void noteOn(int channel, int pitch, int velocity, long timestamp, String bus_name) {
    // println("Note On : Channel = " + channel + " Pitch = " + pitch + " Vel = "+
    // velocity);
  }

  void noteOff(int channel, int pitch, int velocity, long timestamp, String bus_name) {
    // println("Note Off : Channel = " + channel + " Pitch = " + pitch + " Vel = "+
    // velocity);
  }

  void controllerChange(int channel, int number, int value, long timestamp, String bus_name) {
    // println("Control Change: Channel = " + channel + " CC" + number + " Value =
    // "+value + " bus name = " + bus_name);

    if (channel == midiChannel) {
      try {
        Class[] cls = new Class[1];
        cls[0] = int.class;
        Method handler = this.getClass().getMethod("onCCChange" + number, cls);
        handler.invoke(this, value);
      } catch (Exception e) {
        println("Midi error in control change : number = " + number + ", value = " + value);
        e.printStackTrace();
      }
    }
  }

  // *****************************************
  // * Hotcues & deck specific settings
  // *****************************************

  // Deck volume faders
  void onCCChange100(int value) {
    deckA.setVolume(value);
  }

  void onCCChange101(int value) {
    deckB.setVolume(value);
  }

  void onCCChange102(int value) {
    deckC.setVolume(value);
  }

  void onCCChange103(int value) {
    deckD.setVolume(value);
  }

  // Deck play status
  void onCCChange110(int value) {
    if (value == 127) {
      deckA.setIsPlaying(true);
    } else {
      deckA.setIsPlaying(false);
    }
  }

  void onCCChange111(int value) {
    if (value == 127) {
      deckB.setIsPlaying(true);
    } else {
      deckB.setIsPlaying(false);
    }
  }

  void onCCChange112(int value) {
    if (value == 127) {
      deckC.setIsPlaying(true);
    } else {
      deckC.setIsPlaying(false);
    }
  }

  void onCCChange113(int value) {
    if (value == 127) {
      deckD.setIsPlaying(true);
    } else {
      deckD.setIsPlaying(false);
    }
  }

  // Hotcues for Decks
  void onCCChange1(int value) {
    deckA.readyHotcue(value);
  }

  void onCCChange2(int value) {
    deckB.readyHotcue(value);
  }

  void onCCChange3(int value) {
    deckC.readyHotcue(value);
  }

  void onCCChange4(int value) {
    deckD.readyHotcue(value);
  }

  // select hotcue packs
  void onCCChange56(int value) {
    if (value > 0) {
      deckA.setPack(value);
    }
  }

  void onCCChange57(int value) {
    if (value > 0) {
      deckB.setPack(value);
    }
  }

  void onCCChange58(int value) {
    if (value > 0) {
      deckC.setPack(value);
    }
  }

  void onCCChange59(int value) {
    if (value > 0) {
      deckD.setPack(value);
    }
  }

  // ********************************************
  // * respond to effects being used in Traktor *
  // ********************************************

  // Set the degree of blur based on the Traktor filter knob
  void onCCChange105(int value) {
    deckA.setFilter(value);
  }

  void onCCChange106(int value) {
    deckB.setFilter(value);
  }

  void onCCChange107(int value) {
    deckC.setFilter(value);
  }

  void onCCChange108(int value) {
    deckD.setFilter(value);
  }

  // Shader to visualise the delay(echo) effect
  void onCCChange64(int value) {
    if (value > 100) {
      delayOn = true;
    } else if (value == 0) {
      delayOn = false;
    }
  }

  // Shader to visualise the gate/slice/mash effect
  void onCCChange65(int value) {
    if (value > 100) {
      sliceOn = true;
    } else if (value == 0) {
      sliceOn = false;
    }
  }

  // ********************************************
  // * Visualiser settings *
  // ********************************************

  // Select the main visualiser
  void onCCChange21(int value) {
    visualisers.setVisualiser(value);
  }

  // Sends a value from the browse knob to the current visualiser
  void onCCChange45(int value) {
    visualisers.setKnob1(value);
  }

  // set the scaling for the fft analysis, which is it's sensitivity to volume
  void onCCChange48(int value) {
    visualisers.setScaling(value);
  }

  // Button1 for visualiser
  void onCCChange27(int value) {
    if (value > 100) {
      visualisers.toggleButton1();
    }
  }

  // Button2 for visualiser
  void onCCChange28(int value) {
    if (value > 100) {
      visualisers.toggleButton2();
    }
  }

  // Fader1 for visualiser
  void onCCChange52(int value) {
    visualisers.setFader1(value);
  }

  // Fader2 for visualiser
  void onCCChange53(int value) {
    visualisers.setFader2(value);
  }

  // Toggle the waveform visualiser
  void onCCChange26(int value) {
    if (value > 100) {
      waveformOn = !waveformOn;
    }
  }

  // set the scaling for the waveform visualiser, which is it's sensitivity to
  // volume
  void onCCChange49(int value) {
    visWaveform.scale(value);
  }

  // Toggle the kaleidoscope shader
  void onCCChange29(int value) {
    if (value > 100) {
      kaleidoscopeOn = !kaleidoscopeOn;
    }
  }

  // set the alpha value for the beat text
  void onCCChange50(int value) {
    beatWords.setAlpha(value);
  }

  // ************************************************
  // Background colour and word display settings
  // ************************************************

  // Detect a beat and change the background color on each beat,
  // as well as incrementing to the next word to display
  void onCCChange41(int value) {
    if (value > 120) {
      myBgPalette.incColor();
      beatWords.nextWord();
    }
  }

  // Toggle the beat sync (change background color on each beat)
  void onCCChange30(int value) {
    if (value > 100) {
      myBgPalette.toggle();
    }
  }

  // Toggle black or white background
  void onCCChange46(int value) {
    if (value > 100) {
      myBgPalette.toggleBlackOrWhite();
    }
  }

  // toggle the word display and set the list of words to use
  void onCCChange47(int value) {
    beatWords.setCurrentPack(value);
  }

  // *************************************
  // Post processing shader selection
  // *************************************

  // select the shader to use
  void onCCChange60(int value) {
    VisShaders.setShader(value);
  }

  // toggle the post processing shaders
  void onCCChange61(int value) {
    VisShaders.toggleVisShaders();
  }

  // change the first parameter of the post processing shader
  void onCCChange54(int value) {
    VisShaders.setX(value);
  }

  // change the second parameter of the post processing shader
  void onCCChange55(int value) {
    VisShaders.setY(value);
  }

  // ************************************************************************
  // And all the unassigned CC values, waiting for you to give them meaning
  // ************************************************************************

  void onCCChange0(int value) {
  }

  void onCCChange5(int value) {
  }

  void onCCChange6(int value) {
  }

  void onCCChange7(int value) {
  }

  void onCCChange8(int value) {
  }

  void onCCChange9(int value) {
  }

  void onCCChange10(int value) {
  }

  void onCCChange11(int value) {
  }

  void onCCChange12(int value) {
  }

  void onCCChange13(int value) {
  }

  void onCCChange14(int value) {
  }

  void onCCChange15(int value) {
  }

  void onCCChange16(int value) {
  }

  void onCCChange17(int value) {
  }

  void onCCChange18(int value) {
  }

  void onCCChange19(int value) {
  }

  void onCCChange20(int value) {
  }

  void onCCChange22(int value) {
  }

  void onCCChange23(int value) {
  }

  void onCCChange24(int value) {
  }

  void onCCChange25(int value) {
  }

  void onCCChange31(int value) {
  }

  void onCCChange32(int value) {
  }

  void onCCChange33(int value) {
  }

  void onCCChange34(int value) {
  }

  void onCCChange35(int value) {
  }

  void onCCChange36(int value) {
  }

  void onCCChange37(int value) {
  }

  void onCCChange38(int value) {
  }

  void onCCChange39(int value) {
  }

  void onCCChange40(int value) {
  }

  void onCCChange42(int value) {
  }

  void onCCChange43(int value) {
  }

  void onCCChange44(int value) {
  }

  void onCCChange51(int value) {
  }

  void onCCChange62(int value) {
  }

  void onCCChange63(int value) {
  }

  void onCCChange66(int value) {
  }

  void onCCChange67(int value) {
  }

  void onCCChange68(int value) {
  }

  void onCCChange69(int value) {
  }

  void onCCChange70(int value) {
  }

  void onCCChange71(int value) {
  }

  void onCCChange72(int value) {
  }

  void onCCChange73(int value) {
  }

  void onCCChange74(int value) {
  }

  void onCCChange75(int value) {
  }

  void onCCChange76(int value) {
  }

  void onCCChange77(int value) {
  }

  void onCCChange78(int value) {
  }

  void onCCChange79(int value) {
  }

  void onCCChange80(int value) {
  }

  void onCCChange81(int value) {
  }

  void onCCChange82(int value) {
  }

  void onCCChange83(int value) {
  }

  void onCCChange84(int value) {
  }

  void onCCChange85(int value) {
  }

  void onCCChange86(int value) {
  }

  void onCCChange87(int value) {
  }

  void onCCChange88(int value) {
  }

  void onCCChange89(int value) {
  }

  void onCCChange90(int value) {
  }

  void onCCChange91(int value) {
  }

  void onCCChange92(int value) {
  }

  void onCCChange93(int value) {
  }

  void onCCChange94(int value) {
  }

  void onCCChange95(int value) {
  }

  void onCCChange96(int value) {
  }

  void onCCChange97(int value) {
  }

  void onCCChange98(int value) {
  }

  void onCCChange99(int value) {
  }

  void onCCChange104(int value) {
  }

  void onCCChange109(int value) {
  }

  void onCCChange114(int value) {
  }

  void onCCChange115(int value) {
  }

  void onCCChange116(int value) {
  }

  void onCCChange117(int value) {
  }

  void onCCChange118(int value) {
  }

  void onCCChange119(int value) {
  }

  void onCCChange120(int value) {
  }

  void onCCChange121(int value) {
  }

  void onCCChange122(int value) {
  }

  void onCCChange123(int value) {
  }

  void onCCChange124(int value) {
  }

  void onCCChange125(int value) {
  }

  void onCCChange126(int value) {
  }

  void onCCChange127(int value) {
  }

  void onCCChange128(int value) {
  }

  void onCCChange129(int value) {
  }

  void onCCChange130(int value) {
  }

  void onCCChange131(int value) {
  }

  void onCCChange132(int value) {
  }

  void onCCChange133(int value) {
  }

  void onCCChange134(int value) {
  }

  void onCCChange135(int value) {
  }

  void onCCChange136(int value) {
  }

  void onCCChange137(int value) {
  }

  void onCCChange138(int value) {
  }

  void onCCChange139(int value) {
  }

  void onCCChange140(int value) {
  }

  void onCCChange141(int value) {
  }

  void onCCChange142(int value) {
  }

  void onCCChange143(int value) {
  }

  void onCCChange144(int value) {
  }

  void onCCChange145(int value) {
  }

  void onCCChange146(int value) {
  }

  void onCCChange147(int value) {
  }

  void onCCChange148(int value) {
  }

  void onCCChange149(int value) {
  }

  void onCCChange150(int value) {
  }

  void onCCChange151(int value) {
  }

  void onCCChange152(int value) {
  }

  void onCCChange153(int value) {
  }

  void onCCChange154(int value) {
  }

  void onCCChange155(int value) {
  }

  void onCCChange156(int value) {
  }

  void onCCChange157(int value) {
  }

  void onCCChange158(int value) {
  }

  void onCCChange159(int value) {
  }

  void onCCChange160(int value) {
  }

  void onCCChange161(int value) {
  }

  void onCCChange162(int value) {
  }

  void onCCChange163(int value) {
  }

  void onCCChange164(int value) {
  }

  void onCCChange165(int value) {
  }

  void onCCChange166(int value) {
  }

  void onCCChange167(int value) {
  }

  void onCCChange168(int value) {
  }

  void onCCChange169(int value) {
  }

  void onCCChange170(int value) {
  }

  void onCCChange171(int value) {
  }

  void onCCChange172(int value) {
  }

  void onCCChange173(int value) {
  }

  void onCCChange174(int value) {
  }

  void onCCChange175(int value) {
  }

  void onCCChange176(int value) {
  }

  void onCCChange177(int value) {
  }

  void onCCChange178(int value) {
  }

  void onCCChange179(int value) {
  }

  void onCCChange180(int value) {
  }

  void onCCChange181(int value) {
  }

  void onCCChange182(int value) {
  }

  void onCCChange183(int value) {
  }

  void onCCChange184(int value) {
  }

  void onCCChange185(int value) {
  }

  void onCCChange186(int value) {
  }

  void onCCChange187(int value) {
  }

  void onCCChange188(int value) {
  }

  void onCCChange189(int value) {
  }

  void onCCChange190(int value) {
  }

  void onCCChange191(int value) {
  }

  void onCCChange192(int value) {
  }

  void onCCChange193(int value) {
  }

  void onCCChange194(int value) {
  }

  void onCCChange195(int value) {
  }

  void onCCChange196(int value) {
  }

  void onCCChange197(int value) {
  }

  void onCCChange198(int value) {
  }

  void onCCChange199(int value) {
  }

  void onCCChange200(int value) {
  }

  void onCCChange201(int value) {
  }

  void onCCChange202(int value) {
  }

  void onCCChange203(int value) {
  }

  void onCCChange204(int value) {
  }

  void onCCChange205(int value) {
  }

  void onCCChange206(int value) {
  }

  void onCCChange207(int value) {
  }

  void onCCChange208(int value) {
  }

  void onCCChange209(int value) {
  }

  void onCCChange210(int value) {
  }

  void onCCChange211(int value) {
  }

  void onCCChange212(int value) {
  }

  void onCCChange213(int value) {
  }

  void onCCChange214(int value) {
  }

  void onCCChange215(int value) {
  }

  void onCCChange216(int value) {
  }

  void onCCChange217(int value) {
  }

  void onCCChange218(int value) {
  }

  void onCCChange219(int value) {
  }

  void onCCChange220(int value) {
  }

  void onCCChange221(int value) {
  }

  void onCCChange222(int value) {
  }

  void onCCChange223(int value) {
  }

  void onCCChange224(int value) {
  }

  void onCCChange225(int value) {
  }

  void onCCChange226(int value) {
  }

  void onCCChange227(int value) {
  }

  void onCCChange228(int value) {
  }

  void onCCChange229(int value) {
  }

  void onCCChange230(int value) {
  }

  void onCCChange231(int value) {
  }

  void onCCChange232(int value) {
  }

  void onCCChange233(int value) {
  }

  void onCCChange234(int value) {
  }

  void onCCChange235(int value) {
  }

  void onCCChange236(int value) {
  }

  void onCCChange237(int value) {
  }

  void onCCChange238(int value) {
  }

  void onCCChange239(int value) {
  }

  void onCCChange240(int value) {
  }

  void onCCChange241(int value) {
  }

  void onCCChange242(int value) {
  }

  void onCCChange243(int value) {
  }

  void onCCChange244(int value) {
  }

  void onCCChange245(int value) {
  }

  void onCCChange246(int value) {
  }

  void onCCChange247(int value) {
  }

  void onCCChange248(int value) {
  }

  void onCCChange249(int value) {
  }

  void onCCChange250(int value) {
  }

  void onCCChange251(int value) {
  }

  void onCCChange252(int value) {
  }

  void onCCChange253(int value) {
  }

  void onCCChange254(int value) {
  }

  void onCCChange255(int value) {
  }

  // This shader is used to represent the filter control in Traktor.
  // If any deck is using the filter, has a volume over a certain threshold and is
  // playing
  // then the blur is engaged. Its intesity is linked to the level of the fliter
  // knob.

  class BlurShader {
    float intensity;
    boolean on;
    PShader shade;

    BlurShader() {
      intensity = 0;
      on = true;

      shade = loadShader("blurFrag.glsl", "blurVert.glsl");
      shade.set("blurDegree", (float) 0.0);
    }

    void draw() {
      if (mixerState.filterIntensity > 0) {
        shade.set("blurDegree", mixerState.filterIntensity);
        filter(shade);
      }
    }

    void intensity(float v) {
      intensity = v;
    }
  }

  // A class to store a set of shaders used as "post processing" effects
  // they may have different parameters, these can be set using two faders on the
  // Maschine Jam
  class PostProcessingShaders {
    ArrayList<VisShader> VisShaders;
    VisShader currentVisShader;
    boolean VisShadersOn;
    int x;
    int y;

    PostProcessingShaders() {
      VisShaders = new ArrayList<VisShader>();
      VisShaders.add(shaderBrcosa = new ShaderBrcosa());
      VisShaders.add(shaderHue = new ShaderHue());
      VisShaders.add(shaderPixelate = new ShaderPixelate());
      VisShaders.add(shaderChannels = new ShaderChannels());
      VisShaders.add(shaderThreshold = new ShaderThreshold());
      VisShaders.add(shaderNeon = new ShaderNeon());
      VisShaders.add(shaderDeform = new ShaderDeform());
      VisShaders.add(shaderPixelrolls = new ShaderPixelRolls());
      VisShaders.add(shaderModcolor = new ShaderModcolor());
      VisShaders.add(shaderHalftone = new ShaderHalftone());
      VisShaders.add(shaderInvert = new ShaderInvert());

      currentVisShader = VisShaders.get(0);
      x = width / 2;
      y = height / 2;
      VisShadersOn = false;
    }

    void draw() {
      if (VisShadersOn) {
        currentVisShader.draw();
      }
    }

    void setShader(int value) {
      if (value >= VisShaders.size()) {
        value = 0;
      }
      currentVisShader = VisShaders.get(value);
    }

    String getCurrentShaderInfo() {
      return currentVisShader.name + " X = " + x + " Y = " + y;
    }

    void setX(int value) {
      x = round(map(value, 0, 127, 0, width));
      currentVisShader.setX(x);
    }

    void setY(int value) {
      y = round(map(value, 0, 127, 0, height));
      currentVisShader.setY(y);
    }

    void toggleVisShaders() {
      VisShadersOn = !VisShadersOn;
    }
  }

  abstract class VisShader {
    String name;
    String filename;
    boolean on;
    PShader shade;
    int x;
    int y;

    VisShader(String n, String f) {
      name = n;
      filename = f;
      shade = loadShader(filename);
      x = width / 2;
      y = height / 2;
    }

    void draw() {
      filter(shade);
    }

    void setX(int i) {
      x = i;
    }

    void setY(int i) {
      y = i;
    }
  }

  class ShaderBrcosa extends VisShader {
    ShaderBrcosa() {
      super("brcosa", "brcosa.glsl");
      x = width / 3;
      y = 10;// height/3;
      shade.set("brightness", (float) 1.0);
    }

    void draw() {
      shade.set("contrast", map(x, 0, width, -5, 5));
      shade.set("saturation", map(y, 0, height, -5, 5));
      super.draw();
    }
  }

  class ShaderHue extends VisShader {
    ShaderHue() {
      super("hue", "hue.glsl");
    }

    void draw() {
      shade.set("hue", map(x, 0, width, 0, TWO_PI));
      super.draw();
    }
  }

  class ShaderPixelate extends VisShader {
    ShaderPixelate() {
      super("pixelate", "pixelate.glsl");
    }

    void draw() {
      shade.set("pixels", (float) 0.1 * x, (float) 0.1 * x);
      super.draw();
    }
  }

  class ShaderChannels extends VisShader {
    ShaderChannels() {
      super("channels", "channels.glsl");
    }

    void draw() {
      shade.set("rbias", (float) 0.0, (float) 0.0);
      shade.set("gbias", map((float) y, (float) 0, (float) height, (float) -0.2, (float) 0.2), (float) 0.0);
      shade.set("bbias", (float) 0.0, (float) 0.0);
      shade.set("rmult", map((float) x, (float) 0, (float) width, (float) 0.8, (float) 1.5), (float) 1.0);
      shade.set("gmult", (float) 1.0, (float) 1.0);
      shade.set("bmult", (float) 1.0, (float) 1.0);
      super.draw();
    }
  }

  class ShaderThreshold extends VisShader {
    ShaderThreshold() {
      super("threshold", "threshold.glsl");
    }

    void draw() {
      shade.set("threshold", map(x, 0, width, 0, 1));
      super.draw();
    }
  }

  class ShaderNeon extends VisShader {
    ShaderNeon() {
      super("neon", "neon.glsl");
    }

    void draw() {
      shade.set("brt", map((float) x, (float) 0, (float) width, (float) 0, (float) 0.5));
      shade.set("rad", (int) map(y, 0, height, 0, 3));
      super.draw();
    }
  }

  class ShaderDeform extends VisShader {
    ShaderDeform() {
      super("deform", "deform.glsl");
    }

    void draw() {
      shade.set("time", (float) millis() / (float) 1000.0);
      shade.set("mouse", (float) x / width, (float) y / height);
      shade.set("turns", map(sin((float) 0.01 * frameCount), (float) -1, (float) 1, (float) 2.0, (float) 10.0));
      super.draw();
    }
  }

  class ShaderPixelRolls extends VisShader {
    ShaderPixelRolls() {
      super("pixelRolls", "pixelrolls.glsl");
    }

    void draw() {
      shade.set("time", (float) millis() / (float) 1000.0);
      shade.set("pixels", x / 5, (float) 150.0);
      shade.set("rollRate", map(y, 0, height, (float) -0.5, (float) 0.5));
      shade.set("rollAmount", (float) 0.25);
      super.draw();
    }
  }

  class ShaderModcolor extends VisShader {
    ShaderModcolor() {
      super("modcolor", "modcolor.glsl");
    }

    void draw() {
      shade.set("modr", map(x, 0, width, 0, (float) 0.5));
      shade.set("modg", (float) 0.3);
      shade.set("modb", map(y, 0, height, 0, (float) 0.5));
      super.draw();
    }
  }

  class ShaderHalftone extends VisShader {
    ShaderHalftone() {
      super("halftone", "halftone.glsl");
    }

    void draw() {
      shade.set("pixelsPerRow", (int) map(x, 0, width, 2, 100));
      super.draw();
    }
  }

  class ShaderInvert extends VisShader {
    ShaderInvert() {
      super("inversion", "invert.glsl");
    }

    void draw() {
      super.draw();
    }
  }

  class ShaderVHSGlitch extends VisShader {
    ShaderVHSGlitch() {
      super("VHS Glitch", "vhs_glitch.glsl");
      shade.set("iResolution", (float) width, (float) height);
    }

    void draw() {
      shade.set("iGlobalTime", millis() / (float) 1000.0);
      super.draw();
    }
  }

  class ShaderSobel extends VisShader {
    ShaderSobel() {
      super("Sobel", "sobel.glsl");
      shade.set("iResolution", (float) width, (float) height);
    }

    void draw() {
      super.draw();
    }
  }

  class ShaderKaleidoscope extends VisShader {
    int viewAngleMod;
    float rot;

    ShaderKaleidoscope() {
      super("Kaleidoscope", "kaleidoscope.glsl");
      shade.set("rotation", 0);
      shade.set("viewAngle", TWO_PI / 10);
    }

    void draw() {
      shader(shade);
    }
  }

  class BgPalette {
    ArrayList<int[]> palettes = new ArrayList<int[]>();
    int[] bgColors;
    int bgColor;
    int bgColorIndex;
    int BgPaletteIndex;
    boolean bgBeatSync;
    boolean blackOrWhite;

    BgPalette(ArrayList<int[]> p) {

      palettes = p;

      BgPaletteIndex = 0;
      bgColors = palettes.get(BgPaletteIndex);
      bgColor = bgColors[1];
      bgColorIndex = 0;
      bgBeatSync = false;
      blackOrWhite = false;
    }

    void incPalette() {
      BgPaletteIndex++;
      if (BgPaletteIndex > palettes.size() - 1) {
        BgPaletteIndex = 0;
      }
      bgColors = palettes.get(BgPaletteIndex);
    }

    int incColor() {
      bgColorIndex++;
      if (bgColorIndex > bgColors.length - 1) {
        bgColorIndex = 0;
      }
      bgColor = bgColors[bgColorIndex];
      return bgColor;
    }

    void toggle() {
      bgBeatSync = !bgBeatSync;
      if (bgBeatSync) {
        incPalette();
      }
    }

    void toggleBlackOrWhite() {
      blackOrWhite = !blackOrWhite;
    }

    boolean getBlackOrWhite() {
      return blackOrWhite;
    }

    void drawBg() {
      if (bgBeatSync) {
        background(bgColor);
      } else {
        if (blackOrWhite) {
          background(255);
        } else {
          background(0);
        }
      }
    }
  }

  // A class for deck specific settings, used to store things like volume,
  // play/stop status and to
  // link a specific hotcue pack to the deck
  class DeckSettings {
    String name;
    int hotcuePack;
    int hotcueIndex;
    int faderValue;
    float filterValue;
    boolean blurOn;
    boolean playing;

    DeckSettings(String n) {
      name = n;
      hotcuePack = 0;
      hotcueIndex = 0;
      faderValue = 0;
      filterValue = 0;
      playing = false;
      blurOn = false;
    }

    // sets the hotcue pack associated with the deck
    void setPack(int value) {
      // check to see if there is a matching pack loaded
      if (value > hotcuePacks.size()) {
        println("Warning - Pack number " + value + " does not exist. /n Setting to pack 0 ");
        hotcuePack = 0;
        hotcueIndex = 0;
      } else {
        hotcuePack = value - 1;
        hotcueIndex = 0;
      }
    }

    // cues up the next hotcue to draw
    void readyHotcue(int value) {
      if (faderValue > 25) {
        if (value == 15) {
          hotcueIndex = 0;
        } else if (value == 31) {
          hotcueIndex = 1;
        } else if (value == 47) {
          hotcueIndex = 2;
        } else if (value == 63) {
          hotcueIndex = 3;
        } else if (value == 79) {
          hotcueIndex = 4;
        } else if (value == 95) {
          hotcueIndex = 5;
        } else if (value == 111) {
          hotcueIndex = 6;
        } else if (value == 127) {
          hotcueIndex = 7;
        }
        hotcuePacks.readyHotcue(hotcuePack, hotcueIndex);
      }
    }

    void setVolume(int v) {
      faderValue = v;
      isBlurOn();
    }

    int getVolume() {
      return faderValue;
    }

    void setIsPlaying(boolean p) {
      playing = p;
    }

    boolean isPlaying() {
      return playing;
    }

    void setFilter(int v) {
      filterValue = abs(map(v, 1, 127, -8, 8));
      if (filterValue < 0.15) {
        filterValue = 0;
      }
      isBlurOn();
    }

    float getFilter() {
      return filterValue;
    }

    String getStatus() {
      String s;
      if (playing) {
        s = "Playing";
      } else {
        s = "Paused";
      }
      String status = "Deck " + name + " is " + s + " fader = " + faderValue + " filter = " + filterValue;
      return status;
    }

    // if the deck is playing, the filter is engaged and the volume is more than a
    // certain value
    // then toggle the blur indicator on
    void isBlurOn() {
      if (playing && faderValue > 30 && filterValue > 0) {
        blurOn = true;
      } else {
        blurOn = false;
      }
      mixerState.checkForBlurOn();
    }
  }

  // A class that stores the state of the mixer and effects
  // and that decides what to display as a result of that
  class MixerState {
    DeckSettings[] decks;
    float filterIntensity; // stores 0 if the blur is off, or the
    // highest filter value across the 4 decks if it is on

    MixerState() {
      decks = new DeckSettings[4];
      decks[0] = deckA;
      decks[1] = deckB;
      decks[2] = deckC;
      decks[3] = deckD;
      filterIntensity = 0;
    }

    void checkForBlurOn() {
      if ((deckA.blurOn) || (deckB.blurOn) || (deckC.blurOn) || (deckD.blurOn)) {

        filterIntensity = 0;
        for (DeckSettings d : decks) {
          if (d.filterValue > filterIntensity) {
            filterIntensity = d.filterValue;
          }
        }
      } else {
        filterIntensity = 0;
      }
    }
  }

  // A class that stores a list of hotcue packs
  // Each pack is an array 8 images and is linked to the deck hotcues.
  // When a hotcue is triggered the appropriate image is displayed.

  class HotcuePacks {
    ArrayList<PImage[]> hotcues;
    int hotcueFrames;
    int hotcueFrameCount;
    boolean hotcueReady;
    int currentPack;
    int currentHotcue;
    PImage currentImage;
    int alphaDec;
    int alpha;
    int imageX;
    int imageY;

    HotcuePacks() {
      hotcues = loadHotcuePacks();
      hotcueFrames = 15; // How many frames to draw the hotcue for
      hotcueFrameCount = 0;
      alpha = 255;
      alphaDec = alpha / hotcueFrames;

      currentPack = 0;
      currentHotcue = 0;
      readyHotcue(currentPack, currentHotcue);
      hotcueReady = false;
      imageX = width / 2;
      imageY = height / 2;
    }

    void draw() {
      if (hotcueReady) {
        imageMode(CENTER);
        tint(255, alpha);
        image(currentImage, imageX, imageY, width, height);
        imageMode(CORNER);
        hotcueFrameCount++;
        if (hotcueFrameCount > hotcueFrames) {
          hotcueFrameCount = 0;
          hotcueReady = false;
        }
        alpha = alpha - alphaDec;
        noTint();
      }
    }

    void readyHotcue(int p, int hc) {
      currentPack = p;
      currentHotcue = hc;
      hotcueFrameCount = 0;
      alpha = 255;

      currentImage = hotcues.get(currentPack)[hc];

      hotcueReady = true;
    }

    int size() {
      return hotcues.size();
    }

    ArrayList<PImage[]> loadHotcuePacks() {
      ArrayList<PImage[]> hc = new ArrayList();

      // Get the sketch's data directory
      File dataDir = new File(resourcesDir(""));

      FilenameFilter hotCueFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.startsWith("hotcue");
        }
      };

      // list all the hotcue packs in the directory
      File[] hotcueDirs = dataDir.listFiles(hotCueFilter);

      if (hotcueDirs.length < 1) {
        println("No hotcue directory found");
        println("make sure that you have at least one directory,");
        println("inside the sketch's data directory, named \"hotcue_pack<x>\"");
        exit();
      }

      for (File d : hotcueDirs) {
        PImage hotcueImages[] = getHotcueImages(d);
        if (hotcueImages != null) {
          hc.add(hotcueImages);
        }
      }
      return hc;
    }

    // Looks in the data directory for hotcue packs
    // and returns an array of images for each one that it finds.
    PImage[] getHotcueImages(File dir) {
      PImage hotcueImages[] = new PImage[8];

      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; i++) {
        String path = files[i].getAbsolutePath();

        if (path.toLowerCase().endsWith(".png")) {
          PImage image = loadImage(path);
          image.resize(width, 0);
          hotcueImages[i] = image;
        }
      }
      if (hotcueImages.length < 8) {
        println("Warning - Less than 8 images found in " + dir);
        return null;
      } else {
        return (hotcueImages);
      }
    }
  }

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

  // Generic visualiser class, specific visualisers are lower down
  // A visualiser has the following controls mapped onto the Maschine Jam and
  // Traktor transport
  // each specific visualiser decides which of these to make use of and how to
  // respond to them
  // 1) Browse knob, scrolls through range of 0-127
  // 2) Button 1, toggle
  // 3) Button 2, toggle
  // 4) Fader 1, slider with range 0-127
  // 5) Fader 2, slider with range 0-127

  // Each visualiser chooses how to interpret the music, normally using an FFT for
  // frequency analysis. Oblivion and Sprocket basically sweep through the whole
  // spectrum. We may want to limit a specific visualiser (such as the Candywarp
  // one)
  // to few, or more specific frequency bands.
  // To help with this, below is the code needed to pick out the frequency range
  // used by Traktor's
  // Z-ISO EQ.
  //
  // float[] traktorEQ;
  //
  // traktorEQ[0] = fft.calcAvg(20.0, 90.0); // Bass response
  // traktorEQ[1] = fft.calcAvg(90.0, 1470.0); // Mid response
  // traktorEQ[2] = fft.calcAvg(1470.0, 18000.0); // High response

  abstract class Visualiser {
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

    // init FFT analysis
    FFT fft;
    float smoothing = (float) 0.60;
    float[] fftSmooth;
    int avgSize;

    Visualiser(String n) {
      name = n;
      pg = createGraphics(width, height, P3D);

      halfWidth = width / 2;
      halfHeight = height / 2;

      // Each visualiser can has four parameters that can be set from the controller
      // two buttons and two sliders. It is up to the specific visualiser to decide
      // what to do with these
      button1 = false;
      button2 = false;
      fader1 = 0;
      fader2 = 0;
      knob1 = 0;
    }

    void draw() {
      image(pg, 0, 0);
    }

    void toggleButton1() {
      button1 = !button1;
    }

    void toggleButton2() {
      button2 = !button2;
    }

    void setFader1(int v) {
      fader1 = v;
    }

    void setFader2(int v) {
      fader2 = v;
    }

    void setKnob1(int v) {
      knob1 = v;
    }

    void setScaling(float s) {
      scaling = map(s, 0, 127, 0, 20);
    }

    float dB(float x) {
      if (x == 0) {
        return 0;
      } else {
        return 10 * (float) Math.log10(x);
      }
    }
  }

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

    VisOblivion(String n) {
      super(n);

      scaling = 5;

      loadGradients();

      // Initialise the fft analysis
      fft = new FFT(input.bufferSize(), input.sampleRate());

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
      pg.translate(width / 2, height / 2);
      rotationAngle += rotationSpeed;
      pg.rotate(rotationAngle);
      pg.translate(-width / 2, -height / 2);

      for (int i = 0; i < previousValues.length; i++) {

        float startAngle = (i * PI / 100);
        float deltaAngle = PI * 2 / count;
        float value = previousValues[i];
        float percent = (float) i / previousValues.length;

        int col = activeGradient[min((int) (activeGradient.length * percent), activeGradient.length)];
        pg.fill(col, opacity);

        float s = max(2, value * (float) 0.5f * positionRadius / 360f);

        float distance = positionRadius - (percent * positionRadius * value / 40);
        distance = max(-positionRadius, distance);

        for (int j = 0; j < count; j++) {
          float a = startAngle + deltaAngle * j;
          if (j % 2 == 0) {
            a -= startAngle * 2;
          }
          PVector prev = prevPos[i][j];
          PVector curr = new PVector(width / 2 + cos(a) * distance, height / 2 + sin(a) * distance);

          // Draw an ellipse, makes the visualisation more dramatic
          if (button2) {
            pg.ellipse(pg.width / 2 + cos(a) * distance, pg.height / 2 + sin(a) * distance, s, s);
          }

          if (prev != null) {

            float dx = prev.x - curr.x;
            float dy = prev.y - curr.y;
            float d = sqrt(dx * dx + dy * dy);

            pg.pushMatrix();
            pg.translate(curr.x, curr.y);
            pg.rotate(atan2(dy, dx));

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
      fft.forward(input.mix);

      int size = 10;

      for (int n = 0; n < fft.specSize() - size; n += size) {
        float percent = n / (fft.specSize() - size);
        float avg = 0;
        for (int i = n; i < n + size; i++) {
          avg += fft.getBand(n);
        }
        avg = avg * lerp(4, 8, percent) * scaling / size;

        float previous = previousValues[n / size];
        previous *= (float) 0.9;
        previous = max(avg, previous);

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
      File dir = new File(resourcesDir("") + "//gradients//");

      File[] files = dir.listFiles();
      boolean gradientReverse = false;
      for (int i = 0; i < files.length; i++) {
        String path = files[i].getAbsolutePath();

        // check the file type and work with jpg/png files
        if (path.toLowerCase().endsWith(".png")) {
          PImage image = loadImage(path);

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
      rotationSpeed = map(v, 0, 127, (float) -0.1, (float) 0.1);
      if ((rotationSpeed > (float) -0.01) && (rotationSpeed < (float) 0.01)) {
        rotationSpeed = (float) 0.0;
      }
    }

    void setFader2(int v) {
      opacity = round(map(v, 0, 127, 0, 255));
    }

    void setScaling(float s) {
      scaling = map(s, 0, 127, 0, 20);
    }
  }

  // ************************************************************************************************
  // Waveform visualiser class
  //
  // Draws a simple osciliscope type wavform
  // ************************************************************************************************
  class VisWaveform extends Visualiser {
    int scale = 500;

    VisWaveform(String n) {
      super(n);
    }

    void draw() {

      pg.beginDraw();
      pg.clear();
      pg.strokeWeight(2);

      if (myBgPalette.getBlackOrWhite()) {
        pg.stroke(10);
      } else {
        pg.stroke(250);
      }

      pg.pushMatrix();
      pg.translate(0, halfHeight);

      float distance = (float) width / input.bufferSize();
      for (int i = 0; i < input.bufferSize() - 1; i++) {
        float x1 = distance * i;
        float x2 = distance * (i + 1);

        pg.line(x1, input.left.get(i) * scale, x2, input.left.get(i + 1) * scale);
      }

      pg.popMatrix();
      pg.endDraw();
      super.draw();
    }

    void scale(int v) {
      scale = round(map(v, 0, 127, 100, 1000));
    }
  }

  // ************************************************************************************************
  // visSprocket Visualiser class
  // Browse Knob1 -
  // Button1 - Background retention on/off
  // Button2 - Color mode (random or linear)
  // Fader1 - Rotation speed
  // Fader2 - Stroke colour
  // ************************************************************************************************
  class VisSprocket extends Visualiser {

    int specSize;
    float[] angle, x, y;
    float f, b, density;
    int outlineColour = 10;

    VisSprocket(String n) {
      super(n);
      fft = new FFT(input.bufferSize(), input.sampleRate());
      specSize = fft.specSize();

      y = new float[specSize];
      x = new float[specSize];
      angle = new float[specSize];
      density = 1;
      fader1 = 800;
    }

    void draw() {

      fft.forward(input.mix);

      pg.beginDraw();

      if (!button1) {
        pg.clear();
      }

      pg.push();
      if (button2) {
        pg.colorMode(RGB);
      } else {
        pg.colorMode(HSB);
      }

      pg.translate(pg.width / 2, pg.height / 2);

      for (int i = 0; i < specSize; i++) {
        if (button2) {
          pg.fill(random(255), random(255), random(255), 255);
        } else {
          pg.fill(i, 150, 150, 150);
        }

        f = fft.getFreq(i);
        b = fft.getBand(i);

        pg.stroke(outlineColour);
        y[i] = y[i] + b / 10;
        x[i] = x[i] + f / 10;
        angle[i] = angle[i] + f / (fader1 + 1);

        pg.rotateX(sin(angle[i] / 2) / density);
        pg.rotateY(cos(angle[i] / 2) / density);

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
      fader1 = round(map(v, 0, 127, 80, 800));
    }

    void setFader2(int v) {
      outlineColour = round(map(v, 0, 127, 0, 255));
    }

    void setScaling(float s) {
      scaling = map(s, 0, 127, (float) 0.5, 10);
    }
  }

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

    PShader shade;
    float cycle = (float) 0.2;
    float warp = (float) 2.5;
    float scale = (float) 84.0;

    int fftIndex = 1;
    int prevKnobValue = 0;

    VisCandyWarp(String n) {
      super(n);

      shade = loadShader("Candywarp.glsl");

      // settings that are fixed in this visualisation
      shade.set("iResolution", (float) width, (float) height);
      shade.set("thickness", (float) 0.1); // Default : 0.1 Min : 0.5 Max : 1.0
      shade.set("loops", (float) 61.0); // Default : 61.0 Min : 10.0 Max : 100.0
      shade.set("tint", (float) 0.1); // Default : 0.1 Min : -0.5 Max : 0.5
      shade.set("rate", (float) 1.3); // Default : 1.3 Min : -3.0 Max : 3.0
      shade.set("hue", (float) 0.33); // Default : 0.33 Min : -0.5 Max : 0.5

      // settings that vary in this visualisation
      shade.set("time", millis() / (float) 1000.0);
      shade.set("cycle", cycle); // Default : 0.4 Min : 0.01 Max : 0.99
      shade.set("warp", warp); // Default : 2.5 Min : -5.0 Max : 5.0
      shade.set("scale", scale); // Default : 84.0 Min : 10.0 Max : 100.0

      // set up fft analysis
      initAnalysis();
    }

    void draw() {

      analyse();

      pg.beginDraw();

      shade.set("time", millis() / (float) 1000.0);
      shade.set("warp", warp);
      shade.set("cycle", cycle);

      scale = map(fftSmooth[fftIndex], 0, 18, (float) 20.0, (float) 100.0); // use a specific frequency band to modulate
                                                                            // the shader's scale attribute
      shade.set("scale", scale);

      pg.filter(shade);

      pg.endDraw();
      super.draw();
    }

    void initAnalysis() {

      fft = new FFT(input.bufferSize(), input.sampleRate());
      fft.logAverages(11, 1);

      avgSize = fft.avgSize();
      fftSmooth = new float[avgSize];
    }

    void analyse() {
      final float noiseFloor = 0; // -10; // Minimum sound level that we respond to

      fft.forward(input.mix);

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
      cycle = map(v, 0, 127, (float) 0.01, (float) 0.4);
    }

    void setFader2(int v) {
      warp = map(v, 0, 127, (float) -5.0, (float) 5.0);
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

  class WordPacks {
    ArrayList<WordPack> words;
    int currentWordPack;
    int wordCount;
    boolean wordsOn;
    int wordColor;
    int alpha;

    WordPacks() {
      words = new ArrayList<WordPack>();
      currentWordPack = 0;
      wordsOn = false;
      wordCount = 0;
      alpha = 150;
      wordColor = color(0);
    }

    void addWords(String[] wp) {
      words.add(new WordPack(wp));
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
      alpha = round(map(a, 0, 127, 0, 255));
    }
  }

  class WordPack {
    String[] words;
    int currentWord;
    int wordCount;

    WordPack(String[] wp) {
      words = wp;
      wordCount = words.length - 1;
      currentWord = 0;
    }

    void display(int a) {
      textAlign(CENTER, CENTER);
      if (myBgPalette.getBlackOrWhite()) {
        fill(0, a);
      } else {
        fill(255, a);
      }
      textFont(font, 200);

      text(words[currentWord], width / 2, height / 2);
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

  public static void main(String[] args) {
    PApplet.main("com.bouzou.packages.giantspacerobotvisualizer.GiantSpaceRobotVisualizer");
  }

  private static String resourcesDir(String path) {
    return "src/main/resources/" + path;
  }
}
