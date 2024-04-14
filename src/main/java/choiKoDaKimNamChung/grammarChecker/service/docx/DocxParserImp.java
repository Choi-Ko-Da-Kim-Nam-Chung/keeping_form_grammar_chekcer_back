package choiKoDaKimNamChung.grammarChecker.service.docx;


import choiKoDaKimNamChung.grammarChecker.docx.*;
import choiKoDaKimNamChung.grammarChecker.docx.IBody;
import choiKoDaKimNamChung.grammarChecker.request.TextRequest;
import choiKoDaKimNamChung.grammarChecker.response.ExtractData;
import choiKoDaKimNamChung.grammarChecker.response.WordError;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            for (IBodyElement element : footnote.getBodyElements()) {
                if (element.getElementType() == BodyElementType.PARAGRAPH) {
                    if (!((XWPFParagraph) element).getText().isEmpty()) {
                        IBody ibody = paragraphParse((XWPFParagraph) element, spellCheckerType);
                        docx.getFootNote().add(ibody);
                    }
                } else if (element.getElementType() == BodyElementType.TABLE) {
                    IBody ibody = tableParse((XWPFTable) element, spellCheckerType);
                    docx.getFootNote().add(ibody);
                }
            }
        }

        for (XWPFEndnote endnote : document.getEndnotes()) {
            for (IBodyElement element : endnote.getBodyElements()) {
                if (element.getElementType() == BodyElementType.PARAGRAPH) {
                    if (!((XWPFParagraph) element).getText().isEmpty()) {
                        IBody ibody = paragraphParse((XWPFParagraph) element, spellCheckerType);
                        docx.getEndNote().add(ibody);
                    }
                } else if (element.getElementType() == BodyElementType.TABLE) {
                    IBody ibody = tableParse((XWPFTable) element, spellCheckerType);
                    docx.getEndNote().add(ibody);
                }
            }
        }


        List<XWPFHeader> headerList = document.getHeaderList();
        for (XWPFHeader header : headerList) {
            List<IBodyElement> bodyElements = header.getBodyElements();
            for (IBodyElement bodyElement : bodyElements) {
                IBody iBody = iBodyParse(bodyElement, spellCheckerType);
                docx.getHeader().add(iBody);
            }
        }

        List<IBodyElement> paragraphs = document.getBodyElements();
        for (IBodyElement paragraph : paragraphs) {
            IBody result = iBodyParse(paragraph, spellCheckerType);
            docx.getBody().add(result);
        }

        List<XWPFFooter> footerList = document.getFooterList();
        for (XWPFFooter footer : footerList) {
            List<IBodyElement> bodyElements = footer.getBodyElements();
            for (IBodyElement bodyElement : bodyElements) {
                IBody iBody = iBodyParse(bodyElement, spellCheckerType);
                docx.getFooter().add(iBody);
            }
        }
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
        Paragraph result = new Paragraph();
        // TODO : 중간에 미주, 각주가 있을 경우 처리 필요
        String text = paragraph.getText();
        String url = spellCheckerType.getUrl();
        if(!paragraph.getFootnoteText().isEmpty()){  // 미주, 각주가 있으면
            ExtractData extractData = new ExtractData();

            text = removeAllReferences(paragraph); // ref 제거
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
    public String removeAllReferences(XWPFParagraph bodyElement) {
        String note = bodyElement.getFootnoteText(); // 뒤에 있는 plain 미주, 각주
        // original text에서 뒤에 있는 note 제거
        String paragraphText = bodyElement.getText();
        String paragraphTextWithRef = paragraphText.replace(note, "");

        // original text사이에 있는 footnoteRef, endnoteRef 제거
        Pattern pattern = Pattern.compile("\\[(endnoteRef|footnoteRef):(\\d+)\\]");
        StringBuilder resultText = getPlainText(pattern, paragraphTextWithRef);
        return String.valueOf(resultText);
    }

    private static StringBuilder getPlainText(Pattern pattern, String removedNote) {
        Matcher matcher = pattern.matcher(removedNote);
        StringBuilder resultText = new StringBuilder(removedNote);

        // 매칭된 결과를 찾으면서 처리
        while (matcher.find()) {
            // 매칭된 부분 문자열 제거
            int start = matcher.start();
            int end = matcher.end();
            resultText.delete(start - (removedNote.length() - resultText.length()), end - (removedNote.length() - resultText.length()));
        }
        return resultText;
    }

    @Override
    public List<IBody> endNoteFootNoteParse(XWPFAbstractFootnoteEndnote note, IBodyType iBodyType, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public List<IBody> headerParse(XWPFHeader header, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public List<IBody> footerParse(XWPFFooter footer, SpellCheckerType spellCheckerType) {
        return null;
    }
}
