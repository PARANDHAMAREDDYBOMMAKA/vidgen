# Getting Started with YTHub

This guide will help you set up and start generating educational videos for your YouTube channel.

## Prerequisites

1. **Java 17 or higher**
   ```bash
   java -version
   ```

2. **Maven 3.8+**
   ```bash
   mvn -version
   ```

3. **FFmpeg** (required for video encoding)
   ```bash
   # macOS
   brew install ffmpeg

   # Ubuntu/Debian
   sudo apt-get install ffmpeg

   # Windows
   # Download from https://ffmpeg.org/download.html
   ```

4. **Google Cloud Account** (optional, for high-quality TTS)
   - Create a project at https://console.cloud.google.com
   - Enable Text-to-Speech API
   - Create service account credentials
   - Download JSON key file

## Installation

1. **Install dependencies**
   ```bash
   mvn clean install
   ```

2. **Set up configuration** (optional)
   ```bash
   cp src/main/resources/config/video-config.example.yaml src/main/resources/config/video-config.yaml
   ```

3. **Configure Google Cloud TTS** (optional)
   ```bash
   export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your/credentials.json"
   ```

## Quick Start

### Generate Your First Video

Run the main class to generate a Bubble Sort video:

```bash
mvn exec:java -Dexec.mainClass="com.ythub.Main"
```

This will:
- Generate a 4K video about Bubble Sort
- Save it to the `output/` directory
- Take approximately 5-15 minutes depending on your system

### Choose Your Resolution

Edit `src/main/java/com/ythub/Main.java`:

```java
VideoConfig config = new VideoConfig(VideoConfig.Resolution.HD_1080P_30);
VideoConfig config = new VideoConfig(VideoConfig.Resolution.HD_1080P_60);
VideoConfig config = new VideoConfig(VideoConfig.Resolution.UHD_4K);
VideoConfig config = new VideoConfig(VideoConfig.Resolution.UHD_8K);
```

## Creating Custom Videos

### Option 1: Using Java Code

Create a new visualizer class in `src/main/java/com/ythub/dsa/algorithms/`:

```java
public class MergeSortVisualizer {
    public Timeline createMergeSortVideo(int[] array) {
        Timeline timeline = new Timeline("Merge Sort");

    }
}
```

### Option 2: Using JSON Scripts

Create a JSON file in `src/main/resources/scripts/dsa/`:

```json
{
  "title": "Binary Search Algorithm",
  "topic": "searching",
  "difficulty": "beginner",
  "targetArray": [2, 5, 8, 12, 16, 23, 38, 45, 56, 67, 78],
  "scenes": [
    {
      "type": "intro",
      "duration": 5
    }
  ]
}
```

## Video Duration Guidelines

- **Minimum:** 10 minutes (600 seconds)
- **Maximum:** 1 hour (3600 seconds)
- **Recommended:** 12-15 minutes for DSA topics

To ensure proper duration:
1. Add detailed explanations in narration
2. Slow down animation speed
3. Include multiple examples
4. Add practice problems at the end

## Project Structure

```
ythub/
├── src/main/java/com/ythub/
│   ├── Main.java                    # Entry point
│   ├── core/                        # Core framework
│   ├── animation/                   # Animation engine
│   ├── dsa/algorithms/              # DSA visualizers
│   └── voice/                       # TTS engine
├── src/main/resources/
│   ├── scripts/dsa/                 # Video scripts
│   └── assets/                      # Images, fonts, music
├── output/                          # Generated videos
└── temp/                            # Temporary files
```

## Common Tasks

### Generate All DSA Videos

```bash
mvn exec:java -Dexec.args="--generate-all"
```

### Generate Specific Topic

```bash
mvn exec:java -Dexec.args="--topic binary-search"
```

### Build JAR

```bash
mvn clean package
java -jar target/video-generator-1.0.0.jar
```

## Performance Tips

### For 4K Video Generation

- **RAM:** 16GB minimum, 32GB recommended
- **Storage:** 100GB+ free space
- **CPU:** Multi-core processor (6+ cores recommended)
- **GPU:** Not required but can help with rendering

### Speed Up Generation

1. Use lower resolution for testing (1080p30)
2. Reduce FPS temporarily
3. Use shorter test arrays
4. Enable hardware acceleration in FFmpeg

## Troubleshooting

### Out of Memory Error

Increase JVM heap size:
```bash
export MAVEN_OPTS="-Xmx8g"
mvn exec:java
```

### FFmpeg Not Found

Add FFmpeg to your PATH or specify location:
```bash
export PATH="/path/to/ffmpeg:$PATH"
```

### Google TTS Not Working

The system will fall back to silent audio. To enable TTS:
1. Verify credentials are set correctly
2. Enable Text-to-Speech API in Google Cloud
3. Check billing is enabled

### JavaFX Issues

If JavaFX fails to initialize:
```bash
mvn clean install -U
```

## Next Steps

1. Explore the DSA topics list in README.md
2. Create visualizers for other algorithms
3. Customize themes and branding
4. Add background music
5. Implement YouTube upload automation

## Support

For issues and questions:
- Check README.md for detailed documentation
- Review existing visualizer code
- Open an issue on GitHub

Happy video creating!
