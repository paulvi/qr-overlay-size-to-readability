package com.emptech.utils;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

/**
* static methods for image format conversion, using javax.imageio.ImageIO API
*
* */
// https://github.com/szjug/anything2jpg-with-java
public class ImageConverter7 {
    protected static Logger logger = LoggerFactory.getLogger(ImageConverter7.class);

    //TODO list all readers/writers
    public static void listImageReader(){
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
        while (readers.hasNext()) {
            System.out.println("reader: " + readers.next());
        }

    }
 
    /**
     * Converts an image to another format.
     *  Uses Java 7 try-with-resources to handle IOException.
     *
     * @param inputImagePath Path of the source image
     * @param outputImagePath Path of the destination image
     * @param formatName the format to be converted to, one of: jpeg, png, bmp, wbmp, and gif
     * @return true if successful, false otherwise (IOException has happened)
     * @throws FileNotFoundException if inputImagePath or outputImagePath cannot be located
     */
    public static boolean convertFormat(String inputImagePath, String outputImagePath, String formatName)
            throws FileNotFoundException
    //IOException is handled
    {
        BufferedImage inputImage = null;
        try ( FileInputStream inputStream = new FileInputStream(inputImagePath) ) {
            // reads input image from file
            inputImage = ImageIO.read(inputStream);
        } catch (IOException e1) {
            logger.error("IOException {}", e1);
            return false;
        }
        try ( FileOutputStream outputStream = new FileOutputStream(outputImagePath) ) {
            // writes to the output image in specified format
            return ImageIO.write(inputImage, formatName, outputStream);
        } catch (IOException e2) {
            logger.error("IOException {}", e2);
            return false;
        }
    }


    /**
     * Converts an image to another format.
     *  Uses Java 7 try-with-resources to handle IOException.
     *
     * @param inputImagePath Path of the source image
     * @param outputImagePath Path of the destination image
     * @param formatName the format to be converted to, one of: jpeg, png, bmp, wbmp, and gif
     * @return true if successful, false otherwise (IOException has happened)
     * @throws FileNotFoundException if inputImagePath or outputImagePath cannot be located
     */
    public static boolean convertFormat(String inputImagePath, String inputFormatName, String outputImagePath, String formatName)
            throws FileNotFoundException
    //IOException is handled
    {
        BufferedImage inputImage = null;
        try ( FileInputStream inputStream = new FileInputStream(inputImagePath) ) {
            // reads input image from file
            inputImage = ImageIO.read(inputStream);
        } catch (IOException e1) {
            logger.error("IOException {}", e1);
            return false;
        }

        if ("png".equalsIgnoreCase(inputFormatName)) {
            // create a blank, RGB, same width and height, and a white background
            BufferedImage newBufferedImage = new BufferedImage(inputImage.getWidth(),
                    inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newBufferedImage.createGraphics().drawImage(inputImage, 0, 0, Color.WHITE, null);
            inputImage = newBufferedImage;
        }

        try ( FileOutputStream outputStream = new FileOutputStream(outputImagePath) ) {
            // writes to the output image in specified format
            return ImageIO.write(inputImage, formatName, outputStream);
        } catch (IOException e2) {
            logger.error("IOException {}", e2);
            return false;
        }

    }
}