package choiKoDaKimNamChung.grammarChecker.domain.hwp;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Hwp {
    List<List<IBody>> footNote = new ArrayList<>();
    List<List<IBody>> endNote = new ArrayList<>();
    List<IBody> header = new ArrayList<>();
    List<IBody> body = new ArrayList<>();
    List<IBody> footer = new ArrayList<>();
}
