package choiKoDaKimNamChung.grammarChecker.service.docx;


import choiKoDaKimNamChung.grammarChecker.docx.*;
import choiKoDaKimNamChung.grammarChecker.docx.IBody;
import choiKoDaKimNamChung.grammarChecker.request.TextRequest;
import choiKoDaKimNamChung.grammarChecker.response.WordError;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocxParserImp implements DocxParser {

    private final WebClient webClient;
    @Override
    public Docx docxParse(XWPFDocument document, SpellCheckerType spellCheckerType) {

        Docx docx = new Docx();
        List<XWPFHeader> headerList = document.getHeaderList();
        for (XWPFHeader header : headerList) {
            List<IBodyElement> bodyElements = header.getBodyElements();
            for (IBodyElement bodyElement : bodyElements) {
                IBody iBody = iBodyParse(bodyElement, spellCheckerType);
                docx.getHeader().add(iBody);
            }
        }

        List<XWPFParagraph> paragraphs = document.getParagraphs();
        for (XWPFParagraph paragraph : paragraphs) {
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
        return null;
    }

    @Override
    public Paragraph paragraphParse(XWPFParagraph paragraph, SpellCheckerType spellCheckerType) {
        Paragraph result = new Paragraph();
        // TODO : 중간에 미주, 각주가 있을 경우 처리 필요

        String url = spellCheckerType.getUrl();
        TextRequest textRequest = new TextRequest(paragraph.getText());

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

    @Override
    public List<IBody> endNoteFootNoteParse(XWPFAbstractFootnoteEndnote note, IBodyType iBodyType) {
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
