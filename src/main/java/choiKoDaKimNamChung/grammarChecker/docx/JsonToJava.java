//package choiKoDaKimNamChung.grammarChecker.docx;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.io.File;
//import java.io.IOException;
//
//public class JsonToJava {
//    public static void main(String[] args) {
//        File jsonFile = new File("/Users/chtw2001/result.json");  // JSON 파일의 경로
//        ObjectMapper mapper = new ObjectMapper();
//        Docx docx = new Docx();
//        try {
//            docx = mapper.readValue(jsonFile, Docx.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("Error converting JSON to Java object.");
//        }
//        for (IBody iBody : docx.getFootNote()) {
//            System.out.println("footnote = " + iBody);
//
//        }
//        System.out.println();
//        for (IBody iBody : docx.getEndNote()) {
//            System.out.println("endnote = " + iBody);
//
//        }
//    }
//}
