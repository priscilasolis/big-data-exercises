package nearsoft.academy.bigdata.recommendation;

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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
//import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class MovieRecommender{

    int reviews = 0, userNum = 1, productNum = 1;
    HashMap<String, Integer> users = new HashMap<String, Integer>();
    HashMap<String, Integer> products = new HashMap<String, Integer>();


public MovieRecommender(String file){
    List<String> usersList = new ArrayList<String>();
    List<String> productsList = new ArrayList<String>();
    List<String> scoresList = new ArrayList<String>();

    try{
        InputStream fileStream = new FileInputStream(file);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream);
        BufferedReader buffered = new BufferedReader(decoder);

        PrintWriter pw = new PrintWriter(new File("movies.csv"));

        String line;
        StringBuilder sb = new StringBuilder();

        while ((line = buffered.readLine()) != null){
            if(line.contains("review/userId:")){
                String[] parts = line.split("review/userId: ");
                if(!users.containsKey(parts[1])) {
                    users.put(parts[1], userNum);
                    usersList.add("" + userNum);
                    userNum++;
                } else{
                    usersList.add("" + users.get(parts[1]));
                }
            }else if(line.contains("product/productId:")){
                reviews++;
                String[] parts = line.split("product/productId: ");

                if(!products.containsKey(parts[1])) {
                    products.put(parts[1], productNum);
                    productsList.add("" + productNum);
                    productNum++;
                } else{
                    productsList.add("" + products.get(parts[1]));
                }
            }else if(line.contains("review/score:")){
                String[] parts = line.split("review/score: ");
                scoresList.add(parts[1]);
            }
        }

        for (int counter = 0; counter < usersList.size(); counter++) {
            pw.write(usersList.get(counter) + ',' + productsList.get(counter) + ',' + scoresList.get(counter) + '\n');
            pw.println();
        }

        pw.close();

        //pw.write(sb.toString());
        //pw.close();
    } catch(FileNotFoundException e) {
        System.err.println("Caught FileNotFoundException: " + e.getMessage());
    } catch(IOException e) {
        System.err.println("Caught IOException: " + e.getMessage());
    }
}

public int getTotalReviews(){
    return reviews;
}

public int getTotalProducts(){
    return products.size();
}

public int getTotalUsers(){
    return users.size();
}

public <String> List<String> getRecommendationsForUser(String userID){
    List<RecommendedItem> recommendations = null;


    try{
        DataModel model = new FileDataModel(new File("movies.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        recommendations = recommender.recommend(users.get(userID), 3);

    } catch(IOException e) {
        System.err.println("Caught IOException: " + e.getMessage());
    } catch(TasteException e){
        System.err.println("Caught TasteException: " + e.getMessage());
    }

    List<String> results = new ArrayList<String>();
    for (RecommendedItem recommendation : recommendations) {
        results.add((String)getProductID((int)recommendation.getItemID()));
    }

    return results;
}

public String getProductID(int value){
    for (String s : products.keySet()) {
        if (products.get(s)==value) {
            return s;
        }
    }
    return null;
}

}

