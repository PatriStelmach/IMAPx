package Patri.Stelmach.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class EmailDto
{
    private String subject;
    private String sender;

}
