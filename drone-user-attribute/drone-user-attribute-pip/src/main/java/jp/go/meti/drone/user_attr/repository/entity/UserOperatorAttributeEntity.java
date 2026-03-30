package jp.go.meti.drone.user_attr.repository.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserOperatorAttributeEntity [POST]ユーザ属性取得用エンティティ
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOperatorAttributeEntity {
    private String operatorId;

    private String mailAddress;

    private String operatorName;

    private String role;

    private String dipsAccountId;

    private String dipsAccountName;

    private String associatedOperatorId;

    private String phoneNumber;

    private String creationId;

    private LocalDateTime creationDatetime;

    private String updateId;

    private LocalDateTime updateDatetime;

    private boolean deletedFlag;

    private String swimOperatorId;
}
