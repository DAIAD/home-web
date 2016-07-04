import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.math.plot.Plot2DPanel;


public class Plotter {
	int width;
	int height;
	Plot2DPanel plot;
	
	public Plotter(int w,int h){
		width=w;
		height=h;
		plot = new Plot2DPanel();
		plot.addLegend("EAST");
	}
	
	public void addLine(ArrayList<Double> line,String name){
		double[] d = DataPreprocessor.Dtod(line);
		plot.addLinePlot(name, d);
	}
	
	public void addBar(ArrayList<Double> line,String name){
		double[] d = DataPreprocessor.Dtod(line);
		plot.addBarPlot(name, d);
	}
	
	public void drawPlot(){
		JFrame frame = new JFrame("");
		frame.setContentPane(plot);
		frame.setSize(width,height);
		frame.setVisible(true);
	}
	public void singleHistogram(String name,double[][] r){
		Plot2DPanel plot = new Plot2DPanel();
		plot.addHistogramPlot(name,r);
		JFrame frame = new JFrame(name);
		frame.setContentPane(plot);
		frame.setSize(width,height);
		frame.setVisible(true);
	}
	
	public void dPlot(double[] d){
		Plot2DPanel plot = new Plot2DPanel();
		plot.addLinePlot("", d);
		JFrame frame = new JFrame("");
		frame.setContentPane(plot);
		frame.setSize(width,height);
		frame.setVisible(true);
		
	}
	
	public void saveSinglePlot(String name,double[][] r) throws IOException, InterruptedException{
		Plot2DPanel plot = new Plot2DPanel();
		plot.addBarPlot(name,r[1],r[0]);
		JFrame frame = new JFrame(name);
		frame.setContentPane(plot);
		frame.setSize(width,height);
		frame.setVisible(true);
		Thread.sleep(500);
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		frame.paint(image.getGraphics());
		ImageIO.write(image, "png", new File("/home/pant/Desktop/experiments/graphs/pmfs/".concat(name)));
		frame.dispose();
	}
	
	public void plotTS(ArrayList<Double> line,String name){
		double[] d = DataPreprocessor.Dtod(line);
		plot.addLinePlot(name, d);
		JFrame frame = new JFrame(name);
		frame.setContentPane(plot);
		frame.setSize(width,height);
		frame.setVisible(true);
	}
	public void savePlot(int id,int k,int b) throws IOException, InterruptedException{
		JFrame frame = new JFrame("k=".concat(Integer.toString(k)).concat("-b=").concat(Integer.toString(b)));
		frame.setContentPane(plot);
		frame.setSize(width,height);
		frame.setVisible(true);
		Thread.sleep(500);
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		frame.paint(image.getGraphics());
		ImageIO.write(image, "png", new File("/home/pant/Desktop/experiments/graphs/id=".concat(Integer.toString(id)).concat("-k=").concat(Integer.toString(k)).concat("-b=").concat(Integer.toString(b))));
		frame.dispose();
	}
	
}
