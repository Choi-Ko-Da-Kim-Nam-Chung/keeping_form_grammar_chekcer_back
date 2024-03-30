package choiKoDaKimNamChung.grammarChecker.docx;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class Paragraph extends IBody {
    public Paragraph() {
        super(IBodyType.PARAGRAPH);
    }
    List<WordError> errors;


}
