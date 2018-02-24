
import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;


public class HTTPClient{

   public void testConnection(){

      String https_url = "http://nsabater.com/";
      URL url;
      try {

	     url = new URL(https_url);
	     HttpURLConnection con = (HttpURLConnection)url.openConnection();

	     //dump all the content
	     print_content(con);

      } catch (MalformedURLException e) {
	     e.printStackTrace();
      } catch (IOException e) {
	     e.printStackTrace();
      }

   }
   
   public String getYourScores(String publicKeyEncripted){
      URL url;
      try {

	     url = new URL("http://pingpong.nsabater.com/index.php/scores");
	     HttpURLConnection con = (HttpURLConnection)url.openConnection();
	     
	     con.setRequestMethod("GET");
	     //con.setDoOutput(true);
         con.setRequestProperty("Authorization", publicKeyEncripted);   

         return getContent(con);

      } catch (MalformedURLException e) {
	     e.printStackTrace();
      } catch (IOException e) {
	     e.printStackTrace();
      }
      return null;

   }
   
   /*
   public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	    InputStream is = new URL(url).openStream();
	    try {
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	      String jsonText = readAll(rd);
	      JSONObject json = new JSONObject(jsonText);
	      return json;
	    } finally {
	      is.close();
	    }
	  }
    */
   
   private String getContent(HttpURLConnection con){
		if(con!=null){
			String res = "";
			try {
			   BufferedReader br =
				new BufferedReader(
					new InputStreamReader(con.getInputStream()));
	
			   String input;
	
			   while ((input = br.readLine()) != null){
			     res = res + input;
			   }
			   br.close();
			   return res;
	
			} catch (IOException e) {
			   e.printStackTrace();
			}
		}
			
		return null;
		
   }


   private void print_content(HttpURLConnection con){
	if(con!=null){

	try {

	   System.out.println("****** Content of the URL ********");
	   BufferedReader br =
		new BufferedReader(
			new InputStreamReader(con.getInputStream()));

	   String input;

	   while ((input = br.readLine()) != null){
	      System.out.println(input);
	   }
	   br.close();

	} catch (IOException e) {
	   e.printStackTrace();
	}

       }

   }

}