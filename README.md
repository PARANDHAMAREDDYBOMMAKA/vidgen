# YTHub - Automated Educational Video Generator for B.Tech Students

A Java-based video generation system that programmatically creates high-quality educational videos for Data Structures & Algorithms (DSA) and Web Development topics.

## Project Vision

Create an automated pipeline that generates professional educational videos with:
- Clear visual animations and explanations
- Professional AI-powered voiceovers
- Code visualizations with syntax highlighting
- Step-by-step algorithm demonstrations
- Consistent branding and quality

**Target Audience:** B.Tech students learning DSA and Web Development

## Features

### Core Capabilities
- ✅ Automated video generation from scripts/templates
- ✅ Algorithm visualization and animation
- ✅ Code syntax highlighting
- ✅ Professional text-to-speech narration
- ✅ Data structure visualizations (trees, graphs, arrays, etc.)
- ✅ Step-by-step execution walkthroughs
- ✅ Customizable themes and branding
- ✅ Batch video generation

### Video Quality Standards
- **Resolution:** 1080p30 / 1080p60 / 4K60 (Default) / 8K60
- **Frame Rate:** 30 FPS or 60 FPS depending on resolution
- **Bitrate:** 8-12 Mbps for 1080p, 45 Mbps for 4K, 85 Mbps for 8K
- **Audio:** Clear, professional TTS with background music (320kbps)
- **Duration:** 10-60 minutes per video (minimum 10 min, maximum 1 hour)
- **Format:** MP4 (H.265/HEVC codec for better compression)

## Technology Stack

### Core Libraries
- **JavaCV** - Video processing and FFmpeg wrapper
- **FFmpeg** - Video encoding/decoding
- **JavaFX** - Graphics and animations
- **Processing** - Creative visualizations (optional)

### Audio/Voice
- **Google Cloud TTS API** - High-quality text-to-speech
- **Alternative:** ElevenLabs API, AWS Polly
- **javax.sound.sampled** - Audio processing

### Graphics & Visualization
- **Java2D** - 2D graphics rendering
- **JavaFX Canvas** - Animation framework
- **GraphStream** - Graph visualizations

### Utilities
- **Maven** - Dependency management
- **Jackson** - JSON parsing for scripts
- **SLF4J + Logback** - Logging

## DSA Topics Coverage

Each video is designed to be 10-60 minutes long with comprehensive coverage, examples, and visualizations.

### Phase 1: Fundamentals (10-12 Videos)

#### 1. Introduction to DSA
- What is DSA and why it matters (10-15 min video)
- Time and Space Complexity basics (15-20 min video per concept)

#### 2. Arrays
- Array basics and operations (12-18 min video)
- Array problems and patterns (15-25 min video)

#### 3. Searching Algorithms
- Linear Search visualization (10-12 min video)
- Binary Search step-by-step (12-15 min video)

#### 4. Sorting Algorithms
- Bubble Sort animation (10-12 min video)
- Selection Sort animation (10-12 min video)
- Insertion Sort animation (12-15 min video)
- Merge Sort visualization (15-20 min video)
- Quick Sort visualization (15-20 min video)

### Phase 2: Linear Data Structures (8-10 Videos)

#### 5. Linked Lists
- Singly Linked List operations (1 video)
- Doubly Linked List (1 video)
- Common problems (1 video)

#### 6. Stacks
- Stack implementation and operations (1 video)
- Stack applications (1 video)

#### 7. Queues
- Queue implementation (1 video)
- Priority Queue and Deque (1 video)

### Phase 3: Non-Linear Data Structures (12-15 Videos)

#### 8. Trees
- Binary Tree basics (1 video)
- Binary Search Tree operations (2 videos)
- Tree traversals (Inorder, Preorder, Postorder) (1 video)
- AVL Trees (1 video)
- Heap and Heap Sort (2 videos)

#### 9. Graphs
- Graph representations (1 video)
- BFS visualization (1 video)
- DFS visualization (1 video)
- Dijkstra's Algorithm (1 video)
- Spanning Trees (Kruskal, Prim) (2 videos)

### Phase 4: Advanced Topics (10-12 Videos)

