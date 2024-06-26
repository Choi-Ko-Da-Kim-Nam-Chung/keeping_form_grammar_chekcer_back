package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.domain.IBody;
import choiKoDaKimNamChung.grammarChecker.domain.*;
import choiKoDaKimNamChung.grammarChecker.request.TextRequest;
import choiKoDaKimNamChung.grammarChecker.response.WordError;
import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DocxParserImp implements DocxParser {

    private final WebClient webClient;
    @Override
    public SpellData docxParse(XWPFDocument document, SpellCheckerType spellCheckerType) {
        SpellData docx = new SpellData();
        for (XWPFFootnote footnote : document.getFootnotes()) {
            if (footnote.getCTFtnEdn().toString().contains("<w:continuationSeparator/>") || footnote.getCTFtnEdn().toString().contains("<w:separator/>")){
                continue;
            }
            List<IBody> note = new ArrayList<>();
            docx.getFootNote().add(note);
            System.out.println("((XWPFParagraph) footnote).getText() = " + ((XWPFParagraph) footnote.getBodyElements().get(0)).getText());
            asyncIBody(note, footnote.getBodyElements(), spellCheckerType);
        }

        for (XWPFEndnote endnote : document.getEndnotes()) {
            if (endnote.getCTFtnEdn().toString().contains("<w:continuationSeparator/>") || endnote.getCTFtnEdn().toString().contains("<w:separator/>")){
                continue;
            }
            List<IBody> note = new ArrayList<>();
            docx.getEndNote().add(note);
            asyncIBody(note, endnote.getBodyElements(), spellCheckerType);
        }

        asyncIBody(docx.getBody(), document.getBodyElements(), spellCheckerType);

        docx.getFooter().addAll(footerParse(document.getFooterList(), spellCheckerType));
        docx.getHeader().addAll(headerParse(document.getHeaderList(), spellCheckerType));

        return docx;
    }

    public void asyncIBody(List<IBody> body, List<IBodyElement> paragraphs, SpellCheckerType spellCheckerType){
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<CompletableFuture<IBody>> futures = new ArrayList<>();

        for (IBodyElement paragraph : paragraphs) {
            CompletableFuture<IBody> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return iBodyParse(paragraph, spellCheckerType);
                } catch (Exception e) {
                    return null;
                }
            }, executor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenAccept((Void) -> {
                    futures.forEach(future -> {
                        try {
                            IBody iBody = future.get();
                            if (iBody != null) {
                                synchronized (body) {
                                    body.add(iBody);
                                }
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }).join();

        executor.shutdown();
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

                asyncIBody(tableCell.getIBody(), cells.get(j).getBodyElements(), spellCheckerType);
                arr.add(tableCell);
            }
            t.getTable().add(arr);

        }
        return t;
    }
    @Override
    public ParagraphText paragraphParse(XWPFParagraph paragraph, SpellCheckerType spellCheckerType) {
        ParagraphText result;

        if(!paragraph.getFootnoteText().isEmpty()){  // 미주, 각주가 있으면
            result = removeReferences(paragraph.getParagraphText());

        }else{
            result = new ParagraphText();
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

        List<WordError> errors = response.collectList().block();
        result.getErrors().addAll(errors);

        return result;
    }

    // footnoteRef, endnoteRef 제거 및 plainParagraph 처리
    public ParagraphText removeReferences(String paragraphTextWithRef) {
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
        ParagraphText p = new ParagraphText();
        p.setOrgStr(String.valueOf(resultText));
        p.setNotes(notes);
        // plainParagraph 삽입
        return p;
    }

    @Override
    public List<IBody> headerParse(List<XWPFHeader> headerList, SpellCheckerType spellCheckerType) {
        List<IBody> result = new ArrayList<>();
        for (XWPFHeader header : headerList) {
            asyncIBody(result, header.getBodyElements(),spellCheckerType);
        }
        return result;
    }

    @Override
    public List<IBody> footerParse(List<XWPFFooter> footerList, SpellCheckerType spellCheckerType ) {
        List<IBody> result = new ArrayList<>();
        for (XWPFFooter footer : footerList) {
            asyncIBody(result, footer.getBodyElements(),spellCheckerType);
        }

        return result;
    }
}