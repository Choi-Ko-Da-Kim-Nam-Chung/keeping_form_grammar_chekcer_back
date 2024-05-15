package choiKoDaKimNamChung.grammarChecker.service.hwp;

import choiKoDaKimNamChung.grammarChecker.domain.hwp.ParagraphText;
import choiKoDaKimNamChung.grammarChecker.response.WordError;
import choiKoDaKimNamChung.grammarChecker.service.EditDistance;
import choiKoDaKimNamChung.grammarChecker.service.EditDistance.Edit;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.object.bodytext.paragraph.charshape.CharPositionShapeIdPair;
import kr.dogfoot.hwplib.object.bodytext.paragraph.charshape.ParaCharShape;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.HWPChar;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.ParaText;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.Iterator;

@Service
public class EditDistanceParagraphTextApply {

    public void paragraphParse(Paragraph paragraph, ParagraphText paragraphText){
        ParaCharShape charShape = paragraph.getCharShape();
        Iterator<WordError> weIter = paragraphText.getErrors().iterator();
        int replaceCharIdx = 0;
        while(weIter.hasNext()){
            WordError error = weIter.next();
            if(error.getReplaceStr() == null) continue;

            Deque<Edit> edits = EditDistance.trackingEditDistance(error.getOrgStr(), error.getReplaceStr());

            int charIdx = replaceCharIdx + error.getStart();
            paragraph.getCharShape().getPositonShapeIdPairList().iterator();




        }

    }

    private void replaceShape(int charIdx, ){

    }


}
