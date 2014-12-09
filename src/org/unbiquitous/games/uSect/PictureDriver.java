package org.unbiquitous.games.uSect;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import javax.imageio.ImageIO;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.driverManager.UosDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Call.ServiceType;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
import org.apache.commons.io.*;;

public class PictureDriver implements UosDriver, Runnable {
	
	@Override
	public void destroy() {
	}

	@Override
	public UpDriver getDriver() {
		UpDriver driver = new UpDriver("PictureDriver");
		driver.addService("sendPicture");
		return driver;
	}

	@Override
	public List<UpDriver> getParent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings("unused")
	public void run(Response r) throws IOException{
		final DataInputStream input = r.getMessageContext().getDataInputStream();
		final byte[] bytes = IOUtils.toByteArray(input);
		final File img;
		
		Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				
				try {
					BufferedImage img = ImageIO.read(input);
					ImageIO.write(img, "jpg", new File(System.getProperty("user.dir") + "/imagem.jpg"));
				} catch(IOException e){
					e.printStackTrace();
				}
				
				/*try {
				img = new File(System.getProperty("user.dir") + "/imagem.jpg");
				FileOutputStream imgOut = new FileOutputStream(img);
				
				for (byte b : bytes)	
					imgOut.write(b);
				
				imgOut.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
			}
		});
	}

	@Override
	public void init(Gateway gw, InitialProperties prop, String arg2) {
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
