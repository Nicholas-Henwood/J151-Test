package za.ac.vzap.trytons.frontend.util;

public class APIConfig {
    private static String DEFAULT_BASE_URL = "http://localhost:8080/J151-FINAL-BE/api";
    public APIConfig(){
    }
    public static String getBaseUrl() {
        String override = System.getProperty("api.base.url");
        if(override != null && !override.isBlank()){
            return override;
        }
        return DEFAULT_BASE_URL;
    }

}
