package choiKoDaKimNamChung.grammarChecker.docx;

import choiKoDaKimNamChung.grammarChecker.response.WordError;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class Note extends IBody{
    int num;
    List<IBody> error = new ArrayList<>();
    public Note(int num, IBodyType type) {
        super(type);
        this.num = num;
    }
}
