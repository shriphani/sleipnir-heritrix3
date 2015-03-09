package org.archive.modules.extractor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.archive.modules.CrawlURI;
import org.archive.spring.KeyedProperties;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.omg.CORBA.NameValuePair;


public class ExternalProcessExtractor extends ExtractorHTML {
	
	private static Logger logger =
            Logger.getLogger(ExtractorHTML.class.getName());

	{
        setAddress("http://localhost:3000/extract");
    }
    public String getAddress() {
        return (String) kp.get("address");
    }
    public void setAddress(String address) {
        kp.put("address", address); 
    }
    
	private JSONParser jsonParser = new JSONParser();
    
	protected void extract(CrawlURI curi, CharSequence cs) {
		
		System.out.println(getAddress());
		
		try {
            
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(getAddress());
            
            List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>(2);
            params.add(new BasicNameValuePair("url", URLEncoder.encode(curi.toString(), "UTF-8")));
            params.add(new BasicNameValuePair("body", StringEscapeUtils.escapeHtml4(cs.toString())));
            
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            
            HttpResponse response = httpclient.execute(httpPost);
            
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            
            StringBuffer result = new StringBuffer();
    		String line = "";
    		while ((line = rd.readLine()) != null) {
    			result.append(line);
    		}
     
    		String jsonResponse = result.toString();
    		JSONArray arr = (JSONArray) jsonParser.parse(jsonResponse);
    		
    		for (int i = 0; i < arr.size(); i++) {
    			processEmbed(curi, (String) arr.get(i), elementContext(cs, "href"));
    		}
            
            
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } catch (org.json.simple.parser.ParseException e) {
			logger.log(Level.SEVERE,e.getMessage());
		}
		
	}

}
