package choiKoDaKimNamChung.grammarChecker.docx;

import choiKoDaKimNamChung.grammarChecker.service.docx.IBodyType;
import lombok.Data;

import java.util.List;

@Data
public class Table extends IBody{
    public Table(){
        super(IBodyType.TABLE);
    }

    List<List<Paragraph>> table;
}
