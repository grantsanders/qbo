package com.fx.qbo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileHandler {
    
    private String path;

    public FileHandler(String filePath) {
        path = "/Users/grantsanders/Downloads/BlueCartAccounting Report_08102022_1133pm.csv";
    }

    public void formatData() { // run filepath and pull data from CSV to be stored
        try {
        File inFile = new File(path);
        FileReader input = new FileReader(inFile);
        Scanner in = new Scanner(input);
        
        String paramLine = in.nextLine();
        String[] params = paramLine.split(",");
        int counter = 0;
        while(in.hasNext()) {
            String line = in.nextLine();
            

            String[] split = line.split(",");
            
            System.out.println("Line item " + counter + "\n");
            ArrayList<String> itemList = new ArrayList<String>();

            for (int i = 0; i < split.length; i++) {
                            // System.out.println((i) + " " + params[i] + " : " + split[i]);
                            itemList.add(split[i]);

            } counter++;

            CSVIntermediate object = new CSVIntermediate();
            object.setAllFields(itemList);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            System.out.println(gson.toJson(object));

            


        }
    } catch(FileNotFoundException e) {
        Popup FileNotFoundException = new Popup("FileNotFoundException", "Error: File not found");
    }
    }

}
