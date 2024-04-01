package choiKoDaKimNamChung.grammarChecker.docx;

import choiKoDaKimNamChung.grammarChecker.response.WordError;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Getter
@NoArgsConstructor
public abstract class IBody {
    private IBodyType type;
    public IBody(IBodyType iBodyType){
        this.type = iBodyType;
    }
}
