// by @neoxelox https://github.com/neoxelox
// THIS CODE IS GIVEN BY GNU GENERAL PUBLIC LICENSE V3
package com.neoxelox.cayenne;

//LIBRARY COMPONENTS
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.JsonUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

//EXTENSION CARACTERISTICS
@DesignerComponent(
    version = 2,
    description = "Cayenne Extension by @neoxelox (non-official). [V2.1]",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "https://emoji.slack-edge.com/T4SDLL3MJ/cayenne/e01051baaf63e4d7.png")

@SimpleObject(external = true)

//ANDROID PERMISSIONS
@UsesPermissions(permissionNames = 
"android.permission.INTERNET," +
"android.permission.WRITE_EXTERNAL_STORAGE," +
"android.permission.READ_EXTERNAL_STORAGE"
)

//EXTERNAL LIBRARIES
@UsesLibraries(libraries = "json.jar")

//EXTENSION CODE
public class Cayenne extends AndroidNonvisibleComponent implements Component {

    //GLOBALS
    private ComponentContainer container;

    //AUTH VALUES
    public static final String AUTH_URL = "https://auth.mydevices.com/oauth/token";
    public static final String AUTH_HEADER = "content-type";
    public static final String AUTH_HEADER_VALUE = "application/json; charset=UTF-8";
    public static final String AUTH_HELPER1 = "{\"grant_type\":\"password\",\"email\":\"";
    public String cayenneEmail = "";
    public static final String AUTH_HELPER2 = "\",\"password\":\"";
    public String cayennePass = "";
    public static final String AUTH_HELPER3 = "\"}";
    public String DEBUG_CONSTRUCTOR = "";

    //GET DATA VALUES
    public static final String DATA_URL = "https://platform.mydevices.com/v1.1/telemetry/";
    public static final String DATA_HELPER1 = "/sensors/";
    public static final String DATA_HELPER2 = "/summaries?type=";
    public String DATA_SUMMARYTYPE = "latest";
    public static final String DATA_HEADER = "Authorization";
    public String DATA_HEADER_VALUE = "Bearer ";
    //[DEPRECATED]public static final String DATA_HEADER2 = "X-API-Version";
    //[DEPRECATED]public static final String DATA_HEADER_VALUE2 = "1.0";
    public String deviceID = "";
    public String sensorID = "";
    public String authToken = "";
    
    //ACTUATOR VALUES
    public static final String ACTU_URL = "https://platform.mydevices.com/v1.1/things/";
    public static final String ACTU_HELPER1 = "/cmd";
    public static final String BODY_ACTU_HELPER1 = "{\"channel\":";
    public static final String BODY_ACTU_HELPER2 = ",\"value\":";
    public static final String BODY_ACTU_HELPER3 = "}";
    public static final String ACTU_HEADER = "Authorization";
    public String ACTU_HEADER_VALUE = "Bearer ";
    public static final String ACTU_HEADER2 = "content-type";
    public static final String ACTU_HEADER_VALUE2 = "application/json; charset=UTF-8";

    //COMPONENT CREATION
    public Cayenne(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        
        //DEFAULT VALUES

    }

    //PROPERTY BLOCKS
    //VARIABLE PROPERTIES BLOCK
    //DESIGNER-EDITABLE PROPERTIES
    //EDITOR BLOCK
    
