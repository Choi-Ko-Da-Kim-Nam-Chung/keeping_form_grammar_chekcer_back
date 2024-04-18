package choiKoDaKimNamChung.grammarChecker.service.docx;


import choiKoDaKimNamChung.grammarChecker.docx.*;
import choiKoDaKimNamChung.grammarChecker.docx.IBody;
import choiKoDaKimNamChung.grammarChecker.request.TextRequest;
import choiKoDaKimNamChung.grammarChecker.response.ExtractData;
import choiKoDaKimNamChung.grammarChecker.response.ExtractNotes;
import choiKoDaKimNamChung.grammarChecker.response.WordError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DocxParserImp implements DocxParser {

    private final WebClient webClient;
    @Override
    public Docx docxParse(XWPFDocument document, SpellCheckerType spellCheckerType) {
        Docx docx = new Docx();
        File jsonFile = new File("/Users/chtw2001/result.json");  // JSON 파일의 경로
        ObjectMapper mapper = new ObjectMapper();
        try {
            docx = mapper.readValue(jsonFile, Docx.class);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error converting JSON to Java object.");
        }
//        for (XWPFFootnote footnote : document.getFootnotes()) {
//            for (IBodyElement element : footnote.getBodyElements()) {
//                if (element.getElementType() == BodyElementType.PARAGRAPH) {
//                    if (!((XWPFParagraph) element).getText().isEmpty()) {
//                        IBody ibody = paragraphParse((XWPFParagraph) element, spellCheckerType);
//                        docx.getFootNote().add(ibody);
//                    }
//                } else if (element.getElementType() == BodyElementType.TABLE) {
//                    IBody ibody = tableParse((XWPFTable) element, spellCheckerType);
//                    docx.getFootNote().add(ibody);
//                }
//            }
//        }
//
//        for (XWPFEndnote endnote : document.getEndnotes()) {
//            for (IBodyElement element : endnote.getBodyElements()) {
//                if (element.getElementType() == BodyElementType.PARAGRAPH) {
//                    if (!((XWPFParagraph) element).getText().isEmpty()) {
//                        IBody ibody = paragraphParse((XWPFParagraph) element, spellCheckerType);
//                        docx.getEndNote().add(ibody);
//                    }
//                } else if (element.getElementType() == BodyElementType.TABLE) {
//                    IBody ibody = tableParse((XWPFTable) element, spellCheckerType);
//                    docx.getEndNote().add(ibody);
//                }
//            }
//        }

//
//        List<XWPFHeader> headerList = document.getHeaderList();
//        for (XWPFHeader header : headerList) {
//            List<IBodyElement> bodyElements = header.getBodyElements();
//            for (IBodyElement bodyElement : bodyElements) {
//                IBody iBody = iBodyParse(bodyElement, spellCheckerType);
//                docx.getHeader().add(iBody);
//            }
//        }
//        // =>
//        docx.getFooter().addAll(headerParse(headerList, spellCheckerType));
//
        List<IBodyElement> paragraphs = document.getBodyElements();
        for (IBodyElement paragraph : paragraphs) {
            IBody result = iBodyParse(paragraph, spellCheckerType, new EntireInfo(docx));
            docx.getBody().add(result);
        }
//
//        List<XWPFFooter> footerList = document.getFooterList();
//        for (XWPFFooter footer : footerList) {
//            List<IBodyElement> bodyElements = footer.getBodyElements();
//            for (IBodyElement bodyElement : bodyElements) {
//                IBody iBody = iBodyParse(bodyElement, spellCheckerType);
//                docx.getFooter().add(iBody);
//            }
//        }
//        // =>
//        docx.getHeader().addAll(footerParse(footerList, spellCheckerType));

        return docx;
    }

    @Override
    public IBody iBodyParse(IBodyElement bodyElement, SpellCheckerType spellCheckerType, EntireInfo entireInfo) {
        if (bodyElement.getElementType() == BodyElementType.PARAGRAPH) {
            return paragraphParse((XWPFParagraph)bodyElement, spellCheckerType, entireInfo);
//            if(!((XWPFParagraph) bodyElement).getFootnoteText().isEmpty()){}
        } else if (bodyElement.getElementType() == BodyElementType.TABLE) {
            return tableParse((XWPFTable)bodyElement, spellCheckerType, entireInfo);
        } else {
            System.out.println("bodyElement = " + bodyElement.getElementType());
        }
        return null;
    }

    @Override
    public Table tableParse(XWPFTable table, SpellCheckerType spellCheckerType, EntireInfo entireInfo) {
        Table t = new Table();
        Map<Integer, TableCell> checkRowspan = new HashMap<>();
        for (XWPFTableRow row : table.getRows()) {
            List<XWPFTableCell> cells = row.getTableCells();
            int cal = 0;
            List<TableCell> arr = new ArrayList<>();

            for (int j=0; j<cells.size(); j++, cal++) {
                TableCell tableCell = new TableCell();

                if(cells.get(j).getCTTc().getTcPr().getGridSpan() != null){
                    BigInteger colspan = cells.get(j).getCTTc().getTcPr().getGridSpan().getVal();
                    tableCell.setColspan((int) colspan.longValue());
                    cal += (int) colspan.longValue() - 1;
                }

                if(cells.get(j).getCTTc().getTcPr().getVMerge() != null){
                    if(cells.get(j).getCTTc().getTcPr().getVMerge().getVal() == null){
                        checkRowspan.get(cal).plusRowSpan();
                        continue;
                    } else if ("restart".equals(cells.get(j).getCTTc().getTcPr().getVMerge().getVal().toString())){
                        checkRowspan.put(cal, tableCell);
                    }
                }


                for (IBodyElement bodyElement : cells.get(j).getBodyElements()) {
                    tableCell.getIBody().add(iBodyParse(bodyElement, spellCheckerType, entireInfo));
                }
                arr.add(tableCell);
            }
            t.getTable().add(arr);

        }
        return t;
    }

    @Override
    public Paragraph paragraphParse(XWPFParagraph paragraph, SpellCheckerType spellCheckerType, EntireInfo entireInfo) {
        Paragraph result = new Paragraph();
        // TODO : 중간에 미주, 각주가 있을 경우 처리 필요
        String text = paragraph.getText();
//        System.out.println("text = " + text);
        String url = spellCheckerType.getUrl();
        if(!paragraph.getFootnoteText().isEmpty()){  // 미주, 각주가 있으면
            ExtractNotes extractNotes = extractAll(paragraph, entireInfo);
            text = extractNotes.getPlainPragraph();
            result.getFootNoteEndNote().addAll(extractNotes.getErrorList());

        }
        TextRequest textRequest = new TextRequest(text);

        Flux<WordError> response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(textRequest))
                .retrieve()
                .bodyToFlux(WordError.class);

        response.subscribe(wordError -> {
            result.getErrors().add(wordError);
        });
        response.blockLast();
        return result;
    }

    public static ExtractNotes extractAll(XWPFParagraph paragraph, EntireInfo entireInfo){
        ExtractData extractData = new ExtractData();
        String paragraphWithNotes = paragraph.getText();                                // paragraph + ref + note
        String notes = paragraph.getFootnoteText();                                     // note
        String paragraphWithRef = paragraphWithNotes.replace(notes, "");     // paragraph + ref
        
        // footnoteRef, endnoteRef 제거 및 plainParagraph 처리
        removeReferences(extractData, paragraphWithRef);                                // paragraph
        
        //
        ExtractNotes extractNotes = new ExtractNotes();
        matchReferences(extractNotes, extractData.getNoteList(), notes, entireInfo);

        return extractNotes;
    }


    private static void matchReferences(ExtractNotes extractNotes, List<Object[]> ref, String notes, EntireInfo entireInfo) {
        // \n 까지 확인할 수 있도록 Pattern.DOTALL 추가
        Pattern pattern = Pattern.compile("\\[(\\d+):  (.*?)\\]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(notes);
        int paragraphNoteNum = 0;
//        for (Object[] objects : ref) {
//            System.out.println("objects = " + objects);
//        }
        while (matcher.find()) {
            // group(1) => 미주/각주 번호 group(2) => 미주/각주 내용
            int enter = 0;
            // [n:  ] 의 note 의 타입 확인
            int noteNum = Integer.parseInt(matcher.group(1));
            IBodyType type = null;
            if (ref.get(paragraphNoteNum)[0].toString().equals("footnoteRef")){
                type = IBodyType.FOOT_NOTE;
            }else {
                type = IBodyType.END_NOTE;
            }
            Note note = new Note(noteNum, type);

            // [n:  ] 내부에서  \n 을 기준으로 탐색


            System.out.println("matcher.group(2) = " + matcher.group(2));
            for (int i = 0; i < matcher.group(2).length(); i++) {
                if (matcher.group(2).charAt(i) == '\n') {
                    if (entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter() - 1) instanceof Paragraph) {
                        if (type == IBodyType.FOOT_NOTE) {
                            if (!((Paragraph) entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter()-1)).getErrors().isEmpty()) {
                                Paragraph para = new Paragraph();
                                para.getErrors().add(((Paragraph) entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter() - 1)).getErrors().get(enter));
                                note.getError().add(para);
                            } else {
                                note.getError().add(new Paragraph());
                            }
                            entireInfo.countFootEnter();
                        } else {
                            if (!((Paragraph) entireInfo.getDocx().getEndNote().get(noteNum + entireInfo.getEndnoteEnter() - 1)).getErrors().isEmpty()) {
                                Paragraph para = new Paragraph();
                                para.getErrors().add(((Paragraph) entireInfo.getDocx().getEndNote().get(noteNum + entireInfo.getEndnoteEnter() - 1)).getErrors().get(enter));
                                note.getError().add(para);
                            } else {
                                note.getError().add(new Paragraph());
                            }
                            entireInfo.countEndEnter();
                        }
                    } else {
                        if (type == IBodyType.FOOT_NOTE) {
                            if (!((Table) entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter()-1)).getTable().isEmpty()) {
                                Table tb = new Table();
                                tb.setTable(Collections.singletonList(((Table) entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter() - 1)).getTable().get(0)));
                                note.getError().add(tb);
                            } else {
                                note.getError().add(new Table());
                            }
                            entireInfo.countFootEnter();
                        } else {
                            if (!((Table) entireInfo.getDocx().getEndNote().get(noteNum + entireInfo.getEndnoteEnter() - 1)).getTable().isEmpty()) {
                                Table tb = new Table();
                                tb.setTable(Collections.singletonList(((Table) entireInfo.getDocx().getEndNote().get(noteNum + entireInfo.getEndnoteEnter() - 1)).getTable().get(0)));
                                note.getError().add(tb);
                            } else {
                                note.getError().add(new Table());
                            }
                            entireInfo.countEndEnter();
                        }
                    }
                    enter++;
                }
            }
            System.out.println("type = " + type);
            if (entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter() - 1) instanceof Paragraph) {
                if (type == IBodyType.FOOT_NOTE) {
                    if (!((Paragraph) entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter() - 1)).getErrors().isEmpty()) {
                        Paragraph para = new Paragraph();
                        para.getErrors().add(((Paragraph) entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter() - 1)).getErrors().get(enter));
                        note.getError().add(para);
                    } else {
                        note.getError().add(new Paragraph());
                    }
                } else {
                    if (!((Paragraph) entireInfo.getDocx().getEndNote().get(noteNum + entireInfo.getEndnoteEnter() - 1)).getErrors().isEmpty()) {
                        Paragraph para = new Paragraph();
                        para.getErrors().add(((Paragraph) entireInfo.getDocx().getEndNote().get(noteNum + entireInfo.getEndnoteEnter() - 1)).getErrors().get(enter));
                        note.getError().add(para);
                    } else {
                        note.getError().add(new Paragraph());
                    }
                }
            } else {
                if (type == IBodyType.FOOT_NOTE) {
                    if (!((Table) entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter() - 1)).getTable().isEmpty()) {
                        Table tb = new Table();
                        tb.setTable(Collections.singletonList(((Table) entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter() - 1)).getTable().get(0)));
                        note.getError().add(tb);
                    } else {
                        note.getError().add(new Table());
                    }
                } else {
                    if (!((Table) entireInfo.getDocx().getEndNote().get(noteNum + entireInfo.getEndnoteEnter() - 1)).getTable().isEmpty()) {
                        Table tb = new Table();
                        tb.setTable(Collections.singletonList(((Table) entireInfo.getDocx().getEndNote().get(noteNum + entireInfo.getEndnoteEnter() - 1)).getTable().get(0)));
                        note.getError().add(tb);
                    } else {
                        note.getError().add(new Table());
                    }
                }
            }
            System.out.println("enter = " + enter);
            System.out.println("noteNum = " + noteNum);
            enter++;
            System.out.println("hi");
            extractNotes.getErrorList().addAll(note.getError()); // group(2)는 괄호() 안의 주석 내용을 의미

            paragraphNoteNum++;
        }
    }

    // footnoteRef, endnoteRef 제거 및 plainParagraph 처리
    public static void removeReferences(ExtractData extractData, String paragraphTextWithRef) {
        // original text사이에 있는 footnoteRef, endnoteRef 제거
        Pattern pattern = Pattern.compile("\\[(endnoteRef|footnoteRef):(\\d+)\\]");

        Matcher matcher = pattern.matcher(paragraphTextWithRef);
        StringBuilder resultText = new StringBuilder(paragraphTextWithRef);

        // 매칭된 결과를 찾으면서 처리
        while (matcher.find()) {
            // Ref 번호 삽입
            extractData.getNoteList().add(new Object[]{matcher.group(1), Integer.parseInt(matcher.group(2))});
            int start = matcher.start();
            int end = matcher.end();
            // 매칭된 부분 문자열 제거
            resultText.delete(start - (paragraphTextWithRef.length() - resultText.length()), end - (paragraphTextWithRef.length() - resultText.length()));
        }
        // plainParagraph 삽입
        extractData.setPlainPragraph(String.valueOf(resultText));
    }
