package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class MovieRecommender {
    private String path;
    private String csvPath;
    private String dataFileM;
    private UserBasedRecommender recommender;
    BiMap<String, Long> users;
    BiMap<String, Integer> movies;
    BiMap<Integer, String> invertedMovies; 
    private int totalReviews;

    public MovieRecommender(String in_path) throws IOException, TasteException   { 
        path = in_path;
        users = HashBiMap.create();
        movies = HashBiMap.create();
        invertedMovies = HashBiMap.create();
        totalReviews = 0; 

        csvPath = createCSVFile(path);
        dataFileM = parseCSVFile();
    }

    public List<String> getRecommendationsForUser(String in_user) throws IOException, TasteException {
        Long indexUser = users.get(in_user);
        List<String> outputList = new ArrayList<String>();
        System.out.println("Generation recommendations, please wait:");
        DataModel model = new FileDataModel(new File(dataFileM));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        recommender = new GenericUserBasedRecommender(model, neighborhood, similarity); 

        List<RecommendedItem> recommendations = recommender.recommend(indexUser, 3);

        for (RecommendedItem recommendation : recommendations) {
            Integer i = (int) (long) recommendation.getItemID();
            String movieID = invertedMovies.get(i);
            System.out.println(movieID);
            outputList.add(movieID);
        }
        return outputList;
    }

    private String createCSVFile(String in_path) throws IOException, TasteException {
        //File Creation
        File csv = new File("movies.csv");
        if (!csv.exists())
        {
            csv.createNewFile();
            FileInputStream fileInput = new FileInputStream(path);
            GZIPInputStream gzis = new GZIPInputStream(fileInput);
            FileOutputStream fileOutput = new FileOutputStream(csv);
            //file decompress process.
            byte[] buffer = new byte[1024];
            int length;
            while ((length = gzis.read(buffer)) > 0) 
                fileOutput.write(buffer, 0, length);

            fileInput.close();
            gzis.close();
            fileOutput.close();
        }
        return csv.getAbsolutePath();
    }
    
    private String parseCSVFile() throws IOException, TasteException {
        
        File fileDataModel = new File("fileDataModel.csv");
        if(fileDataModel.exists())
            fileDataModel.delete();
        fileDataModel.createNewFile();
        try {
            FileWriter fileOutput = new FileWriter(fileDataModel.getAbsolutePath());
            BufferedWriter fileWriter = new BufferedWriter(fileOutput);
            BufferedReader reader = new BufferedReader(new FileReader(csvPath));
            String writerString = "";
            long userCount = 0;
            int moviesCount = 0;

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String dataLine;
                if(line.startsWith(("product/productId"))) //prodID
                {
                    dataLine = line.split(" ")[1];
                    if(!movies.containsKey(dataLine))
                    {
                        movies.put(dataLine, moviesCount);
                        invertedMovies.put(moviesCount,dataLine);
                        writerString = moviesCount + ",";
                        moviesCount++;
                    }
                    else
                        writerString = (movies.get(dataLine) + ",");  
                }
                else if(line.startsWith(("review/userId")))  //userID
                {
                    dataLine = line.split(" ")[1];
                    if(!users.containsKey(dataLine))
                    {
                        users.put(dataLine, userCount);
                        writerString = userCount + "," + writerString;
                        userCount++;
                    }
                    else
                        writerString = users.get(dataLine) + "," + writerString; 
                }
                else if(line.startsWith(("review/score"))) //score
                {
                    totalReviews++;
                    String reviewScore = line.split(" ")[1];
                    writerString += reviewScore + "\n";
                    fileWriter.write(writerString);
                }
            }
            fileWriter.close();
            fileOutput.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    return fileDataModel.getAbsolutePath();
    }

    public int getTotalProducts() {
        return movies.size();
    }

    public int getTotalUsers() {
        return users.size();
    }

    public int getTotalReviews() {
        return totalReviews;
    }
}