package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.domain.ParagraphText;
import choiKoDaKimNamChung.grammarChecker.response.WordError;
import choiKoDaKimNamChung.grammarChecker.service.EditDistance.Edit;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdnRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

import static choiKoDaKimNamChung.grammarChecker.service.EditDistance.trackingEditDistance;

@Service
@RequiredArgsConstructor
public class DocxEditDistanceDocxParagraphApply implements DocxParagraphApply {
    private final DocxParserImp docxParserImp;

    @Override
    public void paragraphParse(XWPFParagraph paragraph, ParagraphText p) {
        int runIdx = paragraph.getRuns().size() - 1;
        int errorIdx = p.getErrors().size() - 1;
        int charIdx = paragraph.getText().length();

        if (!paragraph.getFootnoteText().isEmpty()) {
            StringBuilder cleanTextBuilder = new StringBuilder();
            List<XWPFRun> runs = paragraph.getRuns();
            List<Integer> runsToRemove = new ArrayList<>();

            for (int i = 0; i < runs.size(); i++) {
                XWPFRun run = runs.get(i);
                String text = run.getText(0);

                if (text != null) {
                    // Remove only footnote text markers
                    text = text.replaceAll("\\[\\d+:.*?\\]", "");

                    if (!text.isEmpty()) {
                        run.setText(text, 0);
                        cleanTextBuilder.append(text);
                    } else {
                        // If the run is empty after cleaning, mark it for removal
                        runsToRemove.add(i);
                    }
                } else {
                    // The run's text is null
                    // It may contain a footnote reference or other elements
                    // Check if it contains a footnote reference
                    CTR ctr = run.getCTR();
                    List<CTFtnEdnRef> footnoteRefs = ctr.getFootnoteReferenceList();

                    if (footnoteRefs != null && !footnoteRefs.isEmpty()) {
                        // The run contains a footnote reference
                        // Extract the footnote reference ID
                        for (CTFtnEdnRef footnoteRef : footnoteRefs) {
                            int footnoteId = footnoteRef.getId().intValue();
                            // Append the footnote reference marker to cleanText
                            String footnoteMarker = "[footnoteRef:" + footnoteId + "]";
                            cleanTextBuilder.append(footnoteMarker);
                        }
                        // Keep the run as it is to preserve the footnote reference
                    } else {
                        // Decide whether to remove or keep runs with null text and no footnote references
                        // For now, we can skip them
                    }
                }
            }

            // Remove runs marked for removal
            for (int i = runsToRemove.size() - 1; i >= 0; i--) {
                int idx = runsToRemove.get(i);
                paragraph.removeRun(idx);
            }

            String cleanText = cleanTextBuilder.toString();


            // Proceed with further processing using cleanText
            runIdx = paragraph.getRuns().size() - 1;
            errorIdx = p.getErrors().size() - 1;
            charIdx = cleanText.length();
        }

        while(errorIdx>=0){
            if(p.getErrors().get(errorIdx).getReplaceStr() == null){
                errorIdx--;
                continue;
            }
            WordError error = p.getErrors().get(errorIdx--);
            Queue<Edit> edits = trackingEditDistance(error.getOrgStr(), error.getReplaceStr());
            while(runIdx >= 0 && charIdx - paragraph.getRuns().get(runIdx).text().length() > error.getEnd() - 1) {
                charIdx -= paragraph.getRuns().get(runIdx).text().length();
                runIdx--;
            }

//            System.out.println(paragraph.getRuns().get(0).text());
            int runEditIndex = paragraph.getRuns().get(runIdx).text().length() - (charIdx - error.getEnd()) - 1;
            int replaceEditIndex = error.getReplaceStr().length() - 1;
            StringBuilder sb = new StringBuilder(paragraph.getRuns().get(runIdx).text());

            for (Edit edit : edits) {
                while(runEditIndex < 0 && runIdx != 0){
                    paragraph.getRuns().get(runIdx).setText(sb.toString(),0);
                    runEditIndex = paragraph.getRuns().get(--runIdx).text().length() - 1;
                    sb = new StringBuilder(paragraph.getRuns().get(runIdx).text());
                }
                if(edit==Edit.CASCADE){
                    sb.setCharAt(runEditIndex--, error.getReplaceStr().charAt(replaceEditIndex--));
                } else if (edit == Edit.DELETE) {
                    charIdx--;
                    sb.deleteCharAt(runEditIndex--);
                } else if(edit == Edit.ADD){
                    charIdx++;
                    sb.insert(runEditIndex + 1, error.getReplaceStr().charAt(replaceEditIndex--));
                } else{
                    runEditIndex--;
                    replaceEditIndex--;
                }

            }
            paragraph.getRuns().get(runIdx).setText(sb.toString(),0);
        }
    }

//
//    protected String changeString(String str, Integer i, Edit edit, Character c){
//        if(edit==Edit.CASCADE){
//            return str.substring(0, i) + c.toString() + str.substring(i+1);
//        }
//        if(edit==Edit.DELETE){
//            return str.substring(0, i) + str.substring(i+1);
//        }
//        if(edit==Edit.ADD){
//            return str.substring(0, i) + c.toString() + str.substring(i);
//        }
//        return str;
//    }

}
