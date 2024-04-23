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

        for (XWPFFootnote footnote : document.getFootnotes()) {
            ArrayList<IBody> note = new ArrayList<>();
            docx.getFootNote().add(note);
            for (IBodyElement element : footnote.getBodyElements()) {
                note.add(iBodyParse(element, spellCheckerType));
            }
        }

        for (XWPFEndnote endnote : document.getEndnotes()) {
            ArrayList<IBody> note = new ArrayList<>();
            docx.getEndNote().add(note);
            for (IBodyElement element : endnote.getBodyElements()) {
                note.add(iBodyParse(element, spellCheckerType));
            }
        }

        List<IBodyElement> paragraphs = document.getBodyElements();
        for (IBodyElement paragraph : paragraphs) {
            IBody result = iBodyParse(paragraph, spellCheckerType);
            docx.getBody().add(result);
        }

        docx.getFooter().addAll(headerParse(document.getHeaderList(), spellCheckerType));
        docx.getHeader().addAll(footerParse(document.getFooterList(), spellCheckerType));

        return docx;
    }

    @Override
    public IBody iBodyParse(IBodyElement bodyElement, SpellCheckerType spellCheckerType) {
        if (bodyElement.getElementType() == BodyElementType.PARAGRAPH) {
            return paragraphParse((XWPFParagraph)bodyElement, spellCheckerType);
//            if(!((XWPFParagraph) bodyElement).getFootnoteText().isEmpty()){}
        } else if (bodyElement.getElementType() == BodyElementType.TABLE) {
            return tableParse((XWPFTable)bodyElement, spellCheckerType);
        } else {
            System.out.println("bodyElement = " + bodyElement.getElementType());
        }
        return null;
    }

    @Override
    public Table tableParse(XWPFTable table, SpellCheckerType spellCheckerType) {
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
                    tableCell.getIBody().add(iBodyParse(bodyElement, spellCheckerType));
                }
                arr.add(tableCell);
            }
            t.getTable().add(arr);

        }
        return t;
    }

    @Override
    public Paragraph paragraphParse(XWPFParagraph paragraph, SpellCheckerType spellCheckerType) {
        Paragraph result;
        if(!paragraph.getFootnoteText().isEmpty()){  // 미주, 각주가 있으면
            result = removeReferences(paragraph.getParagraphText());

        }else{
            result = new Paragraph();
            result.setOrgStr(paragraph.getText());
        }

        TextRequest textRequest = new TextRequest(result.getOrgStr());
        String url = spellCheckerType.getUrl();

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

    // footnoteRef, endnoteRef 제거 및 plainParagraph 처리
    public Paragraph removeReferences(String paragraphTextWithRef) {
        // original text사이에 있는 footnoteRef, endnoteRef 제거
        Pattern pattern = Pattern.compile("\\[(endnoteRef|footnoteRef):(\\d+)\\]");

        Matcher matcher = pattern.matcher(paragraphTextWithRef);
        StringBuilder resultText = new StringBuilder(paragraphTextWithRef);
        List<NoteInfo> notes = new ArrayList<>();

        // 매칭된 결과를 찾으면서 처리
        while (matcher.find()) {
            // Ref 번호 삽입
            int start = matcher.start();
            int end = matcher.end();

            notes.add(new NoteInfo(matcher.group(1).equals("endnoteRef")?NoteInfoType.END_NOTE:NoteInfoType.FOOT_NOTE, Integer.parseInt(matcher.group(2))));
            // 매칭된 부분 문자열 제거
            resultText.delete(start - (paragraphTextWithRef.length() - resultText.length()), end - (paragraphTextWithRef.length() - resultText.length()));
        }
        Paragraph p = new Paragraph();
        p.setOrgStr(String.valueOf(resultText));
        p.setNotes(notes);
        // plainParagraph 삽입
        return p;
    }

    @Override
    public List<IBody> headerParse(List<XWPFHeader> headerList, SpellCheckerType spellCheckerType) {
        List<IBody> result = new ArrayList<>();
        for (XWPFHeader header : headerList) {
            List<IBodyElement> bodyElements = header.getBodyElements();
            for (IBodyElement bodyElement : bodyElements) {
                result.add(iBodyParse(bodyElement, spellCheckerType));
            }
        }
        return result;
    }

    @Override
    public List<IBody> footerParse(List<XWPFFooter> footerList, SpellCheckerType spellCheckerType ) {
        List<IBody> result = new ArrayList<>();
        for (XWPFFooter footer : footerList) {
            List<IBodyElement> bodyElements = footer.getBodyElements();
            for (IBodyElement bodyElement : bodyElements) {
                result.add(iBodyParse(bodyElement, spellCheckerType));
            }
        }

        return result;
    }
}
