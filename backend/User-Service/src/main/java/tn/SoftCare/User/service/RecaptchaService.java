//package tn.SoftCare.User.service;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Map;
//
//@Service
//public class RecaptchaService {
//
//    @Value("${google.recaptcha.secret}")
//    private String secretKey;
//
//    public boolean verifyToken(String token) {
//
//        String url = "https://www.google.com/recaptcha/api/siteverify";
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        Map<String, Object> response = restTemplate.postForObject(
//                url + "?secret=" + secretKey + "&response=" + token,
//                null,
//                Map.class
//        );
//
//        System.out.println("RECAPTCHA RESPONSE: " + response); // ⭐ debug
//
//        if (response == null) {
//            return false;
//        }
//
//        Boolean success = (Boolean) response.get("success");
//
//        return success != null && success;
//    }
//}