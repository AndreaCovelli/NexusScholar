package it.unipi.nexusscholar.dto.mongo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTO extends UserDTO {

    private List<PermissionDTO> permissions;
}
