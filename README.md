# GiantSpaceRobot Traktor Visualiser

## Overview

**GiantSpaceRobot Traktor Visualiser** is a professional real-time audio-visual performance system specifically designed for DJ environments. This sophisticated visualizer creates stunning visual effects that respond to both audio signals and MIDI control messages from Traktor DJ software and Native Instruments' Maschine Jam controller.

**🎯 Key Features:**
- **Real-time audio-reactive visualizations** with FFT analysis and beat detection
- **Professional MIDI integration** with extensive Traktor and Maschine Jam mapping  
- **Multiple visualizer modes** (Oblivion, Sprocket, Candywarp, Waveform)
- **Advanced post-processing shaders** with GPU-accelerated effects
- **Hot cue image triggering** system for live performance
- **SPOUT integration** for projection mapping workflows
- **Multi-monitor support** for professional installations

## Recent Development Updates

This project has recently undergone significant modernization:
- **🔄 Migrated from Processing sketch (.pde) to proper Java Gradle project**
- **📦 Modular architecture** with improved code organization
- **🎛️ Enhanced controller integration** and MIDI handling
- **🔧 Modern build system** with Gradle for easier development and deployment

## Technology Stack

- **Language:** Java 17 with Processing framework
- **Build System:** Gradle
- **Audio Processing:** Minim library
- **MIDI Communication:** TheMidiBus library  
- **Graphics Sharing:** Spout library (Windows)
- **Shaders:** GLSL for real-time effects

⚠️ **Setup Complexity Notice:** This is a professional-grade tool requiring familiarity with Traktor, MIDI mappings, and development environments. Installation involves multiple software components and hardware configuration.

# Installation & Setup

## Prerequisites

- **Java 17+** (required for Gradle build)
- **Traktor 3.4.0+** (DJ software)
- **Virtual MIDI port** (see options below)
- **Native Instruments Maschine Jam** (recommended controller)
- **Traktor Kontrol S5** (optional secondary controller)

## Quick Start

### 1. Clone & Build
```bash
git clone https://github.com/cjkcjk01/GiantSpaceRobotVisualiser
cd GiantSpaceRobotVisualiser
./gradlew build
```

