package choiKoDaKimNamChung.grammarChecker.docx;

import choiKoDaKimNamChung.grammarChecker.service.docx.IBodyType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
public abstract class IBody {
    private IBodyType type;
    public IBody(IBodyType iBodyType){
        this.type = iBodyType;
    }
}
