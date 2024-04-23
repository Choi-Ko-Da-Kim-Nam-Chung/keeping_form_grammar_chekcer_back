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
        EntireInfo entireInfo = new EntireInfo(docx);
        File jsonFile = new File("/Users/chtw2001/result.json");  // 주석까지 매 번 요청하기 좀 그래서 JSON 파일로 넣음
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
//                        IBody ibody = paragraphParse((XWPFParagraph) element, spellCheckerType, entireInfo);
//                        docx.getFootNote().add(ibody);
//                    }
//                } else if (element.getElementType() == BodyElementType.TABLE) {
//                    IBody ibody = tableParse((XWPFTable) element, spellCheckerType, entireInfo);
//                    docx.getFootNote().add(ibody);
//                }
//            }
//        }

//        for (XWPFEndnote endnote : document.getEndnotes()) {
//            for (IBodyElement element : endnote.getBodyElements()) {
//                if (element.getElementType() == BodyElementType.PARAGRAPH) {
//                    if (!((XWPFParagraph) element).getText().isEmpty()) {
//                        IBody ibody = paragraphParse((XWPFParagraph) element, spellCheckerType, entireInfo);
//                        docx.getEndNote().add(ibody);
//                    }
//                } else if (element.getElementType() == BodyElementType.TABLE) {
//                    IBody ibody = tableParse((XWPFTable) element, spellCheckerType, entireInfo);
//                    docx.getEndNote().add(ibody);
//                }
//            }
//        }
//
//        docx.getFooter().addAll(headerParse(document.getHeaderList(), spellCheckerType, entireInfo));
//        docx.getHeader().addAll(footerParse(document.getFooterList(), spellCheckerType, entireInfo));

        List<IBodyElement> paragraphs = document.getBodyElements();
        for (IBodyElement paragraph : paragraphs) {
            IBody result = iBodyParse(paragraph, spellCheckerType, new EntireInfo(docx));
            docx.getBody().add(result);
        }

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
        result.setOrgStr(text);
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

        while (matcher.find()) {
            // group(1) => 미주/각주 번호 group(2) => 미주/각주 내용
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
            for (int i = 0; i < matcher.group(2).length(); i++) {
                if (matcher.group(2).charAt(i) == '\n') {
                    if (entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter() - 1) instanceof Paragraph) {
                        paragraphMatch(entireInfo, type, noteNum, note);
                        entireInfo.countFootEnter();
                    } else {
                        TableMatch(entireInfo, type, noteNum, note);
                    }
                }
            }

            if (entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter() - 1) instanceof Paragraph) {
                paragraphMatch(entireInfo, type, noteNum, note);
            } else {
                TableMatch(entireInfo, type, noteNum, note);
            }

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


    private static void TableMatch(EntireInfo entireInfo, IBodyType type, int noteNum, Note note) {
        if (type == IBodyType.FOOT_NOTE) {
            if (!((Table) entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter()-1)).getTable().isEmpty()) {
                Table tb = new Table();
                tb.setTable(((Table) entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter() - 1)).getTable());
                // tb.setTable(Collections.singletonList(((Table) entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter() - 1)).getTable().get(0)));
                note.getError().add(tb);
            } else {
                note.getError().add(new Table());
            }
            entireInfo.countFootEnter();
        } else {
            if (!((Table) entireInfo.getDocx().getEndNote().get(noteNum + entireInfo.getEndnoteEnter() - 1)).getTable().isEmpty()) {
                Table tb = new Table();
                tb.setTable(((Table) entireInfo.getDocx().getEndNote().get(noteNum + entireInfo.getEndnoteEnter() - 1)).getTable());
                note.getError().add(tb);
            } else {
                note.getError().add(new Table());
            }
            entireInfo.countEndEnter();
        }
    }

    private static void paragraphMatch(EntireInfo entireInfo, IBodyType type, int noteNum, Note note) {
        if (type == IBodyType.FOOT_NOTE) {
            if (!((Paragraph) entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter()-1)).getErrors().isEmpty()) {
                Paragraph para = new Paragraph();
                para.getErrors().addAll(((Paragraph) entireInfo.getDocx().getFootNote().get(noteNum + entireInfo.getFootnoteEnter() - 1)).getErrors());
                note.getError().add(para);
            } else {
                note.getError().add(new Paragraph());
            }
        } else {
            if (!((Paragraph) entireInfo.getDocx().getEndNote().get(noteNum + entireInfo.getEndnoteEnter() - 1)).getErrors().isEmpty()) {
                Paragraph para = new Paragraph();
                para.getErrors().addAll(((Paragraph) entireInfo.getDocx().getEndNote().get(noteNum + entireInfo.getEndnoteEnter() - 1)).getErrors());
                note.getError().add(para);
            } else {
                note.getError().add(new Paragraph());
            }
        }
    }

    @Override
    public List<IBody> endNoteFootNoteParse(XWPFAbstractFootnoteEndnote note, IBodyType iBodyType, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public List<IBody> headerParse(List<XWPFHeader> headerList, SpellCheckerType spellCheckerType, EntireInfo entireInfo) {
        List<IBody> result = new ArrayList<>();
        for (XWPFHeader header : headerList) {
            List<IBodyElement> bodyElements = header.getBodyElements();
            for (IBodyElement bodyElement : bodyElements) {
                result.add(iBodyParse(bodyElement, spellCheckerType, entireInfo));
            }
        }
        return result;
    }

    @Override
    public List<IBody> footerParse(List<XWPFFooter> footerList, SpellCheckerType spellCheckerType, EntireInfo entireInfo) {
        List<IBody> result = new ArrayList<>();
        for (XWPFFooter footer : footerList) {
            List<IBodyElement> bodyElements = footer.getBodyElements();
            for (IBodyElement bodyElement : bodyElements) {
                result.add(iBodyParse(bodyElement, spellCheckerType, entireInfo));
            }
        }

        return result;
    }
}