//    public String removeAllReferences(XWPFParagraph bodyElement) {
//        String note = bodyElement.getFootnoteText(); // 뒤에 있는 plain 미주, 각주
//        // original text에서 뒤에 있는 note 제거
//        String paragraphText = bodyElement.getText();
//        String paragraphTextWithRef = paragraphText.replace(note, "");
//
//        // original text사이에 있는 footnoteRef, endnoteRef 제거
//        Pattern pattern = Pattern.compile("\\[(endnoteRef|footnoteRef):(\\d+)\\]");
//        StringBuilder resultText = getPlainText(pattern, paragraphTextWithRef);
//        return resultText.toString();
//    }
//
//    private static StringBuilder getPlainText(Pattern pattern, String removedNote) {
//        Matcher matcher = pattern.matcher(removedNote);
//        StringBuilder resultText = new StringBuilder(removedNote);
//
//        // 매칭된 결과를 찾으면서 처리
//        while (matcher.find()) {
//            // 매칭된 부분 문자열 제거
//            int start = matcher.start();
//            int end = matcher.end();
//            resultText.delete(start - (removedNote.length() - resultText.length()), end - (removedNote.length() - resultText.length()));
//        }
//        return resultText;
//    }

    @Override
    public List<IBody> endNoteFootNoteParse(XWPFAbstractFootnoteEndnote note, IBodyType iBodyType, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public List<IBody> headerParse(List<XWPFHeader> headerList, SpellCheckerType spellCheckerType) {
        List<IBody> result = new ArrayList<>();
//        for (XWPFHeader header : headerList) {
//            List<IBodyElement> bodyElements = header.getBodyElements();
//            for (IBodyElement bodyElement : bodyElements) {
//                result.add(iBodyParse(bodyElement, spellCheckerType));
//            }
//        }
        return result;
    }

    @Override
    public List<IBody> footerParse(List<XWPFFooter> footerList, SpellCheckerType spellCheckerType) {
        List<IBody> result = new ArrayList<>();
//        for (XWPFFooter footer : footerList) {
//            List<IBodyElement> bodyElements = footer.getBodyElements();
//            for (IBodyElement bodyElement : bodyElements) {
//                result.add(iBodyParse(bodyElement, spellCheckerType));
//            }
//        }

        return result;
    }
}
