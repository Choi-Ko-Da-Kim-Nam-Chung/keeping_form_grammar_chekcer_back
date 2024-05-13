package choiKoDaKimNamChung.grammarChecker.domain.hwp;

import choiKoDaKimNamChung.grammarChecker.response.WordError;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//@EqualsAndHashCode(callSuper = true)
@Data
public class ParagraphText extends IBody {
    public ParagraphText() {
        super(IBodyType.PARAGRAPH);
    }
    String orgStr;

    List<WordError> errors = new ArrayList<>();
    List<NoteInfo> notes = new ArrayList<>();

}
