package services;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class FileScan {
    private String fileChecksum;
    private JSONObject result;
    private String dataId;
    private String apiKey;

    public FileScan(String fileChecksum) {
        this.fileChecksum = fileChecksum;
        Dotenv dotenv = Dotenv.load();
        apiKey = dotenv.get("API_KEY");
    }

    /**
     * This function checks if there is previously cached result for the file.
     * @return boolean whether there is previously cached result for the file (true = file exists, false = file doesn't exist)
     */
    public boolean checkIfFileExists(){
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet("https://api.metadefender.com/v4/hash/"+fileChecksum);
        request.addHeader("apikey", apiKey);
        String result = null;
        try {
            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            result = EntityUtils.toString(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonResponse = new JSONObject(result);
        if(jsonResponse.has("file_info")){
            this.result = jsonResponse;
            return true;
        }else if(jsonResponse.has("error")){
            JSONObject errorResponse = jsonResponse.getJSONObject("error");
            if(errorResponse.getInt("code") == 401001){
                System.out.println("Authentication strategy is invalid");
            }
            return false;
        }else{
            System.out.println("Some unexpected error has occurred !");
            return false;
        }
    }

    /**
     * This function uploads the file to the server for scanning the threats.
     * It also updates the data_id returned for the file in response to dataId variable of the instance.
     * @param file
     */
    public void uploadFile(File file){
        CloseableHttpClient client = HttpClients.createDefault();
        //FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
        HttpPost request = new HttpPost("https://api.metadefender.com/v4/file");
        request.addHeader("apikey", apiKey);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        entityBuilder.addBinaryBody("file", file);
        HttpEntity entity = entityBuilder.build();
        request.setEntity(entity);
        String result=null;
        try {
            CloseableHttpResponse response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();

            result = EntityUtils.toString(responseEntity);
            JSONObject jsonResponse = new JSONObject(result);
            this.dataId = jsonResponse.getString("data_id");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * This function retrieves the scanned results for the file using the file's data_id.
     * It then updates the response containing the scanned result in the result variable of the instance.
     */
    public void retrieveResults(){
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet("https://api.metadefender.com/v4/file/"+this.dataId);
        request.addHeader("apikey", apiKey);
        String result=null;
        try {
            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonResponse = new JSONObject(result);
        if(jsonResponse.has("scan_results")){
            JSONObject scanResults = jsonResponse.getJSONObject("scan_results");
            if(scanResults.getInt("progress_percentage") == 100){
                this.result = jsonResponse;
            }else{
                this.retrieveResults();
            }
        }else if(jsonResponse.has("in_queue")){
            this.retrieveResults();
        }else{
            System.out.println("Some Unexpected error occurred !");
        }

    }

    /**
     * This function displays the scan results of a file on the console for each engine in a readable format.
     */
    public void printResults(){
        JSONObject scanResults = result.getJSONObject("scan_results");
        JSONObject fileInfo = result.getJSONObject("file_info");
        JSONObject scanDetails = scanResults.getJSONObject("scan_details");
        System.out.println("filename: "+fileInfo.get("display_name"));
        System.out.println("overall_status: "+scanResults.get("scan_all_result_a"));
        Iterator<String> iterator = scanDetails.keys();
        while(iterator.hasNext()){
            String engine = iterator.next();
            JSONObject scanDetail = scanDetails.getJSONObject(engine);
            System.out.println("engine: "+engine);
            if(scanDetail.get("threat_found") .equals("")){
                System.out.println("threat_found: Clean");
            }else{
                System.out.println("threat_found: "+scanDetail.get("threat_found"));
            }
            System.out.println("scan_result: "+scanDetail.get("scan_result_i"));
            System.out.println("def_time: "+scanDetail.get("def_time"));
        }
        System.out.println("END");
    }
}
