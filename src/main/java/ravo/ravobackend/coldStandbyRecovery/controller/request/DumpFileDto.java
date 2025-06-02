package ravo.ravobackend.coldStandbyRecovery.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DumpFileDto {
    private String filename;
}
