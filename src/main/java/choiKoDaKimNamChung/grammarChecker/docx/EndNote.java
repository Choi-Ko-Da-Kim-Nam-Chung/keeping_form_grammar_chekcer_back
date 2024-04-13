package choiKoDaKimNamChung.grammarChecker.docx;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class EndNote extends IBody{
    List<IBody> content;
    public EndNote() {
        super(IBodyType.END_NOTE);
    }
}

