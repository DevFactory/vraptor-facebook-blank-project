package br.com.opengti.project.vraptor.controller;

import java.net.URLEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;

import com.restfb.types.User;


@Resource
@Path("index")
public class IndexController {
	
	private final Result result;
	
	HttpServletRequest request;
	
	private String facebookApi = "445061462200108";

	private String facebookSecret = "86c4b56dff205c4be42cc96f0ee1419d";
	
	public IndexController(HttpServletRequest request,Result result) {
		this.result = result;
		this.request = request;
	}	
	
	@Path("/")
	public void index(String signed_request) throws Exception {
		String valor = request.getParameter("signed_request");
		
		
		String urlValor = request.getRequestURL().toString();
		String uriValor = request.getRequestURI().toString();
		
		System.out.println(valor + " " + urlValor + " " + uriValor);
		
		if (StringUtils.isBlank(signed_request)){
	
		result.redirectTo("https://www.facebook.com/dialog/oauth/?client_id=" + facebookApi + 
	                   "&redirect_uri=" + URLEncoder.encode("http://apps.facebook.com/cardelliapp", "UTF-8") + 
	                   "&scope=publish_stream,offline_access,email");
		}else{	
		
		
		System.out.println(signed_request);
		
	    Base64 base64 = new Base64(true);

        String[] signedRequest = signed_request.split("\\.", 2);

        String sig = new String(base64.decode(signedRequest[0].getBytes("UTF-8")));

        JSONObject data = (JSONObject)JSONSerializer.toJSON(new String(base64.decode(signedRequest[1].getBytes("UTF-8"))));
        
        if(!data.getString("algorithm").equals("HMAC-SHA256")) {
        	 System.out.println("Erro Algorith");
        }

        if(!hmacSHA256(signedRequest[1], facebookSecret).equals(sig)) {
        	 System.out.println("Erro siogned");
        }

        if(!data.has("user_id") || !data.has("oauth_token")) {
        	result.redirectTo("https://www.facebook.com/dialog/oauth?client_id=" + facebookApi + 
                    "&redirect_uri=" + URLEncoder.encode("http://localhost:8080/opengti-vraptor-blank-project/index/", "UTF-8") + 
                    "&scope=publish_stream,offline_access,email");
        }
            String accessToken = data.getString("oauth_token");
            FacebookClient facebookClient = new DefaultFacebookClient(accessToken);
            User user = facebookClient.fetchObject("me", User.class);
            Connection<User> friends = facebookClient.fetchConnection("me/friends", User.class);
            
            for (User userTemp : friends.getData()){
            	System.out.println(" - : " + userTemp.getName());
            }
            
         /*   FacebookType publishMessageResponse =
            	  facebookClient.publish("me/feed", FacebookType.class,
            	    Parameter.with("message", "RestFB test"));
            System.out.println("Published message ID: " + publishMessageResponse.getId());
*/
          
            result.include("variable", "Olá " + user.getName() + " você tem " + friends.getData().size() + " amigos ");
		}
	}
	

	private String hmacSHA256(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] hmacData = mac.doFinal(data.getBytes("UTF-8"));
        return new String(hmacData);
    }



}
