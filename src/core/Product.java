package core;

public class Product {
	
	String pid;
	String name;
	String imgurl;
	
	public Product(String pid, String name, String imgurl) {
		this.pid = pid;
		this.name = name;
		this.imgurl = imgurl;
	}
	
	public String getPid() {
		return pid;
	}
	
	public String getName() {
		return name;
	}

	public String getImgurl() {
		return imgurl;
	}
	
}
