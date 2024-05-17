package choiKoDaKimNamChung.grammarChecker.domain.docx;

import choiKoDaKimNamChung.grammarChecker.response.WordError;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//@EqualsAndHashCode(callSuper = true)
@Data
public class Paragraph extends IBody {
    public Paragraph() {
        super(IBodyType.PARAGRAPH);
    }
    String orgStr;
    List<WordError> errors = new ArrayList<>();
    List<NoteInfo> notes = new ArrayList<>();

}