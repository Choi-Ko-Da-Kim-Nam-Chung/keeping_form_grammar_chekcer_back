package choiKoDaKimNamChung.grammarChecker.docx;

import lombok.Data;

import java.util.List;

@Data
public class Table extends IBody{
    public Table(){
        super(IBodyType.TABLE);
    }

    List<List<Paragraph>> table;
}
