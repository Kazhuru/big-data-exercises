package nearsoft.academy.bigdata.recommendation;

import static org.junit.Assume.assumeNoException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.management.Query;

import com.google.common.collect.HashBiMap;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class MovieRecommender {
    private String path;
    private String csvPath;
    private GenericUserBasedRecommender gubRecommender;
    private int totalReviews;
    private HashBiMap<String, Integer> users;
    private HashBiMap<String, Integer> products;
    private FastByIDMap<PreferenceArray> prefArray;

    public MovieRecommender(String in_path) throws IOException, TasteException   { 
        path = in_path;
        totalReviews = 0;
        users = HashBiMap.create();
        products = HashBiMap.create();
        csvPath = createCSVFile(path);


        //A MIRACLE HAPPENS HERE
        
        GenericDataModel model = new GenericDataModel(prefArray);
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        gubRecommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public int getTotalProducts() {
        return products.size();
    }

    public int getTotalUsers() {
        return users.size();
    }

    public List<String> getRecommendationsForUser(String in_user) throws IOException, TasteException {
        return null;
    }

    private String createCSVFile(String in_path) throws IOException, TasteException  {
        //File Creation
        File csv = new File("movies.csv");
        if (csv.exists()) 
            csv.delete();
        else
        {
            csv.createNewFile();
            //input stream to read the source file.
            FileInputStream fileInput = new FileInputStream(path);
            //Gzip input stream to decompress the source
            GZIPInputStream gzis = new GZIPInputStream(fileInput);
            //Output stream where the decompress result
            FileOutputStream fileOutput = new FileOutputStream(csv);

            //file decompress process.
            byte[] buffer = new byte[1024];
            int length;
            while ((length = gzis.read(buffer)) > 0) 
                fileOutput.write(buffer, 0, length);

            //closing the files.
            fileInput.close();
            gzis.close();
            fileOutput.close();
        }

        return csv.getPath();
    }
    
    private void parseCSVFile() {
        
        BufferedReader reader;
		try {
            reader = new BufferedReader(new FileReader(csvPath));
            String[] splitLine;
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
                if(line.startsWith(("product/productId"))) {
                    splitLine = line.split(" ");
                    //TODO
                }
                //MORE MAGIC HERE
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}