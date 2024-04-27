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
import processing.core.*;
import processing.data.*;
import processing.opengl.PShader;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.bouzou.packages.giantspacerobotvisualizer.PostProcessingShaders.*;

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
    File jsonFile = new File(resourcesDir("config.json"));
    JSONObject j = loadJSONObject(jsonFile);

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

    // set up a list of shaders used for post processing effects
    VisShaders = new PostProcessingShaders(this);
    
    // Add a kaleidoscope effect to the entire display
    shaderKaleidoscope = VisShaders.new ShaderKaleidoscope(this);

    // Shaders used to represet effects used in Traktor
    shaderVhs_glitch = VisShaders.new ShaderVHSGlitch(this); // represents slicer, masher and gater
    shaderSobel = VisShaders.new ShaderSobel(this); // reresents echo
    shaderBlur = new BlurShader(); // represents filter (or deck FX)

    // Set up visualisers
    visWaveform = new VisWaveform(this, "Waveform");

    visOblivion = new VisOblivion(this, "Oblivion");
    visSprocket = new VisSprocket(this, "Sprocket");
    visCandyWarp = new VisCandyWarp(this, "CandyWarp");

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
    File jsonFile = new File(resourcesDir("config.json"));
    json = loadJSONObject(jsonFile);

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
    beatWords = new WordPacks(this, myBgPalette, font);

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
    println("Note On : Channel = " + channel + " Pitch = " + pitch + " Vel = "+
    velocity);
  }

  void noteOff(int channel, int pitch, int velocity, long timestamp, String bus_name) {
    println("Note Off : Channel = " + channel + " Pitch = " + pitch + " Vel = "+
    velocity);
  }

  void controllerChange(int channel, int number, int value, long timestamp, String bus_name) {
    println("Control Change: Channel = " + channel + " CC" + number + " Value = " + value + " bus name = " + bus_name);

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


  public static void main(String[] args) {
    PApplet.main("com.bouzou.packages.giantspacerobotvisualizer.GiantSpaceRobotVisualizer");
  }

  static String resourcesDir(String path) {
    return "src/main/resources/" + path;
  }

  public PShader loadShader(java.lang.String fragFilename) {
    // Call super impl with resource path
    return super.loadShader(resourcesDir(fragFilename));
  }

  public PFont loadFont(java.lang.String filename) {
    // Call super impl with resource path
    return super.loadFont(resourcesDir(filename));
  }
}