### 2. Hardware Setup
1. **Set up virtual MIDI port** (note the port name)
   - **macOS:** Use built-in Audio MIDI Setup (Applications > Utilities) 
   - **Windows:** Bome Midi Translator (https://www.bome.com/) or Loopmidi (https://www.tobias-erichsen.de/software/loopmidi.html)
2. **Install Traktor TSI files** from https://maps.djtechtools.com/mappings/6883
3. **Configure Maschine Jam** with provided mapping file

### 3. Software Configuration
1. **Edit config.json** in `src/main/resources/`:
   - Set correct MIDI port names for your system
   - Configure screen size/fullscreen settings
   - Adjust performance settings for your hardware
2. **Test MIDI routing:** Traktor → Virtual MIDI Port → Visualiser

### 4. Run the Application
```bash
./gradlew run
```

**Recommended startup order:** Virtual MIDI Port → Traktor → Visualiser

## Legacy Processing Setup (Deprecated)

*The original Processing (.pde) version is deprecated but still available:*
- **Processing 3.5.4+:** https://processing.org/download/
- **Required libraries:** Minim, Midibus, Spout
- Load `GiantSpaceRobotVisualiser.pde` in Processing IDE
   
   

# Core Features

## Visual Engine

### Primary Visualizers
Each visualizer includes real-time parameter control via MIDI:

**🌀 Oblivion** (`VisOblivion.java`)
- Circular rotating patterns with customizable color palettes
- **Controls:** Palette selection, background retention, drama mode, rotation speed, opacity
- **Specialty:** Reads PNG gradients for unique color schemes

**⚙️ Sprocket** (`VisSprocket.java`) 
- Geometric mechanical-style animations
- **Controls:** Background retention, linear/random color modes, rotation speed, outline color
- **Specialty:** Industrial aesthetic with precise geometric patterns

**🌈 Candywarp** (`VisCandyWarp.java`)
- GPU-accelerated shader-based liquid warp effects
- **Controls:** Frequency band selection (bass to treble), cycle speed, warp intensity
- **Specialty:** Fluid, organic visual transformations

**📊 Waveform** (`VisWaveform.java`)
- Real-time oscilloscope-style audio display
- **Controls:** Sensitivity adjustment, scaling
- **Specialty:** Direct audio signal visualization

### Visual Overlays & Effects

**🔮 Kaleidoscope Mode**
- Psychedelic multi-mirror effects applicable to any visualizer
- Real-time parameter control for trippy visual experiences

**🎯 Hot Cue Image System**
- 8 customizable image packs (8 images each)
- Instant trigger via Traktor hot cues
- Support for 640x640 PNG format for optimal performance
- Per-deck pack assignment

**📝 Beat Words System** (`WordPack.java`)
- Display text sequences synchronized to beat detection
- 16 configurable word packs
- Adjustable opacity and timing

**🎨 Post-Processing Shader Pipeline** (`PostProcessingShaders.java`)
- 15+ real-time GPU effects
- Adjustable parameters for each effect
- Professional-grade visual enhancement

### DJ-Specific Effects

**🎛️ Filter Response**
- Visual blur effects synchronized to Traktor's filter controls
- Real-time response to high/low-pass filtering

**🔄 Echo/Delay Effects**
- Sobel edge detection effects during delay engagement
- Visual feedback matching audio effects

**⚡ Slice/Gate Effects**
- VHS-style glitch effects during slice/gate/mash
- Authentic retro distortion aesthetics

## Performance Features

**🎵 Audio Analysis**
- Real-time FFT (Fast Fourier Transform) processing
- Beat detection and rhythm synchronization  
- Frequency band isolation and response

**🖥️ Multi-Display Support**
- Fullscreen and windowed modes
- Secondary monitor/projector output
- Configurable resolution for performance optimization

**📡 SPOUT Integration**
- Real-time texture sharing with external applications
- Perfect for projection mapping workflows (tested with VPT8)
- Professional VJ integration capabilities

  

# System Requirements

## Software Requirements

**Core Dependencies:**
- **Java 17+** (OpenJDK or Oracle JDK)
- **Traktor 3.4.x+** (Native Instruments DJ software)
- **Virtual MIDI Port:**
  - **macOS:** Audio MIDI Setup (built-in)
  - **Windows:** Bome Midi Translator or Loopmidi

**Development (Optional):**
- **Gradle** (included via wrapper)
- **Git** for version control
- **IDE** (IntelliJ IDEA, Eclipse, VS Code)

## Hardware Requirements

**Minimum System:**
- **CPU:** Dual-core 2.5GHz+
- **RAM:** 4GB
- **GPU:** Dedicated graphics recommended
- **Resolution:** 1280x720 (higher resolutions require more GPU power)

**Recommended System:**
- **CPU:** Quad-core 3.0GHz+
- **RAM:** 8GB+
- **GPU:** Gaming-grade graphics card
- **Resolution:** 1920x1080+ with secondary display support
- **Storage:** SSD for optimal performance

**Professional Setup:**
- **Multi-monitor support** for DJ interface + visualizer output
- **Dedicated projector output** for live performances
- **Low-latency audio interface** for precise synchronization



# Controller Setup

## Supported Hardware

**Primary Controller:** 
- **Native Instruments Maschine Jam** (full mapping included)
- Complete TSI files and configuration available at https://maps.djtechtools.com/mappings/6883

**Secondary Controller:**
- **Traktor Kontrol S5** (optional, for enhanced control)

**Custom Controllers:**
- Any MIDI controller can be used with custom mapping
- See MIDI Controls section below for required CC values
- Traktor mapping required for full functionality

## Performance Optimization

**Resolution vs Performance:**
- **Window size directly impacts performance** - reduce resolution if experiencing frame drops
- **Projector use:** Lower resolutions (720p) often look excellent on projectors
- **Target:** 60fps on gaming-grade systems
- **Optimization tip:** Use secondary display at lower resolution for better performance

**Audio Latency:**
- **Low-latency audio interface recommended** for tight synchronization
- **Buffer size:** Smaller buffers = better sync, higher CPU usage
- **MIDI timing:** Virtual MIDI port quality affects responsiveness

# Configuration

Multiple configuration options are available for customization:

## Main Config File

**Location:** `src/main/resources/config.json`

### Display Settings
```json
{
  "screensize": {
    "fullscreen": true,
    "displayNumber": 1,
    "width": 1920,
    "height": 1080
  }
}
```
- **fullscreen:** Enable/disable fullscreen mode
- **displayNumber:** Target monitor (0=primary, 1=secondary, etc.)
- **width/height:** Window dimensions (used only if fullscreen=false)

### MIDI Configuration
```json
{
  "MIDIdevice": [
    "Maschine Jam - 1",
    "Virtual MIDI Port Name"
  ]
}
```
- **List of MIDI input ports** the visualizer will monitor
- **Typical setup:** Hardware controller + Virtual MIDI from Traktor

### Content Packs
```json
{
  "wordpacks": [
    ["BASS", "DROP", "ENERGY", "FLOW"],
    ["NIGHT", "LIGHTS", "DANCE", "MUSIC"]
  ],
  "palettes": [
    ["#FF0000", "#00FF00", "#0000FF"],
    ["#FF6B35", "#F7931E", "#FFD23F"]
  ]
}
```
- **wordpacks:** Text sequences displayed on beat (16 packs max)
- **palettes:** Color schemes for background cycling (hex values)

## Hot Cue Image Packs

**Location:** `src/main/resources/hotcue_pack1/` through `hotcue_pack8/`

- **8 packs maximum** (hotcue_pack1 through hotcue_pack8)
- **8 images per pack** (one for each hot cue button)
- **Format:** PNG files
- **Recommended resolution:** 640x640 (performance vs quality balance)
- **Naming:** Sequential numbering (1.png, 2.png, etc.)

## MIDI Control Mapping

**Current Status:** Hard-coded MIDI CC values (configurable mapping planned)
**Reference:** See "MIDI Controls" section below for complete CC mapping
**Documentation:** `midi commands.txt` in resources directory

## Oblivion Color Palettes

**Location:** `src/main/resources/gradients/`

The Oblivion visualizer uses a unique gradient system:
- **Method:** Reads PNG files pixel-by-pixel along the X-axis to create color palettes
- **Format:** PNG images with horizontal color gradients
- **Naming:** `gradient_xxx.png` (where xxx is any descriptive string)
- **Creation:** Use any image editor to create custom gradients
- **Usage:** Browse knob cycles through available gradient files

## Development & Customization

**Framework:** Built with [Processing](https://processing.org/) framework, now modernized as a Java Gradle project
**License:** Open source - freely modifiable and extensible
**Architecture:** Modular design with separated visualizer classes (`VisOblivion.java`, `VisSprocket.java`, etc.)
**Extension Points:** 
- Add new visualizers by extending the `Visualiser` base class
- Create custom shaders in GLSL for post-processing effects
- Develop new MIDI mappings and control schemes

# Keyboard Controls

Debug and testing keyboard shortcuts when MIDI controller is not available:

| Key  | Description                                    |
| ---- | ---------------------------------------------- |
| i    | Info, useful info such as FPS                  |
| h    | Displays help                                  |
| ?    | Displays help                                  |
| w    | Toggles the waveform                           |
| d    | Next visualiser                                |
| a    | previous visualiser                            |
| p    | Toggle SPOUT option (see Stout section in doc) |



# Midi Controls

The visualiser uses the following MIDI CC values to control its functions. A config file for the Maschine Jam is included, if you are using some other controller then you will need to be able to configure it to send these values.



| MIDI CC Number | Value                                                        | Description                                                  |
| -------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 100            | 0-127                                                        | Deck A volume                                                |
| 101            | 0-127                                                        | Deck B volume                                                |
| 102            | 0-127                                                        | Deck C volume                                                |
| 103            | 0-127                                                        | Deck D volume                                                |
| 110            | 0-126 = off, 127 = on                                        | Deck A play status                                           |
| 111            | 0-126 = off, 127 = on                                        | Deck B play status                                           |
| 112            | 0-126 = off, 127 = on                                        | Deck C play status                                           |
| 113            | 0-126 = off, 127 = on                                        | Deck D play status                                           |
| 1              | 15 = Hot cue 1<br/>31 = Hot cue 2<br/>47 = Hot cue 3<br/>63 = Hot cue 4<br/>79 = Hot cue 5<br/>95 = Hot cue 6<br/>111 = Hot cue 7<br />127 = Hot cue 8 | Deck A hot cue pressed                                       |
| 2              | 15 = Hot cue 1<br/>31 = Hot cue 2<br/>47 = Hot cue 3<br/>63 = Hot cue 4<br/>79 = Hot cue 5<br/>95 = Hot cue 6<br/>111 = Hot cue 7<br />127 = Hot cue 8 | Deck B hot cue pressed                                       |
| 3              | 15 = Hot cue 1<br/>31 = Hot cue 2<br/>47 = Hot cue 3<br/>63 = Hot cue 4<br/>79 = Hot cue 5<br/>95 = Hot cue 6<br/>111 = Hot cue 7<br />127 = Hot cue 8 | Deck C hot cue pressed                                       |
| 4              | 15 = Hot cue 1<br/>31 = Hot cue 2<br/>47 = Hot cue 3<br/>63 = Hot cue 4<br/>79 = Hot cue 5<br/>95 = Hot cue 6<br/>111 = Hot cue 7<br />127 = Hot cue 8 | Deck D hot cue pressed                                       |
| 56             | 0-7                                                          | Deck A, set hot cue pack. The value indicates which pack to associate with the deck |
| 57             | 0-7                                                          | Deck B, set hot cue pack. The value indicates which pack to associate with the deck |
| 58             | 0-7                                                          | Deck C, set hot cue pack. The value indicates which pack to associate with the deck |
| 59             | 0-7                                                          | Deck D, set hot cue pack. The value indicates which pack to associate with the deck |
| 105            | 0-127                                                        | Deck A filter value                                          |
| 106            | 0-127                                                        | Deck B filter value                                          |
| 107            | 0-127                                                        | Deck C filter value                                          |
| 108            | 0-127                                                        | Deck D filter value                                          |
| 21             | 1,2                                                          | Select main visualiser. 1 chooses the previous one, 2 chooses the next one |
| 45             | 0-127                                                        | Visualiser browse knob, generally an encoder, function varies depending on which main visualiser is running |
| 46             | Toggles if value > 100                                       | Toggle black or white background mode                        |
| 48             | 0-127                                                        | Visualiser scaling                                           |
| 27             | Toggles if value > 100                                       | Visualiser button 1                                          |
| 28             | Toggles if value > 100                                       | Visualiser button 2                                          |
| 52             | 0-127                                                        | Visualiser fader 1                                           |
| 53             | 0-127                                                        | Visualiser fader 2                                           |
| 26             | Toggles if value > 100                                       | Toggle waveform display                                      |
| 49             | 0-127                                                        | Waveform scaling                                             |
| 29             | Toggles if value > 100                                       | Toggle kaleidoscope mode                                     |
| 50             | 0-127                                                        | Beat words opacity                                           |
| 41             | triggers next step if value > 120                            | Increment background colour and beat words                   |
| 30             | Toggles if value > 100                                       | Toggle background colour change on beat                      |
| 47             | 0 = off<br />1-16 = number of wordpack to use                | Select beat words pack                                       |
| 60             | 1-15                                                         | Select post processing shader                                |
| 61             | Toggles if value > 100                                       | Toggle post processing shader                                |
| 54             | 0-127                                                        | Post processing parameter 1                                  |
| 55             | 0-127                                                        | Post processing parameter 2                                  |
| 64             | On if value > 100,<br />off if value = 0                     | Display delay/echo effect shader                             |
| 65             | On if value > 100,<br />off if value = 0                     | Display gater/slice/mash effect shader                       |

# SPOUT Integration

**SPOUT** enables real-time graphics sharing between applications on Windows, perfect for professional VJ workflows.

## Use Cases
- **Projection mapping** with software like VPT8
- **OBS Studio integration** for streaming
- **VJ software integration** for live performances
- **Recording and compositing** workflows

## Tested Applications
- **VPT8** (https://hcgilje.wordpress.com/vpt/) - Confirmed working
- **MadMapper** - Projection mapping
- **Resolume** - VJ software integration

## Resources
- **SPOUT Framework:** https://spout.zeal.co/
- **Processing Library:** https://github.com/leadedge/SpoutProcessing
- **Platform:** Windows only (macOS users can use Syphon alternatives)

# Credits & Acknowledgements

This project builds upon excellent work from the creative coding community:

## Visualizer Algorithms

### Oblivion (`VisOblivion.java`)
**Original Creator:** Tobias Wehrum (Tobias.Wehrum@dragonlab.de)  
**License:** MIT License  
**Source:** [Into Oblivion Music Visualizer](https://blog.dragonlab.de/2016/04/into-oblivion-music-visualizer/)  
**Repository:** [GenerativeArt/s044_into_oblivion](https://github.com/TobiasWehrum/GenerativeArt/tree/master/s044_into_oblivion)

### Sprocket (`VisSprocket.java`)
**Original Creator:** Ben Farahmand  
**Source:** [Audio Visualizer with Processing](http://www.benfarahmand.com/2013/10/source-code-audio-visualizer-with.html)  
**Code:** [GitHub Gist](https://gist.github.com/benfarahmand/6902359)

### Candywarp (`VisCandyWarp.java`)
**Original Creator:** mojovideotech  
**Based on:** Trisomie21 (glslsandbox.com/e#38710.0)  
**Modified by:** @hintz  
**License:** Creative Commons Attribution-NonCommercial-ShareAlike 3.0  
**Source:** [ISF Video Editor](https://editor.isf.video/shaders/5e7a802d7c113618206dec38)

## Shader Effects

### Sobel Edge Detection
**Purpose:** Basic edge detection via convolution  
**Original Creator:** Ken Slade (ken.slade@gmail.com)  
**Based on:** Jeroen Baert (jeroen.baert@cs.kuleuven.be)  
**Source:** [Shadertoy - ldsSWr](https://www.shadertoy.com/view/ldsSWr)  
**Original:** [Shadertoy - Xdf3Rf](https://www.shadertoy.com/view/Xdf3Rf)

### VHS Glitch Effect
**Original Creator:** ryk
**Source:** [Shadertoy - ldjGzV](https://www.shadertoy.com/view/ldjGzV)
**Profile:** [ryk's Shadertoy](https://www.shadertoy.com/user/ryk)
**Usage Note:** Non-commercial use only (per creator's request)

### Post-Processing Framework
**Source:** [Gene Kogan - Processing Shader Examples](https://genekogan.com/works/processing-shader-examples/)
**Usage:** Template and examples for GPU-based post-processing effects
