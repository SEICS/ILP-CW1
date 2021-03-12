package uk.ac.ed.inf.heatmap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.FileReader;
import java.io.*;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class App 
{
	public static void main(String[] args ) {
    	//eclipse doesn't input argument automatically
    	//Will occur IndexOutOfBound Exception for reading args[0] from the command line
    	if (args.length == 0) {
    	    System.out.println("No proper predictions.txt file read");
    	    return;
    	}
    	var filepath = args[0];
    	var predictions = getPredictions(filepath);
    	grid(predictions);
    	
    }

    //Set the map
    //get the 10x10 predictions
    public static void grid(ArrayList<Integer> prediction) {
    	//The width of a single polygon
    	double increRow = Math.abs(Math.abs(-3.192473)-Math.abs(-3.184319))/10;
    	//The length of a single polygon
    	double increColumn = Math.abs(55.942617-55.946233)/10;
    	
    	//list of features of the grid
    	var featureList = new ArrayList<Feature>();
    	
    	for (int i=0; i<=9; i++) {
    		for (int j=0; j<=9; j++) {
    			var polygonPl = new ArrayList<Point>();
    			var polygonPoints = new ArrayList<List<Point>>();
    			
    			//Five points that defines a single polygon
    			var A = Point.fromLngLat(-3.192473+increRow*j, 55.946233-increColumn*i);	
    			var B = Point.fromLngLat(-3.192473+increRow*j, 55.946233-increColumn*(i+1));
    			var C = Point.fromLngLat(-3.192473+increRow*(j+1), 55.946233-increColumn*(i+1)); 
    			var D = Point.fromLngLat(-3.192473+increRow*(j+1), 55.946233-increColumn*i);				
    			var end = A;
    			
    			//store five points as one polygon
    			polygonPl.add(A);
    			polygonPl.add(B);
    			polygonPl.add(C);
    			polygonPl.add(D);
    			polygonPl.add(end);
    			polygonPoints.add(polygonPl);

    			//construct the polygon with points
    			var polygon = Polygon.fromLngLats(polygonPoints);
    			var feature = Feature.fromGeometry(polygon);
    			feature.addNumberProperty("fill-opacity", 0.75);
    			
    			//corresponding prediction reading for this polygon
    			Integer predictions = prediction.get(i*10+j);
    			//corresponding prediction colour and marker
    			var colour = getColour(predictions);
    			//var marker = getMarker(prediction);
    			
    			//fill colour to single polygon
    			feature.addStringProperty("rgb-string", colour);
    			feature.addStringProperty("fill", colour);
    			featureList.add(feature);
    		}
    	}
    	
    	//get features from grid
    	var features = FeatureCollection.fromFeatures(featureList);

    	//create or write the file heatmap.geojson
        File myGeoJson = new File("heatmap.geojson");
        try { 
	        //check if the file creates successfully
	        if (myGeoJson.createNewFile()) {       
	              //new file created
	              System.out.println("File created: " + myGeoJson.getName());
			} else {
			      //file exists, just overwrite it
			      System.out.println("File already exists.");
	        }
        } catch (IOException e) {
	        System.out.println("Error: " + e);
	        e.printStackTrace();
        }
        
    	//convert defined grid into json
    	var line = features.toJson();
        byte data[] = line.getBytes();
        //write into the file called "heatmap.geojson"
        Path p = Paths.get("./heatmap.geojson");
        try (OutputStream out = new BufferedOutputStream(
        		Files.newOutputStream(p))) {
          out.write(data, 0, data.length);
          out.close();
        } catch (IOException e) {
          System.err.println(e);
        } 
    }
    	
  //read the predictions.txt file and convert its value into all integers
    public static ArrayList<Integer> getPredictions(String path) {
    	//List that 100 numeric predictions readings will be stored
        var readings = new ArrayList<Integer>();
    	try{
    		
            //Empty string for storing future readings from file
            String data = "";
            //reading the file with FileReader and BufferedReader from given file path
            File file = new File(path);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            //read the first line of the file
            String line = bufferedReader.readLine();
            
            //If not end of file, keeps reading
            while (line != null){
                data += line+",";
                line = bufferedReader.readLine();
            }
            //remove spaces from the readings
            data = data.replaceAll("\\s","");
           
		    //split predictions by comma
            String[] data2 = data.split(",");
            
            //predictions are 10x10(total 10 rows, each row has 10 readings)
            var numRow = 10;
		    var numCol = 10;
		
		      //turn into numeric readings
		    try {
	            for (int i=0; i<numRow*numCol; i++) {
	                var value = data2[i];
	                readings.add(Integer.parseInt(value));
	            } 
		    } catch (NumberFormatException e) {
		    	  
            }
            bufferedReader.close();
            fileReader.close();
        } catch(IOException e){
        	System.out.println("Error: " + e);
            e.printStackTrace();
        }
    	return readings;
    }
    
    public static String getColour(Integer prediction){
    	//Set of colours gonna be used
    	var Green = "#00ff00";
    	var MediumGreen = "#40ff00";
    	var LightGreen = "#80ff00";
    	var LimeGreen = "#c0ff00";
    	var Gold = "#ffc000";
    	var Orange = "#ff8000";
    	var RedOrange = "#ff4000";
    	var Red = "#ff0000";
    	var Black = "#000000";
    	var Grey = "#aaaaaa";
    	
    	Integer range = prediction;
    	//battery doesn't matter in cw1, here just forever set a true condition to stimulate 
    	//this defines all conditions of which colour gonna be used
    	var battery = 100;
    	if (battery<=10) {
    		//black
    		return Black;
    	}else {
    		if ((range<32)&(range>=0)){
        		//green
        		return Green;
        	}else if((range<64)&(range>=32)){
        		//medium green
        		return MediumGreen;
        	}else if((range<96)&(range>=64)){
        		//light green
        		return LightGreen;
        	}else if((range<128)&(range>=96)){
        		//lime green
        		return LimeGreen;
        	}else if((range<160)&(range>=128)){
        		//gold
        		return Gold;
        	}else if((range<192)&(range>=160)){
        		//orange
        		return Orange;
        	}else if((range<224)&(range>=192)){
        		//red/orange
        		return RedOrange;
        	}else if((range<256)&(range>=224)){
        		//red
        		return Red;
        	}else {
        		//grey
        		return Grey;
        	}
    	}	
    }
    
    //get corresponding marker from prediction
    public static String getMarker(Integer prediction) {
    	Integer range = prediction;
    	//same as the battery comment above
    	var battery = 100.0;
    	if (battery<=10) {
    		//black
    		return "cross";
    	}else {
    		if ((range<128)&(range>=0)){
        		//green/medium green/light green/lime green all marked with lighthouse
    			return "lighthouse";
        	}else if((range<256)&(range>=128)){
        		//gold/orange/(read/orange)/red all marked with danger
        		return "danger";
        	}else {
        		//grey
        		return "";
        	}
    	}
    }
}