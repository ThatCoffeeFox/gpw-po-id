package pl.gpwpoid.origin.ui.views.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.gpwpoid.origin.decorators.validPesel.ValidPesel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileUpdateDTO {
    private Integer accountId;
    private String firstName;
    private String secondaryName;
    private String lastName;

    @ValidPesel
    private String PESEL;
}