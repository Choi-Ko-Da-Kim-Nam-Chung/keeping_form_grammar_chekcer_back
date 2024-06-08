package choiKoDaKimNamChung.grammarChecker.service.hwp;

import choiKoDaKimNamChung.grammarChecker.domain.ParagraphText;
import choiKoDaKimNamChung.grammarChecker.response.WordError;
import choiKoDaKimNamChung.grammarChecker.service.EditDistance;
import choiKoDaKimNamChung.grammarChecker.service.EditDistance.Edit;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.object.bodytext.paragraph.charshape.CharPositionShapeIdPair;
import kr.dogfoot.hwplib.object.bodytext.paragraph.charshape.ParaCharShape;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.HWPCharNormal;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.HWPCharType;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.Iterator;

@Service
public class HwpEditDistanceParagraphApplyImp implements HwpParagraphApply{

    @Override
    public void paragraphParse(Paragraph paragraph, ParagraphText paragraphText){
        ParaCharShape charShape = paragraph.getCharShape();
        Iterator<WordError> weIter = paragraphText.getErrors().iterator();
        int adjustCharIdx = 0;

        while(paragraph.getText().getCharList().get(adjustCharIdx).getType()!= HWPCharType.Normal){
            adjustCharIdx++;
        }

        while(weIter.hasNext()){
            WordError error = weIter.next();
            if(error.getReplaceStr() == null) continue;

            int charIdx = adjustCharIdx + error.getStart();
            int replaceCharIdx = 0;

            Deque<Edit> edits = EditDistance.trackingEditDistance(error.getOrgStr(), error.getReplaceStr());

            while(!edits.isEmpty()){

                Edit edit = edits.pollLast();
                if(edit == Edit.CASCADE){
                    paragraph.getText().getCharList().get(charIdx++).setCode(error.getReplaceStr().charAt(replaceCharIdx++));
                }else if(edit == Edit.DELETE){
                    paragraph.getText().getCharList().remove(charIdx);
                    adjustCharIdx--;
                    //서식밀리는거 재조정해야하는데
                    Iterator<CharPositionShapeIdPair> iterator = paragraph.getCharShape().getPositonShapeIdPairList().iterator();
                    while(iterator.hasNext()){
                        CharPositionShapeIdPair next = iterator.next();
                        if(next.getPosition() >= charIdx){
                            next.setPosition(next.getPosition()-1);
                        }
                    }
                }else if(edit == Edit.ADD){
                    HWPCharNormal hwpCharNormal = new HWPCharNormal();
                    hwpCharNormal.setCode(error.getReplaceStr().charAt(replaceCharIdx++));
                    paragraph.getText().getCharList().add(charIdx++, hwpCharNormal);
                    adjustCharIdx++;
                    //서식밀리는거 재조정
                    Iterator<CharPositionShapeIdPair> iterator = paragraph.getCharShape().getPositonShapeIdPairList().iterator();
                    while(iterator.hasNext()){
                        CharPositionShapeIdPair next = iterator.next();
                        if(next.getPosition() >= charIdx){
                            next.setPosition(next.getPosition()+1);
                        }
                    }

                }else{
                    replaceCharIdx++;
                    charIdx++;
                }
            }

        }

    }



}