#### 10. Hashing
- Hash Tables and collision handling (2 videos)

#### 11. Dynamic Programming
- DP introduction and memoization (1 video)
- Classic DP problems (Fibonacci, Knapsack) (2 videos)

#### 12. Greedy Algorithms
- Greedy approach explained (1 video)
- Classic problems (2 videos)

#### 13. Backtracking
- N-Queens problem (1 video)
- Sudoku solver (1 video)

**Total DSA Videos: 40-50 videos**

## Project Architecture

```
ythub/
├── src/main/java/com/ythub/
│   ├── core/
│   │   ├── VideoGenerator.java          # Main video generation orchestrator
│   │   ├── VideoConfig.java             # Configuration settings
│   │   ├── Scene.java                   # Individual scene representation
│   │   └── Timeline.java                # Video timeline management
│   │
│   ├── animation/
│   │   ├── AnimationEngine.java         # Core animation framework
│   │   ├── Animator.java                # Animation interface
│   │   ├── ArrayAnimator.java           # Array visualizations
│   │   ├── TreeAnimator.java            # Tree visualizations
│   │   ├── GraphAnimator.java           # Graph visualizations
│   │   └── CodeAnimator.java            # Code highlighting animations
│   │
│   ├── voice/
│   │   ├── TTSEngine.java               # Text-to-speech interface
│   │   ├── GoogleTTSProvider.java       # Google Cloud TTS implementation
│   │   ├── ScriptParser.java            # Parse video scripts
│   │   └── AudioMixer.java              # Mix voice with background music
│   │
│   ├── rendering/
│   │   ├── FrameRenderer.java           # Render individual frames
│   │   ├── VideoEncoder.java            # Encode frames to video
│   │   └── EffectsProcessor.java        # Add transitions, effects
│   │
│   ├── dsa/
│   │   ├── algorithms/
│   │   │   ├── BubbleSortVisualizer.java
│   │   │   ├── MergeSortVisualizer.java
│   │   │   ├── BinarySearchVisualizer.java
│   │   │   └── ... (other algorithms)
│   │   │
│   │   ├── datastructures/
│   │   │   ├── ArrayVisualizer.java
│   │   │   ├── LinkedListVisualizer.java
│   │   │   ├── TreeVisualizer.java
│   │   │   ├── GraphVisualizer.java
│   │   │   └── ... (other data structures)
│   │   │
│   │   └── topics/
│   │       ├── TopicGenerator.java      # Base class for topic videos
│   │       ├── SortingTopicGenerator.java
│   │       ├── TreeTopicGenerator.java
│   │       └── ... (other topics)
│   │
│   ├── templates/
│   │   ├── VideoTemplate.java           # Template interface
│   │   ├── IntroTemplate.java           # Standard intro
│   │   ├── OutroTemplate.java           # Standard outro
│   │   └── ThemeManager.java            # Colors, fonts, branding
│   │
│   └── utils/
│       ├── CodeHighlighter.java         # Syntax highlighting
│       ├── ImageUtils.java              # Image processing
│       └── FileManager.java             # File operations
│
├── src/main/resources/
│   ├── scripts/                         # Video scripts (JSON/YAML)
│   │   ├── dsa/
│   │   │   ├── bubble-sort.json
│   │   │   ├── binary-search.json
│   │   │   └── ...
│   │   └── webdev/
│   │
│   ├── assets/
│   │   ├── fonts/                       # Custom fonts
│   │   ├── images/                      # Logos, graphics
│   │   ├── music/                       # Background music
│   │   └── templates/                   # Video templates
│   │
│   └── config/
│       ├── video-config.yaml            # Video settings
│       └── theme-config.yaml            # Theme/branding
│
├── output/                              # Generated videos
├── temp/                                # Temporary files
├── pom.xml                              # Maven configuration
└── README.md                            # This file
```

## Video Generation Pipeline

