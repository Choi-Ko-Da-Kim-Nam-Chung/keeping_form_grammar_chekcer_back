package choiKoDaKimNamChung.grammarChecker.docx;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
public class FootNote extends IBody{
    int num;
    List<IBody> content;
    public FootNote(int num) {
        super(IBodyType.FOOT_NOTE);
        this.num = num;
    }
}

