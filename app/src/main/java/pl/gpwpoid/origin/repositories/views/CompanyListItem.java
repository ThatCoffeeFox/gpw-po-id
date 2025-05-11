package pl.gpwpoid.origin.repositories.views;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompanyListItem {
    private Integer companyId;
    private String name;
    private String code;
}
