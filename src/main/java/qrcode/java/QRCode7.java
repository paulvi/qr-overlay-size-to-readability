package qrcode.java;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emptech.utils.DateTimeUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

// updated from https://javapapers.com/core-java/java-qr-code/
public class QRCode7 {
	protected static Logger logger = LoggerFactory.getLogger(QRCode7.class);

	  /** 1 L = ~7% correction  */
	  /** 2 M = ~15% correction */
	  /** 3 Q = ~25% correction */
	  /** 4 H = ~30% correction */
	static final ErrorCorrectionLevel[] EC_LEVELS = {
		ErrorCorrectionLevel.L, 
		ErrorCorrectionLevel.M, 
		ErrorCorrectionLevel.Q,
		ErrorCorrectionLevel.H, 
	};
	
	private static final String overlayPath = "./PNG_transparency_demonstration_1.png";

	static class QRCodeResult{
		String text;
		int status = -1; // -1 - Unknown, 0 - OK, 1 - ChecksumException, 2 - NotFoundException
	}
	
	private static final int OVERLAY_W = 200;
	private static final int OVERLAY_H = 200;
	
	public static void main(String[] args) {
//		draw(400, 400);
//		draw(500, 500);
//		draw(600, 600);
//		draw(700, 700);
		draw(800, 800);
	}
	
	public static void draw(int w, int h) 
			//throws WriterException, IOException,NotFoundException 
	{
		//String qrCodeData = "Hello World12312!";
		String charset = "UTF-8"; // or "ISO-8859-1"
		List<String> lines;
		try {
			lines = Files.readAllLines(Paths.get("lines_of_text.txt"));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		//String filePath = "QRCode.png";
		Date now = new Date();
		String subfolder = "target/"+DateTimeUtil.dateToString(now)+"_w"+w+"_ow"+OVERLAY_W+"/";
		System.out.println(subfolder);
		
		String filename = "QRCode";
		String ext = "png";
		
		File folder = new File(subfolder);
		folder.mkdirs();

		int i=0;
		for (String line: lines) {
			i++;
			if (line.isEmpty()) {
				continue;
			}
			System.out.println("\n"+i+": "+line);
			
			int l=0;
			for(ErrorCorrectionLevel lev: EC_LEVELS) {
				l++;
				Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
				hintMap.put(EncodeHintType.ERROR_CORRECTION, lev);
				
				String filePath = subfolder+filename+"_"+i+"_"+l+lev+"."+ext;
				createQRCodeWithOverlay(line, charset, hintMap, w, h, Color.BLACK, filePath, ext);
				System.out.println("QR Code image "+filePath+" created successfully!");
				
				Map<EncodeHintType, ErrorCorrectionLevel> hintMap2 = new HashMap<>();
				QRCodeResult result = readQRCode(filePath, charset, hintMap2);
				String outText = "-";
				if (result!=null && result.status ==0 ) {
					outText = result.text;
				}
				logger.info("Data from "+filePath+" QR Code: {} {}", result.status, result.text);
				
				if (result!=null) {
					Color color = null;
					if (result.status ==1 ) {
						color = Color.GREEN;
					}else if (result.status ==2 ) {
						color = Color.RED;
					}
					if (color != null) {
						logger.info("Re-Drawing {} using color {}", filePath, color);
						createQRCodeWithOverlay(line, charset, hintMap, w, h, color, filePath, ext);
					}
				}
	
			}//EC_LEVELS
		}//lines
		
	}
	
	public static boolean createQRCodeWithOverlay(String qrCodeData, 
			String charset, Map hintMap, int qrCodewidth, int qrCodeheight, //TODO  WxH in signature
			Color color,
			String filePath, String ext)
			//throws WriterException
			//throws WriterException, IOException {
	{
		//BitMatrix matrix = new MultiFormatWriter().encode(
				//new String(qrCodeData.getBytes(charset), charset),
		logger.info("Drawing {} using color {}",
				filePath, color);
		BitMatrix matrix;
		try {
			matrix = new QRCodeWriter().encode(qrCodeData,
					BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
		} catch (WriterException e) {
			e.printStackTrace();
			return false;
		}
		
		MatrixToImageConfig DEFAULT_CONFIG = new MatrixToImageConfig(
				0xff000000 | color.getRGB(),
				MatrixToImageConfig.WHITE
				);
		BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix, DEFAULT_CONFIG );
		
		//BufferedImage overlay = ImageIO.read(new File(overlayPath));
		BufferedImage overlay = null;
        try ( FileInputStream inputStream = new FileInputStream(overlayPath) ) {
            // reads input image from file
        	overlay = ImageIO.read(inputStream);
        } catch (IOException e1) {
            logger.error("IOException {}", e1);
            return false;
        }
		BufferedImage newBufferedImage = new BufferedImage(OVERLAY_W, OVERLAY_H, BufferedImage.TYPE_INT_RGB);
		newBufferedImage.createGraphics().drawImage(overlay, 0, 0, null);
		overlay = newBufferedImage;
		
		//Calculate the delta height and width
		int deltaHeight = image.getHeight() - overlay.getHeight();
		int deltaWidth  = image.getWidth()  - overlay.getWidth();
		
		//Draw the new image
		BufferedImage combined = new BufferedImage(qrCodewidth, qrCodeheight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) combined.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)); // [4]
		g.drawImage(overlay, (int)Math.round(deltaWidth/2), (int)Math.round(deltaHeight/2), null);
		 
//		File imageFile = new File(filePath);
//		ImageIO.write(combined, ext, imageFile);
		try ( FileOutputStream outputStream = new FileOutputStream(filePath) ) {
            // writes to the output image in specified format
            return ImageIO.write(combined, ext, outputStream);
        } catch (IOException e2) {
            logger.error("IOException {}", e2);
            return false;
        }
		
