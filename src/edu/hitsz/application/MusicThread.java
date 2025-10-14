package edu.hitsz.application;

import javax.sound.sampled.*;
import java.io.*;

/**
 * 音乐播放线程
 * 支持循环播放和停止播放功能
 * @author hitsz
 */
public class MusicThread extends Thread {

    // 音频文件名
    private String filename;
    private AudioFormat audioFormat;
    private byte[] samples;

    // 是否循环播放
    private boolean loop;

    // 是否停止播放
    private volatile boolean stopped = false;

    // 音频输出线
    private SourceDataLine dataLine;

    /**
     * 构造函数
     * @param filename 音频文件路径
     */
    public MusicThread(String filename) {
        this(filename, false);
    }

    /**
     * 构造函数
     * @param filename 音频文件路径
     * @param loop 是否循环播放
     */
    public MusicThread(String filename, boolean loop) {
        this.filename = filename;
        this.loop = loop;
        reverseMusic();
    }

    /**
     * 读取音频文件
     */
    public void reverseMusic() {
        try {
            // 定义一个AudioInputStream用于接收输入的音频数据
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(filename));
            // 用AudioFormat来获取AudioInputStream的格式
            audioFormat = stream.getFormat();
            samples = getSamples(stream);
        } catch (UnsupportedAudioFileException e) {
            System.err.println("不支持的音频文件格式: " + filename);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("读取音频文件失败: " + filename);
            e.printStackTrace();
        }
    }

    /**
     * 从音频流中读取样本数据
     */
    public byte[] getSamples(AudioInputStream stream) {
        int size = (int) (stream.getFrameLength() * audioFormat.getFrameSize());
        byte[] samples = new byte[size];
        DataInputStream dataInputStream = new DataInputStream(stream);
        try {
            dataInputStream.readFully(samples);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return samples;
    }

    /**
     * 播放音频
     */
    public void play(InputStream source) {
        int size = (int) (audioFormat.getFrameSize() * audioFormat.getSampleRate());
        byte[] buffer = new byte[size];

        // 获取音频输出线
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open(audioFormat, size);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        }

        dataLine.start();

        try {
            int numBytesRead = 0;
            while (numBytesRead != -1 && !stopped) {
                // 从音频流读取数据
                numBytesRead = source.read(buffer, 0, buffer.length);
                // 写入音频输出线
                if (numBytesRead != -1) {
                    dataLine.write(buffer, 0, numBytesRead);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        dataLine.drain();
        dataLine.close();
    }

    @Override
    public void run() {
        // 循环播放或单次播放
        do {
            if (stopped) {
                break;
            }
            InputStream stream = new ByteArrayInputStream(samples);
            play(stream);
        } while (loop && !stopped);
    }

    /**
     * 停止播放音频
     */
    public void stopMusic() {
        stopped = true;
        if (dataLine != null) {
            dataLine.stop();
            dataLine.close();
        }
    }

    /**
     * 设置是否循环播放
     */
    public void setLoop(boolean loop) {
        this.loop = loop;
    }
}

