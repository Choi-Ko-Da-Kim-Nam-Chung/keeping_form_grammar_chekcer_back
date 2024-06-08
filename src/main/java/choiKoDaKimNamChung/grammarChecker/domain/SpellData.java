package choiKoDaKimNamChung.grammarChecker.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SpellData {
    List<List<IBody>> footNote = new ArrayList<>();
    List<List<IBody>> endNote = new ArrayList<>();
    List<IBody> header = new ArrayList<>();
    List<IBody> body = new ArrayList<>();
    List<IBody> footer = new ArrayList<>();
}
