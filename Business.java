/* hw5
 Name: Zhengyang Qi
 SID: 204296544
 */

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

/*
 * Class to perform Big Yelp data analysis
 */
public class Business {
    private String businessID;
    private String businessName;
    private String businessAddress;
    private String reviews;
    private int CharacterCount;
    public static HashSet<String> DocCorpus = new HashSet<String>();
    static String longreview = "";
    
 
    /*
     * Convert read in file to a String
     * @param in is a inputStream
     */
    public static String Allbusiness(InputStream in) throws UnsupportedEncodingException, IOException{
        StringBuilder inputStringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line = bufferedReader.readLine();
        while(line != null){
            inputStringBuilder.append(line);inputStringBuilder.append('\n');
            line = bufferedReader.readLine();
        }
        
        return inputStringBuilder.toString();
    }
    
    /*
     * Constructor
     */
    public Business(String ID, String name, String adress, String review){
        businessID = ID;
        businessName = name;
        businessAddress = adress;
        reviews = review;
        int counter = 0;
        // Count charater in reviews
        for (int i = 0; i < review.length(); i++) {
            if (Character.isLetter(review.charAt(i)))
                counter++;
        }
        CharacterCount = counter;
        
    }
    
    /*
     * Fucntion to read in business
     * @param input is the string of all info
     */
    public static Business readBusiness(String input ){
        // Split the string
        String[] eachItem = input.split(",");
        return new Business(eachItem[0],eachItem[1],eachItem[2],eachItem[3]);
    }
    
    /*
     * Function to get the document count and add it to the map
     * @param map is the map store the document count of each word
     */
    public static void addDocumentCount(Map<String,Integer> map, Business b){
        longreview = b.reviews;
        String[] eachItem = longreview.split(" ");
        
        Set<String> words = new HashSet<String>();
        // Store words in a set to avoid repetition
        for(int i = 0; i < eachItem.length; i++){
            words.add(eachItem[i]);
        }
        
        String[] reviews = words.toArray(new String[0]);
        
        // store all words and count the number of document
        for(String temp : reviews){
            
            if(map.containsKey(temp)){
                map.put(temp, map.get(temp) + 1);
            }
            else{
                map.put(temp, 1);
            }
        }
        
        
    }
    
    /*
     * Implement Comparator to compare business according to the number of characters in review
     */
    public static class CompareBusiness implements Comparator<Business>{
        public int compare(Business b1, Business b2){
            if(b1.CharacterCount < b2.CharacterCount){return 1;}
            else if (b1.CharacterCount > b2.CharacterCount){return -1;}
            else{return 0;}
        }
    }
    
    /*
     * Function to calculate the TfidfScore and store it in the list
     * @param socreList the the list storing the Map.Entry
     */
    public static void getTfidfScore(List<Map.Entry<String, Double>> socreList,
                                     Map<String,Integer> DFCount, Business b, int limit){
    	
    	String[] review = b.reviews.split(" ");
    	
        // Create a new map that dose not contain the repetitive words
        HashMap<String,Integer> renewal = new HashMap<String,Integer>();
        
        for(String temp : review){
                if(renewal.containsKey(temp)) {renewal.put(temp, renewal.get(temp) + 1);}
                else{renewal.put(temp, 1);}
        }
        
        // Loop through all the keys in the renewal map
        for (String currentKey: renewal.keySet()){
            String key =currentKey;
            int value = DFCount.get(currentKey);
            
            // Store the pairs
            if(value < limit){socreList.add(new HashMap.SimpleEntry<String,Double>(key,(double) 0));}
            else{socreList.add(new HashMap.SimpleEntry<String,Double>(key,(double) (renewal.get(key))/value));}
        }
    }
    
    /*
     * Compare the Map.Entry according to the tfidfScore
     */
    public static void sortByTfidf(List<Map.Entry<String, Double>> tfidfScoreList){
        Collections.sort(tfidfScoreList, new compareScoreList());
        
    }
    
    /*
     * Implement Comparator to compare business according to the number tfidfScore
     */
    static public class compareScoreList implements Comparator<Map.Entry<String, Double>>{
        public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2){
            if(e1.getValue()<e2.getValue()){return 1;}
            else if(e1.getValue()>e2.getValue()){return -1;}
            else{return 0;}
        }
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(){
        String result = "------------------------------------------------------------------------------ \n";
        result = result + "Business ID: " + businessID + "\n";
        result = result + "Business Name: " + businessName + "\n";
        result = result + "Business Address: " + businessAddress + "\n";
        result = result + "Character Count: " + CharacterCount;
        
        return result;
    }
    
    /*
     * Function to print the tfidfScores up until the limit
     */
    static void printTopWords(List<Map.Entry<String, Double>> tfidfScoreList, int limit){
        for(int i = 0; i < limit; ++i){
            System.out.format("("+ tfidfScoreList.get(i).getKey()+","+"%.2f)",tfidfScoreList.get(i).getValue());
            if(i%5==0 && i !=0){System.out.println();}
        }
        System.out.println("\n------------------------------------------------------------------------------");
    }
    
    /*
     * main function
     */
    public static void main(String[] args) throws IOException{
        InputStream in = null;
        try{
            in = new FileInputStream ("yelpDatasetParsed_10000.txt");
            String test = Allbusiness(in);
            ArrayList<String> eachBusiness = new ArrayList<>();
            
            Pattern p = Pattern.compile("\\{([^}]*)\\}");
            Matcher m = p.matcher(test);
            int counter = 0;
            while(m.find()){
                counter++;
                eachBusiness.add(m.group(1));
            }
            
            
            Map <String,Integer> corpusDFCount = new HashMap<>();
            List<Business> businessList = new ArrayList<Business>();
            int c = 0;
            while (c<counter) {
                Business b = readBusiness (eachBusiness.get(c));
                if (b == null) // end of file and processed all businesses
                    break ;
                businessList.add(b);
                //System.out.println(businessList.get(c));
                c++;
            }
            
            // sort by character count
            Collections.sort(businessList, new CompareBusiness());
            
            for (Business b : businessList )
                addDocumentCount (corpusDFCount , b);
            
            
            // for the top 10 businesses with most review characters
            for (int i =0; i <10; i ++) {
                List<Map.Entry<String, Double>> tfidfScoreList = new ArrayList<HashMap.Entry<String, Double>>();
                //Entry is a static nested interface of class Map
                getTfidfScore (tfidfScoreList, corpusDFCount, businessList.get(i), 5);
                sortByTfidf(tfidfScoreList);
                System.out.println(businessList.get(i));
                printTopWords(tfidfScoreList,30);
            }
        }finally{
            in.close();
        }
    }
    
    
}




