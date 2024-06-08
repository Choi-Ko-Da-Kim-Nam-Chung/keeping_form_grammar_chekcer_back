package choiKoDaKimNamChung.grammarChecker.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Table extends IBody {
    public Table(){
        super(IBodyType.TABLE);
    }

    List<NoteInfo> captions = new ArrayList<>();
    List<List<TableCell>> table = new ArrayList<>();
}
