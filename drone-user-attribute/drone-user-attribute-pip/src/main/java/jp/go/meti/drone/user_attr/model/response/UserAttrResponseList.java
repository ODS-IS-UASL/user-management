package jp.go.meti.drone.user_attr.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserAttrResponseList　[POST]ユーザ属性取得用レスポンスオブジェクト
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAttrResponseList {
    @JsonProperty("attributeList")
    private List<UserAttrResponse> attributeList;
}
