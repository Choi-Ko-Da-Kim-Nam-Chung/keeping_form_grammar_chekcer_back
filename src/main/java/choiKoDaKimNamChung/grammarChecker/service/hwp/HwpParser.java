package choiKoDaKimNamChung.grammarChecker.service.hwp;

import choiKoDaKimNamChung.grammarChecker.domain.SpellCheckerType;
import choiKoDaKimNamChung.grammarChecker.domain.hwp.*;
import choiKoDaKimNamChung.grammarChecker.request.TextRequest;
import choiKoDaKimNamChung.grammarChecker.response.WordError;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.*;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.ParaText;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class HwpParser {

    private final WebClient webClient;

    public Hwp hwpParse(HWPFile hwpfile, SpellCheckerType spellCheckerType) {
        Hwp hwp = new Hwp();
        for (Section section : hwpfile.getBodyText().getSectionList()) {
            asyncParse(hwp.getBody(), section.getParagraphs(), spellCheckerType);
        }
        extractFootnotesEndnotesHeaderFooter(hwp, hwpfile, spellCheckerType);

        return hwp;
    }

    // 매 번 전체 본문을 순회하는것보다 미주,각주,머리글,바닥글 한번에 하는게 효율적일 것 같아서 한번에 묶음.
    // 따로 미주와 각주만 뽑아내는건 찾지 못함
    public void extractFootnotesEndnotesHeaderFooter(Hwp hwp, HWPFile hwpFile, SpellCheckerType spellCheckerType) {
        for (Section section : hwpFile.getBodyText().getSectionList()) {
            for (Paragraph paragraph : section.getParagraphs()) {
                if (paragraph.getControlList() != null) {
                    for (Control control : paragraph.getControlList()) {
                        if (control.getType() == ControlType.Footnote) {
                            ControlFootnote footnote = (ControlFootnote) control;
                            List<IBody> note = new ArrayList<>();
                            hwp.getFootNote().add(note);
                            asyncParse(note, footnote.getParagraphList().getParagraphs(), spellCheckerType);
                        } else if (control.getType() == ControlType.Endnote) {
                            ControlEndnote endnote = (ControlEndnote) control;
                            List<IBody> note = new ArrayList<>();
                            hwp.getEndNote().add(note);
                            asyncParse(note, endnote.getParagraphList().getParagraphs(), spellCheckerType);
                        } else if (control.getType() == ControlType.Header) {
                            ControlHeader header = (ControlHeader) control;
                            asyncParse(hwp.getHeader(), header.getParagraphList().getParagraphs(), spellCheckerType);
                        } else if (control.getType() == ControlType.Footer) {
                            ControlFooter footer = (ControlFooter) control;
                            asyncParse(hwp.getFooter(), footer.getParagraphList().getParagraphs(), spellCheckerType);
                        }
                    }
                }
            }
        }
    }

    public IBody controlParse(Paragraph paragraph, SpellCheckerType spellCheckerType){
        if(paragraph.getControlList() != null) {
            for (Control control : paragraph.getControlList()) {
                if(control.getType() == ControlType.Table){
                    return tableParse((ControlTable) control, spellCheckerType);
                }else{
                    System.out.println("control.getType() = " + control.getType());
                }
            }
        }

        return paraTextParse(paragraph.getText(), spellCheckerType);
    }

    public ParagraphText paraTextParse(ParaText paraText, SpellCheckerType spellCheckerType){
        ParagraphText result = new ParagraphText();
        try{
            result.setOrgStr(paraText.getNormalString(0));

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
        }catch(Exception e){
            System.out.println("e.getMessage() = " + e.getMessage());
        }
        return new ParagraphText();
    }

    public void asyncParse(List<IBody> body, Paragraph[] paragraphs, SpellCheckerType spellCheckerType){
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<CompletableFuture<IBody>> futures = new ArrayList<>();

        for (Paragraph paragraph : paragraphs) {
            CompletableFuture<IBody> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return controlParse(paragraph, spellCheckerType);
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


    public Table tableParse(ControlTable table, SpellCheckerType spellCheckerType){
        Table t = new Table();
//        table.getCaption().getParagraphList();
        for (Row row : table.getRowList()) {
            List<TableCell> tableCell = new ArrayList<>();
            for (Cell cell : row.getCellList()) {
                TableCell c = new TableCell();
                c.setColspan(cell.getListHeader().getColSpan());
                c.setRowspan(cell.getListHeader().getRowSpan());

                asyncParse(c.getIBody(), cell.getParagraphList().getParagraphs(), spellCheckerType);

                tableCell.add(c);
            }
            t.getTable().add(tableCell);
        }

        return t;
    }

}
