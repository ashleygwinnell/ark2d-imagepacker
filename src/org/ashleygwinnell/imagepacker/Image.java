package org.ashleygwinnell.imagepacker;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Image {
	private String m_name;
	private String m_path;
	private java.awt.image.BufferedImage m_image;

	int index;
	int x;
	int y;

	int m_width;
	int m_height;

	int m_originalWidth;
	int m_originalHeight;

	boolean m_rotated;
	boolean m_trimmed;

	boolean m_hasBeenLaid;
	TrimmedImage m_trimmedImage = null;;

	public Image(File f) throws IOException {
		java.awt.image.BufferedImage img = ImageIO.read(f);

		index = 0;
		x = 0;
		y = 0;

		m_image = img;
		m_name = f.getName().replace('\\', '/');
		m_path = f.getAbsolutePath();
		m_width = img.getWidth(null);
		m_height = img.getHeight(null);
		m_originalWidth = img.getWidth(null);
		m_originalHeight = img.getHeight(null);

		m_rotated = false;

		m_hasBeenLaid = false; // used by force pack strategy
	}

	public boolean collides(Image other) {
		ImagePacker p = ImagePacker.getInstance();
		int x1 = x - p.getSpacing();
		int x2 = other.x - p.getSpacing();
		int y1 = y - p.getSpacing();
		int y2 = other.y - p.getSpacing();
		int w1 = m_width + (2*p.getSpacing());
		int w2 = other.m_width + (2*p.getSpacing());
		int h1 = m_height + (2*p.getSpacing());;
		int h2 = other.m_height + (2*p.getSpacing());;
		if ((x1 + w1) >= x2 && x1 <= (x2 + w2) && (y1 + h1) >= y2 && y1 <= (y2 + h2))  {
			return true;
		}
		return false;
	}

	public void setName(String name) {
		m_name = name.replace('\\', '/');
	}
	public String getName() {
		return m_name;
	}
	public String getNameNoExtension() {
		if (m_name.contains(".")) {
			return m_name.substring(0, m_name.lastIndexOf("."));
		}
		return m_name;
	}

	public void setPath(String path) {
		m_path = path;
	}
	public String getPath() {
		return m_path;
	}

	public void setAWTImage(java.awt.image.BufferedImage i) {
		m_image = i;
	}
	java.awt.image.BufferedImage getAWTImage() {
		return m_image;
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setSize(int width, int height) {
		m_width = width;
		m_height = height;
	}
	public int getWidth() {
		return m_width;
	}
	public int getHeight() {
		return m_height;
	}
	public int getOriginalWidth() {
		return m_originalWidth;
	}
	public int getOriginalHeight() {
		return m_originalHeight;
	}
	public void rotate() {
		m_rotated = !m_rotated;

		int h = m_height;
		m_height = m_width;
		m_width = h;
	}
	public boolean isRotated() {
		return m_rotated;
	}

	public void trim() {
		TrimmedImage img = trimImageImpl(m_image);
		if (img.x != 0 ||
			img.y != 0 ||
			img.width != m_width ||
			img.height != m_height)
			{
			m_trimmed = true;
			m_trimmedImage = img;
			m_image = m_trimmedImage.img;

			m_width = m_trimmedImage.width;
			m_height = m_trimmedImage.height;
			x += m_trimmedImage.x;
			y += m_trimmedImage.y;

			//System.out.println("Trimmed " + m_name + " from w:" + m_width + " h:" + m_height + " to w:" + m_trimmedImage.width + " h:" + m_trimmedImage.height);
		}
	}


	public boolean isTrimmed() {
		return m_trimmed;
	}
	public int getTrimmedWidth() {
		return (m_trimmedImage != null)
			? m_trimmedImage.width
			: m_width;
	}
	public int getTrimmedHeight() {
		return (m_trimmedImage != null)
			? m_trimmedImage.height
			: m_height;
	}
	public int getTrimX() {
		return (m_trimmedImage != null)
			? m_trimmedImage.x
			: x;
	}
	public int getTrimY() {
		return (m_trimmedImage != null)
			? m_trimmedImage.y
			: y;
	}

	public static class TrimmedImage {
		public BufferedImage img;
		public int x;
		public int y;
		public int width;
		public int height;
		public TrimmedImage(){

		}
	}
	public static Image.TrimmedImage trimImageImpl(BufferedImage image) {
	    return trimSlowButAccurateStrategy(image);
	}
	public static Image.TrimmedImage trimSlowButAccurateStrategy(BufferedImage image) {
		int width = image.getWidth();
	    int height = image.getHeight();
	    int top = 0;
	    int bottom = height;
	    int left = 0;
	    int right = width;

	    findTop:
        for (int y = 0; y < height; y++) {
        	for (int x = 0; x < width; x++) {
        		int val = image.getRGB(x, y);
        		int alphaval = (val >> 24) & 0xff;
        		//int greenval = (val >> 16) & 0xff;
        		//int blueval = (val >> 8) & 0xff;
        		//int redval = val & 0xff;
        		//System.out.println("x:" + x + " y:" + y + " = r:" + redval + " g:" + greenval + " b:" + blueval + " a:" + alphaval);
	            if (alphaval > 0) {
	                top = y;
	                break findTop;
	            }
	        }
        }

		findBottom:
        for (int y = height-1; y >= 0; y--) {
        	for (int x = 0; x < width; x++) {
        		int val = image.getRGB(x, y);
        		int alphaval = (val >> 24) & 0xff;//val & 0xff;
	            if (alphaval > 0) {
	                bottom = (y+1 > height)?y:y+1;
	                break findBottom;
	            }
	        }
        }
        findLeft:
        for (int x = 0; x < width; x++) {
        	for (int y = 0; y < height; y++) {
        		int val = image.getRGB(x, y);
        		int alphaval = (val >> 24) & 0xff;//val & 0xff;
	            if (alphaval > 0) {
	                left = x;
	                break findLeft;
	            }
	        }
        }
        findRight:
        for (int x = width - 1; x >= 0; x--) {
        	for (int y = 0; y < height; y++) {
        		int val = image.getRGB(x, y);
        		int alphaval = (val >> 24) & 0xff;//val & 0xff;
	            if (alphaval > 0) {
	                right = (x+1 > width)?x:x+1;
	                break findRight;
	            }
	        }
        }


	    Image.TrimmedImage trimmedimg = new Image.TrimmedImage();;
	    trimmedimg.img = image.getSubimage(left, top, right - left, bottom - top);
	    trimmedimg.x = left;
	    trimmedimg.y = top;
	    trimmedimg.width = right - left;
	    trimmedimg.height = bottom - top;
	    return trimmedimg;
	}
	public static Image.TrimmedImage trimStarStrategy(BufferedImage image) {
		int width = image.getWidth();
	    int height = image.getHeight();
	    int top = height / 2;
	    int bottom = top;
	    int left = width / 2 ;
	    int right = left;
	    for (int x = 0; x < width; x++) {
	        for (int y = 0; y < height; y++) {
	            if (image.getRGB(x, y) != 0){
	                top    = Math.min(top, x);
	                bottom = Math.max(bottom, x);
	                left   = Math.min(left, x);
	                right  = Math.max(right, x);
	            }
	        }
	    }

	    Image.TrimmedImage trimmedimg = new Image.TrimmedImage();;
	    trimmedimg.img = image.getSubimage(left, top, right - left, bottom - top);
	    trimmedimg.x = left;
	    trimmedimg.y = top;
	    trimmedimg.width = right - left;
	    trimmedimg.height = bottom - top;
	    return trimmedimg;
	}
	public static Image.TrimmedImage trimOptimisedStrategy(BufferedImage image) {
		WritableRaster raster = image.getAlphaRaster();
	    int width = raster.getWidth();
	    int height = raster.getHeight();
	    int left = 0;
	    int top = 0;
	    int right = width - 1;
	    int bottom = height - 1;
	    int minRight = width - 1;
	    int minBottom = height - 1;

	    top:
	    for (;top < bottom; top++){
	        for (int x = 0; x < width; x++){
	            if (raster.getSample(x, top, 0) != 0){
	                minRight = x;
	                minBottom = top;
	                break top;
	            }
	        }
	    }

	    left:
	    for (;left < minRight; left++){
	        for (int y = height - 1; y > top; y--){
	            if (raster.getSample(left, y, 0) != 0){
	                minBottom = y;
	                break left;
	            }
	        }
	    }

	    bottom:
	    for (;bottom > minBottom; bottom--){
	        for (int x = width - 1; x >= left; x--){
	            if (raster.getSample(x, bottom, 0) != 0){
	                minRight = x;
	                break bottom;
	            }
	        }
	    }

	    right:
	    for (;right > minRight; right--){
	        for (int y = bottom; y >= top; y--){
	            if (raster.getSample(right, y, 0) != 0){
	                break right;
	            }
	        }
	    }

	    Image.TrimmedImage trimmedimg = new Image.TrimmedImage();;
	    trimmedimg.img = image.getSubimage(left, top, right - left, bottom - top);
	    trimmedimg.x = left;
	    trimmedimg.y = top;
	    trimmedimg.width = right - left;
	    trimmedimg.height = bottom - top;
	    return trimmedimg;
	}

}
