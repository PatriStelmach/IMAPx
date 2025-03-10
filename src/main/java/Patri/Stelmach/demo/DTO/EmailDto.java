package Patri.Stelmach.demo.DTO;

        import lombok.AllArgsConstructor;
        import lombok.Data;

@Data
@AllArgsConstructor
public class EmailDto
{
    private String subject;
    private String sender;

}