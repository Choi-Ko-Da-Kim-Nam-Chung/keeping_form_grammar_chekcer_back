package choiKoDaKimNamChung.grammarChecker.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ExtractData {
    private String plainPragraph = "";
    private List<Object[]> noteList = new ArrayList<>();
}
