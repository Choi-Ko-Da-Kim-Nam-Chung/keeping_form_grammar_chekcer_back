package choiKoDaKimNamChung.grammarChecker.service.docx;

import org.apache.poi.xwpf.usermodel.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class parseHeaderFooter {

    static Set<String> footnotesSet = new HashSet<>();
    static Set<String> endnotesSet = new HashSet<>();
    static ArrayList<Map<String, Object>> footendNotes = new ArrayList<>();
    static ArrayList<Map<String, Object>> headerfooterList = new ArrayList<>();

    public static void main(String[] args) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {

//        final String FILEPATH = "/Users/chtw2001/Documents/report.docx";
        final String FILEPATH = "/Users/chtw2001/Desktop/report.docx";

        FileInputStream fileStream = null;
        XWPFDocument document = null;
        try {
            fileStream = new FileInputStream(FILEPATH);
            document = new XWPFDocument(fileStream);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // ------------------------------------------------------------------------------------------------------------------ 파일 로드
        headerfooterExtract(document);
        // ------------------------------------------------------------------------------------------------------------------ 머리글 / 바닥글 추출(테이블은 형식 정해지면 진행)
        parseHeaderFooter docxReader = new parseHeaderFooter();
        for (int i = 0; i < document.getBodyElements().size(); i++) {
            IBodyElement bodyElement = document.getBodyElements().get(i);
            docxReader.bodyParse(bodyElement, i);
        }
        // ------------------------------------------------------------------------------------------------------------------ 미주 / 각주 추출(테이블은 형식 정해지면 진행)

        System.out.println("footendNotes = "+footendNotes);
        System.out.println("headerfooterList = "+headerfooterList);

        fileStream.close();
    }

    private static void headerfooterExtract(XWPFDocument document) {
        List<XWPFHeader> headers = document.getHeaderList();
        for (XWPFHeader header : headers) {
            for (IBodyElement element : header.getBodyElements()) {
                Map<String, Object> headerElement = new HashMap<>();
                if (element.getElementType() == BodyElementType.PARAGRAPH) {
                    if (!((XWPFParagraph) element).getText().isEmpty()) {
                        headerElement.put("type", "header");
                        headerElement.put("content", ((XWPFParagraph) element).getText());
//                        headerElement.put("content", grammarChecker(((XWPFParagraph) element).getText()));
                    }
                } else if (element.getElementType() == BodyElementType.TABLE) {
                    headerElement.put("type", "header:table");
                    headerElement.put("content", "table");
//                    System.out.println("Table"); // table 수정 로직에 넣기
                } else {
//                    System.out.println("element = " + element.getElementType());
                }
                if (!(headerElement.isEmpty())) {
                    headerfooterList.add(headerElement);
                }
            }
        }

        // 바닥글 추출 및 출력
        List<XWPFFooter> footers = document.getFooterList();
        for (XWPFFooter footer : footers) {
            for (IBodyElement element : footer.getBodyElements()) {
                Map<String, Object> footerElement = new HashMap<>();
                if (element.getElementType() == BodyElementType.PARAGRAPH) {
                    if (!((XWPFParagraph) element).getText().isEmpty()) {
                        footerElement.put("type", "footer");
                        footerElement.put("content", ((XWPFParagraph) element).getText());
//                        footerElement.put("content", grammarChecker(((XWPFParagraph) element).getText()));
                    }
                } else if (element.getElementType() == BodyElementType.TABLE) {
                    footerElement.put("type", "footer:table");
                    footerElement.put("content", "table");
//                    System.out.println("Table"); // table 수정 로직에 넣기
                } else {
//                    System.out.println("element = " + element.getElementType());
                }
                if (!(footerElement.isEmpty())) {
                    headerfooterList.add(footerElement);
                }
            }
        }
    }

    public static List<String> extractNotes(String text) {
        List<String> endnotes = new ArrayList<>();
        // 숫자가 1자리수, 2자리수, 혹은 3자리수일 수 있는 경우를 고려한 정규 표현식
        Pattern pattern = Pattern.compile("\\[(\\d+):  (.*?)\\]");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            // 매칭된 주석 내용을 리스트에 추가
            endnotes.add(matcher.group(2)); // group(2)는 괄호() 안의 주석 내용을 의미
        }

        return endnotes;
    }
        private static StringBuilder removeAllReferences(XWPFParagraph bodyElement, int elementNum) {
            // 분석, ref 없애고 몇번 paragraph의 어떤 내용이 들어있는지 넣어야함. (맞춤법 검사 돌려야함)

            // original text에서 뒤에 있는 note 추출
            String note = bodyElement.getFootnoteText().replaceAll("\n", "").trim();
            List<String> endnotes = extractNotes(note);

            // original text에서 뒤에 있는 note 제거
            String paragraphText = bodyElement.getText().replaceAll("\n", "").trim();
            String removedNote = paragraphText.replace(note, "");

            // original text사이에 있는 footnoteRef, endnoteRef 제거
            Pattern pattern = Pattern.compile("\\[(endnoteRef|footnoteRef):(\\d+)\\]");
            Matcher matcher = pattern.matcher(removedNote);

            // 결과 문자열 초기화 (원본 문자열로 시작)
            StringBuilder resultText = new StringBuilder(removedNote);

            // 매칭된 결과를 찾으면서 처리
            int i = 0;

            while (matcher.find()) {
                Map<String, Object> refEx = new HashMap<>();

                String refType = matcher.group(1);
                String refNumber = matcher.group(2);
                refEx.put("elementNum", elementNum);
                refEx.put("type", refType.replace("Ref", ""));
                refEx.put("index", refNumber);
                refEx.put("content", endnotes.get(i)); // endnote.get(i)는 맞춤법 검사를 돌려서 받아와야함. 여기에 api 요청하는 함수 사용해야함

                // 매칭된 부분 문자열 제거
                int start = matcher.start();
                int end = matcher.end();
                resultText.delete(start - (removedNote.length() - resultText.length()), end - (removedNote.length() - resultText.length()));

                footendNotes.add(refEx);
                i++;
            }
            return resultText;
        }
    public void tableParse(XWPFTable table, int elementNum){
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell tableCell : row.getTableCells()) {
                for (IBodyElement bodyElement : tableCell.getBodyElements()) {
                    //셀 병합 체크도 필요
                    bodyParse(bodyElement, elementNum);
                }
            }
        }
    }
    public void bodyParse(IBodyElement bodyElement, int elementNum){
        try {

            if(bodyElement.getElementType() == BodyElementType.PARAGRAPH){
                if(!((XWPFParagraph) bodyElement).getFootnoteText().isEmpty()){
                    //미주각주 파싱 코드 + [ref]제거코드 필요
                    checkInnerFootEndnote(bodyElement, elementNum);
                }
            }else if(bodyElement.getElementType() == BodyElementType.TABLE){
                tableParse((XWPFTable) bodyElement, elementNum);
            }else{
//                System.out.println("bodyElement = " + bodyElement.getElementType());
                // alt + enter 나오면 페이지네이션 1개 추가하면 될듯
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkInnerFootEndnote(IBodyElement bodyElement, int elementNum) {
            if (bodyElement.getElementType() == BodyElementType.PARAGRAPH){
                // 분석, ref 없애고 몇번 paragraph의 어떤 내용이 들어있는지 넣어야함. (맞춤법 검사 돌려야함)
                removeAllReferences((XWPFParagraph) bodyElement, elementNum);

            } else if (bodyElement.getElementType() == BodyElementType.TABLE){
                tableParse((XWPFTable) bodyElement, elementNum);
            } else {
                System.out.println(bodyElement.getElementType());
            }
    }
}
