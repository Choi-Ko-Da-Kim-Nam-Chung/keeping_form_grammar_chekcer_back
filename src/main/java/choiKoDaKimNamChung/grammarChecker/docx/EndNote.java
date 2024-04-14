package choiKoDaKimNamChung.grammarChecker.docx;

import choiKoDaKimNamChung.grammarChecker.response.WordError;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class EndNote extends IBody{
    int num;
    List<IBody> content;
    public EndNote(int num) {
        super(IBodyType.END_NOTE);
        this.num = num;
    }
}

