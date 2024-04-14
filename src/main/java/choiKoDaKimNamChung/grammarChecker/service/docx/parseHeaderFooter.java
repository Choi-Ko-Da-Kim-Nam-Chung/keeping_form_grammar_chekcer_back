package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.docx.FootNote;
import choiKoDaKimNamChung.grammarChecker.docx.IBody;
import choiKoDaKimNamChung.grammarChecker.docx.Paragraph;
import choiKoDaKimNamChung.grammarChecker.docx.SpellCheckerType;
import choiKoDaKimNamChung.grammarChecker.response.ExtractData;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class parseHeaderFooter {

    static Set<String> footnotesSet = new HashSet<>();
    static Set<String> endnotesSet = new HashSet<>();
    static ArrayList<Map<String, Object>> footendNotes = new ArrayList<>();
    static ArrayList<Map<String, Object>> headerfooterList = new ArrayList<>();
    static int footnoteEnter = 0;
    static int endnoteEnter = 0;
    public static DocxParserImp docxParserImp;



    public static void extractAll(XWPFParagraph paragraph){
        ExtractData extractData = new ExtractData();
        String paragraphWithNotes = paragraph.getText();                                // paragraph + ref + note
        String notes = paragraph.getFootnoteText();                                     // note
        String paragraphWithRef = paragraphWithNotes.replace(notes, "");     // paragraph + ref
        removeReferences(extractData, paragraphWithRef);                                // paragraph


    }

    public static void removeReferences(ExtractData extractData, String paragraphTextWithRef) {
        // original text사이에 있는 footnoteRef, endnoteRef 제거
        Pattern pattern = Pattern.compile("\\[(endnoteRef|footnoteRef):(\\d+)\\]");
        getPlainTextAndRefNum(extractData, pattern, paragraphTextWithRef);
    }

    private static void getPlainTextAndRefNum(ExtractData extractData, Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        StringBuilder resultText = new StringBuilder(text);

        // 매칭된 결과를 찾으면서 처리
        while (matcher.find()) {
            // Ref 번호 삽입
            System.out.println(matcher.group(1));
            System.out.println(Integer.parseInt(matcher.group(2)));
            extractData.getNoteList().add(new Object[]{matcher.group(1), Integer.parseInt(matcher.group(2))});
            int start = matcher.start();
            int end = matcher.end();
            // 매칭된 부분 문자열 제거
            resultText.delete(start - (text.length() - resultText.length()), end - (text.length() - resultText.length()));
        }

        // plainParagraph 삽입
        extractData.setPlainPragraph(String.valueOf(resultText));
//        System.out.println("extractData = " + extractData);
//        for (Object[] objects : extractData.getNoteList()) {
//            System.out.println(Arrays.toString(objects));
//
//        }
    }
    public List<String> extractNotes(XWPFParagraph paragraph) {
        List<String> endnotes = new ArrayList<>();
        String paragraphWithNotes = paragraph.getText();                                // paragraph + ref + note
        String notes = paragraph.getFootnoteText();                                     // note
        String paragraphWithRef = paragraphWithNotes.replace(notes, "");     // paragraph + ref
//        String plainParagraph = removeAllReferences(paragraphWithRef);                  // paragraph

        extractNotes(notes, endnotes);

        return endnotes;

    }

    private static void extractNotes(String notes, List<String> endnotes) {
        // 숫자가 1자리수, 2자리수, 혹은 3자리수 일 수 있는 경우를 고려한 정규 표현식, \n 까지 확인할 수 있도록 Pattern.DOTALL 추가
        Pattern pattern = Pattern.compile("\\[(\\d+):  (.*?)\\]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(notes);

        while (matcher.find()) {
            // group(1) => 미주/각주 번호 group(2) => 미주/각주 내용
            System.out.println("matcher.group(1) = " +  Integer.parseInt(matcher.group(1)));
            int count = 0;
            for (int i = 0; i < matcher.group(2).length(); i++) {
                System.out.println(" matcher.group(2).charAt(i) = " +  matcher.group(2).charAt(i));
                if (matcher.group(2).charAt(i) == '\n') {
                    count++;
                }
            }

            System.out.println("count = " + count);
            for (String s : matcher.group(2).split("\n")) {
                System.out.println("s = " + s);
            }
            endnotes.add(matcher.group(2)); // group(2)는 괄호() 안의 주석 내용을 의미
        }
    }

    public static void main(String[] args) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        SpellCheckerType spellCheckerType = SpellCheckerType.BUSAN_UNIV;
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
        for (IBodyElement bodyElement : document.getBodyElements()) {
            if (bodyElement.getElementType() == BodyElementType.PARAGRAPH){
                if (!((XWPFParagraph)bodyElement).getFootnoteText().isEmpty()){
                    System.out.println("text: "+((XWPFParagraph)bodyElement).getFootnoteText());
                    extractAll((XWPFParagraph) bodyElement);
                    System.out.println();
//                    for (String s : result) {
//                        System.out.println("s = " + s);
//                    }
                }

            }

        }

        // ------------------------------------------------------------------------------------------------------------------ 파일 로드
//        headerfooterExtract(document);
        // ------------------------------------------------------------------------------------------------------------------ 머리글 / 바닥글 추출(테이블은 형식 정해지면 진행)
//        parseHeaderFooter docxReader = new parseHeaderFooter();
//        for (int i = 0; i < document.getBodyElements().size(); i++) {
//            IBodyElement bodyElement = document.getBodyElements().get(i);
//            docxReader.bodyParse(bodyElement, i);
//        }
        // ------------------------------------------------------------------------------------------------------------------ 미주 / 각주 추출(테이블은 형식 정해지면 진행)
//        List<XWPFFootnote> footnotes = document.getFootnotes();
//        FootNote footNotes = new FootNote();
//        for (XWPFFootnote footnote : footnotes) {
//            XWPFAbstractFootnoteEndnote footnoteEndnote = footnote;
//            List<IBodyElement> bodyElements = footnote.getBodyElements();
//            for (IBodyElement element : bodyElements) {
//                // 체크하는 element의 타입
//                if (element.getElementType() == BodyElementType.PARAGRAPH) {
//                    if (!((XWPFParagraph) element).getText().isEmpty()) {
//                        XWPFParagraph paragraph = (XWPFParagraph) element;
//                        Paragraph para = new Paragraph();
////                        IBody ibody = docxParserImp.paragraphParse(paragraph, spellCheckerType);
////                        footNotes.getContent().add(para);
//                        System.out.println("Paragraph in Footnote: " + paragraph.getText());
//                    }
//
//                } else if (element.getElementType() == BodyElementType.TABLE) {
//                    XWPFTable table = (XWPFTable) element;
////                    IBody ibody = docxParserImp.tableParse(table, spellCheckerType);
////                    footNotes.getContent().add(ibody);
//                    System.out.println("Table in Footnote");
//                }
//            }
//        }
//        System.out.println("footNotes = " + footNotes);
        
        
//        System.out.println("footendNotes = "+footendNotes);
//        System.out.println("headerfooterList = "+headerfooterList);

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
//                removeAllReferences((XWPFParagraph) bodyElement, elementNum);

            } else if (bodyElement.getElementType() == BodyElementType.TABLE){
                tableParse((XWPFTable) bodyElement, elementNum);
            } else {
                System.out.println(bodyElement.getElementType());
            }
    }
}
