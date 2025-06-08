package pl.gpwpoid.origin.ui.views.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileUpdateDTO {
    private Integer accountId;
    private String firstName;
    private String secondaryName;
    private String lastName;
}