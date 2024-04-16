package choiKoDaKimNamChung.grammarChecker.docx;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class Note extends IBody{
    int num;
    List<IBody> content;
    public Note(int num, IBodyType type) {
        super(type);
        this.num = num;
    }
}
