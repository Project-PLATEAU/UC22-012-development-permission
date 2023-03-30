package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 範囲選択地番取得フォーム
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class GetLotNuberByFigureForm  implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 座標 */
	@ApiModelProperty(value = "範囲図形（ポリゴン）の座標")
	private double[][] coodinates; 
}
