package choiKoDaKimNamChung.grammarChecker.docx;

import choiKoDaKimNamChung.grammarChecker.response.WordError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FootNote extends IBody{
    int num;
    List<IBody> content;
    public FootNote(int num) {
        super(IBodyType.FOOT_NOTE);
        this.num = num;
    }
}