```
1. Script Input → Parse JSON/YAML script with narration + visualization steps
2. Voice Generation → Convert script text to audio using TTS
3. Scene Creation → Generate visual scenes based on script
4. Animation → Apply animations for algorithms/data structures
5. Frame Rendering → Render each frame (30 FPS)
6. Audio Sync → Sync voice narration with visuals
7. Post-Processing → Add intro/outro, transitions, effects
8. Encoding → Encode to final MP4 video
9. Output → Save to output directory
```

## Video Script Format

Example script structure (JSON):

```json
{
  "title": "Bubble Sort Algorithm",
  "duration": 600,
  "topic": "sorting",
  "difficulty": "beginner",
  "scenes": [
    {
      "type": "intro",
      "duration": 5,
      "title": "Bubble Sort Algorithm"
    },
    {
      "type": "narration",
      "duration": 15,
      "text": "Bubble sort is a simple sorting algorithm that repeatedly steps through the list...",
      "visuals": {
        "type": "title_slide",
        "title": "What is Bubble Sort?",
        "bullets": [
          "Simple comparison-based algorithm",
          "Time Complexity: O(n²)",
          "Space Complexity: O(1)"
        ]
      }
    },
    {
      "type": "visualization",
      "duration": 30,
      "text": "Let's visualize how bubble sort works with an example array",
      "animation": {
        "type": "bubble_sort",
        "data": [64, 34, 25, 12, 22, 11, 90],
        "speed": "medium",
        "highlightComparisons": true,
        "showCode": true
      }
    }
  ]
}
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- FFmpeg installed on system
- Google Cloud account (for TTS API)

### Installation

```bash
# Clone the repository
git clone <repo-url>
cd ythub

# Install dependencies
mvn clean install

# Set up configuration
cp src/main/resources/config/video-config.example.yaml src/main/resources/config/video-config.yaml
# Edit with your API keys and settings

# Run the generator
mvn exec:java -Dexec.mainClass="com.ythub.Main"
```

### Generate Your First Video

```bash
# Generate a single video
mvn exec:java -Dexec.args="--topic bubble-sort"

# Generate all videos for a category
mvn exec:java -Dexec.args="--category sorting"

# Batch generate all DSA videos
mvn exec:java -Dexec.args="--generate-all"
```

## Configuration

### video-config.yaml
```yaml
video:
  resolution: "3840x2160"  # 4K (use "7680x4320" for 8K)
  fps: 60
  codec: "hevc"  # H.265 for better compression
  bitrate: "45000k"  # 45 Mbps for 4K
  format: "mp4"

audio:
  sampleRate: 48000
  bitrate: "320k"
  channels: 2

theme:
  primaryColor: "#2196F3"
  secondaryColor: "#FF5722"
  backgroundColor: "#FFFFFF"
  textColor: "#212121"
  codeTheme: "monokai"

tts:
  provider: "google"
  voice: "en-US-Neural2-J"
  speed: 1.0
  pitch: 0.0
```

## Development Roadmap

### Milestone 1: Core Framework (Week 1-2)
- [x] Project setup and dependencies
- [ ] Basic video generation pipeline
- [ ] Frame rendering system
- [ ] Audio integration

### Milestone 2: DSA Visualizations (Week 3-4)
- [ ] Array animations
- [ ] Sorting algorithm visualizers
- [ ] Search algorithm visualizers
- [ ] Code highlighting

### Milestone 3: Advanced Topics (Week 5-6)
- [ ] Tree visualizations
- [ ] Graph visualizations
- [ ] Complex animations
- [ ] Template system

### Milestone 4: Production (Week 7-8)
- [ ] All DSA topic videos
- [ ] Quality improvements
- [ ] Batch generation
- [ ] YouTube upload integration

## Quality Standards

Each video must have:
- ✅ Clear, professional narration
- ✅ Smooth animations (no lag or stuttering)
- ✅ Accurate algorithm implementation
- ✅ Proper code syntax highlighting
- ✅ Consistent branding and theme
- ✅ Background music (subtle, non-distracting)
- ✅ Captions/subtitles
- ✅ Engaging visual design

## Contributing

This is a personal project, but suggestions are welcome!

## License

MIT License

## Contact

For questions or feedback about this project, please open an issue.

---

**Note:** This is an automated video generation system built with Java. All videos are programmatically created for educational purposes.