		//return true;
	}

	public static void createQRCode(String qrCodeData, 
			String charset, Map hintMap, int qrCodewidth, int qrCodeheight, 
			String filePath, String ext)
			throws WriterException, IOException {
		BitMatrix matrix = new MultiFormatWriter().encode(
				//new String(qrCodeData.getBytes(charset), charset),
				qrCodeData,
				BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
		//String ext = filePath.substring(filePath.lastIndexOf('.') + 1);
		MatrixToImageWriter.writeToFile(matrix, ext, new File(filePath));
	}
	
	public static void createQRCode_shortest(String qrCodeData, 
			String charset, Map hintMap, int qrCodewidth, int qrCodeheight, 
			String filePath, String ext)
			throws WriterException, IOException {
		MatrixToImageWriter.writeToPath(
				new QRCodeWriter().encode(qrCodeData, BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap),
				ext, new File(filePath).toPath()
				);
	}
	

	public static QRCodeResult readQRCode(String filePath, String charset, Map hintMap)
			//throws FileNotFoundException, IOException, NotFoundException 
	{
		QRCodeResult qrcodeResult = new QRCodeResult();
		qrcodeResult.status = -10;
		
		//BufferedImage image = ImageIO.read(new FileInputStream(filePath));
		BufferedImage inputImage = null;
        try ( FileInputStream inputStream = new FileInputStream(filePath) ) {
            // reads input image from file
            inputImage = ImageIO.read(inputStream);
        } catch (IOException e1) {
        	qrcodeResult.status = -5;
            logger.error("IOException {}", e1);
            return qrcodeResult;
        }
		BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
				new BufferedImageLuminanceSource( inputImage )));
		Result result; 
		
		qrcodeResult.status = -1;
		try {
			//qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
			
			result = new QRCodeReader().decode(binaryBitmap, hintMap);
		} catch (NotFoundException e) {
			qrcodeResult.status = 2;
			logger.error("Could not read with QRCodeReader - NotFoundException {}", e);
			//e.printStackTrace();
			return qrcodeResult;
		} catch (ChecksumException e) {
			qrcodeResult.status = 1;
			logger.error("Could not read with QRCodeReader - ChecksumException {}", e);
			//e.printStackTrace();
			return qrcodeResult;
		} catch (FormatException e) {
			qrcodeResult.status = 3;
			logger.error("Could not read with QRCodeReader - FormatException {}", e);
			//e.printStackTrace();
			return qrcodeResult;
		}
		qrcodeResult.text = result.getText();
		//TODO show more of Result contents
		return qrcodeResult;
	}
}