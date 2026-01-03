package com.ythub.rendering;

import com.ythub.core.VideoConfig;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class VideoEncoder {

    private static final Logger logger = LoggerFactory.getLogger(VideoEncoder.class);

    private final VideoConfig config;

    public VideoEncoder(VideoConfig config) {
        this.config = config;
    }

    public void encodeVideo(List<String> framePaths, String outputPath) {
        FFmpegFrameRecorder recorder = null;
        Java2DFrameConverter converter = new Java2DFrameConverter();

        try {
            recorder = new FFmpegFrameRecorder(outputPath, config.getWidth(), config.getHeight());
            recorder.setFrameRate(config.getFps());
            recorder.setVideoBitrate(config.getBitrate() * 1000);

            if ("hevc".equalsIgnoreCase(config.getCodec())) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_HEVC);
            } else {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            }

            recorder.setFormat(config.getFormat());
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.start();

            logger.info("Encoding {} frames to {}", framePaths.size(), outputPath);

            for (int i = 0; i < framePaths.size(); i++) {
                BufferedImage image = ImageIO.read(new File(framePaths.get(i)));
                Frame frame = converter.convert(image);
                recorder.record(frame);

                if (i % config.getFps() == 0) {
                    logger.info("Encoded {}/{} frames", i, framePaths.size());
                }
            }

            recorder.stop();
            recorder.release();

            logger.info("Video encoding complete");

        } catch (Exception e) {
            logger.error("Error encoding video", e);
            throw new RuntimeException("Video encoding failed", e);
        } finally {
            if (recorder != null) {
                try {
                    recorder.close();
                } catch (Exception e) {
                    logger.warn("Error closing recorder", e);
                }
            }
            converter.close();
        }
    }

    public void mergeAudioVideo(String videoPath, String audioPath, String outputPath) {
        try {
            File videoFile = new File(videoPath);
            File audioFile = new File(audioPath);

            if (!videoFile.exists()) {
                throw new RuntimeException("Video file not found: " + videoPath);
            }

            if (!audioFile.exists()) {
                logger.warn("Audio file not found: {}. Video will have no audio.", audioPath);
                copyVideoWithoutAudio(videoPath, outputPath);
                return;
            }

            logger.info("Merging video: {} ({} bytes) with audio: {} ({} bytes)",
                videoPath, videoFile.length(), audioPath, audioFile.length());

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", videoPath,
                    "-i", audioPath,
                    "-c:v", "copy",
                    "-c:a", "aac",
                    "-b:a", config.getAudioBitrate() + "k",
                    "-map", "0:v:0",
                    "-map", "1:a:0",
                    "-shortest",
                    outputPath
            );

            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg merge failed with exit code: " + exitCode);
            }

            logger.info("Audio-video merge complete with perfect sync: {}", outputPath);

        } catch (Exception e) {
            logger.error("Error merging audio and video", e);
            throw new RuntimeException("Audio-video merge failed", e);
        }
    }

    private void copyVideoWithoutAudio(String videoPath, String outputPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", videoPath,
                    "-c", "copy",
                    outputPath
            );

            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg copy failed with exit code: " + exitCode);
            }

            logger.info("Video copied without audio: {}", outputPath);

        } catch (Exception e) {
            logger.error("Error copying video", e);
            throw new RuntimeException("Video copy failed", e);
        }
    }
}
