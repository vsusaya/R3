package core;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;


public class ProductButton extends JButton {
	
	private static final int IMG_HEIGHT = 80;
	private static final int IMG_WIDTH = 80;
	
	private static final int BTN_HEIGHT = 90;
	private static final int BTN_WIDTH = 200;
	
	String pid;
	String imgurl;
	
	ProductButton(String name) {
		super(name);
		//this.setLayout(new FlowLayout());
		this.setPreferredSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
	}
	
	public void setPid(String pid) {
		this.pid = pid;
	}
	
	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}

	public String getPid() {
		return pid;
	}
	
	private String getImgurl() {
		return imgurl;
	}
	
	public void setImage() {
		
		//get image from url
		Image origImage = null;
		try {
			URL url = new URL(getImgurl());
			origImage = ImageIO.read(url);
			Image image = origImage.getScaledInstance(IMG_WIDTH, IMG_HEIGHT, java.awt.Image.SCALE_SMOOTH);
			this.setIcon(new ImageIcon(image));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