    //FUNCTIONS
    //AUTHTOKEN FUNCTION
   @SimpleFunction(description = "Returns an AuthToken")
    public String getAuthToken(String CayenneEmail, String CayennePass) {
        cayenneEmail = CayenneEmail;
        cayennePass = CayennePass;
        String bodyConstructor = AUTH_HELPER1 + cayenneEmail + AUTH_HELPER2 + cayennePass + AUTH_HELPER3;
        String rawToken = "";

        try {
            URL url = new URL(AUTH_URL);
			URLConnection con = url.openConnection();
			HttpURLConnection http = (HttpURLConnection)con;
			http.setRequestMethod("POST");
            http.setDoOutput(true);

            byte[] out =  bodyConstructor .getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            http.setFixedLengthStreamingMode(length);
			http.setRequestProperty(AUTH_HEADER, AUTH_HEADER_VALUE);
            http.connect();
            
            try {
                OutputStream os = http.getOutputStream();
                os.write(out);
            } catch (Exception e) {
                return "[POST]ERROR WRITING DATA! : " + e;
            }
            rawToken = parseResponse(http.getInputStream(), "UTF-8");
            http.disconnect();
            JSONObject obj = new JSONObject(rawToken);
            return obj.getString("access_token");

        } catch (Exception e) {
            return "[POST]ERROR SETTING VARIABLES! : " + e;
        }

    }
    //GET DEVICE DATA FUNCTION
    @SimpleFunction(description = "Returns Sensors's data (in String). SummaryType VALUES: [latest]: Gets the latest data. TAG VALUES: [v]: Sensor data | [ts]: TimeStamp | [unit]: Sensor's data unit | [device_type]: Sensor's type")
    public String getSensorData(String DeviceID, String SensorID, String AuthToken, String Tag, String SummaryType) {
        if ( (DeviceID != "") && (SensorID != "") && (AuthToken != "") && (Tag != "") && (SummaryType != "")) 
        {
        
        deviceID = DeviceID;
        sensorID = SensorID;
        authToken = AuthToken;
        DATA_SUMMARYTYPE = SummaryType;
        String urlConstructor = DATA_URL + deviceID + DATA_HELPER1 + sensorID + DATA_HELPER2 + DATA_SUMMARYTYPE;
        DATA_HEADER_VALUE = "Bearer " + authToken;
        String rawValue = "";

        try {
            URL url2 = new URL(urlConstructor);
			URLConnection con2 = url2.openConnection();
			HttpURLConnection http2 = (HttpURLConnection)con2;
			http2.setRequestMethod("GET");
            http2.setRequestProperty(DATA_HEADER, DATA_HEADER_VALUE);
            http2.connect();

            try {

                rawValue = parseResponse2(http2.getInputStream(), "UTF-8");
                rawValue = rawValue.replace("[","");
                rawValue = rawValue.replace("]","");
                JSONObject obj2 = new JSONObject(rawValue);
                http2.disconnect();
                return obj2.getString(Tag);//return Float.parseFloat(obj.getString("v"));

            } catch (Exception e) {
                return "[GET]ERROR GETTING SENSOR DATA!: " + e;
            }
        } catch (Exception e) {
            return "[GET]ERROR SETTING VARIABLES!: " + e;
        }
    } else{
        return "ERROR! PLEASE FILL THE GAPS!";
     }
    }
    //STRING TO FLOAT FUNCTION
   @SimpleFunction(description = "Converts String to Double (Float)")
    public double stringToFloat(String Text) {
        return Double.parseDouble(Text); //Float.toString(Value)
    }
    //STRING TO INT FUNCTION
   @SimpleFunction(description = "Converts String to Int")
    public int stringToInt(String Text) {
        return Integer.parseInt(Text);
    }
    //TIMESTAMP PARSER FUNCTION
   @SimpleFunction(description = "Parses the TimeStamp (which you can get in getSensorData function with [ts] tag) and converts it to the value you chose. VALUES: [year] [month] [day] [hour] [minute] [second] [secondComplete]: Returns second with decimals. [firstPart]: Returns day/month/year. [secondPart]: Returns hour:minute:second.")
   public String parseTimeStamp(String TimeStamp, String Value) {
   String parseHelper = "";
   if (Value == "") {
        parseHelper = "NO VALUE SELECTED!";
    }else if (Value == "year") {
        parseHelper = TimeStamp.substring(0, 4); 
    }else if (Value == "month") {
        parseHelper = TimeStamp.substring(5, 7);
    }else if (Value == "day") {
        parseHelper = TimeStamp.substring(8, 10);
    }else if (Value == "hour") {
        parseHelper = TimeStamp.substring(11, 13);
    }else if (Value == "minute") {
        parseHelper = TimeStamp.substring(14, 16);
    }else if (Value == "second") {
        parseHelper = TimeStamp.substring(17, 19);
    }else if (Value == "secondComplete") {
        parseHelper = TimeStamp.substring(17, 23);
    }else if (Value == "firstPart") {
        parseHelper = TimeStamp.substring(8, 10) + "/" + TimeStamp.substring(5, 7) + "/" + TimeStamp.substring(0, 4); 
    }else if (Value == "secondPart") {
        parseHelper = TimeStamp.substring(11, 13) + ":" + TimeStamp.substring(14, 16) + ":" + TimeStamp.substring(17, 19);
    }
       return(parseHelper);
   }
   //FUNCTION TO SEND A COMMAND TO AN ACTUATOR
   @SimpleFunction(description = "Controls an MQTT Device's Actuator, if it is not MQTT controls may be buged. The function doesn't return anything, but it calls controlActuatorConfirm, where you can get the confirmation, this is not needed at all. VALUE: any number. HINT: You can actually send data to a non-actuator widget lol!")
   public void controlActuator(String DeviceID, String AuthToken, String Channel, String Value) {
    String ACTU_bodyConstructor = BODY_ACTU_HELPER1 + Channel + BODY_ACTU_HELPER2 + Value + BODY_ACTU_HELPER3;
    String ACTU_urlConstructor = ACTU_URL + DeviceID + ACTU_HELPER1;
    ACTU_HEADER_VALUE = "Bearer " + AuthToken;
    String rawConfirm = "";

    try {
        URL url3 = new URL(ACTU_urlConstructor);
        URLConnection con3 = url3.openConnection();
        HttpURLConnection http3 = (HttpURLConnection)con3;
        http3.setRequestMethod("POST");
        http3.setDoOutput(true);

        byte[] out3 =  ACTU_bodyConstructor .getBytes(StandardCharsets.UTF_8);
        int length3 = out3.length;
        http3.setFixedLengthStreamingMode(length3);
        http3.setRequestProperty(ACTU_HEADER, ACTU_HEADER_VALUE);
        http3.setRequestProperty(ACTU_HEADER2, ACTU_HEADER_VALUE2);
        http3.connect();
        
        try {
            OutputStream os3 = http3.getOutputStream();
            os3.write(out3);
        } catch (Exception e) {
            controlActuatorConfirm("[POST]ERROR WRITING DATA! : " + e);
        }
        rawConfirm = parseResponse(http3.getInputStream(), "UTF-8");
        http3.disconnect();
        JSONObject obj3 = new JSONObject(rawConfirm);
        controlActuatorConfirm(obj3.getString("success"));

    } catch (Exception e) {
        controlActuatorConfirm("[POST]ERROR SETTING VARIABLES! : " + e);
    }

   }
    //[DEBUG]
    /*@SimpleFunction(description = "Debug Variables")
    public String debugVariables() {
        DEBUG_CONSTRUCTOR = AUTH_HELPER1 + cayenneEmail + AUTH_HELPER2 + cayennePass + AUTH_HELPER3;
        String urlConstructor = DATA_URL + deviceID + DATA_HELPER1 + sensorID + DATA_HELPER2 + DATA_SUMMARYTYPE;
        return "AUTH_BODY: " + DEBUG_CONSTRUCTOR + " GET URL: " + urlConstructor;
    }*/

    //EVENTS
    @SimpleEvent(description = "Shows the Success confirmation or Error after an Actuator command, this is not needed at all.")
    public void controlActuatorConfirm(String Confirmation){
        EventDispatcher.dispatchEvent(this, "controlActuatorConfirm", Confirmation);
    }   
    
    //EXTERNAL FUNCTIONS
    //PARSE AUTH RESPONSE
    public static String parseResponse(InputStream inputStream, String encoding) throws IOException {
        return readFully(inputStream).toString(encoding);
    }

    public static ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
    }
    //PARSE GET RESPONSE
    public static String parseResponse2(InputStream inputStream, String encoding) throws IOException {
        return readFully2(inputStream).toString(encoding);
    }

    public static ByteArrayOutputStream readFully2(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
    }

 }

